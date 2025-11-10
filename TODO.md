# 📋 TODO - Meal Planner Project
**Deadline:** Fredag (3 dage tilbage)  
**Fokus:** Kodevedligeholdelse, Hastighed, Refactoring

---

## 🔴 KRITISKE FIXES (Må gøres i dag/i morgen)

### 1. Fix Circular JSON Reference (30 min)
**Problem:** `Consumer` ↔ `WeeklyMealPlan` circular reference giver uendelig JSON loop

**Løsning:**
```java
// I Consumer.java
@JsonIgnore
private List<WeeklyMealPlan> mealPlans;
```

**Test:** 
- Generer meal plan
- Check at JSON ikke looper
- Verify frontend viser data korrekt

---

### 2. Implementer HashSet Cache for Meals (1-2 timer)
**Hvorfor:** Hastighed - O(1) lookup i stedet for O(n)

**Implementation:**
```java
@Service
public class MealCacheService {
    private final Set<Meal> mealCache = new HashSet<>();
    private final MealRepository mealRepository;
    
    @PostConstruct
    public void initCache() {
        mealCache.addAll(mealRepository.findAll());
    }
    
    public Optional<Meal> findByName(String name) {
        return mealCache.stream()
            .filter(m -> m.getMealName().equals(name))
            .findFirst();
    }
    
    public void addToCache(Meal meal) {
        mealCache.add(meal);
    }
}
```

**Opdater:**
- `MealPlanServiceImpl` til at bruge cache
- Tilføj cache invalidation ved nye meals

**Test:**
- Performance test: Generer 10 meal plans
- Check cache hits vs misses

---

### 3. Add Missing Backend Endpoints (1 time)
**Mangler:**
- `PUT /api/profile/preferences` - Update user preferences
- `GET /api/csrf` - Ensure CSRF token generation

**Implementation:**
```java
// ProfileController.java
@PutMapping("/preferences")
public ResponseEntity<Consumer> updatePreferences(
    @AuthenticationPrincipal OAuth2User principal,
    @RequestBody PreferencesDTO preferences) {
    // Update consumer preferences
    // Return updated consumer
}

// CsrfController.java
@GetMapping("/api/csrf")
public void getCsrf() {
    // Just triggers CSRF token creation
}
```

---

## 🟡 REFACTORING & CODE QUALITY (Tirsdag-Onsdag)

### 4. Service Layer Cleanup (2-3 timer)

#### A. Extract DTOs
**Opret:** `dto/` package med:
- `MealPlanRequestDTO` - For generation request
- `MealPlanResponseDTO` - For response
- `PreferencesDTO` - For preferences update
- `ProfileDTO` - For profile data

**Fordel:** Type safety, validation, clear contracts

#### B. Reducer `OpenAIServiceImpl` Complexity
**Problem:** 200+ linjer, multiple responsibilities

**Løsning:**
```java
// Split into:
OpenAIService (interface)
  ├── OpenAIClient (HTTP communication)
  ├── PromptBuilder (Build prompts)
  └── ResponseParser (Parse OpenAI responses)
```

**Metrics før/efter:**
- Lines per method: Target < 20
- Cyclomatic complexity: Target < 10

#### C. Improve Error Handling
**Tilføj:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(OpenAIException.class)
    public ResponseEntity<ErrorDTO> handleOpenAI(OpenAIException e) {
        // Log + return user-friendly error
    }
    
    @ExceptionHandler(MealPlanException.class)
    public ResponseEntity<ErrorDTO> handleMealPlan(MealPlanException e) {
        // Log + return user-friendly error
    }
}
```

---

### 5. Database Optimization (1-2 timer)

#### A. Add Indexes
```java
@Entity
@Table(indexes = {
    @Index(name = "idx_meal_name", columnList = "mealName"),
    @Index(name = "idx_consumer_email", columnList = "email"),
    @Index(name = "idx_plan_date", columnList = "weekStartDate")
})
public class WeeklyMealPlan { ... }
```

#### B. Optimize Queries
```java
// MealPlanRepository.java
@Query("SELECT DISTINCT mp FROM WeeklyMealPlan mp " +
       "LEFT JOIN FETCH mp.meals " +
       "WHERE mp.consumer.id = :consumerId " +
       "ORDER BY mp.weekStartDate DESC")
List<WeeklyMealPlan> findByConsumerWithMeals(@Param("consumerId") String consumerId);
```

**Test:** Check N+1 query problem is resolved

#### C. Add Pagination to History
```java
@GetMapping("/history")
public ResponseEntity<Page<WeeklyMealPlan>> getHistory(
    @AuthenticationPrincipal OAuth2User principal,
    @PageableDefault(size = 10) Pageable pageable) {
    // Return paginated results
}
```

---

### 6. Testing Strategy (2-3 timer)

#### A. Unit Tests Priority
**Must have:**
- `OpenAIServiceTest` - Mock OpenAI responses
- `MealPlanServiceTest` - Business logic
- `MealCacheServiceTest` - Cache operations

**Template:**
```java
@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {
    @Mock
    private MealRepository mealRepository;
    
    @Mock
    private OpenAIService openAIService;
    
    @InjectMocks
    private MealPlanServiceImpl service;
    
    @Test
    void generateMealPlan_withValidConsumer_shouldReturnPlan() {
        // Given
        Consumer consumer = new Consumer();
        List<Meal> mockMeals = List.of(/* ... */);
        
        when(openAIService.generateMealPlan(any()))
            .thenReturn(mockMeals);
        
        // When
        WeeklyMealPlan result = service.generateMealPlan(consumer);
        
        // Then
        assertNotNull(result);
        assertEquals(5, result.getMeals().size());
    }
}
```

#### B. Integration Tests
**Focus on:**
- `MealPlanControllerTest` - API endpoints
- `SecurityConfigTest` - OAuth + CSRF

---

## 🟢 NICE TO HAVE (Onsdag-Torsdag hvis tid)

### 7. Frontend Improvements (1-2 timer)

#### A. Add Loading States
- Disable "Generate" button while loading
- Show progress indicator
- Add success/error toasts

#### B. Improve Meal Card Display
- Add meal images (from TheMealDB)
- Show preparation time
- Add "View Recipe" link

#### C. Add Filters to History
- Filter by date range
- Search by meal name
- Sort by newest/oldest

---

### 8. Logging & Monitoring (1 time)

**Add structured logging:**
```java
@Slf4j
@Service
public class MealPlanServiceImpl {
    public WeeklyMealPlan generateMealPlan(Consumer consumer) {
        log.info("Generating meal plan for consumer: {}", consumer.getEmail());
        
        try {
            // ... logic
            log.info("Successfully generated meal plan with {} meals", meals.size());
            return plan;
        } catch (Exception e) {
            log.error("Failed to generate meal plan for consumer: {}", 
                     consumer.getEmail(), e);
            throw e;
        }
    }
}
```

**Metrics to track:**
- Meal plan generation time
- OpenAI API call latency
- Cache hit rate

---

### 9. Security Hardening (30 min)

**Add:**
```properties
# Rate limiting (Spring Boot 3)
spring.cloud.gateway.filter.request-rate-limiter.enabled=true

# CORS configuration
spring.web.cors.allowed-origins=http://localhost:8080
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE
```

**Validate:**
- CSRF tokens required on all mutating endpoints
- OAuth2 redirects properly secured
- No sensitive data in logs

---

### 10. Documentation (1 time)

**Create:**
- `README.md` - Setup instructions, architecture overview
- `API.md` - API endpoint documentation
- JavaDoc on public methods

**README Template:**
```markdown
# Meal Planner

AI-powered weekly meal planning application.

## Features
- Google OAuth2 authentication
- AI-generated meal plans using OpenAI
- Personalized preferences (diet, allergies, dislikes)
- Meal history tracking
- Dark mode & bilingual support

## Setup
1. Clone repository
2. Configure `application.properties`
3. Run: `./mvnw spring-boot:run`

## Tech Stack
- Spring Boot 3.5
- PostgreSQL/H2
- OpenAI API
- TheMealDB API
- Bootstrap 5

## API Endpoints
See [API.md](API.md)
```

---

## 📊 PERFORMANCE TARGETS

| Metric | Current | Target |
|--------|---------|--------|
| Meal plan generation | 5-10s | < 3s |
| Page load time | 2s | < 1s |
| Database queries per request | 10+ | < 5 |
| Code coverage | 0% | > 60% |
| Lines per method | 50+ | < 20 |
| Cyclomatic complexity | 15+ | < 10 |

---

## 🗓️ TIDSPLAN

### **Mandag Aften (i aften)**
- ✅ Fix circular JSON reference
- ✅ Add missing endpoints (preferences, CSRF)
- ✅ Test basic meal plan generation

### **Tirsdag**
- ⏰ 09:00-11:00: Implementer HashSet cache
- ⏰ 11:00-13:00: Database optimization (indexes, queries)
- ⏰ 14:00-17:00: Refactor OpenAIService + DTOs
- ⏰ 17:00-18:00: Write unit tests

### **Onsdag**
- ⏰ 09:00-12:00: Complete unit tests (target 60% coverage)
- ⏰ 13:00-15:00: Frontend improvements (loading states, images)
- ⏰ 15:00-17:00: Logging & monitoring
- ⏰ 17:00-18:00: Security review

### **Torsdag**
- ⏰ 09:00-12:00: Integration tests
- ⏰ 13:00-15:00: Documentation (README, API docs)
- ⏰ 15:00-17:00: Performance testing & optimization
- ⏰ 17:00-18:00: Final bug fixes

### **Fredag (Deadline)**
- ⏰ 09:00-12:00: Final testing & polish
- ⏰ 13:00-15:00: Code review & cleanup
- ⏰ 15:00-17:00: Deployment preparation
- ⏰ 17:00: 🎉 **DEADLINE**

---

## 🎯 SUCCESS CRITERIA

- [ ] Meal plan generation works reliably
- [ ] All CRUD operations functional
- [ ] Frontend responsive & bug-free
- [ ] 60%+ test coverage
- [ ] No critical security issues
- [ ] Performance targets met
- [ ] Code properly documented
- [ ] Ready for deployment

---

## 📝 NOTER

**Prioritering:**
1. **Funktionalitet** - Alt skal virke
2. **Performance** - Cache + DB optimization
3. **Code Quality** - Refactoring + tests
4. **Polish** - UI improvements + docs

**Hvis tidspres:**
- Skip: Frontend polish, extensive documentation
- Prioriter: Core functionality, basic tests, critical refactoring

**Når du er færdig med en task:**
- Commit med beskrivende message
- Test grundigt
- Marker som færdig i denne TODO

---

God arbejdslyst! 🚀
