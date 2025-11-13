# JavaScript Moduler

Koden er opdelt i mindre, læsbare moduler:

## Struktur

```
static/js/
├── i18n.js       - Oversættelser (dansk/engelsk)
├── theme.js      - Dark/light mode funktionalitet
├── csrf.js       - CSRF token håndtering
├── profile.js    - Bruger profil og præferencer
├── mealplan.js   - Måltidsplan generering og visning
├── history.js    - Historik visning
└── main.js       - Initialisering og event listeners
```

## Moduler

### i18n.js
- Håndterer oversættelser mellem dansk og engelsk
- `translate(key)` - Oversætter en nøgle
- `toggleLanguage()` - Skifter sprog
- `updateTranslations()` - Opdaterer alle oversatte elementer

### theme.js
- Håndterer dark/light mode
- `toggleTheme()` - Skifter tema
- `initTheme()` - Initialiserer tema ved page load

### csrf.js
- CSRF token sikkerhed
- `getCsrfToken()` - Henter CSRF token fra cookies
- `ensureCsrfToken()` - Sikrer at token eksisterer

### profile.js
- Bruger profil og præferencer
- `loadProfile()` - Loader bruger data
- `loadPreferences()` - Viser brugerens præferencer

### mealplan.js
- Måltidsplan funktionalitet
- `loadCurrentMealPlan()` - Henter nuværende plan
- `displayMealPlan()` - Viser planen med ChatGPT besked
- `generateMealPlan()` - Genererer ny månedlig plan
- `sendMealPlanByEmail()` - Sender plan via email

### history.js
- Historik funktionalitet
- `loadHistory()` - Henter historik
- `displayHistory()` - Viser historik liste
- `showHistoryModal()` - Viser detaljer i modal

### main.js
- Initialiserer hele applikationen
- Sætter event listeners op
- Kalder load funktioner ved page load

## Rækkefølge i HTML

Scriptsne skal inkluderes i denne rækkefølge:
```html
<script src="js/i18n.js"></script>
<script src="js/theme.js"></script>
<script src="js/csrf.js"></script>
<script src="js/profile.js"></script>
<script src="js/mealplan.js"></script>
<script src="js/history.js"></script>
<script src="js/main.js"></script>
```

main.js skal være sidst da den afhænger af alle andre moduler.
