const navToggle = document.getElementById('navToggle');
const navLinks = document.getElementById('navLinks');

if (navToggle && navLinks) {
    navToggle.addEventListener('click', () => {
        navLinks.classList.toggle('nav__links--open');
    });

    navLinks.querySelectorAll('a').forEach((link) => {
        link.addEventListener('click', () => navLinks.classList.remove('nav__links--open'));
    });
}

const reviews = Array.from(document.querySelectorAll('#reviewsList .review'));
let reviewIndex = 0;

function showReview(index) {
    reviews.forEach((item, i) => {
        item.classList.toggle('review--active', i === index);
    });
}

if (reviews.length) {
    document.getElementById('prevReview')?.addEventListener('click', () => {
        reviewIndex = (reviewIndex - 1 + reviews.length) % reviews.length;
        showReview(reviewIndex);
    });

    document.getElementById('nextReview')?.addEventListener('click', () => {
        reviewIndex = (reviewIndex + 1) % reviews.length;
        showReview(reviewIndex);
    });

    setInterval(() => {
        reviewIndex = (reviewIndex + 1) % reviews.length;
        showReview(reviewIndex);
    }, 7000);
}

function validatePhone(value) {
    const digits = value.replace(/\D/g, '');
    return digits.length >= 11;
}

function showMessage(el, text, isOk) {
    if (!el) return;
    el.textContent = text;
    el.classList.remove('form__msg--ok', 'form__msg--error');
    el.classList.add(isOk ? 'form__msg--ok' : 'form__msg--error');
}

function handleSimpleForm(formId, msgId) {
    const form = document.getElementById(formId);
    const msg = document.getElementById(msgId);
    if (!form) return;

    form.addEventListener('submit', (event) => {
        event.preventDefault();

        const formData = new FormData(form);
        const name = String(formData.get('name') || '').trim();
        const phone = String(formData.get('phone') || '').trim();

        if (!name) {
            showMessage(msg, 'Пожалуйста, укажите имя.', false);
            return;
        }

        if (!validatePhone(phone)) {
            showMessage(msg, 'Укажите корректный телефон в формате +7...', false);
            return;
        }

        showMessage(msg, 'Спасибо! Заявка принята, скоро с вами свяжемся.', true);
        form.reset();
    });
}

handleSimpleForm('quickForm', 'quickFormMsg');
handleSimpleForm('bookingForm', 'bookingMsg');

// Плавная прокрутка для якорей
Array.from(document.querySelectorAll('a[href^="#"]')).forEach((a) => {
    a.addEventListener('click', (e) => {
        const targetId = a.getAttribute('href');
        if (!targetId || targetId.length === 1) return;

        const target = document.querySelector(targetId);
        if (!target) return;

        e.preventDefault();
        target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
});