(function () {
    "use strict";
    var csrfToken = document.querySelector('meta[name="_csrf"]').content;
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    function cargar() {
        var estado = document.getElementById("f-estado").value;
        fetch("/recepcionista/api/crud/lista" + (estado ? "?estado=" + encodeURIComponent(estado) : ""))
            .then(function (r) {
                return r.json();
            })
            .then(render);
    }

    window.recargarReservas = cargar;

    function render(lista) {
        var tb = document.getElementById("tbody");
        tb.innerHTML = "";
        document.getElementById("vacio").style.display = lista.length ? "none" : "block";
        lista.forEach(function (r) {
            var hab = r.habitaciones === 1 ? "1 Habitación" : r.habitaciones + " Habitaciones";
            var hue = r.huespedes === 1 ? "1 huésped incluido" : r.huespedes + " huéspedes incluidos";
            var desc = "Reserva a nombre de: " + r.cliente + "; " + hab + "; " + hue + ".";
            var tr = document.createElement("tr");
            var acciones = r.pagada
                ? '<button class="ic-btn ic-print" title="Imprimir comprobante">🖨</button>' +
                '<button class="btn-anular">Anular</button>'
                : '<button class="ic-btn ic-del" title="Eliminar">✖</button>' +
                '<button class="btn-comp">Generar Comprobante</button>';
            tr.innerHTML =
                "<td>N° " + r.codigo + "</td>" +
                "<td>" + desc + "</td>" +
                '<td><div class="col-acciones">' +
                '<a class="ic-btn ic-edit" title="Editar" href="/recepcionista/reservas/editar/' + r.codigo + '">✎</a>' +
                acciones +
                '</div></td>';
            if (r.pagada) {
                tr.querySelector(".ic-print").onclick = function () {
                    window.ComprobantePago.imprimir(r.codigo);
                };
                tr.querySelector(".btn-anular").onclick = function () {
                    window.ComprobantePago.anular(r.codigo);
                };
            } else {
                tr.querySelector(".ic-del").onclick = function () {
                    eliminar(r.codigo);
                };
                tr.querySelector(".btn-comp").onclick = function () {
                    window.ComprobantePago.abrirModal(r.codigo);
                };
            }
            tb.appendChild(tr);
        });
    }

    function eliminar(codigo) {
        if (!confirm("¿Eliminar la reserva N° " + codigo + "? (solo si no está pagada)")) return;
        var h = {};
        h[csrfHeader] = csrfToken;
        fetch("/recepcionista/api/crud/" + codigo, {method: "DELETE", headers: h})
            .then(leer).then(function (res) {
            if (!res.ok) throw new Error(res.body.mensaje || "No se pudo eliminar");
            cargar();
        }).catch(function (e) {
            alert(e.message);
        });
    }

    function leer(r) {
        return r.json().then(function (b) {
            return {ok: r.ok, body: b};
        });
    }

    document.getElementById("f-estado").onchange = cargar;
    cargar();
})();
