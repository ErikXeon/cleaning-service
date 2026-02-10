const state = {
    user: null,
    services: []
};

const el = {
    alertBox: document.getElementById('alertBox'),
    btnRefresh: document.getElementById('btnRefresh'),
    btnLogout: document.getElementById('btnLogout'),
    profileBlock: document.getElementById('profileBlock'),
    notAuthorizedText: document.getElementById('notAuthorizedText'),
    authPanel: document.getElementById('authPanel'),
    ordersPanel: document.getElementById('ordersPanel'),
    historyPanel: document.getElementById('historyPanel'),
    servicesContainer: document.getElementById('servicesContainer'),
    ordersContainer: document.getElementById('ordersContainer'),
    loginForm: document.getElementById('loginForm'),
    registerForm: document.getElementById('registerForm'),
    verifyForm: document.getElementById('verifyForm'),
    orderForm: document.getElementById('orderForm'),
    btnResendCode: document.getElementById('btnResendCode')
};

init();

function init() {
    setupTabs();
    bindEvents();
    bootstrap();
}

async function bootstrap() {
    await refreshSession();
}

function bindEvents() {
    el.btnRefresh.addEventListener('click', refreshSession);
    el.btnLogout.addEventListener('click', logout);

    el.loginForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const data = Object.fromEntries(new FormData(el.loginForm));
        const response = await api('/api/auth/login', {
            method: 'POST',
            body: data
        });

        if (!response.ok) {
            showMessage(response.message || 'Ошибка входа', true);
            return;
        }

        showMessage(response.data?.message || 'Вход выполнен');
        el.loginForm.reset();
        await refreshSession();
    });

    el.registerForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const data = Object.fromEntries(new FormData(el.registerForm));
        const response = await api('/api/auth/register', {
            method: 'POST',
            body: data
        });

        if (!response.ok) {
            showMessage(response.message || 'Ошибка регистрации', true);
            return;
        }

        showMessage('Регистрация выполнена. Подтвердите email в разделе "Подтверждение".');
        el.registerForm.reset();
    });

    el.verifyForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const data = Object.fromEntries(new FormData(el.verifyForm));
        const response = await api('/api/auth/verify', {
            method: 'POST',
            body: data
        });

        if (!response.ok) {
            showMessage(response.message || 'Ошибка подтверждения', true);
            return;
        }

        const text = typeof response.data === 'string' ? response.data : 'Email подтвержден';
        showMessage(text);
        el.verifyForm.reset();
    });

    el.btnResendCode.addEventListener('click', async () => {
        const email = el.verifyForm.elements.email.value?.trim();
        if (!email) {
            showMessage('Введите email для повторной отправки кода', true);
            return;
        }

        const response = await api('/api/auth/resend-code', {
            method: 'POST',
            body: { email }
        });

        if (!response.ok) {
            showMessage(response.message || 'Не удалось отправить код', true);
            return;
        }

        showMessage(response.data?.message || 'Код отправлен повторно');
    });

    el.orderForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const formData = new FormData(el.orderForm);
        const dateTime = formData.get('dateTime');
        const address = formData.get('address')?.toString().trim();
        const notes = formData.get('notes')?.toString().trim() || null;

        const serviceIds = getCheckedServiceIds();

        if (!serviceIds.length) {
            showMessage('Выберите хотя бы одну услугу', true);
            return;
        }

        const response = await api('/api/client/orders', {
            method: 'POST',
            body: {
                dateTime: toBackendDateTime(dateTime),
                serviceIds,
                notes,
                address
            }
        });

        if (!response.ok) {
            showMessage(response.message || 'Ошибка создания заявки', true);
            return;
        }

        showMessage('Заявка успешно создана');
        el.orderForm.reset();
        await loadOrders();
    });
}

async function refreshSession() {
    const response = await api('/api/profile');

    if (!response.ok) {
        state.user = null;
        renderSession();
        return;
    }

    state.user = response.data;
    renderSession();

    const roles = state.user.roles || [];
    const isClient = roles.includes('ROLE_CLIENT');

    if (isClient) {
        await loadServices();
        await loadOrders();
    }
}

function renderSession() {
    const user = state.user;

    if (!user) {
        el.profileBlock.classList.add('hidden');
        el.notAuthorizedText.classList.remove('hidden');
        el.btnLogout.classList.add('hidden');
        el.ordersPanel.classList.add('hidden');
        el.historyPanel.classList.add('hidden');
        el.authPanel.classList.remove('hidden');
        return;
    }

    el.profileBlock.innerHTML = `
        <p><strong>${escapeHtml(user.firstName || '')} ${escapeHtml(user.lastName || '')}</strong></p>
        <p>Email: ${escapeHtml(user.email || '')}</p>
        <p>Роли: ${escapeHtml((user.roles || []).join(', '))}</p>
        <p>Статус: ${user.enabled ? 'Активен' : 'Не активирован'}</p>
    `;
    el.profileBlock.classList.remove('hidden');
    el.notAuthorizedText.classList.add('hidden');
    el.btnLogout.classList.remove('hidden');

    const isClient = (user.roles || []).includes('ROLE_CLIENT');
    if (isClient) {
        el.ordersPanel.classList.remove('hidden');
        el.historyPanel.classList.remove('hidden');
    } else {
        el.ordersPanel.classList.add('hidden');
        el.historyPanel.classList.add('hidden');
    }
}

async function loadServices() {
    el.servicesContainer.innerHTML = '<p class="muted">Загрузка услуг...</p>';
    const response = await api('/api/client/services');

    if (!response.ok) {
        el.servicesContainer.innerHTML = '<p class="muted">Не удалось загрузить услуги.</p>';
        return;
    }

    state.services = Array.isArray(response.data) ? response.data : [];

    if (!state.services.length) {
        el.servicesContainer.innerHTML = '<p class="muted">Услуги отсутствуют.</p>';
        return;
    }

    el.servicesContainer.innerHTML = state.services.map(service => {
        return `
            <label class="service-item">
                <span>
                    <strong>${escapeHtml(service.name)}</strong>
                    <span class="service-meta">${formatMoney(service.price)} • ${service.durationMinutes || 0} мин</span>
                </span>
                <input type="checkbox" name="service" value="${service.id}" />
            </label>
        `;
    }).join('');
}

async function loadOrders() {
    el.ordersContainer.innerHTML = '<p class="muted">Загрузка заявок...</p>';
    const response = await api('/api/client/orders');

    if (!response.ok) {
        el.ordersContainer.innerHTML = '<p class="muted">Не удалось загрузить заявки.</p>';
        return;
    }

    if (!Array.isArray(response.data)) {
        const message = response.data?.message || 'Нет бронирований';
        el.ordersContainer.innerHTML = `<p class="muted">${escapeHtml(message)}</p>`;
        return;
    }

    if (!response.data.length) {
        el.ordersContainer.innerHTML = '<p class="muted">Заявок пока нет.</p>';
        return;
    }

    el.ordersContainer.innerHTML = response.data.map(order => renderOrderCard(order)).join('');
}

function renderOrderCard(order) {
    const services = (order.services || []).map(service => service.name).join(', ') || '—';
    return `
        <article class="order-card">
            <div class="order-head">
                <strong>Заявка #${order.id}</strong>
                <span class="badge">${escapeHtml(order.status || '')}</span>
            </div>
            <p><strong>Дата:</strong> ${formatDate(order.dateTime)}</p>
            <p><strong>Адрес:</strong> ${escapeHtml(order.address || '—')}</p>
            <p><strong>Услуги:</strong> ${escapeHtml(services)}</p>
            <p><strong>Комментарий:</strong> ${escapeHtml(order.notes || '—')}</p>
            <p><strong>Стоимость:</strong> ${formatMoney(order.totalPrice)}</p>
        </article>
    `;
}

function getCheckedServiceIds() {
    return Array.from(document.querySelectorAll('input[name="service"]:checked'))
        .map(item => Number(item.value))
        .filter(Number.isFinite);
}

async function logout() {
    const response = await api('/api/auth/logout', { method: 'POST' });
    if (!response.ok) {
        showMessage(response.message || 'Ошибка выхода', true);
        return;
    }
    showMessage(response.data?.message || 'Вы вышли из системы');
    state.user = null;
    renderSession();
}

function setupTabs() {
    const tabs = document.querySelectorAll('.tab');
    const panels = document.querySelectorAll('.tab-panel');

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(item => item.classList.remove('active'));
            panels.forEach(panel => panel.classList.remove('active'));
            tab.classList.add('active');
            document.getElementById(tab.dataset.tab).classList.add('active');
        });
    });
}

function showMessage(message, isError = false) {
    el.alertBox.textContent = message;
    el.alertBox.classList.remove('hidden', 'error', 'success');
    el.alertBox.classList.add(isError ? 'error' : 'success');
}

async function api(url, options = {}) {
    try {
        const response = await fetch(url, {
            method: options.method || 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: options.body ? JSON.stringify(options.body) : undefined
        });

        const contentType = response.headers.get('content-type') || '';
        const payload = contentType.includes('application/json')
            ? await response.json()
            : await response.text();

        if (!response.ok) {
            return {
                ok: false,
                status: response.status,
                message: payload?.message || payload?.error || 'Ошибка запроса',
                data: payload
            };
        }

        return { ok: true, status: response.status, data: payload };
    } catch (_error) {
        return { ok: false, message: 'Проблема сети. Проверьте подключение.' };
    }
}

function formatMoney(value) {
    const number = Number(value);
    if (!Number.isFinite(number)) {
        return '—';
    }
    return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', maximumFractionDigits: 0 }).format(number);
}

function formatDate(value) {
    if (!value) {
        return '—';
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString('ru-RU');
}

function toBackendDateTime(value) {
    if (!value) {
        return null;
    }
    return `${value}:00`;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}