// Get CSRF token
function getCsrfToken() {
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === 'XSRF-TOKEN') {
            return decodeURIComponent(value);
        }
    }
    return '';
}

// Ensure CSRF token exists
async function ensureCsrfToken() {
    if (!getCsrfToken()) {
        await fetch('/api/csrf', { credentials: 'same-origin' });
    }
}
