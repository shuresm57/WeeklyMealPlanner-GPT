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
let currentTheme = localStorage.getItem('theme') || 'light';
let currentMealPlanId = null;

// Apply saved preferences
document.documentElement.setAttribute('data-theme', currentTheme);
document.documentElement.setAttribute('lang', currentLang);

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

// Load profile
async function loadProfile() {
    try {
        const response = await fetch('/api/profile');
        if (response.ok) {
            const profile = await response.json();
            document.getElementById('userName').textContent = profile.name || 'User';
            document.getElementById('userEmail').textContent = profile.email || '';
            
            // Show content, hide skeleton
            document.getElementById('profileSkeleton').style.display = 'none';
            document.getElementById('profileContent').style.display = 'block';
            
            // Load preferences
            loadPreferences(profile);
        }
    } catch (error) {
        console.error('Error loading profile:', error);
    }
}

// Load preferences
function loadPreferences(profile) {
    const dietType = profile.dietType || 'omnivore';
    const allergies = profile.allergies || [];
    const dislikes = profile.dislikes || [];
    
    document.getElementById('dietType').textContent = translations[currentLang][dietType] || dietType;
    document.getElementById('allergiesList').textContent = allergies.length > 0 ? allergies.join(', ') : translations[currentLang].none;
    document.getElementById('dislikesList').textContent = dislikes.length > 0 ? dislikes.join(', ') : translations[currentLang].none;
    
    document.getElementById('preferencesSkeleton').style.display = 'none';
    document.getElementById('preferencesContent').style.display = 'block';
}

// Load current meal plan
async function loadCurrentMealPlan() {
    try {
        const response = await fetch('/api/mealplan/current');
        
        if (response.status === 204) {
            showEmptyState();
            return;
        }
        
        if (response.ok) {
            const plan = await response.json();
            displayMealPlan(plan);
        } else {
            showEmptyState();
        }
    } catch (error) {
        console.error('Error loading meal plan:', error);
        showEmptyState();
    } finally {
        document.getElementById('mealPlanSkeleton').style.display = 'none';
    }
}

// Display meal plan
function displayMealPlan(planData) {
    const container = document.getElementById('mealPlanContent');
    const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];
    
    let plan = planData;
    let message = null;
    
    // Check if we received a MealPlanResponse object
    if (planData.mealPlan) {
        plan = planData.mealPlan;
        message = planData.message;
    }
    
    currentMealPlanId = plan.id;
    
    if (!plan.meals || plan.meals.length === 0) {
        showEmptyState();
        return;
    }
    
    let html = '';
    
    // Show ChatGPT message if available
    if (message) {
        html += `
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="bi bi-robot"></i> <strong>ChefGPT says:</strong> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
    }
    
    // Add email button
    html += `
        <div class="mb-3 d-flex justify-content-end">
            <button class="btn btn-outline-primary btn-sm" onclick="sendMealPlanByEmail()">
                <i class="bi bi-envelope"></i> <span data-i18n="sendEmail">Send via Email</span>
            </button>
        </div>
    `;
    
    // Calculate weeks
    const totalMeals = plan.meals.length;
    const weeksCount = Math.ceil(totalMeals / 5);
    
    for (let week = 0; week < weeksCount; week++) {
        if (weeksCount > 1) {
            html += `<h5 class="mt-4 mb-3">Week ${week + 1}</h5>`;
        }
        
        const startIdx = week * 5;
        const endIdx = Math.min(startIdx + 5, totalMeals);
        
        for (let i = startIdx; i < endIdx; i++) {
            const meal = plan.meals[i];
            const dayIndex = i % 5;
            
            html += `
                <div class="card shadow-sm mb-3 meal-card">
                    <div class="card-body">
                        <div class="meal-day">${days[dayIndex] || 'Day ' + (dayIndex + 1)}</div>
                        <h5 class="meal-title">${meal.mealName}</h5>
                        <div class="meal-ingredients">
                            ${meal.ingredients.slice(0, 5).map(ing => 
                                `<span class="ingredient-badge">${ing}</span>`
                            ).join('')}
                            ${meal.ingredients.length > 5 ? `<span class="ingredient-badge">+${meal.ingredients.length - 5} more</span>` : ''}
                        </div>
                    </div>
                </div>
            `;
        }
    }
    
    container.innerHTML = html;
    document.getElementById('mealPlanSkeleton').style.display = 'none';
    document.getElementById('emptyState').style.display = 'none';
}

// Show empty state
function showEmptyState() {
    document.getElementById('mealPlanSkeleton').style.display = 'none';
    document.getElementById('mealPlanContent').innerHTML = '';
    document.getElementById('emptyState').style.display = 'block';
}

// Generate meal plan
async function generateMealPlan() {
    const btn = document.getElementById('generateBtn');
    const spinner = document.getElementById('loadingSpinner');
    
    btn.disabled = true;
    spinner.classList.remove('d-none');
    document.getElementById('mealPlanContent').innerHTML = '';
    document.getElementById('emptyState').style.display = 'none';

    await ensureCsrfToken();
    const csrfToken = getCsrfToken();

    try {
        const response = await fetch('/api/mealplan/generate?type=monthly', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken
            },
            credentials: 'same-origin'
        });

        if (response.ok) {
            const planResponse = await response.json();
            displayMealPlan(planResponse);
            loadHistory();
        } else {
            const error = await response.json();
            alert(error.message || 'Failed to generate meal plan');
        }
    } catch (error) {
        console.error('Error generating meal plan:', error);
        alert('An error occurred. Please try again.');
    } finally {
        btn.disabled = false;
        spinner.classList.add('d-none');
    }
}

// Send meal plan by email
async function sendMealPlanByEmail() {
    if (!currentMealPlanId) {
        alert('No meal plan to send');
        return;
    }
    
    await ensureCsrfToken();
    const csrfToken = getCsrfToken();
    
    try {
        const response = await fetch(`/api/mealplan/${currentMealPlanId}/email`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken
            },
            credentials: 'same-origin'
        });
        
        if (response.ok) {
            alert(translations[currentLang].emailSent || 'Email sent!');
        } else {
            alert(translations[currentLang].emailFailed || 'Failed to send email');
        }
    } catch (error) {
        console.error('Error sending email:', error);
        alert(translations[currentLang].emailFailed || 'Failed to send email');
    }
}

// Load history
async function loadHistory() {
    try {
        const response = await fetch('/api/mealplan/history');
        if (response.ok) {
            const history = await response.json();
            displayHistory(history);
        }
    } catch (error) {
        console.error('Error loading history:', error);
    } finally {
        document.getElementById('historySkeleton').style.display = 'none';
    }
}

// Display history
function displayHistory(history) {
    const container = document.getElementById('historyContent');
    
    if (!history || history.length === 0) {
        document.getElementById('historyEmpty').style.display = 'block';
        document.getElementById('historyContent').style.display = 'none';
        return;
    }
    
    container.innerHTML = history.slice(0, 10).map(plan => {
        const date = new Date(plan.weekStartDate).toLocaleDateString();
        const mealCount = plan.meals ? plan.meals.length : 0;
        
        return `
            <a href="#" class="list-group-item list-group-item-action history-item" data-plan='${JSON.stringify(plan)}'>
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="history-date">${date}</div>
                        <div class="history-meals">${mealCount} meals</div>
                    </div>
                    <i class="bi bi-chevron-right text-muted"></i>
                </div>
            </a>
        `;
    }).join('');
    
    document.getElementById('historyContent').style.display = 'block';
    document.getElementById('historyEmpty').style.display = 'none';
    
    // Add click handlers
    document.querySelectorAll('.history-item').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const plan = JSON.parse(item.dataset.plan);
            showHistoryModal(plan);
        });
    });
}

// Show history modal
function showHistoryModal(plan) {
    const modal = new bootstrap.Modal(document.getElementById('historyModal'));
    const title = document.getElementById('historyModalTitle');
    const body = document.getElementById('historyModalBody');
    
    const date = new Date(plan.weekStartDate).toLocaleDateString();
    title.textContent = `Meal Plan - ${date}`;
    
    body.innerHTML = plan.meals.map((meal, index) => `
        <div class="mb-3">
            <h6 class="text-primary">Day ${index + 1}</h6>
            <strong>${meal.mealName}</strong>
            <p class="small text-muted mb-0">${meal.ingredients.join(', ')}</p>
        </div>
    `).join('');
    
    modal.show();
}

// Theme toggle
function toggleTheme() {
    currentTheme = currentTheme === 'light' ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', currentTheme);
    localStorage.setItem('theme', currentTheme);
    
    const icon = document.querySelector('#themeToggle i');
    icon.className = currentTheme === 'dark' ? 'bi bi-sun-fill' : 'bi bi-moon-fill';
}

// Language toggle
function toggleLanguage() {
    currentLang = currentLang === 'en' ? 'da' : 'en';
    document.documentElement.setAttribute('lang', currentLang);
    localStorage.setItem('lang', currentLang);
    updateTranslations();
    
    const langBtn = document.getElementById('langToggle');
    if (langBtn) {
        langBtn.textContent = currentLang === 'en' ? '🇩🇰' : '🇬🇧';
    }
}

// Update translations
function updateTranslations() {
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (translations[currentLang][key]) {
            el.textContent = translations[currentLang][key];
        }
    });
}

// Initialize
if (window.location.pathname.includes('dashboard.html')) {
    document.addEventListener('DOMContentLoaded', async () => {
        await ensureCsrfToken();
        
        // Set theme icon
        const themeIcon = document.querySelector('#themeToggle i');
        if (themeIcon) {
            themeIcon.className = currentTheme === 'dark' ? 'bi bi-sun-fill' : 'bi bi-moon-fill';
        }
        
        // Set language flag
        const langBtn = document.getElementById('langToggle');
        if (langBtn) {
            langBtn.textContent = currentLang === 'en' ? '🇩🇰' : '🇬🇧';
        }
        
        // Update translations
        updateTranslations();
        
        // Load data
        loadProfile();
        loadCurrentMealPlan();
        loadHistory();
        
        // Event listeners
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
