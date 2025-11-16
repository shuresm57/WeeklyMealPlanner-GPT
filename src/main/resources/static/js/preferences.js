let allergies = [];
let dislikes = [];

function renderTags(container, items, removeCallback) {
    container.innerHTML = '';
    items.forEach((item, index) => {
        const tag = document.createElement('div');
        tag.className = 'tag-item';
        tag.innerHTML = `
            <span>${item}</span>
            <button type="button" onclick="${removeCallback}(${index})">
                <i class="bi bi-x"></i>
            </button>
        `;
        container.appendChild(tag);
    });
}

function addAllergy() {
    const input = document.getElementById('allergiesInput');
    const value = input.value.trim();
    if (value && !allergies.includes(value)) {
        allergies.push(value);
        renderTags(document.getElementById('allergiesTags'), allergies, 'removeAllergy');
        input.value = '';
    }
}

function removeAllergy(index) {
    allergies.splice(index, 1);
    renderTags(document.getElementById('allergiesTags'), allergies, 'removeAllergy');
}

function addDislike() {
    const input = document.getElementById('dislikesInput');
    const value = input.value.trim();
    if (value && !dislikes.includes(value)) {
        dislikes.push(value);
        renderTags(document.getElementById('dislikesTags'), dislikes, 'removeDislike');
        input.value = '';
    }
}

function removeDislike(index) {
    dislikes.splice(index, 1);
    renderTags(document.getElementById('dislikesTags'), dislikes, 'removeDislike');
}

async function loadCurrentPreferences() {
    try {
        const response = await fetch('/api/profile');
        if (response.ok) {
            const profile = await response.json();
            document.getElementById('dietType').value = profile.dietType || 'omnivore';
            
            allergies = profile.allergies || [];
            dislikes = profile.dislikes || [];
            
            renderTags(document.getElementById('allergiesTags'), allergies, 'removeAllergy');
            renderTags(document.getElementById('dislikesTags'), dislikes, 'removeDislike');
        }
    } catch (error) {
        console.error('Error loading preferences:', error);
    }
}

async function savePreferences(event) {
    event.preventDefault();

    const preferences = {
        dietType: document.getElementById('dietType').value,
        allergies: allergies,
        dislikes: dislikes
    };

    await ensureCsrfToken();
    const csrfToken = getCsrfToken();

    try {
        const response = await fetch('/api/profile/preferences', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken
            },
            credentials: 'same-origin',
            body: JSON.stringify(preferences)
        });

        if (response.ok) {
            alert('Preferences saved successfully!');
            window.location.href = 'dashboard.html';
        } else {
            alert('Failed to save preferences');
        }
    } catch (error) {
        console.error('Error saving preferences:', error);
        alert('An error occurred');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    loadCurrentPreferences();
    document.getElementById('preferencesForm').addEventListener('submit', savePreferences);
    
    document.getElementById('addAllergyBtn').addEventListener('click', addAllergy);
    document.getElementById('allergiesInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            addAllergy();
        }
    });
    
    document.getElementById('addDislikeBtn').addEventListener('click', addDislike);
    document.getElementById('dislikesInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            addDislike();
        }
    });
});
