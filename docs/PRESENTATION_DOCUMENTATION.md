# Weekly Meal Planner - Komplet Program Dokumentation

## 📋 Indholdsfortegnelse

1. [Projekt Oversigt](#projekt-oversigt)
2. [Arkitektur](#arkitektur)
3. [Backend (Java/Spring Boot)](#backend-javaspring-boot)
4. [Frontend (HTML/CSS/JavaScript)](#frontend-htmlcssjavascript)
5. [Database](#database)
6. [Sikkerhed](#sikkerhed)
7. [Deployment](#deployment)

---

## Projekt Oversigt

### Formål
Weekly Meal Planner er en web-applikation der bruger ChatGPT (OpenAI) til at generere personlige måltidsplaner baseret på brugerens præferencer.

### Nøgle Features
- ✅ OAuth2 login med Google
- ✅ Personlige præferencer (diæt, allergier, dislikes)
- ✅ AI-genereret månedlig måltidsplan (20 måltider)
- ✅ ChatGPT bekræftelsesbesked
- ✅ Email funktionalitet
- ✅ Historik over tidligere planer
- ✅ Dark/Light mode
- ✅ Dansk/Engelsk support

### Teknologi Stack
**Backend:**
- Java 21
- Spring Boot 3.4.1
- Spring Security + OAuth2
- JPA/Hibernate
- H2 Database (in-memory)
- Thymeleaf (email templates)

**Frontend:**
- HTML5
- CSS3 + Bootstrap 5.3.2
- Vanilla JavaScript (ES6+)
- Bootstrap Icons

**External APIs:**
- OpenAI GPT-4
- Google OAuth2
- JavaMail (SMTP)

---

## Arkitektur

### System Diagram
```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │ HTTPS
       ↓
┌─────────────────────────────────────┐
│      Spring Boot Application        │
│  ┌───────────────────────────────┐  │
│  │   Security Layer (OAuth2)     │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │   Controllers (REST API)      │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │   Services (Business Logic)   │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │   Repositories (Data Access)  │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │   H2 Database (In-Memory)     │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
       │                    │
       │ HTTP               │ SMTP
       ↓                    ↓
┌─────────────┐      ┌─────────────┐
│  OpenAI API │      │ Email Server│
└─────────────┘      └─────────────┘
```

### Projektstruktur
```
src/
├── main/
│   ├── java/com/example/weeklymealplannergpt/
│   │   ├── config/                 # Konfiguration
│   │   │   ├── OAuth2LoginSuccessHandler.java
│   │   │   └── SecurityConfig.java
│   │   ├── controller/             # REST endpoints
│   │   │   ├── CsrfController.java
│   │   │   ├── MealPlanController.java
│   │   │   └── ProfileController.java
│   │   ├── dto/                    # Data Transfer Objects
│   │   │   ├── MealPlanResponse.java
│   │   │   ├── OpenAIRequest.java
│   │   │   ├── OpenAIResponse.java
│   │   │   ├── PreferencesRequest.java
│   │   │   └── TheMealDbResponse.java
│   │   ├── exception/              # Custom exceptions
│   │   │   └── MealGenerationException.java
│   │   ├── model/                  # Entities
│   │   │   ├── Consumer.java
│   │   │   ├── Meal.java
│   │   │   └── WeeklyMealPlan.java
│   │   ├── repository/             # Database access
│   │   │   ├── ConsumerRepository.java
│   │   │   ├── MealRepository.java
│   │   │   └── WeeklyMealPlanRepository.java
│   │   ├── service/                # Business logic
│   │   │   ├── consumer/
│   │   │   │   ├── ConsumerService.java
│   │   │   │   └── ConsumerServiceImpl.java
│   │   │   ├── email/
│   │   │   │   ├── EmailService.java
│   │   │   │   └── EmailServiceImpl.java
│   │   │   ├── mealplan/
│   │   │   │   ├── MealCacheService.java
│   │   │   │   ├── MealPlanService.java
│   │   │   │   ├── MealPlanServiceImpl.java
│   │   │   │   └── TheMealDBService.java
│   │   │   └── openai/
│   │   │       ├── OpenAIService.java
│   │   │       └── OpenAIServiceImpl.java
│   │   └── WeeklyMealPlannerGptApplication.java
│   └── resources/
│       ├── static/                 # Frontend files
│       │   ├── js/
│       │   │   ├── csrf.js
│       │   │   ├── history.js
│       │   │   ├── i18n.js
│       │   │   ├── main.js
│       │   │   ├── mealplan.js
│       │   │   ├── profile.js
│       │   │   └── theme.js
│       │   ├── app.js              # Legacy (bruges ikke længere)
│       │   ├── dashboard.html
│       │   ├── index.html
│       │   ├── preferences.html
│       │   └── styles.css
│       ├── templates/              # Email templates
│       │   └── weekly-meal-plan.html
│       └── application.properties  # Configuration
└── test/                           # Unit tests
```

---

## Backend (Java/Spring Boot)

### 1. Configuration

#### SecurityConfig.java
**Placering:** `config/SecurityConfig.java`

**Formål:** Konfigurerer Spring Security med OAuth2

**Nøgle Features:**
- OAuth2 login med Google
- CSRF protection
- Session management
- Public/protected endpoints

**Kode Gennemgang:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            // Tillad statiske filer uden authentication
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/styles.css", 
                                "/app.js", "/api/csrf").permitAll()
                .anyRequest().authenticated()
            )
            // OAuth2 login
            .oauth2Login(oauth2 -> oauth2
                .successHandler(successHandler)
            )
            // CSRF protection (vigtigt for sikkerhed!)
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .build();
    }
}
```

**Hvorfor OAuth2?**
- Ingen password management
- Sikker authentication
- Bruger kan bruge eksisterende Google konto

---

#### OAuth2LoginSuccessHandler.java
**Formål:** Håndterer hvad der sker efter succesfuld login

**Workflow:**
1. Bruger logger ind med Google
2. Spring modtager OAuth2 token
3. Handler extracter bruger info (email, navn)
4. Tjekker om bruger findes i database
5. Hvis ny: Opret Consumer med default præferencer
6. Redirect til dashboard

```java
@Override
public void onAuthenticationSuccess(request, response, authentication) {
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getAttribute("email");
    
    Consumer consumer = consumerRepository.findByEmail(email)
        .orElseGet(() -> {
            // Ny bruger - opret med defaults
            Consumer newConsumer = new Consumer();
            newConsumer.setEmail(email);
            newConsumer.setName(oAuth2User.getAttribute("name"));
            newConsumer.setDietType("omnivore");
            newConsumer.setAllergies(Collections.emptyList());
            return consumerRepository.save(newConsumer);
        });
    
    response.sendRedirect("/dashboard.html");
}
```

---

### 2. Models (Entities)

#### Consumer.java
**Formål:** Repræsenterer en bruger

**Felter:**
```java
@Entity
public class Consumer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String email;           // Fra Google OAuth
    private String name;            // Fra Google OAuth
    private String dietType;        // omnivore, vegetarian, vegan
    
    @ElementCollection
    private List<String> allergies; // Brugerens allergier
    
    @ElementCollection
    private List<String> dislikes;  // Ting brugeren ikke kan lide
    
    @OneToMany(mappedBy = "consumer")
    private List<WeeklyMealPlan> mealPlans; // Historik
}
```

**Database Relations:**
- En Consumer kan have mange WeeklyMealPlans (One-to-Many)

---

#### WeeklyMealPlan.java
**Formål:** Repræsenterer en måltidsplan

**Felter:**
```java
@Entity
public class WeeklyMealPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDate weekStartDate;  // Mandag i ugen
    
    @ManyToOne
    @JoinColumn(name = "consumer_id")
    private Consumer consumer;        // Ejer af planen
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<Meal> meals;         // 5-20 måltider
}
```

**Cascade ALL:** Når en plan slettes, slettes måltiderne også

---

#### Meal.java
**Formål:** Repræsenterer et enkelt måltid

**Felter:**
```java
@Entity
public class Meal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String mealName;          // "Week 1 Monday - Pasta Carbonara"
    
    @ElementCollection
    private List<String> ingredients; // ["pasta", "eggs", "bacon"]
    
    private String imgUrl;            // Billede URL (ikke brugt endnu)
}
```

**Optimization:** Meals genbruges via MealCacheService for at spare database plads


---

### 3. Controllers (REST API)

#### MealPlanController.java
**Placering:** `controller/MealPlanController.java`

**Formål:** Håndterer alle meal plan relaterede requests

**Endpoints:**

##### POST /api/mealplan/generate
Genererer en ny måltidsplan

**Request:**
```http
POST /api/mealplan/generate?type=monthly
Headers:
  X-XSRF-TOKEN: <csrf-token>
```

**Response:**
```json
{
  "mealPlan": {
    "id": 1,
    "weekStartDate": "2024-11-11",
    "meals": [...]
  },
  "message": "Your 4-week meal plan with 20 meals has been created successfully!"
}
```

**Kode:**
```java
@PostMapping("/generate")
public MealPlanResponse generateMealPlan(
        @AuthenticationPrincipal OAuth2User principal,
        @RequestParam(defaultValue = "monthly") String type) {
    
    String email = principal.getAttribute("email");
    Consumer consumer = consumerService.findByEmail(email);
    
    if ("weekly".equalsIgnoreCase(type)) {
        return mealPlanService.generateWeeklyMealPlan(consumer);
    } else {
        return mealPlanService.generateMonthlyMealPlan(consumer);
    }
}
```

---

##### GET /api/mealplan/current
Henter nuværende uges måltidsplan

**Response:**
```json
{
  "id": 1,
  "weekStartDate": "2024-11-11",
  "meals": [
    {
      "mealName": "Week 1 Monday - Spaghetti Carbonara",
      "ingredients": ["pasta", "eggs", "bacon", "parmesan"]
    }
  ]
}
```

---

##### POST /api/mealplan/{id}/email
Sender måltidsplan via email

**Request:**
```http
POST /api/mealplan/1/email
Headers:
  X-XSRF-TOKEN: <csrf-token>
```

**Response:**
```json
{
  "message": "Email sent successfully"
}
```

**Error Response:**
```json
{
  "error": "Email functionality is disabled..."
}
```

---

#### ProfileController.java
**Formål:** Håndterer bruger profil og præferencer

**Endpoints:**

##### GET /api/profile
Henter bruger info

```java
@GetMapping
public Map<String, Object> getProfile(@AuthenticationPrincipal OAuth2User principal) {
    Consumer consumer = consumerService.findByEmail(email);
    
    return Map.of(
        "name", consumer.getName(),
        "email", consumer.getEmail(),
        "dietType", consumer.getDietType(),
        "allergies", consumer.getAllergies(),
        "dislikes", consumer.getDislikes()
    );
}
```

---

##### POST /api/profile/preferences
Opdaterer præferencer

**Request:**
```json
{
  "dietType": "vegetarian",
  "allergies": ["peanuts", "shellfish"],
  "dislikes": ["mushrooms"]
}
```

---

### 4. Services (Business Logic)

#### OpenAIServiceImpl.java
**Formål:** Kommunikerer med OpenAI API

**Nøgle Metode:**
```java
public List<Meal> generateMealPlan(Consumer consumer, int weeks) {
    int totalMeals = weeks * 5;
    
    // Byg prompt til ChatGPT
    String prompt = String.format("""
        Create a %d-week dinner plan (%d dinners, Monday-Friday).
        
        User preferences:
        - Allergies: %s
        - Diet: %s
        - Dislikes: %s
        
        JSON format: {
            "meals": [...],
            "message": "Your plan is ready!"
        }
        """, 
        weeks, totalMeals,
        consumer.getAllergies(),
        consumer.getDietType(),
        consumer.getDislikes()
    );
    
    return getMealPlanFromPrompt(prompt);
}
```

**Workflow:**
1. Byg prompt med bruger præferencer
2. Send til OpenAI GPT-4
3. Parse JSON response
4. Extract meals og message
5. Return List<Meal>

**Error Handling:**
- Retry logic hvis API fejler
- Fallback hvis JSON er invalid
- Logger alle errors

---

#### MealPlanServiceImpl.java
**Formål:** Orkestrer måltidsplan generering

**Nøgle Workflow:**

```java
@Transactional
public MealPlanResponse generateMealPlan(Consumer consumer, int weeks) {
    // 1. Generer måltider via OpenAI
    List<Meal> generatedMeals = openAIService.generateMealPlan(consumer, weeks);
    String message = openAIService.getLastGeneratedMessage();
    
    // 2. Tjek cache for at genbruge eksisterende måltider
    List<Meal> finalMeals = new ArrayList<>();
    for (Meal generatedMeal : generatedMeals) {
        Meal existingMeal = mealCacheService.getMealByName(mealName);
        
        if (existingMeal != null) {
            finalMeals.add(existingMeal);  // Genbrug
        } else {
            Meal savedMeal = mealRepository.save(generatedMeal);
            mealCacheService.addToCache(savedMeal);
            finalMeals.add(savedMeal);
        }
    }
    
    // 3. Opret og gem plan
    WeeklyMealPlan plan = new WeeklyMealPlan();
    plan.setConsumer(consumer);
    plan.setMeals(finalMeals);
    plan.setWeekStartDate(getWeekStartDate());
    
    WeeklyMealPlan saved = weeklyMealPlanRepository.save(plan);
    
    return new MealPlanResponse(saved, message);
}
```

**Optimization: Meal Caching**
- MealCacheService bruger en ConcurrentHashMap
- O(1) lookup tid
- Sparer database calls
- Reducerer duplikerede måltider

---

#### EmailServiceImpl.java
**Formål:** Sender emails via SMTP

**Features:**
- Optional email (kan deaktiveres)
- Thymeleaf HTML templates
- Support for månedlige planer

**Kode:**
```java
public void sendMealPlan(Consumer consumer, WeeklyMealPlan mealPlan) {
    // Tjek om email er enabled
    if (!emailEnabled) {
        throw new MessagingException("Email is disabled");
    }
    
    // Opret email
    MimeMessage message = javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    
    helper.setTo(consumer.getEmail());
    helper.setSubject("Your meal plan is ready!");
    
    // Render Thymeleaf template
    Context context = new Context();
    context.setVariable("consumer", consumer);
    context.setVariable("mealPlan", mealPlan);
    
    String html = templateEngine.process("weekly-meal-plan", context);
    helper.setText(html, true);
    
    // Send
    javaMailSender.send(message);
}
```

---

## Frontend (HTML/CSS/JavaScript)

### Fil Oversigt

#### HTML Filer

##### index.html
**Formål:** Landing page med login

**Features:**
- Simpel landing page
- "Login with Google" knap
- Redirect til OAuth2 flow

**Kode:**
```html
<button onclick="window.location.href='/oauth2/authorization/google'">
    <i class="bi bi-google"></i> Continue with Google
</button>
```

---

##### dashboard.html
**Formål:** Hovedsiden efter login

**Struktur:**
```html
<nav>
  <!-- Navbar med sprog/tema knapper -->
</nav>

<div class="container">
  <div class="row">
    <!-- Venstre kolonne: Profil + Præferencer -->
    <div class="col-lg-3">
      <div class="profile-card">...</div>
      <div class="preferences-card">...</div>
    </div>
    
    <!-- Midter kolonne: Måltidsplan -->
    <div class="col-lg-6">
      <button id="generateBtn">Generate Plan</button>
      <div id="mealPlanContent">
        <!-- Måltider dynamisk indsættes her -->
      </div>
    </div>
    
    <!-- Højre kolonne: Historik -->
    <div class="col-lg-3">
      <div class="history-card">...</div>
    </div>
  </div>
</div>
```

**Bootstrap Grid:**
- 3 kolonner layout på desktop
- Stacker på mobil
- Responsive design

---

##### preferences.html
**Formål:** Rediger præferencer

**Features:**
- Form med diæt, allergier, dislikes
- Tag input for lister
- Save knap

---

#### CSS (styles.css)

**Placering:** `static/styles.css`

**Features:**
- Dark/Light mode support via CSS variables
- Responsive design
- Animations
- Custom components

**Dark Mode Implementation:**
```css
:root {
  --bg-color: #ffffff;
  --text-color: #333333;
  --card-bg: #f8f9fa;
}

[data-theme="dark"] {
  --bg-color: #1a1a1a;
  --text-color: #ffffff;
  --card-bg: #2d2d2d;
}

body {
  background-color: var(--bg-color);
  color: var(--text-color);
}
```

**Custom Components:**
```css
.meal-card {
  transition: transform 0.2s;
}

.meal-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.ingredient-badge {
  display: inline-block;
  padding: 4px 12px;
  background: #e3f2fd;
  border-radius: 12px;
  margin: 4px;
}
```

---

#### JavaScript Moduler

**Struktur:**
```
js/
├── i18n.js       - Oversættelser
├── theme.js      - Dark/light mode
├── csrf.js       - Sikkerhed
├── profile.js    - Bruger data
├── mealplan.js   - Måltidsplan logik
├── history.js    - Historik
└── main.js       - Initialisering
```

##### i18n.js
**Formål:** Håndterer oversættelser

```javascript
const translations = {
  en: {
    generate: 'Generate Plan',
    noMealPlan: 'No meal plan yet'
  },
  da: {
    generate: 'Generer Plan',
    noMealPlan: 'Ingen måltidsplan endnu'
  }
};

function translate(key) {
  return translations[currentLang][key];
}

function updateTranslations() {
  document.querySelectorAll('[data-i18n]').forEach(el => {
    const key = el.getAttribute('data-i18n');
    el.textContent = translate(key);
  });
}
```

**Workflow:**
1. Load saved language fra localStorage
2. Apply translations ved page load
3. Update når bruger skifter sprog
4. Persist valg i localStorage

---

##### theme.js
**Formål:** Dark/Light mode toggle

```javascript
function toggleTheme() {
  currentTheme = currentTheme === 'light' ? 'dark' : 'light';
  document.documentElement.setAttribute('data-theme', currentTheme);
  localStorage.setItem('theme', currentTheme);
  
  // Update icon
  const icon = document.querySelector('#themeToggle i');
  icon.className = currentTheme === 'dark' 
    ? 'bi bi-sun-fill' 
    : 'bi bi-moon-fill';
}
```

---

##### csrf.js
**Formål:** CSRF token håndtering

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

async function ensureCsrfToken() {
  if (!getCsrfToken()) {
    await fetch('/api/csrf', { credentials: 'same-origin' });
  }
}
```

**Hvorfor CSRF Protection?**
- Beskytter mod Cross-Site Request Forgery
- Token valideres på serveren
- Skal sendes med alle POST requests

---

##### mealplan.js
**Formål:** Måltidsplan generering og visning

**Nøgle Funktioner:**

**displayMealPlan(planData):**
```javascript
function displayMealPlan(planData) {
  let plan = planData.mealPlan || planData;
  let message = planData.message;
  
  // Vis ChatGPT besked
  if (message) {
    html += `
      <div class="alert alert-success">
        <i class="bi bi-robot"></i> 
        <strong>ChatGPT says:</strong> ${message}
      </div>
    `;
  }
  
  // Loop gennem uger
  const weeksCount = Math.ceil(plan.meals.length / 5);
  for (let week = 0; week < weeksCount; week++) {
    // Vis uge header hvis flere uger
    if (weeksCount > 1) {
      html += `<h5>Week ${week + 1}</h5>`;
    }
    
    // Loop gennem dage
    for (let day = 0; day < 5; day++) {
      const meal = plan.meals[week * 5 + day];
      html += renderMealCard(meal, day);
    }
  }
}
```

**generateMealPlan():**
```javascript
async function generateMealPlan() {
  const csrfToken = getCsrfToken();
  
  const response = await fetch('/api/mealplan/generate?type=monthly', {
    method: 'POST',
    headers: {
      'X-XSRF-TOKEN': csrfToken
    }
  });
  
  const planResponse = await response.json();
  displayMealPlan(planResponse);
  loadHistory();
}
```

---

##### history.js
**Formål:** Viser historik over tidligere planer

```javascript
function displayHistory(history) {
  const container = document.getElementById('historyContent');
  
  container.innerHTML = history.map(plan => {
    const date = new Date(plan.weekStartDate).toLocaleDateString();
    const mealCount = plan.meals.length;
    
    return `
      <a href="#" class="history-item" 
         data-plan='${JSON.stringify(plan)}'>
        <div>${date}</div>
        <div>${mealCount} meals</div>
      </a>
    `;
  }).join('');
  
  // Add click handlers
  document.querySelectorAll('.history-item').forEach(item => {
    item.addEventListener('click', (e) => {
      const plan = JSON.parse(item.dataset.plan);
      showHistoryModal(plan);
    });
  });
}
```

---

##### main.js
**Formål:** Initialiserer applikationen

```javascript
document.addEventListener('DOMContentLoaded', async () => {
  // 1. Sikkerhed
  await ensureCsrfToken();
  
  // 2. UI Setup
  initTheme();
  updateLanguageButtons();
  updateTranslations();
  
  // 3. Load Data
  loadProfile();
  loadCurrentMealPlan();
  loadHistory();
  
  // 4. Event Listeners
  document.getElementById('generateBtn')
    .addEventListener('click', generateMealPlan);
  
  document.getElementById('themeToggle')
    .addEventListener('click', toggleTheme);
});
```

**Rækkefølge er vigtig:**
1. Sikkerhed først (CSRF)
2. UI setup
3. Data loading
4. Event handlers til sidst


---

## Database

### H2 In-Memory Database

**Hvorfor H2?**
- ✅ Ingen opsætning nødvendig
- ✅ Perfekt til development og testing
- ✅ Nulstilles ved restart (frisk start hver gang)
- ✅ Indbygget web console

**Console Access:**
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (blank)
```

### Database Schema

#### consumer Table
```sql
CREATE TABLE consumer (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255),
    diet_type VARCHAR(50),
    -- allergies og dislikes gemmes i separate tabeller
);

CREATE TABLE consumer_allergies (
    consumer_id UUID,
    allergies VARCHAR(255),
    FOREIGN KEY (consumer_id) REFERENCES consumer(id)
);
```

#### weekly_meal_plan Table
```sql
CREATE TABLE weekly_meal_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    week_start_date DATE NOT NULL,
    consumer_id UUID,
    FOREIGN KEY (consumer_id) REFERENCES consumer(id)
);
```

#### meal Table
```sql
CREATE TABLE meal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meal_name VARCHAR(500),
    img_url VARCHAR(500)
);

CREATE TABLE meal_ingredients (
    meal_id BIGINT,
    ingredients VARCHAR(255),
    FOREIGN KEY (meal_id) REFERENCES meal(id)
);
```

### Relations

```
consumer (1) ──── (N) weekly_meal_plan
                        │
                        │ (N)
                        │
                      meal
```

- En Consumer har mange WeeklyMealPlans
- En WeeklyMealPlan har mange Meals
- Meals kan genbruges via cache

---

## Sikkerhed

### 1. OAuth2 Authentication

**Flow:**
```
1. Bruger klikker "Login with Google"
2. Redirect til Google OAuth2
3. Bruger godkender permissions
4. Google sender authorization code
5. Spring exchanger code for access token
6. Spring henter bruger info (email, navn)
7. Session oprettes
8. Bruger redirectes til dashboard
```

**Fordele:**
- Ingen passwords at håndtere
- Google håndterer 2FA
- Bruger info er verificeret

---

### 2. CSRF Protection

**Hvad er CSRF?**
Cross-Site Request Forgery - onde sites der laver requests på vegne af brugeren.

**Beskyttelse:**
```
1. Server genererer unik CSRF token
2. Token gemmes i cookie
3. Frontend sender token med hver POST request
4. Server validerer at token matcher
```

**Implementation:**
```javascript
// Frontend
const token = getCsrfToken();
fetch('/api/endpoint', {
  method: 'POST',
  headers: {
    'X-XSRF-TOKEN': token
  }
});
```

```java
// Backend
@Configuration
public class SecurityConfig {
    .csrf(csrf -> csrf
        .csrfTokenRepository(
            CookieCsrfTokenRepository.withHttpOnlyFalse()
        )
    )
}
```

---

### 3. Input Validation

**Backend Validation:**
```java
@PostMapping("/profile/preferences")
public ResponseEntity<?> updatePreferences(
        @Valid @RequestBody PreferencesRequest request) {
    
    // Spring validerer automatisk
    // @NotNull, @Size, @Email etc.
}
```

**Frontend Validation:**
```javascript
function validateInput(value) {
    if (!value || value.trim() === '') {
        return false;
    }
    // XSS protection - escape HTML
    return DOMPurify.sanitize(value);
}
```

---

### 4. API Secrets

**application.properties:**
```properties
# ALDRIG commit secrets til Git!
openai.api.key=${OPENAI_API_KEY}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_SECRET}
```

**Environment Variables:**
```bash
export OPENAI_API_KEY=sk-proj-...
export GOOGLE_SECRET=GOCSPX-...
```

---

### 5. Rate Limiting (Ikke implementeret endnu)

**Anbefaling til produktion:**
```java
@RateLimiter(name = "mealplan", fallbackMethod = "rateLimitFallback")
@PostMapping("/generate")
public MealPlanResponse generateMealPlan() {
    // Max 10 requests per hour per bruger
}
```

---

## Deployment

### Local Development

```bash
# 1. Clone repository
git clone <repo-url>
cd WeeklyMealPlanner-GPT

# 2. Set environment variables
export OPENAI_API_KEY=your-key
export GOOGLE_CLIENT_ID=your-id
export GOOGLE_CLIENT_SECRET=your-secret

# 3. Run
./mvnw spring-boot:run

# 4. Access
http://localhost:8080
```

---

### Docker Deployment

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build & Run:**
```bash
# Build JAR
./mvnw clean package -DskipTests

# Build Docker image
docker build -t mealplanner .

# Run
docker run -p 8080:8080 \
  -e OPENAI_API_KEY=your-key \
  -e GOOGLE_CLIENT_ID=your-id \
  -e GOOGLE_CLIENT_SECRET=your-secret \
  mealplanner
```

---

### Hetzner Cloud Deployment

**1. Opret Server:**
```bash
# CX21: 2 vCPU, 4GB RAM, 40GB SSD - 5.83€/md
```

**2. Install Docker:**
```bash
ssh root@<server-ip>

apt update && apt upgrade -y
apt install docker.io docker-compose -y
systemctl start docker
systemctl enable docker
```

**3. Deploy:**
```bash
# Upload JAR
scp target/*.jar root@<server-ip>:/opt/mealplanner/

# Create systemd service
cat > /etc/systemd/system/mealplanner.service << EOF
[Unit]
Description=Meal Planner
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/mealplanner
ExecStart=/usr/bin/java -jar app.jar
Environment="OPENAI_API_KEY=your-key"
Environment="GOOGLE_CLIENT_ID=your-id"
Environment="GOOGLE_CLIENT_SECRET=your-secret"
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# Start service
systemctl daemon-reload
systemctl start mealplanner
systemctl enable mealplanner

# Check logs
journalctl -u mealplanner -f
```

**4. Setup Nginx Reverse Proxy:**
```bash
apt install nginx certbot python3-certbot-nginx -y

cat > /etc/nginx/sites-available/mealplanner << EOF
server {
    server_name mealplanner.dk;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }
}
EOF

ln -s /etc/nginx/sites-available/mealplanner /etc/nginx/sites-enabled/
nginx -t
systemctl restart nginx

# SSL Certificate
certbot --nginx -d mealplanner.dk
```

---

### Production Checklist

- [ ] Skift til PostgreSQL (fra H2)
- [ ] Setup email server (Gmail/Postmark)
- [ ] Enable rate limiting
- [ ] Setup monitoring (Prometheus + Grafana)
- [ ] Configure backups
- [ ] Add error tracking (Sentry)
- [ ] Setup CI/CD (GitHub Actions)
- [ ] Add health check endpoints
- [ ] Configure logging (ELK stack)
- [ ] Security audit
- [ ] Load testing
- [ ] Documentation update

---

## Konklusion

### Projekt Statistikker

**Backend:**
- 32 Java filer
- ~3,500 linjer kode
- 7 REST endpoints
- 3 database entities
- 8 services

**Frontend:**
- 3 HTML filer
- 1 CSS fil
- 7 JavaScript moduler
- ~800 linjer JavaScript
- Bootstrap 5 framework

**Features:**
- OAuth2 authentication
- AI meal generation
- Email notifications
- Multi-language support
- Dark mode
- Responsive design
- Meal history
- Preference management

---

### Hvad Har Vi Lært?

**Backend:**
- Spring Boot arkitektur
- OAuth2 implementation
- REST API design
- JPA/Hibernate ORM
- Service layer patterns
- Exception handling
- Transaction management

**Frontend:**
- Modern JavaScript (ES6+)
- Modular design
- Async/await
- DOM manipulation
- API integration
- Responsive design
- i18n implementation

**DevOps:**
- Docker containerization
- Environment variables
- Deployment strategies
- Nginx configuration
- SSL/TLS setup

**Integration:**
- OpenAI API
- Google OAuth2
- Email services
- Database management

---

### Næste Steps

**Forbedringer:**
- [ ] Tilføj billeder til måltider (via TheMealDB API)
- [ ] Implementer indkøbsliste funktion
- [ ] Meal favoritter
- [ ] Del planer med andre
- [ ] Mobile app (React Native)
- [ ] Meal prep timers
- [ ] Nutritional information
- [ ] Recipe instructions

**Teknisk Gæld:**
- [ ] Unit tests (mangler coverage)
- [ ] Integration tests
- [ ] API documentation (Swagger)
- [ ] Performance optimization
- [ ] Caching strategy
- [ ] Database migration til PostgreSQL

---

## Appendiks

### Nyttige Kommandoer

```bash
# Build
./mvnw clean package

# Run tests
./mvnw test

# Run with profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Check dependencies
./mvnw dependency:tree

# Database console
open http://localhost:8080/h2-console

# View logs
tail -f logs/spring.log

# Check port
lsof -i :8080
```

###  Troubleshooting

**Problem: Port 8080 already in use**
```bash
# Find process
lsof -i :8080

# Kill process
kill -9 <PID>
```

**Problem: OAuth2 redirect mismatch**
- Tjek Google Console authorized redirect URIs
- Skal matche: `http://localhost:8080/login/oauth2/code/google`

**Problem: OpenAI API errors**
- Tjek API key er gyldig
- Tjek rate limits
- Tjek account balance

**Problem: Email not sending**
- Tjek `spring.mail.enabled=true`
- Verificer SMTP credentials
- Tjek firewall/ports

---

### Ressourcer

**Documentation:**
- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [OpenAI API](https://platform.openai.com/docs/api-reference)
- [Bootstrap 5](https://getbootstrap.com/docs/5.3/)

**Tools:**
- [Postman](https://www.postman.com/) - API testing
- [DBeaver](https://dbeaver.io/) - Database tool
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) - IDE

**Community:**
- [Stack Overflow](https://stackoverflow.com/)
- [Spring Community](https://spring.io/community)
- [GitHub Issues](https://github.com/)

---

**Projekt udviklet af:** Dit Navn
**Dato:** November 2024
**Version:** 1.0.0
**Licens:** MIT

