(function () {
    "use strict";
    var csrfToken = document.querySelector('meta[name="_csrf"]').content;
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    var cart = [];          // {idProducto, nombre, precio, cantidad, stock}
    var reservaSel = null;  // {idReserva, ...}

    function money(n) {
        return "S/ " + (Number(n) || 0).toFixed(2);
    }

    function debounce(fn, ms) {
        var t;
        return function () {
            var a = arguments, c = this;
            clearTimeout(t);
            t = setTimeout(function () {
                fn.apply(c, a);
            }, ms);
        };
    }

    // ---------- formas de pago ----------
    function cargarFormas() {
        fetch("/recepcionista/api/venta-producto/formas-pago")
            .then(function (r) {
                return r.json();
            })
            .then(function (lista) {
                var sel = document.getElementById("f-formapago");
                sel.innerHTML = "";
                lista.forEach(function (f) {
                    var o = document.createElement("option");
                    o.value = f.idFormaPago;
                    o.textContent = f.nombrePago;
                    o.dataset.nombre = f.nombrePago;
                    sel.appendChild(o);
                });
                sel.onchange = toggleOperacion;
                toggleOperacion();
            });
    }

    function formaEsEfectivo() {
        var sel = document.getElementById("f-formapago");
        var opt = sel.selectedOptions[0];
        return !opt || /efectivo/i.test(opt.dataset.nombre || opt.textContent);
    }

    function toggleOperacion() {
        var mostrar = !formaEsEfectivo();
        document.getElementById("lbl-operacion").style.display = mostrar ? "block" : "none";
        var inp = document.getElementById("f-operacion");
        inp.style.display = mostrar ? "block" : "none";
        if (!mostrar) inp.value = "";
    }

    // ---------- catalogo ----------
    function cargarProductos() {
        var cat = document.getElementById("f-categoria").value;
        var q = document.getElementById("f-buscar").value.trim();
        var url = "/recepcionista/api/venta-producto/productos?";
        if (cat) url += "categoriaId=" + encodeURIComponent(cat) + "&";
        if (q) url += "q=" + encodeURIComponent(q);
        fetch(url).then(function (r) {
            return r.json();
        }).then(renderProductos);
    }

    function renderProductos(lista) {
        var cont = document.getElementById("productos");
        cont.innerHTML = "";
        document.getElementById("prod-vacio").style.display = lista.length ? "none" : "block";
        lista.forEach(function (p) {
            var card = document.createElement("div");
            card.className = "prod-card" + (p.disponible ? "" : " agotado");
            card.innerHTML =
                '<div class="prod-cat">' + p.nombreCategoria + '</div>' +
                '<div class="prod-nom">' + p.nombreProducto + '</div>' +
                '<div class="prod-pie">' +
                '<span class="prod-precio">' + money(p.precioActual) + '</span>' +
                '<span class="prod-stock' + (p.stockBajo ? ' bajo' : '') + '">Stock: ' + p.stock + '</span>' +
                '</div>' +
                '<button class="btn-add" ' + (p.disponible ? '' : 'disabled') + '>' + (p.disponible ? '＋ Agregar' : 'Agotado') + '</button>';
            var btn = card.querySelector(".btn-add");
            if (p.disponible) btn.onclick = function () {
                agregar(p);
            };
            cont.appendChild(card);
        });
    }

    // ---------- carrito ----------
    function agregar(p) {
        var it = cart.find(function (x) {
            return x.idProducto === p.idProducto;
        });
        if (it) {
            if (it.cantidad + 1 > p.stock) {
                alert("No hay más stock de " + p.nombreProducto + " (máx " + p.stock + ").");
                return;
            }
            it.cantidad++;
        } else {
            cart.push({
                idProducto: p.idProducto,
                nombre: p.nombreProducto,
                precio: Number(p.precioActual),
                cantidad: 1,
                stock: p.stock
            });
        }
        renderCart();
    }

    function cambiar(idx, delta) {
        var it = cart[idx];
        var nuevo = it.cantidad + delta;
        if (nuevo <= 0) {
            cart.splice(idx, 1);
            renderCart();
            return;
        }
        if (nuevo > it.stock) {
            alert("Stock máximo: " + it.stock + ".");
            return;
        }
        it.cantidad = nuevo;
        renderCart();
    }

    function renderCart() {
        var cont = document.getElementById("cart-items");
        cont.innerHTML = "";
        document.getElementById("cart-vacio").style.display = cart.length ? "none" : "block";
        cart.forEach(function (it, idx) {
            var row = document.createElement("div");
            row.className = "cart-row";
            row.innerHTML =
                '<div class="cart-info"><strong>' + it.nombre + '</strong><small>' + money(it.precio) + ' c/u</small></div>' +
                '<div class="cart-qty">' +
                '<button class="qbtn" data-d="-1">−</button>' +
                '<span>' + it.cantidad + '</span>' +
                '<button class="qbtn" data-d="1">＋</button>' +
                '</div>' +
                '<div class="cart-sub">' + money(it.precio * it.cantidad) + '</div>' +
                '<button class="cart-del" title="Quitar">✖</button>';
            row.querySelectorAll(".qbtn").forEach(function (b) {
                b.onclick = function () {
                    cambiar(idx, parseInt(b.dataset.d, 10));
                };
            });
            row.querySelector(".cart-del").onclick = function () {
                cart.splice(idx, 1);
                renderCart();
            };
            cont.appendChild(row);
        });
        var total = cart.reduce(function (s, it) {
            return s + it.precio * it.cantidad;
        }, 0);
        var subtotal = total / 1.18, igv = total - subtotal;
        document.getElementById("t-subtotal").textContent = money(subtotal);
        document.getElementById("t-igv").textContent = money(igv);
        document.getElementById("t-total").textContent = money(total);
    }

    // ---------- reserva (cargar a habitacion) ----------
    document.getElementById("chk-habitacion").onchange = function () {
        var on = this.checked;
        document.getElementById("box-reserva").style.display = on ? "block" : "none";
        document.getElementById("box-pago").style.display = on ? "none" : "block";
        if (!on) {
            reservaSel = null;
            pintarReservaSel();
        }
    };
    var buscarReserva = debounce(function () {
        var q = document.getElementById("f-reserva").value.trim();
        var box = document.getElementById("reserva-result");
        if (q.length < 2) {
            box.innerHTML = "";
            return;
        }
        fetch("/recepcionista/api/venta-producto/reservas?q=" + encodeURIComponent(q))
            .then(function (r) {
                return r.json();
            })
            .then(function (lista) {
                box.innerHTML = "";
                if (!lista.length) {
                    box.innerHTML = '<div class="reserva-empty">Sin coincidencias</div>';
                    return;
                }
                lista.forEach(function (r) {
                    var d = document.createElement("div");
                    d.className = "reserva-opt";
                    d.innerHTML = '<strong>N° ' + (r.codigoReserva || r.idReserva) + '</strong> · Hab. ' + r.habitacion + ' · ' + r.cliente;
                    d.onclick = function () {
                        reservaSel = r;
                        box.innerHTML = "";
                        document.getElementById("f-reserva").value = "";
                        pintarReservaSel();
                    };
                    box.appendChild(d);
                });
            });
    }, 300);
    document.getElementById("f-reserva").addEventListener("input", buscarReserva);

    function pintarReservaSel() {
        var c = document.getElementById("reserva-sel");
        if (!reservaSel) {
            c.style.display = "none";
            c.innerHTML = "";
            return;
        }
        c.style.display = "block";
        c.innerHTML = '<div>Reserva <strong>N° ' + (reservaSel.codigoReserva || reservaSel.idReserva) + '</strong><br>' +
            'Hab. ' + reservaSel.habitacion + ' · ' + reservaSel.cliente + '</div>' +
            '<button type="button" class="cart-del" id="quitar-res">✖</button>';
        document.getElementById("quitar-res").onclick = function () {
            reservaSel = null;
            pintarReservaSel();
        };
    }

    // ---------- confirmar ----------
    document.getElementById("btn-confirmar").onclick = function () {
        if (!cart.length) {
            alert("Agrega al menos un producto.");
            return;
        }
        var cargar = document.getElementById("chk-habitacion").checked;
        if (cargar && !reservaSel) {
            alert("Selecciona una reserva para cargar a la habitación.");
            return;
        }

        var idFormaPago = null, numOp = null;
        if (!cargar) {
            var selFp = document.getElementById("f-formapago");
            idFormaPago = selFp.value ? parseInt(selFp.value, 10) : null;
            if (!formaEsEfectivo()) numOp = document.getElementById("f-operacion").value.trim() || null;
        }
        var dto = {
            idReserva: reservaSel ? reservaSel.idReserva : null,
            cargarAHabitacion: cargar,
            idFormaPago: idFormaPago,
            numeroOperacion: numOp,
            items: cart.map(function (it) {
                return {idProducto: it.idProducto, cantidad: it.cantidad};
            })
        };
        var headers = {"Content-Type": "application/json"};
        headers[csrfHeader] = csrfToken;
        var btn = this;
        btn.disabled = true;
        fetch("/recepcionista/api/venta-producto/confirmar", {
            method: "POST",
            headers: headers,
            body: JSON.stringify(dto)
        })
            .then(function (r) {
                return r.json().then(function (b) {
                    return {ok: r.ok, body: b};
                });
            })
            .then(function (res) {
                btn.disabled = false;
                if (!res.ok || !res.body.ok) throw new Error((res.body && res.body.mensaje) || "No se pudo registrar la venta");
                mostrarComprobante(res.body.comprobante);
            })
            .catch(function (e) {
                btn.disabled = false;
                alert(e.message);
            });
    };

    function mostrarComprobante(c) {
        var filas = (c.items || []).map(function (i) {
            return '<tr><td>' + i.producto + '</td><td>' + i.cantidad + '</td><td>' + money(i.precioUnitario) + '</td><td>' + money(i.total) + '</td></tr>';
        }).join("");
        var body = document.getElementById("comp-body");
        body.innerHTML =
            '<div class="comp-head">' + c.serie + '-' + c.numeroComprobante +
            (c.cargadoHabitacion ? ' · <span class="tag">Cargado a habitación</span>' : '') + '</div>' +
            (c.reserva ? '<div class="comp-res">Reserva N° ' + (c.reserva.codigoReserva || c.reserva.idReserva) + ' · ' + c.reserva.cliente + '</div>' : '') +
            '<table class="comp-tabla"><thead><tr><th>Producto</th><th>Cant.</th><th>P. Unit.</th><th>Total</th></tr></thead><tbody>' + filas + '</tbody></table>' +
            '<div class="comp-tot"><div><span>Subtotal</span><b>' + money(c.subtotal) + '</b></div>' +
            '<div><span>IGV</span><b>' + money(c.igv) + '</b></div>' +
            '<div class="total-final"><span>Total</span><b>' + money(c.total) + '</b></div></div>';
        document.getElementById("modal-comp").style.display = "flex";
    }

    document.getElementById("comp-cerrar").onclick = function () {
        document.getElementById("modal-comp").style.display = "none";
        cart = [];
        reservaSel = null;
        document.getElementById("chk-habitacion").checked = false;
        document.getElementById("box-reserva").style.display = "none";
        document.getElementById("box-pago").style.display = "block";
        document.getElementById("f-operacion").value = "";
        toggleOperacion();
        pintarReservaSel();
        renderCart();
        cargarProductos();
    };

    // ---------- init ----------
    document.getElementById("f-categoria").onchange = cargarProductos;
    document.getElementById("f-buscar").addEventListener("input", debounce(cargarProductos, 300));
    cargarProductos();
    cargarFormas();
    renderCart();
})();
