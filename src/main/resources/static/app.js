const API_URL = '/api/chat';
let categoriaActual = 'Tramite';
let sessionId = localStorage.getItem('redvecinal_session_id') || null;
let enviando = false;

const chat = document.getElementById('chat');
const form = document.getElementById('form-chat');
const input = document.getElementById('input-mensaje');
const escribiendo = document.getElementById('escribiendo');
const botonesCat = document.querySelectorAll('#categorias button');

botonesCat.forEach(btn => {
    btn.addEventListener('click', () => {
        botonesCat.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        categoriaActual = btn.dataset.cat;
    });
});

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const mensaje = input.value.trim();
    if (!mensaje || enviando) return;

    agregarMensaje(mensaje, 'usuario');
    input.value = '';
    input.disabled = true;
    enviando = true;
    escribiendo.classList.remove('oculto');
    chat.scrollTop = chat.scrollHeight;

    try {
        const body = { mensaje, categoria: categoriaActual };
        if (sessionId) body.sessionId = sessionId;

        const response = await fetch(API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (!response.ok) throw new Error('Error de red ' + response.status);

        const data = await response.json();
        if (data.sessionId) {
            sessionId = data.sessionId;
            localStorage.setItem('redvecinal_session_id', sessionId);
        }

        agregarMensaje(data.respuesta, 'asistente', data.fuente, data.tiempoMs);
    } catch (error) {
        agregarMensaje('No pude conectar con el asistente. Verifica tu conexion e intenta nuevamente.', 'asistente', 'error');
    } finally {
        enviando = false;
        escribiendo.classList.add('oculto');
        input.disabled = false;
        input.focus();
    }
});

function agregarMensaje(texto, tipo, fuente, tiempoMs) {
    const div = document.createElement('div');
    div.className = 'mensaje ' + tipo;

    const burbuja = document.createElement('div');
    burbuja.className = 'burbuja';
    burbuja.textContent = texto;

    if (tipo === 'asistente' && fuente) {
        const br = document.createElement('br');
        burbuja.appendChild(br);

        const badge = document.createElement('span');
        badge.className = 'fuente-badge fuente-' + fuente;
        const etiqueta = fuente === 'faq' ? 'FAQ' : fuente === 'ollama' ? 'IA' : 'ERROR';
        badge.textContent = etiqueta;
        burbuja.appendChild(badge);

        if (tiempoMs) {
            const info = document.createElement('span');
            info.className = 'fuente-info';
            info.textContent = ' ' + tiempoMs + ' ms';
            burbuja.appendChild(info);
        }
    }

    div.appendChild(burbuja);
    chat.appendChild(div);
    chat.scrollTop = chat.scrollHeight;
}

if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('service-worker.js').catch(() => {});
}