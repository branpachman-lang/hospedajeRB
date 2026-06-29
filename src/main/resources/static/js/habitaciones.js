(function () {
  "use strict";

  // ---- CSRF ----
  var csrfToken  = document.querySelector('meta[name="_csrf"]').content;
  var csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  // ---- Estado en memoria ----
  var habitaciones = [];
  var reservas = [];
  var pisoSel = null;
  var semanaInicio = domingoDe(new Date());

  var DIAS = ["Domingo","Lunes","Martes","Miércoles","Jueves","Viernes","Sábado"];

  // ---- Utilidades de fecha ----
  function domingoDe(d){ var x=new Date(d.getFullYear(),d.getMonth(),d.getDate()); x.setDate(x.getDate()-x.getDay()); return x; }
  function addDias(d,n){ var x=new Date(d); x.setDate(x.getDate()+n); return x; }
  function fmt(d){ return d.getFullYear()+"-"+String(d.getMonth()+1).padStart(2,"0")+"-"+String(d.getDate()).padStart(2,"0"); }
  function parseISO(s){ var p=s.split("-"); return new Date(+p[0],+p[1]-1,+p[2]); }
  function difDias(a,b){ return Math.round((a-b)/86400000); }
  function fmtCorto(d){ return d.getDate()+"/"+(d.getMonth()+1); }

  // ---- Carga de datos ----
  function cargarHabitaciones(){
    var t = document.getElementById("f-tipo").value;
    var e = document.getElementById("f-estado").value;
    var q = [];
    if(t) q.push("idTipo="+t);
    if(e) q.push("idEstado="+e);
    fetch("/recepcionista/api/habitaciones"+(q.length?"?"+q.join("&"):""))
      .then(function(r){return r.json();})
      .then(function(data){
        habitaciones = data;
        var pisos = pisosUnicos();
        if(pisos.indexOf(pisoSel)===-1){ pisoSel = pisos.length? pisos[0] : null; }
        renderPisos();
        renderLista();
        cargarReservas();
      });
  }

  function cargarReservas(){
    var desde = fmt(semanaInicio);
    var hasta = fmt(addDias(semanaInicio,6));
    fetch("/recepcionista/api/reservas?desde="+desde+"&hasta="+hasta)
      .then(function(r){return r.json();})
      .then(function(data){ reservas = data; renderCalendario(); });
  }

  // ---- Render ----
  function pisosUnicos(){
    var set = {};
    habitaciones.forEach(function(h){ set[h.piso]=true; });
    return Object.keys(set).map(Number).sort(function(a,b){return a-b;});
  }

  function renderPisos(){
    var cont = document.getElementById("pisos");
    cont.innerHTML = "";
    pisosUnicos().forEach(function(p){
      var n = habitaciones.filter(function(h){return h.piso===p;}).length;
      var b = document.createElement("button");
      b.className = "piso-tab"+(p===pisoSel?" activo":"");
      b.innerHTML = '<div class="num">Piso '+String(p).padStart(2,"0")+'</div><div class="cnt">'+n+' habitaciones</div>';
      b.onclick = function(){ pisoSel = p; renderPisos(); renderLista(); renderCalendario(); };
      cont.appendChild(b);
    });
  }

  function opcionesEstado(idActual){
    return (window.ESTADOS||[]).map(function(e){
      var sel = e.idEstadoHabitacion===idActual ? " selected" : "";
      return '<option value="'+e.idEstadoHabitacion+'"'+sel+'>'+e.nombreEstado+'</option>';
    }).join("");
  }

  function renderLista(){
    var cont = document.getElementById("lista");
    cont.innerHTML = "";
    var lista = habitaciones.filter(function(h){return h.piso===pisoSel;});
    if(!lista.length){ cont.innerHTML = '<div class="cal__vacio">Sin habitaciones.</div>'; return; }
    lista.forEach(function(h){
      var card = document.createElement("div");
      card.className = "hab-card";
      card.innerHTML =
        '<h3>Habitación '+h.numero+'</h3>'+
        '<div class="sub">Habitación: "'+h.tipo+'"</div>'+
        '<div class="precio">Precio: S/. '+h.precio+'</div>'+
        '<div class="desc">'+(h.descripcion||"")+'</div>'+
        '<div class="acciones">'+
          '<span class="badge c-'+h.color+'">'+h.estado+'</span>'+
        '</div>';
      cont.appendChild(card);
    });
  }

  function renderCalendario(){
    var cal = document.getElementById("cal");
    cal.innerHTML = "";

    // rango en la etiqueta de semana
    document.getElementById("sem-rango").textContent =
      fmtCorto(semanaInicio)+" – "+fmtCorto(addDias(semanaInicio,6));

    // cabecera
    var head = document.createElement("div");
    head.className = "cal__head";
    head.innerHTML = '<div class="cell"></div>' + [0,1,2,3,4,5,6].map(function(i){
      var d = addDias(semanaInicio,i);
      return '<div class="cell"><small>'+DIAS[i]+'</small><b>'+d.getDate()+'</b></div>';
    }).join("");
    cal.appendChild(head);

    var lista = habitaciones.filter(function(h){return h.piso===pisoSel;});
    if(!lista.length){ var v=document.createElement("div"); v.className="cal__vacio"; v.textContent="Selecciona un piso con habitaciones."; cal.appendChild(v); return; }

    lista.forEach(function(h){
      var row = document.createElement("div");
      row.className = "cal__row";
      var html = '<div class="lbl">'+h.numero+'</div>';
      for(var i=0;i<7;i++){ html += '<div class="day"></div>'; }
      row.innerHTML = html;

      // reservas de esta habitacion que se solapan con la semana
      reservas.filter(function(r){return r.idHabitacion===h.id;}).forEach(function(r){
        var ini = parseISO(r.ingreso), fin = parseISO(r.salida);
        var s = difDias(ini, semanaInicio);
        var e = difDias(fin, semanaInicio);
        if(e<0 || s>6) return;                 // fuera de la semana
        s = Math.max(0,s); e = Math.min(6,e);
        var bar = document.createElement("div");
        bar.className = "cal__bar c-"+r.color;
        bar.style.gridColumn = (2+s)+" / "+(2+e+1);
        bar.innerHTML = '<b>'+r.cliente+'</b><span>'+r.estado+'</span>';
        row.appendChild(bar);
      });
      cal.appendChild(row);
    });
  }

  function cambiarEstado(idHab, idEstado){
    var headers = { "Content-Type":"application/x-www-form-urlencoded" };
    headers[csrfHeader] = csrfToken;
    fetch("/recepcionista/api/habitaciones/"+idHab+"/estado", {
      method:"POST", headers:headers, body:"idEstado="+idEstado
    }).then(function(r){
      if(!r.ok) throw new Error("No se pudo cambiar el estado");
      cargarHabitaciones();   // refresca badges/colores
    }).catch(function(err){ alert(err.message); });
  }

  // ---- Eventos ----
  document.getElementById("f-tipo").onchange   = cargarHabitaciones;
  document.getElementById("f-estado").onchange = cargarHabitaciones;
  document.getElementById("sem-prev").onclick  = function(){ semanaInicio = addDias(semanaInicio,-7); cargarReservas(); };
  document.getElementById("sem-next").onclick  = function(){ semanaInicio = addDias(semanaInicio, 7); cargarReservas(); };
  document.getElementById("sem-hoy").onclick   = function(){ semanaInicio = domingoDe(new Date()); cargarReservas(); };

  // ---- Inicio ----
  cargarHabitaciones();
})();
