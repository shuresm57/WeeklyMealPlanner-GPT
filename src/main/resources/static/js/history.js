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
