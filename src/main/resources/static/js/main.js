// Initialize dashboard
if (window.location.pathname.includes('dashboard.html')) {
    document.addEventListener('DOMContentLoaded', async () => {
        await ensureCsrfToken();
        
        initTheme();
        
        const langBtn = document.getElementById('langToggle');
        if (langBtn) {
            langBtn.textContent = currentLang === 'en' ? 'DK' : 'EN';
        }
        
        updateTranslations();
        
        loadProfile();
        loadCurrentMealPlan();
        loadHistory();
        
        const generateBtn = document.getElementById('generateBtn');
        if (generateBtn) {
            generateBtn.addEventListener('click', generateMealPlan);
        }
        
        const themeToggle = document.getElementById('themeToggle');
        if (themeToggle) {
            themeToggle.addEventListener('click', toggleTheme);
        }
        
        const langToggle = document.getElementById('langToggle');
        if (langToggle) {
            langToggle.addEventListener('click', toggleLanguage);
        }
    });
}
