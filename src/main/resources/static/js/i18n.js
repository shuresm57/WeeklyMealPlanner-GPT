// i18n Translations
const translations = {
    en: {
        preferences: 'Preferences',
        diet: 'Diet Type',
        allergies: 'Allergies',
        dislikes: 'Dislikes',
        currentPlan: 'Current Meal Plan',
        generate: 'Generate Plan',
        generating: 'Generating your meal plan...',
        noMealPlan: 'No meal plan yet',
        clickGenerate: 'Click "Generate Plan" to create your first meal plan',
        history: 'Meal History',
        noHistory: 'No history yet',
        omnivore: 'Omnivore',
        vegetarian: 'Vegetarian',
        vegan: 'Vegan',
        none: 'None',
        sendEmail: 'Send via Email',
        emailSent: 'Email sent!',
        emailFailed: 'Failed to send email'
    },
    da: {
        preferences: 'Præferencer',
        diet: 'Diættype',
        allergies: 'Allergier',
        dislikes: 'Ikke bryder sig om',
        currentPlan: 'Nuværende Måltidsplan',
        generate: 'Generer Plan',
        generating: 'Genererer din måltidsplan...',
        noMealPlan: 'Ingen måltidsplan endnu',
        clickGenerate: 'Klik "Generer Plan" for at oprette din første måltidsplan',
        history: 'Måltidshistorik',
        noHistory: 'Ingen historik endnu',
        omnivore: 'Altædende',
        vegetarian: 'Vegetar',
        vegan: 'Veganer',
        none: 'Ingen',
        sendEmail: 'Send på Email',
        emailSent: 'Email sendt!',
        emailFailed: 'Email kunne ikke sendes'
    }
};

let currentLang = localStorage.getItem('lang') || 'en';

// Update translations
function updateTranslations() {
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (translations[currentLang][key]) {
            el.textContent = translations[currentLang][key];
        }
    });
}

// Set language
function setLanguage(lang) {
    currentLang = lang;
    document.documentElement.setAttribute('lang', currentLang);
    localStorage.setItem('lang', currentLang);
    updateTranslations();
    updateLanguageButtons();
}

// Update language button states
function updateLanguageButtons() {
    const enBtn = document.getElementById('langEN');
    const daBtn = document.getElementById('langDA');
    
    if (enBtn && daBtn) {
        if (currentLang === 'en') {
            enBtn.classList.add('active');
            enBtn.classList.remove('btn-outline-secondary');
            enBtn.classList.add('btn-secondary');
            daBtn.classList.remove('active', 'btn-secondary');
            daBtn.classList.add('btn-outline-secondary');
        } else {
            daBtn.classList.add('active');
            daBtn.classList.remove('btn-outline-secondary');
            daBtn.classList.add('btn-secondary');
            enBtn.classList.remove('active', 'btn-secondary');
            enBtn.classList.add('btn-outline-secondary');
        }
    }
}

function translate(key) {
    return translations[currentLang][key] || key;
}
