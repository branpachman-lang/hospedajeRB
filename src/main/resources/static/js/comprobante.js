(function () {
  "use strict";
  var csrfToken  = document.querySelector('meta[name="_csrf"]').content;
  var csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  var codigoActual = null;
  var resumenActual = null;
  var formas = null, tipos = null;

  function money(n){ return "S/ " + (Number(n)||0).toFixed(2); }
  function el(id){ return document.getElementById(id); }

  function cargarCatalogos(){
    var p1 = formas ? Promise.resolve(formas)
      : fetch("/recepcionista/api/comprobante/formas-pago").then(function(r){return r.json();}).then(function(d){ formas=d; return d; });
    var p2 = tipos ? Promise.resolve(tipos)
      : fetch("/recepcionista/api/comprobante/tipos-comprobante").then(function(r){return r.json();}).then(function(d){ tipos=d; return d; });
    return Promise.all([p1, p2]);
  }

  function abrirModal(codigo){
    codigoActual = codigo;
    fetch("/recepcionista/api/comprobante/resumen/" + encodeURIComponent(codigo))
      .then(function(r){ return r.json().then(function(b){ return {ok:r.ok, body:b}; }); })
      .then(function(res){
        if(!res.ok){ throw new Error(res.body.mensaje || "No se pudo cargar la reserva"); }
        resumenActual = res.body;
        if(resumenActual.yaPagada){ alert("Esta reserva ya tiene un comprobante emitido."); return; }
        return cargarCatalogos().then(function(){ pintarResumen(); el("modal-pago").style.display="flex"; });
      })
      .catch(function(e){ alert(e.message); });
  }

  function pintarResumen(){
    var r = resumenActual;
    var habs = (r.habitaciones||[]).map(function(h){
      return '<tr><td>'+h.descripcion+'</td><td class="sub">'+h.detalle+'</td><td class="imp">'+money(h.importe)+'</td></tr>';
    }).join("");
    var cargos = (r.cargos||[]).map(function(c){
      return '<tr><td>'+c.descripcion+'</td><td class="sub">'+c.detalle+'</td><td class="imp">'+money(c.importe)+'</td></tr>';
    }).join("");
    el("pago-resumen").innerHTML =
      '<div class="pago-cli"><strong>'+r.cliente+'</strong>'+
        (r.numeroDocumento ? ' · '+r.tipoDocumento+' '+r.numeroDocumento : '')+
        ' · '+r.totalHuespedes+' huésped(es)</div>'+
      '<table class="pago-tabla"><tbody>'+habs+
        (cargos ? '<tr class="sep"><td colspan="3">Consumos cargados a la habitación</td></tr>'+cargos : '')+
      '</tbody></table>'+
      '<div class="pago-tot">'+
        '<div><span>Estadía</span><b>'+money(r.totalEstadia)+'</b></div>'+
        (Number(r.totalCargos)>0 ? '<div><span>Consumos</span><b>'+money(r.totalCargos)+'</b></div>' : '')+
        '<div class="grande"><span>Total a pagar</span><b>'+money(r.total)+'</b></div>'+
      '</div>';

    // tipos
    var selTipo = el("pago-tipo"); selTipo.innerHTML="";
    (tipos||[]).forEach(function(t){
      var esFactura = /factura/i.test(t);
      var o = document.createElement("option");
      o.value = t;
      if(esFactura && !r.esRuc){ o.disabled = true; o.textContent = t + " (requiere RUC)"; }
      else o.textContent = t;
      selTipo.appendChild(o);
    });
    // seleccionar boleta por defecto
    for(var i=0;i<selTipo.options.length;i++){ if(!selTipo.options[i].disabled){ selTipo.selectedIndex=i; break; } }
    el("e-pago-tipo").textContent = r.esRuc ? "" : "Factura deshabilitada: el cliente no tiene RUC.";

    // formas
    var selForma = el("pago-forma"); selForma.innerHTML="";
    (formas||[]).forEach(function(f){
      var o=document.createElement("option"); o.value=f.idFormaPago; o.textContent=f.nombrePago; selForma.appendChild(o);
    });
  }

  function cerrar(){ el("modal-pago").style.display="none"; codigoActual=null; }

  function emitir(){
    if(!codigoActual) return;
    var tipo = el("pago-tipo").value;
    if(!tipo){ el("e-pago-tipo").textContent="Selecciona un tipo de comprobante."; return; }
    var idForma = el("pago-forma").value ? parseInt(el("pago-forma").value,10) : null;
    var headers={ "Content-Type":"application/json" }; headers[csrfHeader]=csrfToken;
    var btn=el("pago-emitir"); btn.disabled=true;
    fetch("/recepcionista/api/comprobante/emitir/"+encodeURIComponent(codigoActual),
          { method:"POST", headers:headers, body:JSON.stringify({ idFormaPago:idForma, tipoComprobante:tipo }) })
      .then(function(r){ return r.json().then(function(b){ return {ok:r.ok, body:b}; }); })
      .then(function(res){
        btn.disabled=false;
        if(!res.ok || !res.body.ok) throw new Error((res.body && res.body.mensaje) || "No se pudo emitir el comprobante");
        var id = res.body.comprobante.idComprobante;
        cerrar();
        window.open("/recepcionista/comprobante/"+id+"/ticket", "_blank");
        if(window.recargarReservas) window.recargarReservas();
      })
      .catch(function(e){ btn.disabled=false; alert(e.message); });
  }

  function imprimir(codigo){
    fetch("/recepcionista/api/comprobante/vigente/"+encodeURIComponent(codigo))
      .then(function(r){return r.json();})
      .then(function(res){
        if(!res.ok || !res.idComprobante){ alert("No se encontró un comprobante emitido para esta reserva."); return; }
        window.open("/recepcionista/comprobante/"+res.idComprobante+"/ticket", "_blank");
      });
  }

  function anular(codigo){
    if(!confirm("¿Anular la reserva N° "+codigo+"? Se emitirá una NOTA DE CRÉDITO, la reserva quedará cancelada y la habitación liberada.")) return;
    var headers={}; headers[csrfHeader]=csrfToken;
    fetch("/recepcionista/api/comprobante/anular/"+encodeURIComponent(codigo), { method:"POST", headers:headers })
      .then(function(r){ return r.json().then(function(b){ return {ok:r.ok, body:b}; }); })
      .then(function(res){
        if(!res.ok || !res.body.ok) throw new Error((res.body && res.body.mensaje) || "No se pudo anular");
        var nc = res.body.comprobante;
        alert("Reserva anulada. Nota de Crédito "+nc.serie+"-"+nc.numero+" emitida.");
        window.open("/recepcionista/comprobante/"+nc.idComprobante+"/ticket", "_blank");
        if(window.recargarReservas) window.recargarReservas();
      })
      .catch(function(e){ alert(e.message); });
  }

  el("pago-cancelar").onclick = cerrar;
  el("pago-emitir").onclick = emitir;

  window.ComprobantePago = { abrirModal:abrirModal, imprimir:imprimir, anular:anular };
})();
