let currentMealPlanId = null;
const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];

async function loadCurrentMealPlan() {
    try {
        const cached = CacheService.get('current_mealplan');
        if (cached) {
            displayMealPlan(cached);
            document.getElementById('mealPlanSkeleton').style.display = 'none';
            return;
        }

        const response = await fetch('/api/mealplan/current');
        if (response.status === 204 || !response.ok) {
            showEmptyState();
        } else {
            const data = await response.json();
            CacheService.set('current_mealplan', data);
            displayMealPlan(data);
        }
    } catch (error) {
        console.error('Error loading meal plan:', error);
        showEmptyState();
    } finally {
        document.getElementById('mealPlanSkeleton').style.display = 'none';
    }
}

function displayMealPlan(planData) {
    const { mealPlan: plan = planData, message = null } = planData;
    currentMealPlanId = plan.id;
    
    if (!plan.meals?.length) return showEmptyState();
    
    const html = [
        message && `<div class="alert alert-success alert-dismissible fade show"><i class="bi bi-robot"></i> <strong>ChatGPT says:</strong> ${message}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>`,
        `<div class="mb-3 d-flex justify-content-end"><button class="btn btn-outline-primary btn-sm" onclick="sendMealPlanByEmail()"><i class="bi bi-envelope"></i> Send via Email</button></div>`,
        ...renderWeeks(plan.meals)
    ].filter(Boolean).join('');
    
    document.getElementById('mealPlanContent').innerHTML = html;
    document.getElementById('mealPlanSkeleton').style.display = 'none';
    document.getElementById('emptyState').style.display = 'none';
}

function renderWeeks(meals) {
    const weeksCount = Math.ceil(meals.length / 5);
    return Array.from({ length: weeksCount }, (_, week) => {
        const weekHeader = weeksCount > 1 ? `<h5 class="mt-4 mb-3">Week ${week + 1}</h5>` : '';
        const weekMeals = meals.slice(week * 5, (week + 1) * 5).map((meal, i) => renderMealCard(meal, i)).join('');
        return weekHeader + weekMeals;
    });
}

function renderMealCard(meal, dayIndex) {
    const ingredients = meal.ingredients.slice(0, 5).map(ing => `<span class="ingredient-badge">${ing}</span>`).join('');
    const more = meal.ingredients.length > 5 ? `<span class="ingredient-badge">+${meal.ingredients.length - 5} more</span>` : '';
    return `<div class="card shadow-sm mb-3 meal-card"><div class="card-body"><div class="meal-day">${DAYS[dayIndex]}</div><h5 class="meal-title">${meal.mealName}</h5><div class="meal-ingredients">${ingredients}${more}</div></div></div>`;
}

function showEmptyState() {
    document.getElementById('mealPlanSkeleton').style.display = 'none';
    document.getElementById('mealPlanContent').innerHTML = '';
    document.getElementById('emptyState').style.display = 'block';
}

async function generateMealPlan() {
    const btn = document.getElementById('generateBtn');
    const spinner = document.getElementById('loadingSpinner');
    
    btn.disabled = true;
    spinner.classList.remove('d-none');
    document.getElementById('mealPlanContent').innerHTML = '';
    document.getElementById('emptyState').style.display = 'none';

    try {
        const response = await fetch('/api/mealplan/generate?type=monthly', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': getCsrfToken() },
            credentials: 'same-origin'
        });

        if (response.ok) {
            const data = await response.json();
            CacheService.invalidate('current_mealplan');
            CacheService.invalidate('mealplan_history');
            CacheService.set('current_mealplan', data);
            displayMealPlan(data);
            loadHistory();
        } else {
            alert((await response.json()).message || 'Failed to generate meal plan');
        }
    } catch (error) {
        console.error('Error generating meal plan:', error);
        alert('An error occurred. Please try again.');
    } finally {
        btn.disabled = false;
        spinner.classList.add('d-none');
    }
}

async function sendMealPlanByEmail() {
    if (!currentMealPlanId) return alert('No meal plan to send');
    
    try {
        const response = await fetch(`/api/mealplan/${currentMealPlanId}/email`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': getCsrfToken() },
            credentials: 'same-origin'
        });
        alert(response.ok ? 'Email sent!' : (await response.json()).error || 'Failed to send email');
    } catch (error) {
        console.error('Error sending email:', error);
        alert('Failed to send email');
    }
}
