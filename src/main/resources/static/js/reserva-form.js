(function () {
  "use strict";
  var csrfToken  = document.querySelector('meta[name="_csrf"]').content;
  var csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  var rooms = [];
  var disponibles = [];
  var modalCtx = { room: -1, edit: -1 };

  var SUF = { nombres:"nombres", apellidos:"apellidos", fechaNacimiento:"fnac",
              numeroDocumento:"numdoc", correo:"correo", telefono:"telefono" };

  function val(id){ var el=document.getElementById(id); return el ? el.value.trim() : ""; }
  function setVal(id,v){ var el=document.getElementById(id); if(el) el.value = v==null?"":v; }
  function labelDoc(id){ var el=document.getElementById(id); return el && el.selectedOptions[0] ? el.selectedOptions[0].text : ""; }
  function habDe(id){ return disponibles.find(function(h){return h.id===id;}); }
  function capDe(id){ var h=habDe(id); return h?h.capacidad:0; }

  // ---------- utilidades de fecha (DD/MM/AAAA <-> ISO) ----------
  function dd(n){ return (n<10?"0":"")+n; }
  function fmtISOaDMY(iso){
    if(!iso) return "";
    var m=/^(\d{4})-(\d{2})-(\d{2})/.exec(iso);
    return m ? (m[3]+"/"+m[2]+"/"+m[1]) : "";
  }
  function parseDMYaISO(str){
    if(!str) return null;
    var m=/^(\d{2})\/(\d{2})\/(\d{4})$/.exec(str.trim());
    if(!m) return null;
    var d=+m[1], mo=+m[2], y=+m[3];
    if(mo<1||mo>12||d<1||d>31||y<1900||y>2100) return null;
    var dt=new Date(y, mo-1, d);
    if(dt.getFullYear()!==y || dt.getMonth()!==mo-1 || dt.getDate()!==d) return null; // rechaza 31/02, etc.
    return y+"-"+dd(mo)+"-"+dd(d);
  }
  // aplica mascara DD/MM/AAAA mientras se escribe
  function maskFecha(el){
    if(!el) return;
    el.addEventListener("input", function(){
      var v=this.value.replace(/\D/g,"").slice(0,8), out=v;
      if(v.length>4) out=v.slice(0,2)+"/"+v.slice(2,4)+"/"+v.slice(4);
      else if(v.length>2) out=v.slice(0,2)+"/"+v.slice(2);
      this.value=out;
    });
  }
  function hace18(){ var d=new Date(); d.setFullYear(d.getFullYear()-18); return d; }
  // inicializa un calendario (flatpickr) sobre el input; si no carga, usa la mascara
  function fpFecha(el, extra){
    if(!el) return null;
    if(window.flatpickr){ return flatpickr(el, Object.assign({dateFormat:"d/m/Y", allowInput:true, locale:"es"}, extra||{})); }
    maskFecha(el); return null;
  }

  function personaISO(p){
    return { nombres:p.nombres, apellidos:p.apellidos, fechaNacimiento:parseDMYaISO(p.fechaNacimiento),
             direccion:p.direccion, idTipoDocumento:p.idTipoDocumento, numeroDocumento:p.numeroDocumento,
             correo:p.correo, telefono:p.telefono };
  }

  // ---------- lecturas ----------
  function getCliente(){
    return { nombres:val("cli-nombres"), apellidos:val("cli-apellidos"), fechaNacimiento:val("cli-fnac"),
             direccion:val("cli-direccion"), idTipoDocumento:parseInt(val("cli-tipodoc"),10)||null,
             numeroDocumento:val("cli-numdoc"), correo:val("cli-correo"), telefono:val("cli-telefono") };
  }
  function getModal(){
    return { nombres:val("hu-nombres"), apellidos:val("hu-apellidos"), fechaNacimiento:val("hu-fnac"),
             direccion:val("hu-direccion"), idTipoDocumento:parseInt(val("hu-tipodoc"),10)||null,
             numeroDocumento:val("hu-numdoc"), correo:val("hu-correo"), telefono:val("hu-telefono") };
  }

  // ---------- validaciones ----------
  function edad(iso){
    if(!iso) return -1;
    var b=new Date(iso), t=new Date(), e=t.getFullYear()-b.getFullYear();
    var m=t.getMonth()-b.getMonth();
    if(m<0 || (m===0 && t.getDate()<b.getDate())) e--;
    return e;
  }
  function erroresPersona(p, tipoLabel){
    var e={};
    if(!p.nombres)   e.nombres="Ingresa el nombre.";
    if(!p.apellidos) e.apellidos="Ingresa el apellido.";
    if(!p.fechaNacimiento){ e.fechaNacimiento="Ingresa la fecha de nacimiento."; }
    else {
      var iso=parseDMYaISO(p.fechaNacimiento);
      if(!iso) e.fechaNacimiento="Fecha inválida. Usa DD/MM/AAAA.";
      else if(new Date(iso) > new Date()) e.fechaNacimiento="La fecha no puede ser futura.";
      else if(edad(iso) < 18) e.fechaNacimiento="Debe ser mayor de edad (18 años o más).";
    }
    if(!p.numeroDocumento) e.numeroDocumento="Ingresa el documento.";
    else if(tipoLabel==='DNI' && !/^[0-9]{8}$/.test(p.numeroDocumento)) e.numeroDocumento="El DNI debe tener 8 dígitos.";
    if(p.correo && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(p.correo)) e.correo="El correo no es válido.";
    if(p.telefono && !/^[0-9]{9}$/.test(p.telefono)) e.telefono="El teléfono debe tener 9 dígitos.";
    return e;
  }
  function duplicados(cand, lista){
    var e={};
    lista.forEach(function(o){
      if(cand.numeroDocumento && o.numeroDocumento===cand.numeroDocumento) e.numeroDocumento="Ese documento ya está en la reserva.";
      if(cand.telefono && o.telefono && o.telefono===cand.telefono) e.telefono="Ese teléfono ya está en la reserva.";
    });
    return e;
  }
  function personasExcepto(roomIdx, guestIdx){
    var arr=[ getCliente() ];
    rooms.forEach(function(r, ri){
      r.huespedes.forEach(function(h, hi){ if(!(ri===roomIdx && hi===guestIdx)) arr.push(h); });
    });
    return arr;
  }
  function mostrarErrores(prefix, e){
    Object.keys(SUF).forEach(function(k){
      var span=document.getElementById("e-"+prefix+"-"+SUF[k]);
      var input=document.getElementById(prefix+"-"+SUF[k]);
      if(span) span.textContent = e[k]||"";
      if(input){ if(e[k]) input.classList.add("invalido"); else input.classList.remove("invalido"); }
    });
  }
  function limpiarErroresHab(){
    document.querySelectorAll(".cin-err,.cout-err").forEach(function(s){ s.textContent=""; });
    document.querySelectorAll(".cin,.cout").forEach(function(i){ i.classList.remove("invalido"); });
  }

  // ---------- carga inicial ----------
  fetch("/recepcionista/api/habitaciones")
    .then(function(r){return r.json();})
    .then(function(data){
      disponibles = data;
      ["cli-numdoc","hu-numdoc"].forEach(function(id){ soloDigitos(id,8); });
      ["cli-telefono","hu-telefono"].forEach(function(id){ soloDigitos(id,9); });
      fpFecha(document.getElementById("cli-fnac"), { maxDate: hace18() });
      fpFecha(document.getElementById("hu-fnac"), { maxDate: hace18() });
      if (window.CODIGO_EDIT) {
        document.getElementById("btn-guardar").textContent = "✔ Guardar Cambios";
        cargarDetalle(window.CODIGO_EDIT);
      } else { rooms=[ nuevaRoom() ]; render(); }
    });

  function soloDigitos(id, max){
    var el=document.getElementById(id); if(!el) return;
    el.addEventListener("input", function(){ this.value = this.value.replace(/\D/g,"").slice(0,max); });
  }
  function nuevaRoom(){
    var first = disponibles.length ? disponibles[0].id : null;
    return { idHabitacion:first, checkIn:"", checkOut:"", huespedes:[] };
  }
  function cargarDetalle(codigo){
    fetch("/recepcionista/api/crud/r/"+encodeURIComponent(codigo))
      .then(function(r){return r.json();})
      .then(function(d){
        var c=d.cliente||{};
        setVal("cli-nombres",c.nombres); setVal("cli-apellidos",c.apellidos); setVal("cli-fnac",fmtISOaDMY(c.fechaNacimiento));
        setVal("cli-direccion",c.direccion); setVal("cli-tipodoc",c.idTipoDocumento); setVal("cli-numdoc",c.numeroDocumento);
        setVal("cli-correo",c.correo); setVal("cli-telefono",c.telefono);
        rooms=(d.habitaciones||[]).map(function(h){
          return { idHabitacion:h.idHabitacion, checkIn:fmtISOaDMY(h.checkIn), checkOut:fmtISOaDMY(h.checkOut),
                   huespedes:(h.huespedes||[]).map(function(hu){
                     return Object.assign({}, hu, { fechaNacimiento:fmtISOaDMY(hu.fechaNacimiento) }); }) };
        });
        if(!rooms.length) rooms=[nuevaRoom()];
        render();
      });
  }

  // ---------- render ----------
  function opcionesHab(sel){
    return disponibles.map(function(h){ return '<option value="'+h.id+'"'+(h.id===sel?' selected':'')+'>Habitacion → '+h.numero+'</option>'; }).join("");
  }
  function caracteristicas(id){ var h=habDe(id); return h ? (h.tipo+' · cap '+h.capacidad+' · '+(h.descripcion||"")) : ""; }

  function render(){
    var cont=document.getElementById("habitaciones");
    cont.innerHTML="";
    rooms.forEach(function(room, idx){
      var div=document.createElement("div");
      div.className="hab-block";
      var filas=room.huespedes.map(function(g,i){
        return '<tr><td>'+String(i+1).padStart(2,"0")+'</td><td>'+(g.nombres||"")+' '+(g.apellidos||"")+
               '</td><td><button class="mini-edit" data-h="'+i+'">✎ Editar</button>'+
               '<button class="mini-del" data-h="'+i+'">✖ Eliminar</button></td></tr>';
      }).join("");
      div.innerHTML=
        '<div class="hab-head"><h2 class="seccion">Habitación '+(idx+1)+'</h2>'+
          '<button type="button" class="btn-rojo small del-room">✖ Eliminar Habitación</button></div>'+
        '<div class="form-grid">'+
          '<div class="campo2"><label>N° Habitación</label><select class="sel-hab">'+opcionesHab(room.idHabitacion)+'</select></div>'+
          '<div class="campo2"><label>Características de la Habitación</label><input class="car" readonly value="'+caracteristicas(room.idHabitacion).replace(/"/g,"&quot;")+'"></div>'+
          '<div class="campo2"><label>Check-In</label><input type="text" class="cin" inputmode="numeric" maxlength="10" placeholder="DD/MM/AAAA" value="'+(room.checkIn||"")+'"><small class="err cin-err"></small></div>'+
          '<div class="campo2"><label>Check-Out</label><input type="text" class="cout" inputmode="numeric" maxlength="10" placeholder="DD/MM/AAAA" value="'+(room.checkOut||"")+'"><small class="err cout-err"></small></div>'+
        '</div>'+
        '<div class="fila-btns">'+
          ((idx===rooms.length-1) ? '<button type="button" class="btn-verde add-room">＋ Agregar nueva Habitación</button>' : '<span></span>')+
          '<button type="button" class="btn-verde add-hue">＋ Añadir Huéspedes</button>'+
        '</div>'+
        (room.huespedes.length ? '<table class="tabla-huespedes"><thead><tr><th>Huésped</th><th>Nombres y Apellidos</th><th>Acciones</th></tr></thead><tbody>'+filas+'</tbody></table>' : '');

      div.querySelector(".sel-hab").onchange=function(){
        room.idHabitacion=parseInt(this.value,10);
        if(room.huespedes.length>capDe(room.idHabitacion)) alert("Ojo: esta habitación admite máximo "+capDe(room.idHabitacion)+" huéspedes.");
        render();
      };
      var cin=div.querySelector(".cin"), cout=div.querySelector(".cout");
      var fpc=fpFecha(cout,{ onChange:function(sel,ds){ room.checkOut=ds; } });
      fpFecha(cin,{ onChange:function(sel,ds){ room.checkIn=ds; if(fpc && sel[0]) fpc.set("minDate", sel[0]); } });
      cin.addEventListener("change",function(){ room.checkIn=this.value; });
      cout.addEventListener("change",function(){ room.checkOut=this.value; });
      div.querySelector(".del-room").onclick=function(){
        if(rooms.length<=1){ alert("La reserva debe tener al menos una habitación."); return; }
        rooms.splice(idx,1); render();
      };
      var ar=div.querySelector(".add-room"); if(ar) ar.onclick=function(){ rooms.push(nuevaRoom()); render(); };
      div.querySelector(".add-hue").onclick=function(){ abrirModal(idx,-1); };
      div.querySelectorAll(".mini-edit").forEach(function(b){ b.onclick=function(){ abrirModal(idx,+b.dataset.h); }; });
      div.querySelectorAll(".mini-del").forEach(function(b){ b.onclick=function(){ room.huespedes.splice(+b.dataset.h,1); render(); }; });
      cont.appendChild(div);
    });
  }

  // ---------- modal ----------
  function abrirModal(roomIdx, editIdx){
    modalCtx={ room:roomIdx, edit:editIdx };
    var g = editIdx>=0 ? rooms[roomIdx].huespedes[editIdx] : {};
    setVal("hu-nombres",g.nombres); setVal("hu-apellidos",g.apellidos); setVal("hu-fnac",g.fechaNacimiento);
    setVal("hu-direccion",g.direccion); setVal("hu-tipodoc",g.idTipoDocumento); setVal("hu-numdoc",g.numeroDocumento);
    setVal("hu-correo",g.correo); setVal("hu-telefono",g.telefono);
    mostrarErrores("hu",{});
    document.getElementById("modal-agregar").textContent = editIdx>=0 ? "✔ Guardar Cambios" : "✔ Agregar";
    document.getElementById("modal").style.display="flex";
  }
  function cerrarModal(){ document.getElementById("modal").style.display="none"; }
  document.getElementById("modal-cancelar").onclick=cerrarModal;

  document.getElementById("modal-agregar").onclick=function(){
    var room=rooms[modalCtx.room];
    var nuevo=getModal();
    var e=erroresPersona(nuevo, labelDoc("hu-tipodoc"));
    Object.assign(e, duplicados(nuevo, personasExcepto(modalCtx.room, modalCtx.edit)));
    mostrarErrores("hu", e);
    if(Object.keys(e).length) return;
    var cap=capDe(room.idHabitacion);
    if(modalCtx.edit>=0){ room.huespedes[modalCtx.edit]=nuevo; }
    else {
      if(room.huespedes.length>=cap){ alert("No se pueden agregar más huéspedes: la habitación admite máximo "+cap+"."); return; }
      room.huespedes.push(nuevo);
    }
    cerrarModal(); render();
  };

  // ---------- buscar cliente ----------
  document.getElementById("btn-buscar-cli").onclick=function(){
    var doc=val("cli-numdoc"); if(!doc){ alert("Ingresa el N° de documento."); return; }
    fetch("/recepcionista/api/crud/buscar?doc="+encodeURIComponent(doc))
      .then(function(r){return r.json();})
      .then(function(res){
        if(!res.found){ alert("No se encontró un cliente con ese documento. Completa los datos para crearlo."); return; }
        var p=res.persona;
        setVal("cli-nombres",p.nombres); setVal("cli-apellidos",p.apellidos); setVal("cli-fnac",fmtISOaDMY(p.fechaNacimiento));
        setVal("cli-direccion",p.direccion); setVal("cli-tipodoc",p.idTipoDocumento);
        setVal("cli-correo",p.correo); setVal("cli-telefono",p.telefono);
        mostrarErrores("cli",{});
      });
  };

  // ---------- guardar ----------
  document.getElementById("btn-guardar").onclick=function(){
    var cliente=getCliente();
    var ec=erroresPersona(cliente, labelDoc("cli-tipodoc"));
    var listaHu=[]; rooms.forEach(function(r){ r.huespedes.forEach(function(h){ listaHu.push(h); }); });
    Object.assign(ec, duplicados(cliente, listaHu));
    mostrarErrores("cli", ec);
    if(Object.keys(ec).length){ window.scrollTo({top:0,behavior:"smooth"}); return; }

    for(var i=0;i<rooms.length;i++){
      var r=rooms[i];
      if(!r.idHabitacion){ alert("Selecciona la habitación "+(i+1)+"."); return; }
      if(r.huespedes.length>capDe(r.idHabitacion)){ alert("La habitación "+(i+1)+" excede su capacidad."); return; }
    }

    // --- validacion de fechas (inline, DD/MM/AAAA) ---
    limpiarErroresHab();
    var blocks=document.querySelectorAll(".hab-block");
    var hayErrorFecha=false, primerErr=null;
    function errHab(i, cual, msg){
      hayErrorFecha=true;
      var blk=blocks[i]; if(!blk) return;
      var span=blk.querySelector(cual==="cin" ? ".cin-err" : ".cout-err");
      var inp =blk.querySelector("."+cual);
      if(span) span.textContent=msg;
      if(inp)  inp.classList.add("invalido");
      if(!primerErr) primerErr=blk;
    }
    for(var i=0;i<rooms.length;i++){
      var r=rooms[i];
      var inIso  = parseDMYaISO(r.checkIn);
      var outIso = parseDMYaISO(r.checkOut);
      if(!r.checkIn) errHab(i,"cin","Ingresa la fecha de Check-In.");
      else if(!inIso) errHab(i,"cin","Fecha inválida. Usa DD/MM/AAAA.");
      if(!r.checkOut) errHab(i,"cout","Ingresa la fecha de Check-Out.");
      else if(!outIso) errHab(i,"cout","Fecha inválida. Usa DD/MM/AAAA.");
      if(inIso && outIso && outIso<=inIso)
        errHab(i,"cout","El Check-Out debe ser posterior al Check-In.");
    }
    // cruces dentro del formulario (misma habitacion, fechas que se solapan)
    for(var a=0;a<rooms.length;a++){
      for(var b=a+1;b<rooms.length;b++){
        var x=rooms[a], y=rooms[b];
        var xi=parseDMYaISO(x.checkIn), xo=parseDMYaISO(x.checkOut),
            yi=parseDMYaISO(y.checkIn), yo=parseDMYaISO(y.checkOut);
        if(x.idHabitacion===y.idHabitacion && xi && xo && yi && yo && xi < yo && yi < xo){
          errHab(b,"cout","Esta habitación se cruza con otra asignación en estas fechas.");
        }
      }
    }
    if(hayErrorFecha){ if(primerErr) primerErr.scrollIntoView({behavior:"smooth",block:"center"}); return; }

    // --- construir dto con fechas ISO ---
    var dto={ cliente:personaISO(cliente), habitaciones:rooms.map(function(r){
      return { idHabitacion:r.idHabitacion, checkIn:parseDMYaISO(r.checkIn), checkOut:parseDMYaISO(r.checkOut),
               huespedes:r.huespedes.map(personaISO) }; })};

    var editando=!!window.CODIGO_EDIT;
    var url=editando ? "/recepcionista/api/crud/"+encodeURIComponent(window.CODIGO_EDIT) : "/recepcionista/api/crud";
    var headers={ "Content-Type":"application/json" }; headers[csrfHeader]=csrfToken;
    fetch(url,{ method:editando?"PUT":"POST", headers:headers, body:JSON.stringify(dto) })
      .then(function(r){ return r.json().then(function(b){ return {ok:r.ok, body:b}; }); })
      .then(function(res){
        if(!res.ok) throw new Error(res.body.mensaje || "No se pudo guardar la reserva");
        window.location.href="/recepcionista/reservas";
      })
      .catch(function(e){ alert(e.message); });
  };
})();
