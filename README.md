# WeeklyMealPlanner-GPT

En intelligent madplanlægningsapplikation der bruger kunstig intelligens til at generere personlige ugemenu'er.

## Indholdsfortegnelse

- [Om projektet](#om-projektet)
- [Funktioner](#funktioner)
- [Teknologier](#teknologier)
- [Projektstruktur](#projektstruktur)

## Om projektet

WeeklyMealPlanner-GPT er en webapplikation der hjælper brugere med at planlægge deres måltider for ugen eller måneden. Applikationen bruger OpenAI's GPT til at generere skræddersyede madplaner baseret på brugerens individuelle præferencer, allergier og kosttype.

Systemet integrerer med TheMealDB for at hente opskrifter og billeder af retter, og giver mulighed for at sende madplaner direkte til brugerens email.

## Funktioner

### Brugeradministration
- OAuth2 authentication til sikker login
- Brugerprofiler med gemte præferencer

### Madplangenerering
- AI-drevet generering af månedlige madplaner (4 ugers planer)
- Personalisering baseret på allergier, kosttype og afvisninger
- Caching af genererede måltider for forbedret performance

### Brugerindstillinger
- Definition af allergier
- Valg af kosttype (vegetar/veganer/omnivore)
- Angivelse af ingredienser man ikke kan lide
- Sprogindstillinger

### Email funktionalitet
- Afsendelse af madplaner via email
- HTML formaterede emails med madplaner

### Historik
- Gemmer tidligere genererede madplaner
- Mulighed for at se historik over tidligere planer

## Teknologier

### Backend
- Java 21
- Spring Boot 3.5.7
- Spring Security med OAuth2
- Spring Data JPA
- Hibernate
- MySQL database
- H2 in-memory database til udvikling

### Frontend
- Thymeleaf templates
- HTML/CSS/JavaScript
- Responsive design

### Eksterne integrationer
- OpenAI GPT API til madplangenerering
- TheMealDB API til opskrifter og billeder

### Build værktøjer
- Maven
- JaCoCo til code coverage
- Lombok til boilerplate reduktion

### DevOps
- Docker support
- Docker Compose konfiguration

## Projektstruktur

Projektet følger en standard Spring Boot struktur:

### Model
- `Consumer`: Brugermodel med præferencer
- `Meal`: Repræsentation af individuelle måltider
- `WeeklyMealPlan`: Ugentlige madplaner knyttet til brugere

### Services
- `OpenAIService`: Integration med OpenAI API
- `TheMealDBService`: Integration med TheMealDB API
- `MealPlanService`: Hovedlogik for madplangenerering
- `MealCacheService`: Caching af genererede måltider
- `ConsumerService`: Brugeradministration
- `EmailService`: Email funktionalitet

### Controllers
- `MealPlanController`: API endpoints til madplaner
- `ProfileController`: Brugerprofilhåndtering
- `HealthController`: Sundhedstjek endpoints

### Configuration
- `SecurityConfig`: Sikkerhedskonfiguration
- `OAuth2LoginSuccessHandler`: OAuth2 login håndtering
- `AsyncConfig`: Asynkron konfiguration

### Repositories
- JPA repositories til databaseadgang
- Optimerede med indices for performance
