(function () {
  "use strict";

  var csrfToken = document.querySelector('meta[name="_csrf"]').content;
  var csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
  var productos = [];
  var carrito = new Map();
  var categoriaActual = "";
  var reservaActual = null;
  var debounce = 0;

  function $(id) { return document.getElementById(id); }
  function money(n) { return "S/ " + (Number(n || 0)).toFixed(2); }
  function initials(name) {
    return String(name || "P").split(/\s+/).filter(Boolean).slice(0, 2).map(function (x) { return x[0]; }).join("").toUpperCase();
  }
  function headersJson() {
    var h = { "Content-Type": "application/json" };
    h[csrfHeader] = csrfToken;
    return h;
  }

  function cargarProductos() {
    var q = $("buscar-producto").value.trim();
    var params = new URLSearchParams();
    if (categoriaActual) params.set("categoriaId", categoriaActual);
    if (q) params.set("q", q);
    fetch("/recepcionista/api/venta-producto/productos?" + params.toString())
      .then(function (r) { return r.json(); })
      .then(function (data) {
        productos = data || [];
        renderProductos();
        renderCarrito();
      })
      .catch(function () {
        $("productos").innerHTML = "";
        $("productos-vacio").hidden = false;
      });
  }

  function renderProductos() {
    var cont = $("productos");
    var empty = $("productos-vacio");
    cont.innerHTML = "";
    empty.hidden = productos.length > 0;
    productos.forEach(function (p) {
      var cantidad = carrito.get(p.idProducto) || 0;
      var sinStock = Number(p.stock) <= 0;
      var excedido = cantidad > Number(p.stock);
      var card = document.createElement("article");
      card.className = "producto-card" + (sinStock || excedido ? " sin-stock" : "");
      card.innerHTML =
        '<span class="stock-badge ' + (p.stockBajo ? "bajo" : "") + '">' + (sinStock ? "Sin stock" : "Stock: " + p.stock) + '</span>' +
        '<div class="producto-img">' + initials(p.nombreProducto) + '</div>' +
        '<div class="producto-info">' +
          '<span class="producto-cat">' + (p.nombreCategoria || "Producto") + '</span>' +
          '<h3>' + p.nombreProducto + '</h3>' +
          (sinStock ? '<div class="sin-stock-msg">No hay stock del producto</div>' : '') +
          (excedido ? '<div class="sin-stock-msg">Stock insuficiente. Disponible: ' + p.stock + '</div>' : '') +
          '<div class="producto-bottom"><span class="precio">' + money(p.precioActual) + '</span>' +
          '<button class="btn-add" type="button" ' + (sinStock ? "disabled" : "") + ' title="Agregar al carrito">+</button></div>' +
        '</div>';
      card.querySelector(".btn-add").onclick = function () {
        carrito.set(p.idProducto, cantidad + 1);
        renderProductos();
        renderCarrito();
      };
      cont.appendChild(card);
    });
  }

  function productoPorId(id) {
    return productos.find(function (p) { return Number(p.idProducto) === Number(id); });
  }

  function renderCarrito() {
    var cont = $("cart-items");
    var empty = $("cart-empty");
    cont.innerHTML = "";
    var entries = Array.from(carrito.entries());
    empty.hidden = entries.length > 0;
    $("cart-count").textContent = entries.reduce(function (acc, it) { return acc + it[1]; }, 0) + " items";

    var total = 0;
    var invalid = false;

    entries.forEach(function (entry) {
      var id = entry[0], cantidad = entry[1];
      var p = productoPorId(id);
      if (!p) return;
      var excedido = cantidad > Number(p.stock);
      if (excedido) invalid = true;
      var linea = Number(p.precioActual) * cantidad;
      total += linea;

      var div = document.createElement("div");
      div.className = "cart-line" + (excedido ? " error" : "");
      div.innerHTML =
        '<div>' +
          '<div class="cart-title">' + p.nombreProducto + '</div>' +
          '<div class="cart-small">Precio Unit: ' + money(p.precioActual) + '</div>' +
          '<div class="cart-actions">' +
            '<button class="qty-btn menos" type="button">-</button>' +
            '<strong>' + cantidad + '</strong>' +
            '<button class="qty-btn mas" type="button">+</button>' +
            '<button class="trash-btn" type="button" title="Quitar">Quitar</button>' +
          '</div>' +
          (excedido ? '<div class="line-error">Supera el stock disponible (' + p.stock + ')</div>' : '') +
        '</div>' +
        '<div class="line-total">' + money(linea) + '</div>';
      div.querySelector(".menos").onclick = function () {
        if (cantidad <= 1) carrito.delete(id);
        else carrito.set(id, cantidad - 1);
        renderProductos(); renderCarrito();
      };
      div.querySelector(".mas").onclick = function () {
        carrito.set(id, cantidad + 1);
        renderProductos(); renderCarrito();
      };
      div.querySelector(".trash-btn").onclick = function () {
        carrito.delete(id);
        renderProductos(); renderCarrito();
      };
      cont.appendChild(div);
    });

    var subtotal = total / 1.18;
    var igv = total - subtotal;
    $("subtotal").textContent = money(subtotal);
    $("igv").textContent = money(igv);
    $("total").textContent = money(total);
    $("stock-alert").hidden = !invalid;
    $("btn-finalizar").disabled = !entries.length || invalid;
    $("btn-cargo").disabled = !entries.length || invalid || !reservaActual;
  }

  function buscarReservas(q) {
    if (!q || q.length < 2) {
      $("resultados-reserva").hidden = true;
      $("resultados-reserva").innerHTML = "";
      return;
    }
    fetch("/recepcionista/api/venta-producto/reservas?q=" + encodeURIComponent(q))
      .then(function (r) { return r.json(); })
      .then(function (data) {
        var box = $("resultados-reserva");
        box.innerHTML = "";
        if (!data.length) {
          box.innerHTML = '<div class="reserva-seleccionada">No se encontraron reservas activas.</div>';
          box.hidden = false;
          return;
        }
        data.forEach(function (res) {
          var btn = document.createElement("button");
          btn.type = "button";
          btn.className = "reserva-result";
          btn.innerHTML =
            '<strong>Habitacion ' + res.habitacion + ' - ' + res.cliente + '</strong>' +
            '<div class="reserva-meta"><span>Codigo: ' + (res.codigoReserva || res.idReserva) + '</span><span>' + res.fechaIngreso + ' / ' + res.fechaSalida + '</span><span>' + res.tipoHabitacion + '</span></div>';
          btn.onclick = function () {
            reservaActual = res;
            $("resultados-reserva").hidden = true;
            $("buscar-reserva").value = "";
            renderReserva();
            renderCarrito();
          };
          box.appendChild(btn);
        });
        box.hidden = false;
      });
  }

  function renderReserva() {
    var panel = $("reserva-seleccionada");
    if (!reservaActual) {
      panel.hidden = true;
      panel.innerHTML = "";
      $("venta-contexto").textContent = "VENTA DIRECTA";
      $("cliente-titulo").textContent = "Cliente Externo (Generico)";
      $("cliente-subtitulo").textContent = "Documento: 00000000";
      $("btn-cliente-externo").classList.add("activo");
      return;
    }
    $("btn-cliente-externo").classList.remove("activo");
    $("venta-contexto").textContent = "HUESPED";
    $("cliente-titulo").textContent = reservaActual.cliente;
    $("cliente-subtitulo").textContent = "Habitacion " + reservaActual.habitacion + " - " + reservaActual.tipoHabitacion;
    var huespedes = Array.isArray(reservaActual.huespedes) ? reservaActual.huespedes : [];
    panel.innerHTML =
      '<strong>Venta vinculada a habitacion ' + reservaActual.habitacion + '</strong>' +
      '<div class="reserva-meta"><span>Cliente: ' + reservaActual.cliente + '</span><span>Ingreso: ' + reservaActual.fechaIngreso + '</span><span>Salida: ' + reservaActual.fechaSalida + '</span><span>Tipo: ' + reservaActual.tipoHabitacion + '</span></div>' +
      '<div class="reserva-huespedes"><strong>Huespedes</strong>' +
      (huespedes.length ? '<ul>' + huespedes.map(function (h) { return '<li>' + h + '</li>'; }).join('') + '</ul>' : '<p>Sin huespedes registrados.</p>') +
      '</div>';
    panel.hidden = false;
  }

  function confirmar(cargarAHabitacion) {
    var items = Array.from(carrito.entries()).map(function (entry) {
      return { idProducto: Number(entry[0]), cantidad: Number(entry[1]) };
    });
    fetch("/recepcionista/api/venta-producto/confirmar", {
      method: "POST",
      headers: headersJson(),
      body: JSON.stringify({
        idReserva: reservaActual ? reservaActual.idReserva : null,
        cargarAHabitacion: !!cargarAHabitacion,
        items: items
      })
    })
      .then(function (r) {
        return r.json().then(function (body) { return { ok: r.ok, body: body }; });
      })
      .then(function (res) {
        if (!res.ok) throw new Error(res.body.mensaje || "No se pudo registrar la venta.");
        mostrarExito(res.body.comprobante);
      })
      .catch(function (e) { alert(e.message); cargarProductos(); });
  }

  function mostrarExito(comp) {
    $("mensaje-inventario").textContent = comp.cargadoHabitacion
      ? "Cargo registrado a la habitacion e inventario actualizado"
      : "Inventario actualizado y pago verificado";
    $("ticket").innerHTML = renderTicket(comp);
    $("modal-exito").hidden = false;
    carrito.clear();
  }

  function renderTicket(comp) {
    var reserva = comp.reserva;
    var items = (comp.items || []).map(function (i) {
      return '<div class="ticket-row"><span>' + i.producto + ' x' + i.cantidad + '</span><strong>' + money(i.total) + '</strong></div>';
    }).join("");
    return '' +
      '<h3>BOLOGNESI REAL</h3>' +
      '<p style="text-align:center">RUC: 20123456789<br>Calle Bolognesi 456 - Tacna</p><hr>' +
      '<div class="ticket-row"><strong>BOLETA ELECTRONICA</strong><strong>' + comp.serie + '-' + comp.numeroComprobante + '</strong></div>' +
      '<div class="ticket-row"><span>Fecha:</span><span>' + String(comp.fechaEmision).replace("T", " ").slice(0, 16) + '</span></div>' +
      (reserva ? '<div class="ticket-reserva"><strong>Reserva / Huesped</strong><br>Cliente: ' + reserva.cliente + '<br>Huespedes: ' + ((reserva.huespedes || []).join(", ") || "Sin huespedes registrados") + '<br>Habitacion: ' + reserva.habitacion + ' - ' + reserva.tipoHabitacion + '<br>Ingreso: ' + reserva.fechaIngreso + '<br>Salida: ' + reserva.fechaSalida + '<br>Caracteristicas: ' + (reserva.descripcionHabitacion || "Sin descripcion") + '</div>' : '') +
      '<hr>' + items + '<hr>' +
      '<div class="ticket-row"><span>SUBTOTAL:</span><strong>' + money(comp.subtotal) + '</strong></div>' +
      '<div class="ticket-row"><span>IGV (18%):</span><strong>' + money(comp.igv) + '</strong></div>' +
      '<div class="ticket-row ticket-total"><span>TOTAL:</span><strong>' + money(comp.total) + '</strong></div>' +
      '<div style="height:90px;background:#eee;display:grid;place-items:center;margin:26px auto 10px;width:110px">QR</div>' +
      '<p style="text-align:center;font-size:10px">Representacion impresa de la boleta. Gracias por su compra.</p>';
  }

  document.querySelectorAll(".tab-categoria").forEach(function (btn) {
    btn.onclick = function () {
      document.querySelectorAll(".tab-categoria").forEach(function (b) { b.classList.remove("activo"); });
      btn.classList.add("activo");
      categoriaActual = btn.dataset.id || "";
      cargarProductos();
    };
  });
  $("buscar-producto").addEventListener("input", function () {
    clearTimeout(debounce);
    debounce = setTimeout(cargarProductos, 260);
  });
  $("buscar-reserva").addEventListener("input", function () {
    var q = this.value.trim();
    clearTimeout(debounce);
    debounce = setTimeout(function () { buscarReservas(q); }, 260);
  });
  $("btn-cliente-externo").onclick = function () {
    reservaActual = null;
    renderReserva();
    renderCarrito();
  };
  $("btn-finalizar").onclick = function () { confirmar(false); };
  $("btn-cargo").onclick = function () { confirmar(true); };
  $("btn-cancelar").onclick = function () {
    carrito.clear();
    reservaActual = null;
    renderReserva();
    renderProductos();
    renderCarrito();
  };
  $("btn-imprimir").onclick = function () { window.print(); };
  $("btn-correo").onclick = function () { alert("Envio por correo pendiente de integracion SMTP del comprobante."); };
  $("btn-nueva-venta").onclick = function () {
    $("modal-exito").hidden = true;
    reservaActual = null;
    renderReserva();
    cargarProductos();
  };

  renderReserva();
  cargarProductos();
})();
