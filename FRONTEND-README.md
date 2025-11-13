# Frontend Dokumentation - Weekly Meal Planner

## Indholdsfortegnelse
1. [Oversigt](#oversigt)
2. [Filstruktur](#filstruktur)
3. [HTML Sider](#html-sider)
4. [JavaScript Moduler](#javascript-moduler)
5. [Styling](#styling)
6. [API Integration](#api-integration)
7. [Sikkerhed](#sikkerhed)
8. [Fejlfinding](#fejlfinding)

---

## Oversigt

Weekly Meal Planner frontend er bygget med moderne web-teknologier uden brug af frameworks. Applikationen bruger:

- **HTML5** - Semantisk markup
- **CSS3** - Custom properties (variabler) og responsive design
- **Vanilla JavaScript** - Modulær ES6+ kode
- **Bootstrap 5.3.2** - UI komponenter og grid system
- **Bootstrap Icons** - Ikoner
- **OAuth2** - Google login integration

### Design Filosofi
Applikationen følger en **dark-first** design tilgang med hvid tekst på mørke baggrunde for bedre læsbarhed og reduceret øjenbelastning.

---

## Filstruktur

```
src/main/resources/static/
├── index.html              # Landingsside (login)
├── dashboard.html          # Hovedapplikation
├── preferences.html        # Indstillinger (ikke i brug)
├── styles.css              # Global styling
└── js/
    ├── csrf.js            # CSRF token håndtering
    ├── profile.js         # Brugerprofillogik
    ├── mealplan.js        # Måltidsplan funktionalitet
    ├── history.js         # Historik visning
    └── main.js            # Applikationsinitialisering
```

---

## HTML Sider

### 1. index.html - Landingsside

**Formål:** Velkomstside med login funktionalitet

**Nøglefunktioner:**
- Google OAuth2 login knap
- Responsivt hero-sektion
- Automatisk redirect til dashboard efter login

**Vigtige elementer:**
```html
<a href="/oauth2/authorization/google" class="btn btn-primary btn-lg">
    <i class="bi bi-google"></i> Log ind med Google
</a>
```

**Flow:**
1. Bruger klikker "Log ind med Google"
2. Omdirigeres til Google's login side
3. Efter godkendelse sendes bruger til `/dashboard.html`

---

### 2. dashboard.html - Hovedapplikation

**Formål:** Hovedinterface hvor brugere kan generere og se måltidsplaner

**Layout:**
```
┌─────────────────────────────────────┐
│         Navigation Bar               │
├───────────┬─────────────┬───────────┤
│  Profil   │  Måltidsplan │ Historik │
│           │              │           │
│  Præf.    │  Generate    │ Tidligere │
│           │  Button      │  planer   │
└───────────┴─────────────┴───────────┘
```

**Sektioner:**

#### Venstre sidebar (col-lg-3)
- **Profilkort:** Viser brugerens navn og email
- **Præferencer:** Diættype, allergier, dislikes

#### Midtersektion (col-lg-6)
- **Måltidsplan:** Viser genereret plan med:
  - Ugedage (Mandag-Fredag)
  - Måltidsnavn
  - Ingredienser (max 5 vist + antal ekstra)
  - Email send-knap
- **Empty state:** Vises når ingen plan er genereret
- **Generate knap:** Opretter ny månedsplan (20 måltider)

#### Højre sidebar (col-lg-3)
- **Historik:** Liste over tidligere planer
- **Klikbare:** Åbner modal med fuld plandetaljer

**Skeleton Loaders:**
Alle sektioner har skeleton loaders der vises under indlæsning:
```html
<div id="profileSkeleton" class="skeleton-loader">
    <div class="skeleton skeleton-circle mx-auto mb-3"></div>
    <div class="skeleton skeleton-text mb-2"></div>
</div>
```

---

## JavaScript Moduler

### 1. csrf.js - CSRF Beskyttelse

**Formål:** Håndterer CSRF tokens for sikre POST requests

**Funktioner:**

#### `getCsrfToken()`
```javascript
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
```
- Læser `XSRF-TOKEN` cookie
- Returnerer dekoderet token
- Bruges i alle POST/PUT/DELETE requests

#### `ensureCsrfToken()`
```javascript
async function ensureCsrfToken() {
    if (!getCsrfToken()) {
        await fetch('/api/csrf', { credentials: 'same-origin' });
    }
}
```
- Sikrer token eksisterer
- Henter nyt token hvis nødvendigt
- Kaldes ved app-start

**Brug:**
```javascript
const response = await fetch('/api/endpoint', {
    method: 'POST',
    headers: {
        'X-XSRF-TOKEN': getCsrfToken(),
        'Content-Type': 'application/json'
    }
});
```

---

### 2. profile.js - Brugerprofil

**Formål:** Håndterer brugerprofildata og præferencer

**Funktioner:**

#### `loadProfile()`
```javascript
async function loadProfile() {
    const response = await fetch('/api/profile');
    const profile = await response.json();
    
    document.getElementById('userName').textContent = profile.name;
    document.getElementById('userEmail').textContent = profile.email;
    
    loadPreferences(profile);
}
```

**Data model:**
```javascript
{
    id: 1,
    name: "Bruger Navn",
    email: "bruger@example.com",
    dietType: "vegetarian",  // omnivore, vegetarian, vegan
    allergies: ["peanuts", "dairy"],
    dislikes: ["broccoli"],
    language: "en"
}
```

#### `loadPreferences(profile)`
- Viser diættype (oversat)
- Viser allergier som kommasepareret liste
- Viser dislikes som kommasepareret liste
- Skjuler skeleton loader

---

### 3. mealplan.js - Måltidsplan Logik

**Formål:** Håndterer generering og visning af måltidsplaner

**Hovedfunktioner:**

#### `loadCurrentMealPlan()`
```javascript
async function loadCurrentMealPlan() {
    const response = await fetch('/api/mealplan/current');
    
    if (response.status === 204) {
        showEmptyState();
        return;
    }
    
    if (response.ok) {
        displayMealPlan(await response.json());
    }
}
```
- Henter nuværende måltidsplan
- Håndterer 204 (ingen plan) scenarie
- Viser empty state eller plan

#### `displayMealPlan(planData)`
**Vigtig funktion - 99 linjer optimeret kode**

```javascript
function displayMealPlan(planData) {
    const { mealPlan: plan = planData, message = null } = planData;
    currentMealPlanId = plan.id;
    
    if (!plan.meals?.length) return showEmptyState();
    
    const html = [
        message && `<div class="alert alert-success">ChatGPT says: ${message}</div>`,
        `<button onclick="sendMealPlanByEmail()">Send via Email</button>`,
        ...renderWeeks(plan.meals)
    ].filter(Boolean).join('');
    
    document.getElementById('mealPlanContent').innerHTML = html;
}
```

**Funktionalitet:**
- Destructuring af response (kan være MealPlanResponse eller WeeklyMealPlan)
- Viser ChatGPT besked hvis tilgængelig
- Genererer HTML for alle uger
- Gemmer plan ID til email funktionalitet

#### `renderWeeks(meals)`
```javascript
function renderWeeks(meals) {
    const weeksCount = Math.ceil(meals.length / 5);
    return Array.from({ length: weeksCount }, (_, week) => {
        const weekHeader = weeksCount > 1 ? `<h5>Week ${week + 1}</h5>` : '';
        const weekMeals = meals
            .slice(week * 5, (week + 1) * 5)
            .map((meal, i) => renderMealCard(meal, i))
            .join('');
        return weekHeader + weekMeals;
    });
}
```
- Beregner antal uger (5 måltider pr. uge)
- Genererer uge-headers kun hvis multiple uger
- Slicer måltider i grupper af 5
- Mapper til meal cards

#### `renderMealCard(meal, dayIndex)`
```javascript
function renderMealCard(meal, dayIndex) {
    const ingredients = meal.ingredients
        .slice(0, 5)
        .map(ing => `<span class="ingredient-badge">${ing}</span>`)
        .join('');
    const more = meal.ingredients.length > 5 
        ? `<span>+${meal.ingredients.length - 5} more</span>` 
        : '';
    
    return `
        <div class="card meal-card">
            <div class="meal-day">${DAYS[dayIndex]}</div>
            <h5 class="meal-title">${meal.mealName}</h5>
            <div class="meal-ingredients">${ingredients}${more}</div>
        </div>
    `;
}
```
- Viser max 5 ingredienser
- Tæller og viser resterende ingredienser
- Bruger globalt DAYS array for ugedag-navne

#### `generateMealPlan()`
```javascript
async function generateMealPlan() {
    const btn = document.getElementById('generateBtn');
    const spinner = document.getElementById('loadingSpinner');
    
    btn.disabled = true;
    spinner.classList.remove('d-none');
    
    try {
        const response = await fetch('/api/mealplan/generate?type=monthly', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json', 
                'X-XSRF-TOKEN': getCsrfToken() 
            }
        });
        
        if (response.ok) {
            displayMealPlan(await response.json());
            loadHistory(); // Opdater historik
        }
    } finally {
        btn.disabled = false;
        spinner.classList.add('d-none');
    }
}
```

**Flow:**
1. Deaktiver knap og vis spinner
2. Send POST request til `/api/mealplan/generate?type=monthly`
3. Hvis success: vis plan og opdater historik
4. Genaktiver knap uanset resultat (finally)

#### `sendMealPlanByEmail()`
```javascript
async function sendMealPlanByEmail() {
    if (!currentMealPlanId) return alert('No meal plan to send');
    
    const response = await fetch(`/api/mealplan/${currentMealPlanId}/email`, {
        method: 'POST',
        headers: { 
            'X-XSRF-TOKEN': getCsrfToken() 
        }
    });
    
    alert(response.ok ? 'Email sent!' : 'Failed to send email');
}
```

---

### 4. history.js - Historik

**Formål:** Viser og håndterer tidligere måltidsplaner

#### `loadHistory()`
```javascript
async function loadHistory() {
    const response = await fetch('/api/mealplan/history');
    const history = await response.json();
    displayHistory(history);
}
```

#### `displayHistory(history)`
```javascript
function displayHistory(history) {
    if (!history || history.length === 0) {
        document.getElementById('historyEmpty').style.display = 'block';
        return;
    }
    
    const html = history.slice(0, 10).map(plan => `
        <a href="#" class="list-group-item history-item" 
           data-plan='${JSON.stringify(plan)}'>
            <div class="history-date">${new Date(plan.weekStartDate).toLocaleDateString()}</div>
            <div class="history-meals">${plan.meals.length} meals</div>
        </a>
    `).join('');
    
    document.getElementById('historyContent').innerHTML = html;
    
    // Tilføj click handlers
    document.querySelectorAll('.history-item').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            showHistoryModal(JSON.parse(item.dataset.plan));
        });
    });
}
```

**Features:**
- Viser max 10 seneste planer
- Gemmer plan data i `data-plan` attribut
- Formaterer dato med toLocaleDateString()
- Dynamiske click handlers

#### `showHistoryModal(plan)`
```javascript
function showHistoryModal(plan) {
    const modal = new bootstrap.Modal(document.getElementById('historyModal'));
    
    document.getElementById('historyModalTitle').textContent = 
        `Meal Plan - ${new Date(plan.weekStartDate).toLocaleDateString()}`;
    
    document.getElementById('historyModalBody').innerHTML = 
        plan.meals.map((meal, i) => `
            <div class="mb-3">
                <h6>Day ${i + 1}</h6>
                <strong>${meal.mealName}</strong>
                <p class="small text-muted">${meal.ingredients.join(', ')}</p>
            </div>
        `).join('');
    
    modal.show();
}
```

---

### 5. main.js - Initialisering

**Formål:** Bootstrap applikationen ved page load

```javascript
if (window.location.pathname.includes('dashboard.html')) {
    document.addEventListener('DOMContentLoaded', async () => {
        await ensureCsrfToken();
        
        loadProfile();
        loadCurrentMealPlan();
        loadHistory();
        
        const generateBtn = document.getElementById('generateBtn');
        if (generateBtn) {
            generateBtn.addEventListener('click', generateMealPlan);
        }
    });
}
```

**Initialiseringssekvens:**
1. Vent på DOM ready
2. Sørg for CSRF token eksisterer
3. Load profil parallelt
4. Load nuværende måltidsplan
5. Load historik
6. Bind event handlers

---

## Styling

### CSS Arkitektur

**File:** `styles.css`

#### CSS Custom Properties (Variabler)
```css
:root {
    --bg-primary: #1a1a1a;      /* Primær baggrund (mørk) */
    --bg-secondary: #2d2d2d;    /* Sekundær baggrund */
    --text-primary: #ffffff;    /* Primær tekst (hvid) */
    --text-secondary: #adb5bd;  /* Sekundær tekst (grå) */
    --border-color: #495057;    /* Border farve */
    --card-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.3);
}
```

**Light theme support** (hvis nødvendig):
```css
[data-theme="light"] {
    --bg-primary: #ffffff;
    --bg-secondary: #f8f9fa;
    --text-primary: #212529;
    --text-secondary: #6c757d;
    /* ... */
}
```

#### Komponenter

**Meal Cards:**
```css
.meal-card {
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    cursor: pointer;
    border-left: 4px solid transparent;
}

.meal-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15) !important;
    border-left-color: #0d6efd;
}
```
- Hover effekt med translation
- Border accent ved hover
- Smooth transitions

**Ingredient Badges:**
```css
.ingredient-badge {
    background-color: var(--bg-secondary);
    border-radius: 0.25rem;
    padding: 0.25rem 0.5rem;
    font-size: 0.75rem;
    color: var(--text-secondary);
}
```

**Skeleton Loaders:**
```css
.skeleton {
    background: linear-gradient(
        90deg, 
        #2d2d2d 25%, 
        #3d3d3d 50%, 
        #2d2d2d 75%
    );
    background-size: 200% 100%;
    animation: loading 1.5s ease-in-out infinite;
}

@keyframes loading {
    0% { background-position: 200% 0; }
    100% { background-position: -200% 0; }
}
```
- Shimmering effekt
- Indikerer indlæsning
- Bedre UX end blank skærm

#### Responsive Design

```css
@media (max-width: 991px) {
    .col-lg-3,
    .col-lg-6 {
        margin-bottom: 1rem;
    }
}
```

**Bootstrap Breakpoints:**
- `xs`: < 576px (mobil)
- `sm`: ≥ 576px (mobil landscape)
- `md`: ≥ 768px (tablet)
- `lg`: ≥ 992px (desktop)
- `xl`: ≥ 1200px (large desktop)

---

## API Integration

### Endpoints

#### Authentication
```
GET  /oauth2/authorization/google  - Start Google OAuth flow
GET  /                              - Landing page
GET  /dashboard.html                - Dashboard (kræver auth)
```

#### Profile
```
GET  /api/profile                   - Hent brugerprofile
PUT  /api/profile/preferences       - Opdater præferencer
```

**Request (PUT):**
```json
{
    "dietType": "vegetarian",
    "allergies": ["peanuts", "dairy"],
    "dislikes": ["broccoli"],
    "language": "en"
}
```

#### Meal Plans
```
GET  /api/mealplan/current          - Hent nuværende plan
POST /api/mealplan/generate?type=monthly  - Generer ny plan
GET  /api/mealplan/history          - Hent historik
POST /api/mealplan/{id}/email       - Send plan via email
```

**Response (MealPlanResponse):**
```json
{
    "mealPlan": {
        "id": 1,
        "weekStartDate": "2025-11-13",
        "meals": [
            {
                "id": 1,
                "mealName": "Spaghetti Carbonara",
                "ingredients": ["pasta", "eggs", "bacon", "parmesan", "pepper"]
            }
        ]
    },
    "message": "Here's your personalized meal plan!"
}
```

#### CSRF
```
GET  /api/csrf                      - Hent CSRF token
```

### Fetch Patterns

**GET Request:**
```javascript
const response = await fetch('/api/profile', {
    credentials: 'same-origin'  // Inkluder cookies
});

if (response.ok) {
    const data = await response.json();
}
```

**POST Request:**
```javascript
const response = await fetch('/api/mealplan/generate?type=monthly', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'X-XSRF-TOKEN': getCsrfToken()
    },
    credentials: 'same-origin',
    body: JSON.stringify(data)  // hvis der er body
});
```

**Error Handling:**
```javascript
try {
    const response = await fetch('/api/endpoint');
    
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Request failed');
    }
    
    return await response.json();
} catch (error) {
    console.error('Error:', error);
    alert('En fejl opstod: ' + error.message);
}
```

---

## Sikkerhed

### CSRF Protection

**Token Flow:**
1. Ved app start: `ensureCsrfToken()` kalder `/api/csrf`
2. Server sætter `XSRF-TOKEN` cookie
3. JavaScript læser cookie med `getCsrfToken()`
4. Token sendes i `X-XSRF-TOKEN` header ved POST/PUT/DELETE
5. Server validerer token matcher cookie

**Vigtig:** CSRF token skal inkluderes i ALLE muterende requests!

### OAuth2 Authentication

**Google Login Flow:**
```
1. Bruger klikker "Log ind med Google"
   ↓
2. Redirect til Google's login
   ↓
3. Bruger godkender app
   ↓
4. Google redirecter tilbage med authorization code
   ↓
5. Backend udveksler code til access token
   ↓
6. Backend opretter session
   ↓
7. Bruger redirectes til dashboard
```

**Session Management:**
- Spring Security håndterer sessions
- Cookie-baseret (`JSESSIONID`)
- Automatisk logout ved tab af session

### Content Security

**HTTP Headers:**
```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 0
```

**Same-Origin Policy:**
Alle API requests bruger `credentials: 'same-origin'` for at sikre cookies sendes.

---

## Fejlfinding

### Common Issues

#### 1. "CSRF token missing"
**Symptom:** 403 Forbidden ved POST requests

**Solution:**
```javascript
// Sørg for at await ensureCsrfToken() før requests
await ensureCsrfToken();
const token = getCsrfToken();

fetch('/api/endpoint', {
    method: 'POST',
    headers: { 'X-XSRF-TOKEN': token }
});
```

#### 2. "Not authenticated"
**Symptom:** Redirect til login page

**Mulige årsager:**
- Session expired (timeout)
- Cookies disabled
- Ikke logget ind

**Solution:**
- Log ind igen via Google
- Check browser cookie settings

#### 3. "Meal plan not loading"
**Debug steps:**
```javascript
// 1. Check network tab i DevTools
// 2. Log response
const response = await fetch('/api/mealplan/current');
console.log('Status:', response.status);
console.log('Data:', await response.json());

// 3. Check for errors
if (response.status === 204) {
    console.log('No meal plan exists');
} else if (!response.ok) {
    console.error('API error:', response.status);
}
```

#### 4. "Email not sending"
**Check:**
- Er `currentMealPlanId` sat?
- Er CSRF token valid?
- Check server logs for SMTP fejl

```javascript
console.log('Current plan ID:', currentMealPlanId);
```

### Browser Console

**Nyttige kommandoer:**
```javascript
// Check CSRF token
getCsrfToken()

// Check cookies
document.cookie

// Genindlæs profil
loadProfile()

// Force reload plan
loadCurrentMealPlan()

// Check localStorage
localStorage
```

### Network Debugging

**Chrome DevTools → Network tab:**
1. Filter på `XHR` for AJAX requests
2. Check Request Headers for CSRF token
3. Check Response for error messages
4. Look for HTTP status codes:
   - 200: Success
   - 204: No Content (tom response)
   - 401: Unauthorized (ikke logget ind)
   - 403: Forbidden (CSRF fejl)
   - 500: Server error

---

## Performance Optimering

### Best Practices

**1. Reducer DOM manipulationer:**
```javascript
// ❌ Dårligt - Multiple DOM updates
meals.forEach(meal => {
    container.innerHTML += renderMeal(meal);
});

// ✅ Godt - Single DOM update
const html = meals.map(renderMeal).join('');
container.innerHTML = html;
```

**2. Event Delegation:**
```javascript
// ❌ Dårligt - Multiple listeners
items.forEach(item => {
    item.addEventListener('click', handleClick);
});

// ✅ Godt - Single delegated listener
container.addEventListener('click', (e) => {
    if (e.target.matches('.history-item')) {
        handleClick(e);
    }
});
```

**3. Debouncing:**
For fremtidige search features:
```javascript
function debounce(func, wait) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

const searchMeals = debounce(async (query) => {
    // API call
}, 300);
```

---

## Fremtidige Forbedringer

### Potentielle Features

1. **Internationalisering (i18n)**
   - Komplet dansk oversættelse
   - Sprog-switcher
   - Formatering af datoer/tal per locale

2. **Offline Support**
   - Service Worker
   - Cache meal plans
   - Sync when online

3. **Progressive Web App (PWA)**
   - Installérbar app
   - Push notifications for nye planer
   - Add to homescreen

4. **Advanced Filtering**
   - Søg i måltider
   - Filter efter ingrediens
   - Favorit-markering

5. **Meal Customization**
   - Erstat enkelte måltider
   - Gem custom opskrifter
   - Rating system

---

## Konklusion

Weekly Meal Planner frontend er bygget med moderne web-standarder og følger best practices for:
- **Performance** - Minimal bundle, optimeret DOM manipulering
- **Sikkerhed** - CSRF protection, OAuth2, secure headers
- **UX** - Loading states, responsive design, intuitive navigation
- **Maintainability** - Modulær kode, klare ansvarsområder

For spørgsmål eller bidrag, kontakt udviklingsteamet.

---

**Version:** 1.0  
**Sidst opdateret:** November 2025  
**Forfatter:** WeeklyMealPlanner Development Team
