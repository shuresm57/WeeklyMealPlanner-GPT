let currentMealPlanId = null;

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
    
    if (message) {
        html += `
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="bi bi-robot"></i> <strong>ChatGPT says:</strong> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
    }
    
    html += `
        <div class="mb-3 d-flex justify-content-end">
            <button class="btn btn-outline-primary btn-sm" onclick="sendMealPlanByEmail()">
                <i class="bi bi-envelope"></i> <span data-i18n="sendEmail">${translate('sendEmail')}</span>
            </button>
        </div>
    `;
    
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
                        <div class="meal-day">${days[dayIndex]}</div>
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
            alert(translate('emailSent'));
        } else {
            const error = await response.json();
            alert(error.error || translate('emailFailed'));
        }
    } catch (error) {
        console.error('Error sending email:', error);
        alert(translate('emailFailed'));
    }
}
