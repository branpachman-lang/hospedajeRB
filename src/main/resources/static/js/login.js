// Mostrar / ocultar contrasena (icono del ojo del prototipo)
document.querySelectorAll('[data-toggle]').forEach(function (btn) {
    btn.addEventListener('click', function () {
        var input = document.getElementById(btn.getAttribute('data-toggle'));
        if (!input) return;
        input.type = input.type === 'password' ? 'text' : 'password';
    });
});
