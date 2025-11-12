// Load profile
async function loadProfile() {
    try {
        const response = await fetch('/api/profile');
        if (response.ok) {
            const profile = await response.json();
            document.getElementById('userName').textContent = profile.name || 'User';
            document.getElementById('userEmail').textContent = profile.email || '';
            
            document.getElementById('profileSkeleton').style.display = 'none';
            document.getElementById('profileContent').style.display = 'block';
            
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
    
    document.getElementById('dietType').textContent = translate(dietType);
    document.getElementById('allergiesList').textContent = allergies.length > 0 ? allergies.join(', ') : translate('none');
    document.getElementById('dislikesList').textContent = dislikes.length > 0 ? dislikes.join(', ') : translate('none');
    
    document.getElementById('preferencesSkeleton').style.display = 'none';
    document.getElementById('preferencesContent').style.display = 'block';
}
