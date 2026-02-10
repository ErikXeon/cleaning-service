const state = {
    user: null,
    services: [],
    verificationEmail: ''
};

const ORDER_STATUSES = ['NEW', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];

const el = {
    alertBox: document.getElementById('alertBox'),
    toastContainer: document.getElementById('toastContainer'),
    btnRefresh: document.getElementById('btnRefresh'),
    btnLogout: document.getElementById('btnLogout'),
    authPanel: document.getElementById('authPanel'),
    profileWidget: document.getElementById('profileWidget'),
    profileName: document.getElementById('profileName'),
    profileEmail: document.getElementById('profileEmail'),
    profileStatus: document.getElementById('profileStatus'),
    verifyTabButton: document.getElementById('verifyTabButton'),
    verifyEmailText: document.getElementById('verifyEmailText'),

    loginForm: document.getElementById('loginForm'),
    registerForm: document.getElementById('registerForm'),
    verifyForm: document.getElementById('verifyForm'),
    btnResendCode: document.getElementById('btnResendCode'),

    clientOrderPanel: document.getElementById('clientOrderPanel'),
    clientOrdersPanel: document.getElementById('clientOrdersPanel'),
    orderForm: document.getElementById('orderForm'),
    servicesContainer: document.getElementById('servicesContainer'),
    clientOrdersContainer: document.getElementById('clientOrdersContainer'),

    managerPanel: document.getElementById('managerPanel'),
    managerOrdersContainer: document.getElementById('managerOrdersContainer'),
    assignRoleForm: document.getElementById('assignRoleForm'),
    pdfForm: document.getElementById('pdfForm')
};

init();

function init() {
    setupTabs();
    bindEvents();
    refreshSession();
}

function bindEvents() {
    el.btnRefresh.addEventListener('click', refreshSession);
    el.btnLogout.addEventListener('click', logout);

    el.loginForm.addEventListener('submit', onLogin);
    el.registerForm.addEventListener('submit', onRegister);
    el.verifyForm.addEventListener('submit', onVerify);
    el.btnResendCode.addEventListener('click', onResendCode);

    el.orderForm.addEventListener('submit', onCreateOrder);
    el.assignRoleForm.addEventListener('submit', onAssignRole);
    el.pdfForm.addEventListener('submit', onDownloadPdf);
}

async function refreshSession() {
    const response = await api('/api/profile');

    if (!response.ok) {
        state.user = null;
        renderApp();
        return;
    }

    state.user = response.data;
    renderApp();

    const roles = state.user.roles || [];
    if (roles.includes('ROLE_CLIENT')) {
        await loadServices();
        await loadClientOrders();
    }
    if (roles.includes('ROLE_MANAGER')) {
        await loadManagerOrders();
    }
}

function renderApp() {
    const user = state.user;
    if (!user) {
        el.profileWidget.classList.add('hidden');
        el.authPanel.classList.remove('hidden');
        el.btnLogout.classList.add('hidden');
        el.clientOrderPanel.classList.add('hidden');
        el.clientOrdersPanel.classList.add('hidden');
        el.managerPanel.classList.add('hidden');
        return;
    }

    el.profileName.textContent = `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'Без имени';
    el.profileEmail.textContent = user.email || '—';
    el.profileStatus.textContent = user.enabled ? 'Активен' : 'Не активирован';
    el.profileWidget.classList.remove('hidden');

    el.authPanel.classList.add('hidden');
    el.btnLogout.classList.remove('hidden');

    const roles = user.roles || [];
    el.clientOrderPanel.classList.toggle('hidden', !roles.includes('ROLE_CLIENT'));
    el.clientOrdersPanel.classList.toggle('hidden', !roles.includes('ROLE_CLIENT'));
    el.managerPanel.classList.toggle('hidden', !roles.includes('ROLE_MANAGER'));
}

async function onLogin(event) {
    event.preventDefault();
    const payload = Object.fromEntries(new FormData(el.loginForm));
    const response = await api('/api/auth/login', { method: 'POST', body: payload });

    if (!response.ok) {
        showMessage(response.message || 'Ошибка входа', true);
        return;
    }

    showMessage(response.data?.message || 'Вход выполнен');
    el.loginForm.reset();
    await refreshSession();
}

async function onRegister(event) {
    event.preventDefault();
    const payload = Object.fromEntries(new FormData(el.registerForm));

    const response = await api('/api/auth/register', { method: 'POST', body: payload });
    if (!response.ok) {
        showMessage(response.message || 'Ошибка регистрации', true);
        return;
    }

    state.verificationEmail = payload.email;
    el.verifyForm.elements.email.value = payload.email;
    el.verifyEmailText.textContent = payload.email;
    el.verifyTabButton.classList.remove('hidden');
    openTab('verifyTab');

    showMessage('Регистрация успешна. Введите код подтверждения из почты.');
}

async function onVerify(event) {
    event.preventDefault();

    const payload = Object.fromEntries(new FormData(el.verifyForm));
    if (!payload.email) {
        showMessage('Не найден email для подтверждения. Зарегистрируйтесь снова.', true);
        return;
    }

    const response = await api('/api/auth/verify', { method: 'POST', body: payload });
    if (!response.ok) {
        showMessage(response.message || 'Ошибка подтверждения', true);
        return;
    }

    const message = typeof response.data === 'string' ? response.data : 'Email подтвержден';
    showMessage(`${message}. Теперь выполните вход.`);
    el.verifyForm.reset();
    openTab('loginTab');
}

async function onResendCode() {
    const email = el.verifyForm.elements.email.value || state.verificationEmail;
    if (!email) {
        showMessage('Не найден email для отправки кода', true);
        return;
    }

    const response = await api('/api/auth/resend-code', { method: 'POST', body: { email } });
    if (!response.ok) {
        showMessage(response.message || 'Не удалось отправить код', true);
        return;
    }

    showMessage(response.data?.message || 'Код отправлен повторно');
}

async function onCreateOrder(event) {
    event.preventDefault();

    const formData = new FormData(el.orderForm);
    const serviceIds = Array.from(document.querySelectorAll('input[name="service"]:checked'))
        .map(item => Number(item.value))
        .filter(Number.isFinite);

    if (!serviceIds.length) {
        showMessage('Выберите хотя бы одну услугу', true);
        return;
    }

    const payload = {
        dateTime: toBackendDateTime(formData.get('dateTime')),
        address: String(formData.get('address') || '').trim(),
        notes: String(formData.get('notes') || '').trim() || null,
        serviceIds
    };

    const response = await api('/api/client/orders', { method: 'POST', body: payload });
    if (!response.ok) {
        showMessage(response.message || 'Ошибка создания заявки', true);
        return;
    }

    showMessage('Заявка успешно создана');
    el.orderForm.reset();
    await loadClientOrders();
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

    el.servicesContainer.innerHTML = state.services.map(service => `
        <label class="service-item">
            <span>
                <strong>${escapeHtml(service.name)}</strong>
                <span class="service-meta">${formatMoney(service.price)} • ${service.durationMinutes || 0} мин</span>
            </span>
            <input type="checkbox" name="service" value="${service.id}" />
        </label>
    `).join('');
}

async function loadClientOrders() {
    el.clientOrdersContainer.innerHTML = '<p class="muted">Загрузка заявок...</p>';
    const response = await api('/api/client/orders');
    if (!response.ok) {
        el.clientOrdersContainer.innerHTML = '<p class="muted">Не удалось загрузить заявки.</p>';
        return;
    }

    const orders = normalizeOrdersResponse(response.data);
    if (!orders.length) {
        el.clientOrdersContainer.innerHTML = '<p class="muted">Заявок пока нет.</p>';
        return;
    }

    el.clientOrdersContainer.innerHTML = orders.map(renderOrderCard).join('');
}

async function loadManagerOrders() {
    el.managerOrdersContainer.innerHTML = '<p class="muted">Загрузка заявок...</p>';
    const response = await api('/api/manager/orders');

    if (!response.ok) {
        el.managerOrdersContainer.innerHTML = '<p class="muted">Не удалось загрузить заявки для менеджера.</p>';
        return;
    }

    const orders = normalizeOrdersResponse(response.data);
    if (!orders.length) {
        el.managerOrdersContainer.innerHTML = '<p class="muted">Нет заявок.</p>';
        return;
    }

    el.managerOrdersContainer.innerHTML = orders.map(renderManagerOrderCard).join('');
    bindManagerOrderActions();
}

function renderOrderCard(order) {
    const services = (order.services || []).map(item => item.name).join(', ') || '—';
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

function renderManagerOrderCard(order) {
    const services = (order.services || []).map(item => item.name).join(', ') || '—';
    const cleanerEmail = order.cleaningStaff?.email || '';
    const statusOptions = ORDER_STATUSES.map(status =>
        `<option value="${status}" ${status === order.status ? 'selected' : ''}>${status}</option>`
    ).join('');

    return `
        <article class="order-card" data-order-id="${order.id}">
            <div class="order-head">
                <strong>Заявка #${order.id}</strong>
                <span class="badge">${escapeHtml(order.status || '')}</span>
            </div>
            <p><strong>Клиент:</strong> ${escapeHtml(order.client?.email || '—')}</p>
            <p><strong>Уборщик:</strong> ${escapeHtml(cleanerEmail || 'не назначен')}</p>
            <p><strong>Дата:</strong> ${formatDate(order.dateTime)}</p>
            <p><strong>Адрес:</strong> ${escapeHtml(order.address || '—')}</p>
            <p><strong>Услуги:</strong> ${escapeHtml(services)}</p>
            <div class="manager-actions">
                <form class="assign-cleaner-form inline-form">
                    <input name="cleanerEmail" type="email" required placeholder="email уборщика" value="${escapeHtml(cleanerEmail)}" />
                    <button class="btn btn-secondary" type="submit">Назначить уборщика</button>
                </form>
                <form class="update-status-form inline-form">
                    <select name="status" required>${statusOptions}</select>
                    <button class="btn btn-primary" type="submit">Обновить статус</button>
                </form>
            </div>
        </article>
    `;
}

function bindManagerOrderActions() {
    document.querySelectorAll('.assign-cleaner-form').forEach(form => {
        form.addEventListener('submit', async event => {
            event.preventDefault();
            const orderId = event.target.closest('.order-card')?.dataset.orderId;
            const cleanerEmail = String(new FormData(form).get('cleanerEmail') || '').trim();
            if (!orderId || !cleanerEmail) {
                showMessage('Неверные данные для назначения уборщика', true);
                return;
            }
            const response = await api(`/api/manager/orders/${orderId}/assign`, {
                method: 'POST',
                body: { cleanerEmail }
            });
            if (!response.ok) {
                showMessage(response.message || 'Не удалось назначить уборщика', true);
                return;
            }
            showMessage(`Уборщик назначен для заявки #${orderId}`);
            await loadManagerOrders();
        });
    });

    document.querySelectorAll('.update-status-form').forEach(form => {
        form.addEventListener('submit', async event => {
            event.preventDefault();
            const orderId = event.target.closest('.order-card')?.dataset.orderId;
            const status = String(new FormData(form).get('status') || '');
            if (!orderId || !status) {
                showMessage('Неверные данные для обновления статуса', true);
                return;
            }
            const response = await api(`/api/manager/orders/${orderId}/status`, {
                method: 'POST',
                body: { status }
            });
            if (!response.ok) {
                showMessage(response.message || 'Не удалось обновить статус', true);
                return;
            }
            showMessage(`Статус заявки #${orderId} обновлен`);
            await loadManagerOrders();
        });
    });
}

async function onAssignRole(event) {
    event.preventDefault();
    const payload = Object.fromEntries(new FormData(el.assignRoleForm));
    const response = await api('/api/manager/users/roles', { method: 'POST', body: payload });
    if (!response.ok) {
        showMessage(response.message || 'Не удалось назначить роль', true);
        return;
    }

    showMessage(`Роль назначена: ${payload.email} -> ${payload.role}`);
    el.assignRoleForm.reset();
}

async function onDownloadPdf(event) {
    event.preventDefault();
    const cleanerEmail = String(new FormData(el.pdfForm).get('cleanerEmail') || '').trim();
    const date = String(new FormData(el.pdfForm).get('date') || '').trim();

    if (!cleanerEmail) {
        showMessage('Укажите email уборщика', true);
        return;
    }

    const query = date ? `?date=${encodeURIComponent(date)}` : '';
    const url = `/api/manager/orders/pdf/${encodeURIComponent(cleanerEmail)}${query}`;

    const response = await fetch(url, { method: 'GET', credentials: 'include' });
    if (!response.ok) {
        const message = await extractErrorMessage(response);
        showMessage(message || 'Не удалось скачать PDF', true);
        return;
    }

    const blob = await response.blob();
    const downloadUrl = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = `tasks-${cleanerEmail}.pdf`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(downloadUrl);

    showMessage('PDF успешно скачан');
}

async function logout() {
    const response = await api('/api/auth/logout', { method: 'POST' });
    if (!response.ok) {
        showMessage(response.message || 'Ошибка выхода', true);
        return;
    }

    showMessage(response.data?.message || 'Вы вышли из системы');
    state.user = null;
    renderApp();
}

function setupTabs() {
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', () => openTab(tab.dataset.tab));
    });
}

function openTab(tabId) {
    document.querySelectorAll('.tab').forEach(tab => {
        tab.classList.toggle('active', tab.dataset.tab === tabId);
    });
    document.querySelectorAll('.tab-panel').forEach(panel => {
        panel.classList.toggle('active', panel.id === tabId);
    });
}

function showMessage(message, isError = false) {
    const tone = isError ? 'error' : 'success';

    if (el.alertBox && !el.authPanel.classList.contains('hidden')) {
        el.alertBox.textContent = message;
        el.alertBox.classList.remove('hidden', 'error', 'success');
        el.alertBox.classList.add(tone);
    }

    showToast(message, tone);
}

function showToast(message, tone = 'success') {
    if (!el.toastContainer) {
        return;
    }

    const toast = document.createElement('div');
    toast.className = `toast ${tone}`;
    toast.textContent = message;
    el.toastContainer.appendChild(toast);

    window.setTimeout(() => {
        toast.remove();
    }, 3800);
}

function normalizeOrdersResponse(data) {
    if (Array.isArray(data)) {
        return data;
    }
    return [];
}

async function api(url, options = {}) {
    try {
        const response = await fetch(url, {
            method: options.method || 'GET',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: options.body ? JSON.stringify(options.body) : undefined
        });

        const contentType = response.headers.get('content-type') || '';
        const payload = contentType.includes('application/json') ? await response.json() : await response.text();

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

async function extractErrorMessage(response) {
    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
        const payload = await response.json();
        return payload?.message || payload?.error;
    }
    return await response.text();
}

function formatMoney(value) {
    const number = Number(value);
    if (!Number.isFinite(number)) return '—';
    return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', maximumFractionDigits: 0 }).format(number);
}

function formatDate(value) {
    if (!value) return '—';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString('ru-RU');
}

function toBackendDateTime(value) {
    if (!value) return null;
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