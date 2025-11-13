# Comprehensive Service Layer Analysis & Testing Guide

**Generated:** 2025-11-13  
**Project:** Weekly Meal Planner GPT

---

## Table of Contents
1. [Service Overview](#service-overview)
2. [Current State Analysis](#current-state-analysis)
3. [Metrics & KPIs](#metrics--kpis)
4. [Improvement Recommendations](#improvement-recommendations)
5. [Testing Strategy](#testing-strategy)
6. [Simple Test Examples](#simple-test-examples)
7. [TODO List](#todo-list)

---

## Service Overview

### 1. ConsumerService
**Purpose:** Manage user/consumer data operations  
**Layer:** Data Access  
**Dependencies:** ConsumerRepository

**Methods:**
- `save(Consumer)` - Create/update consumer
- `findByEmail(String)` - Retrieve by email (OAuth login)
- `findById(UUID)` - Retrieve by ID
- `findAll()` - List all consumers
- `deleteById(UUID)` - Remove consumer
- `existsById(UUID)` - Check existence

---

### 2. EmailService
**Purpose:** Send meal plan emails to users  
**Layer:** Communication  
**Dependencies:** JavaMailSender, SpringTemplateEngine

**Methods:**
- `sendWeeklyMeanPlan(Consumer)` - Send current week's plan
- `sendMealPlan(Consumer, WeeklyMealPlan)` - Send specific plan

**Configuration:**
- `spring.mail.enabled` - Toggle email functionality (default: false)

---

### 3. MealCacheService
**Purpose:** In-memory cache for meal lookup performance  
**Layer:** Caching  
**Dependencies:** MealRepository

**Methods:**
- `getMealByName(String)` - Lookup cached meal
- `addToCache(Meal)` - Add new meal to cache
- `initCache()` - Load all meals on startup (@PostConstruct)

---

### 4. MealPlanService
**Purpose:** Core business logic for meal plan generation  
**Layer:** Business Logic  
**Dependencies:** OpenAIService, ConsumerService, MealRepository, WeeklyMealPlanRepository, MealCacheService, EmailService

**Methods:**
- `generateWeeklyMealPlan(Consumer)` - Generate 1 week (5 meals)
- `generateMonthlyMealPlan(Consumer)` - Generate 4 weeks (20 meals)
- `getCurrentWeekPlan(UUID)` - Get current week's plan
- `getPlanHistory(UUID)` - Get all past plans
- `sendMealPlanByEmail(UUID, Long)` - Email specific plan

---

### 5. TheMealDBService
**Purpose:** External API integration for meal data  
**Layer:** External Integration  
**Dependencies:** RestTemplate

**Methods:**
- `searchMealsByName(String)` - Search by meal name
- `searchMealsByIngredient(String)` - Search by ingredient
- `getMealById(String)` - Get specific meal details

**API:** https://www.themealdb.com/api/json/v1/1

---

### 6. OpenAIService
**Purpose:** AI-powered meal plan generation  
**Layer:** External Integration  
**Dependencies:** RestTemplate, ObjectMapper

**Methods:**
- `generateMealPlan(Consumer)` - Generate 1 week
- `generateMealPlan(Consumer, int weeks)` - Generate N weeks
- `getLastGeneratedMessage()` - Get AI response message

**Configuration:**
- `openai.api.key` - API authentication
- `openai.api.url` - API endpoint
- `openai.model` - GPT model (e.g., gpt-3.5-turbo)

---

## Current State Analysis

### ✅ Strengths

1. **Good Separation of Concerns**
   - Clear interface/implementation separation
   - Services follow Single Responsibility Principle
   - Proper dependency injection via constructor

2. **Logging**
   - Comprehensive logging in MealPlanService
   - Good error tracking in OpenAIService

3. **Transactions**
   - Proper @Transactional usage in MealPlanService

4. **Caching Strategy**
   - Meal caching reduces database hits
   - PostConstruct initialization is efficient

5. **Error Handling**
   - Custom MealGenerationException
   - Proper exception wrapping in services

### ⚠️ Issues & Code Smells

#### 1. ConsumerService
**Issues:**
- Generic `RuntimeException` instead of custom exception
- No null/empty email validation
- Missing business validations

**Risk Level:** MEDIUM

#### 2. EmailService
**Issues:**
- Method name typo: `sendWeeklyMeanPlan` → should be `sendWeeklyMealPlan`
- Throws exception when disabled (should log and return gracefully)
- No retry mechanism for failed emails
- No email queue/async processing

**Risk Level:** HIGH (naming bug), LOW (functionality)

#### 3. MealCacheService
**Issues:**
- Not thread-safe (HashSet without synchronization)
- No cache eviction strategy (memory leak risk)
- No cache size limit
- System.out.println instead of logger
- Case-sensitive meal name lookup (potential misses)

**Risk Level:** HIGH (thread safety), MEDIUM (memory)

#### 4. MealPlanService
**Issues:**
- Field injection (@Autowired) instead of constructor
- Week calculation might fail near year boundaries
- No duplicate plan prevention (can generate multiple plans for same week)
- Email sending is synchronous (blocks response)
- No retry logic for OpenAI failures

**Risk Level:** MEDIUM

#### 5. TheMealDBService
**Issues:**
- No error handling for API failures
- No retry mechanism
- No rate limiting
- No response validation
- URL encoding missing for search parameters

**Risk Level:** MEDIUM

#### 6. OpenAIService
**Issues:**
- Hardcoded file path: "chatgpt-prompt.txt"
- No validation of OpenAI response
- Swallows exceptions (returns empty list)
- No timeout configuration
- No token usage tracking
- max_tokens=1000 might be too low for monthly plans

**Risk Level:** HIGH (error handling), MEDIUM (reliability)

---

## Metrics & KPIs

### Performance Metrics

#### Current Performance Indicators
```
Metric                          | Target    | How to Measure
--------------------------------|-----------|----------------------------------
Meal Plan Generation Time       | < 5s      | Log timestamp diff in MealPlanService
OpenAI API Response Time        | < 3s      | Add timer around restTemplate.exchange()
Database Query Time             | < 100ms   | Enable Hibernate statistics
Email Delivery Time             | < 2s      | Timer in EmailService
Cache Hit Rate                  | > 80%     | Counter: hits/(hits+misses)
Memory Usage (cache)            | < 100MB   | JMX monitoring of mealCache size
API Error Rate                  | < 1%      | Counter: errors/total_requests
Plan Generation Success Rate    | > 95%     | Counter: success/total_attempts
```

### Implementation Example
```java
// Add to MealPlanService
private final MeterRegistry meterRegistry; // Micrometer

@Transactional
public MealPlanResponse generateWeeklyMealPlan(Consumer consumer) {
    Timer.Sample sample = Timer.start(meterRegistry);
    try {
        // existing code
        meterRegistry.counter("mealplan.generation.success").increment();
        return result;
    } catch (Exception e) {
        meterRegistry.counter("mealplan.generation.failure").increment();
        throw e;
    } finally {
        sample.stop(Timer.builder("mealplan.generation.time").register(meterRegistry));
    }
}
```

### Business Metrics
```
Metric                          | Target    | SQL Query
--------------------------------|-----------|----------------------------------
Active Users                    | -         | SELECT COUNT(*) FROM consumer WHERE meal_plans > 0
Plans per User (avg)            | > 4       | SELECT AVG(plan_count) FROM (SELECT COUNT(*) as plan_count FROM weekly_meal_plan GROUP BY consumer_id)
Cache Size                      | -         | mealCache.size()
Most Popular Meals              | -         | SELECT meal_name, COUNT(*) FROM meal_weekly_meal_plan GROUP BY meal_name ORDER BY COUNT(*) DESC LIMIT 10
Email Success Rate              | > 90%     | Log analysis or separate email_log table
```

---

## Improvement Recommendations

### Priority 1: Critical Issues

#### 1.1 Fix MealCacheService Thread Safety
```java
@Service
public class MealCacheServiceImpl implements MealCacheService {
    private final Set<Meal> mealCache = Collections.synchronizedSet(new HashSet<>());
    // OR use ConcurrentHashMap for better performance
    private final Map<String, Meal> mealCache = new ConcurrentHashMap<>();
    
    public Meal getMealByName(String name) {
        return mealCache.get(name.toLowerCase()); // Case-insensitive
    }
}
```

#### 1.2 Fix Email Service Method Name
```java
// EmailService interface - rename method
void sendWeeklyMealPlan(Consumer consumer) throws MessagingException; // Fixed typo
```

#### 1.3 Add Custom Exceptions
```java
// New exceptions
public class ConsumerNotFoundException extends RuntimeException {
    public ConsumerNotFoundException(String email) {
        super("Consumer not found with email: " + email);
    }
}

public class EmailDisabledException extends RuntimeException {
    public EmailDisabledException() {
        super("Email functionality is disabled");
    }
}
```

#### 1.4 Fix OpenAI Error Handling
```java
private List<Meal> getMealPlanFromPrompt(String prompt) {
    try {
        // existing code
    } catch (HttpClientErrorException e) {
        logger.error("OpenAI API client error: {}", e.getMessage());
        throw new MealGenerationException("Invalid request to AI service", e);
    } catch (HttpServerErrorException e) {
        logger.error("OpenAI API server error: {}", e.getMessage());
        throw new MealGenerationException("AI service temporarily unavailable", e);
    } catch (ResourceAccessException e) {
        logger.error("OpenAI API timeout: {}", e.getMessage());
        throw new MealGenerationException("AI service timed out", e);
    } catch (Exception e) {
        logger.error("Unexpected error calling OpenAI API: ", e);
        throw new MealGenerationException("Failed to generate meal plan", e);
    }
}
```

### Priority 2: High Impact Improvements

#### 2.1 Add Response Validation
```java
// TheMealDBService
public List<TheMealDbResponse.MealDto> searchMealsByName(String name) {
    if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("Search name cannot be empty");
    }
    
    String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
    String url = API_BASE_URL + "/search.php?s=" + encodedName;
    
    try {
        TheMealDbResponse response = restTemplate.getForObject(url, TheMealDbResponse.class);
        if (response == null) {
            logger.warn("Null response from TheMealDB API for name: {}", name);
            return Collections.emptyList();
        }
        return response.getMeals() != null ? response.getMeals() : Collections.emptyList();
    } catch (RestClientException e) {
        logger.error("Error calling TheMealDB API: {}", e.getMessage());
        return Collections.emptyList();
    }
}
```

#### 2.2 Add Retry Logic with Resilience4j
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>
```

```java
// OpenAIService
@Retry(name = "openai", fallbackMethod = "generateMealPlanFallback")
public List<Meal> generateMealPlan(Consumer consumer, int weeks) throws IOException {
    // existing code
}

private List<Meal> generateMealPlanFallback(Consumer consumer, int weeks, Exception e) {
    logger.error("All retries failed for meal plan generation", e);
    throw new MealGenerationException("Unable to generate meal plan after retries", e);
}
```

```yaml
# application.yml
resilience4j:
  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 1s
        retryExceptions:
          - java.io.IOException
          - org.springframework.web.client.ResourceAccessException
```

#### 2.3 Use Constructor Injection
```java
// MealPlanServiceImpl - replace @Autowired fields
@Service
public class MealPlanServiceImpl implements MealPlanService {
    private final OpenAIService openAIService;
    private final ConsumerService consumerService;
    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final MealRepository mealRepository;
    private final MealCacheService mealCacheService;
    private final EmailService emailService;

    public MealPlanServiceImpl(
            OpenAIService openAIService,
            ConsumerService consumerService,
            WeeklyMealPlanRepository weeklyMealPlanRepository,
            MealRepository mealRepository,
            MealCacheService mealCacheService,
            EmailService emailService) {
        this.openAIService = openAIService;
        this.consumerService = consumerService;
        this.weeklyMealPlanRepository = weeklyMealPlanRepository;
        this.mealRepository = mealRepository;
        this.mealCacheService = mealCacheService;
        this.emailService = emailService;
    }
}
```

#### 2.4 Async Email Sending
```java
@Service
public class EmailServiceImpl implements EmailService {
    
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendMealPlanAsync(Consumer consumer, WeeklyMealPlan mealPlan) {
        try {
            sendMealPlan(consumer, mealPlan);
            return CompletableFuture.completedFuture(null);
        } catch (MessagingException e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}

// Configuration
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("email-");
        executor.initialize();
        return executor;
    }
}
```

### Priority 3: Nice to Have

#### 3.1 Cache Eviction Strategy
```java
@Service
public class MealCacheServiceImpl implements MealCacheService {
    private final Cache<String, Meal> mealCache;
    
    public MealCacheServiceImpl(MealRepository mealRepository) {
        this.mealCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(24, TimeUnit.HOURS)
            .recordStats()
            .build();
    }
    
    public Meal getMealByName(String name) {
        return mealCache.get(name.toLowerCase(), key -> 
            mealRepository.findByMealNameIgnoreCase(key).orElse(null)
        );
    }
}
```

#### 3.2 Add Request/Response Logging Interceptor
```java
@Component
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
                                        ClientHttpRequestExecution execution) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.debug("Request: {} {}", request.getMethod(), request.getURI());
        
        ClientHttpResponse response = execution.execute(request, body);
        
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("Response: {} in {}ms", response.getStatusCode(), duration);
        
        return response;
    }
}
```

#### 3.3 Add Health Checks
```java
@Component
public class OpenAIHealthIndicator implements HealthIndicator {
    private final OpenAIService openAIService;
    
    @Override
    public Health health() {
        try {
            // Simple health check - could be a lightweight API call
            boolean isHealthy = checkOpenAIConnection();
            return isHealthy ? Health.up().build() : Health.down().build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
}
```

---

## Testing Strategy

### Testing Pyramid

```
              /\
             /  \
            / E2E \ (10%)
           /______\
          /        \
         /Integration\ (30%)
        /____________\
       /              \
      /   Unit Tests   \ (60%)
     /__________________\
```

### Test Coverage Goals
- **Unit Tests:** 80% code coverage
- **Integration Tests:** All service interactions
- **E2E Tests:** Critical user flows

### Test Types

#### 1. Unit Tests (Mockito)
- Test individual methods in isolation
- Mock all dependencies
- Fast execution (< 1ms per test)
- **Target:** All service methods

#### 2. Integration Tests (SpringBootTest)
- Test service with real repository
- Use H2 in-memory database
- Test transaction handling
- **Target:** Repository interactions, caching

#### 3. API Tests (MockMvc)
- Test REST endpoints
- Mock service layer
- **Target:** Controllers with services

#### 4. External API Tests (WireMock)
- Mock external APIs (OpenAI, TheMealDB)
- Test error scenarios
- **Target:** OpenAIService, TheMealDBService

---

## Simple Test Examples

### 1. ConsumerService Tests

```java
@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

    @Mock
    private ConsumerRepository consumerRepository;

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    // ✅ Test successful find
    @Test
    void findByEmail_whenExists_returnsConsumer() {
        // Arrange
        String email = "test@example.com";
        Consumer expected = createTestConsumer(email);
        when(consumerRepository.findByEmail(email)).thenReturn(Optional.of(expected));

        // Act
        Consumer actual = consumerService.findByEmail(email);

        // Assert
        assertThat(actual).isNotNull();
        assertThat(actual.getEmail()).isEqualTo(email);
        verify(consumerRepository).findByEmail(email);
    }

    // ⚠️ Test error case
    @Test
    void findByEmail_whenNotExists_throwsException() {
        // Arrange
        String email = "notfound@example.com";
        when(consumerRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> consumerService.findByEmail(email))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Consumer not found");
    }

    // ✅ Test save
    @Test
    void save_persistsConsumer() {
        // Arrange
        Consumer consumer = createTestConsumer("new@example.com");
        when(consumerRepository.save(any(Consumer.class))).thenReturn(consumer);

        // Act
        Consumer saved = consumerService.save(consumer);

        // Assert
        assertThat(saved).isNotNull();
        verify(consumerRepository).save(consumer);
    }

    // ✅ Test findById with Optional
    @Test
    void findById_whenExists_returnsOptionalWithConsumer() {
        // Arrange
        UUID id = UUID.randomUUID();
        Consumer consumer = createTestConsumer("test@example.com");
        consumer.setId(id);
        when(consumerRepository.findById(id)).thenReturn(Optional.of(consumer));

        // Act
        Optional<Consumer> result = consumerService.findById(id);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    // ✅ Test existsById
    @Test
    void existsById_whenExists_returnsTrue() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(consumerRepository.existsById(id)).thenReturn(true);

        // Act
        boolean exists = consumerService.existsById(id);

        // Assert
        assertThat(exists).isTrue();
    }

    // Helper method
    private Consumer createTestConsumer(String email) {
        Consumer consumer = new Consumer();
        consumer.setId(UUID.randomUUID());
        consumer.setEmail(email);
        consumer.setName("Test User");
        consumer.setDietType("omnivore");
        return consumer;
    }
}
```

### 2. EmailService Tests

```java
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        // Use reflection to set the private field
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
    }

    // ✅ Test successful email
    @Test
    void sendMealPlan_whenEnabled_sendsEmail() throws MessagingException {
        // Arrange
        Consumer consumer = createTestConsumer();
        WeeklyMealPlan plan = createTestMealPlan();
        MimeMessage mimeMessage = mock(MimeMessage.class);
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("weekly-meal-plan"), any(Context.class)))
            .thenReturn("<html>Test</html>");

        // Act
        emailService.sendMealPlan(consumer, plan);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
        verify(templateEngine).process(eq("weekly-meal-plan"), any(Context.class));
    }

    // ⚠️ Test email disabled
    @Test
    void sendMealPlan_whenDisabled_throwsException() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        Consumer consumer = createTestConsumer();
        WeeklyMealPlan plan = createTestMealPlan();

        // Act & Assert
        assertThatThrownBy(() -> emailService.sendMealPlan(consumer, plan))
            .isInstanceOf(MessagingException.class)
            .hasMessageContaining("disabled");
        
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ⚠️ Test email failure
    @Test
    void sendMealPlan_whenMailerFails_throwsRuntimeException() throws MessagingException {
        // Arrange
        Consumer consumer = createTestConsumer();
        WeeklyMealPlan plan = createTestMealPlan();
        MimeMessage mimeMessage = mock(MimeMessage.class);
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>Test</html>");
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThatThrownBy(() -> emailService.sendMealPlan(consumer, plan))
            .isInstanceOf(RuntimeException.class)
            .hasCauseInstanceOf(MessagingException.class);
    }

    private Consumer createTestConsumer() {
        Consumer consumer = new Consumer();
        consumer.setEmail("test@example.com");
        consumer.setName("Test User");
        return consumer;
    }

    private WeeklyMealPlan createTestMealPlan() {
        WeeklyMealPlan plan = new WeeklyMealPlan();
        plan.setWeekStartDate(LocalDate.now());
        return plan;
    }
}
```

### 3. MealCacheService Tests

```java
@ExtendWith(MockitoExtension.class)
class MealCacheServiceTest {

    @Mock
    private MealRepository mealRepository;

    @InjectMocks
    private MealCacheServiceImpl cacheService;

    @Test
    void initCache_loadsAllMealsFromDatabase() {
        // Arrange
        List<Meal> meals = Arrays.asList(
            createMeal("Pasta"),
            createMeal("Pizza"),
            createMeal("Salad")
        );
        when(mealRepository.findAll()).thenReturn(meals);

        // Act
        cacheService.initCache();

        // Assert
        assertThat(cacheService.getMealByName("Pasta")).isNotNull();
        assertThat(cacheService.getMealByName("Pizza")).isNotNull();
        assertThat(cacheService.getMealByName("Salad")).isNotNull();
        verify(mealRepository).findAll();
    }

    @Test
    void getMealByName_whenInCache_returnsMeal() {
        // Arrange
        Meal meal = createMeal("Pasta");
        cacheService.addToCache(meal);

        // Act
        Meal result = cacheService.getMealByName("Pasta");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMealName()).isEqualTo("Pasta");
    }

    @Test
    void getMealByName_whenNotInCache_returnsNull() {
        // Act
        Meal result = cacheService.getMealByName("NotExisting");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void addToCache_storesNewMeal() {
        // Arrange
        Meal meal = createMeal("Burger");

        // Act
        cacheService.addToCache(meal);
        Meal result = cacheService.getMealByName("Burger");

        // Assert
        assertThat(result).isEqualTo(meal);
    }

    private Meal createMeal(String name) {
        Meal meal = new Meal();
        meal.setMealName(name);
        meal.setIngredients(Arrays.asList("ingredient1", "ingredient2"));
        return meal;
    }
}
```

### 4. MealPlanService Tests (Enhanced)

```java
@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock private WeeklyMealPlanRepository weeklyMealPlanRepository;
    @Mock private MealRepository mealRepository;
    @Mock private OpenAIService openAIService;
    @Mock private MealCacheService mealCacheService;
    @Mock private ConsumerService consumerService;
    @Mock private EmailService emailService;

    @InjectMocks
    private MealPlanServiceImpl mealPlanService;

    private Consumer testConsumer;

    @BeforeEach
    void setUp() {
        testConsumer = new Consumer();
        testConsumer.setId(UUID.randomUUID());
        testConsumer.setEmail("test@example.com");
        testConsumer.setDietType("vegetarian");
        testConsumer.setAllergies(Set.of("peanuts"));
        testConsumer.setDislikes(Set.of("mushrooms"));
    }

    // ✅ Happy path - using cached meals
    @Test
    void generateWeeklyMealPlan_withCachedMeals_usesCacheInsteadOfDatabase() {
        // Arrange
        List<Meal> generatedMeals = createMealList(5);
        Meal cachedMeal = generatedMeals.get(0);
        
        when(consumerService.existsById(testConsumer.getId())).thenReturn(true);
        when(openAIService.generateMealPlan(testConsumer, 1)).thenReturn(generatedMeals);
        when(openAIService.getLastGeneratedMessage()).thenReturn("Generated successfully");
        when(mealCacheService.getMealByName(cachedMeal.getMealName())).thenReturn(cachedMeal);
        when(mealCacheService.getMealByName(not(eq(cachedMeal.getMealName())))).thenReturn(null);
        when(mealRepository.save(any(Meal.class))).thenAnswer(i -> i.getArgument(0));
        when(weeklyMealPlanRepository.save(any(WeeklyMealPlan.class)))
            .thenAnswer(i -> i.getArgument(0));

        // Act
        MealPlanResponse result = mealPlanService.generateWeeklyMealPlan(testConsumer);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMealPlan().getMeals()).hasSize(5);
        verify(mealRepository, times(4)).save(any(Meal.class)); // 4 new, 1 cached
        verify(mealCacheService, times(4)).addToCache(any(Meal.class));
    }

    // ⚠️ Consumer doesn't exist
    @Test
    void generateWeeklyMealPlan_whenConsumerDoesNotExist_throwsException() {
        // Arrange
        when(consumerService.existsById(testConsumer.getId())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> mealPlanService.generateWeeklyMealPlan(testConsumer))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Consumer does not exist");
        
        verify(openAIService, never()).generateMealPlan(any(), anyInt());
    }

    // ⚠️ OpenAI returns empty list
    @Test
    void generateWeeklyMealPlan_whenNoMealsGenerated_throwsMealGenerationException() {
        // Arrange
        when(consumerService.existsById(testConsumer.getId())).thenReturn(true);
        when(openAIService.generateMealPlan(testConsumer, 1)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> mealPlanService.generateWeeklyMealPlan(testConsumer))
            .isInstanceOf(MealGenerationException.class)
            .hasMessageContaining("Could not generate meals");
    }

    // ✅ Monthly plan generates 20 meals
    @Test
    void generateMonthlyMealPlan_generates20Meals() {
        // Arrange
        List<Meal> meals = createMealList(20);
        when(consumerService.existsById(testConsumer.getId())).thenReturn(true);
        when(openAIService.generateMealPlan(testConsumer, 4)).thenReturn(meals);
        when(openAIService.getLastGeneratedMessage()).thenReturn("Monthly plan ready");
        when(mealCacheService.getMealByName(any())).thenReturn(null);
        when(mealRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(weeklyMealPlanRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        MealPlanResponse result = mealPlanService.generateMonthlyMealPlan(testConsumer);

        // Assert
        assertThat(result.getMealPlan().getMeals()).hasSize(20);
        verify(openAIService).generateMealPlan(testConsumer, 4);
    }

    // ✅ Get current week plan
    @Test
    void getCurrentWeekPlan_returnsThisWeeksPlan() {
        // Arrange
        WeeklyMealPlan plan = new WeeklyMealPlan();
        LocalDate weekStart = LocalDate.now().with(ChronoField.DAY_OF_WEEK, 1);
        plan.setWeekStartDate(weekStart);
        
        when(weeklyMealPlanRepository.findByConsumerIdAndWeekStartDate(
            testConsumer.getId(), weekStart)).thenReturn(plan);

        // Act
        WeeklyMealPlan result = mealPlanService.getCurrentWeekPlan(testConsumer.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getWeekStartDate()).isEqualTo(weekStart);
    }

    // ✅ Send email with valid plan
    @Test
    void sendMealPlanByEmail_withValidPlan_sendsEmail() throws MessagingException {
        // Arrange
        Long planId = 123L;
        WeeklyMealPlan plan = new WeeklyMealPlan();
        plan.setConsumer(testConsumer);
        
        when(weeklyMealPlanRepository.findById(planId)).thenReturn(Optional.of(plan));

        // Act
        mealPlanService.sendMealPlanByEmail(testConsumer.getId(), planId);

        // Assert
        verify(emailService).sendMealPlan(testConsumer, plan);
    }

    // ⚠️ Send email with wrong consumer
    @Test
    void sendMealPlanByEmail_whenPlanBelongsToDifferentConsumer_throwsException() {
        // Arrange
        Long planId = 123L;
        Consumer otherConsumer = new Consumer();
        otherConsumer.setId(UUID.randomUUID());
        
        WeeklyMealPlan plan = new WeeklyMealPlan();
        plan.setConsumer(otherConsumer);
        
        when(weeklyMealPlanRepository.findById(planId)).thenReturn(Optional.of(plan));

        // Act & Assert
        assertThatThrownBy(() -> 
            mealPlanService.sendMealPlanByEmail(testConsumer.getId(), planId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("does not belong to consumer");
        
        verify(emailService, never()).sendMealPlan(any(), any());
    }

    private List<Meal> createMealList(int count) {
        List<Meal> meals = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Meal meal = new Meal();
            meal.setMealName("Meal " + i);
            meal.setIngredients(Arrays.asList("ingredient1", "ingredient2"));
            meals.add(meal);
        }
        return meals;
    }
}
```

### 5. OpenAIService Tests (with WireMock)

```java
@ExtendWith(MockitoExtension.class)
class OpenAIServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OpenAIServiceImpl openAIService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(openAIService, "openAiApiKey", "test-key");
        ReflectionTestUtils.setField(openAIService, "openAiApiUrl", "https://api.openai.com/v1/chat/completions");
        ReflectionTestUtils.setField(openAIService, "model", "gpt-3.5-turbo");
    }

    @Test
    void generateMealPlan_withValidResponse_parsesMeals() throws IOException {
        // Arrange
        Consumer consumer = createTestConsumer();
        String mockResponse = createMockOpenAIResponse();
        OpenAIResponse openAIResponse = createOpenAIResponse(mockResponse);
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.POST), 
            any(HttpEntity.class), 
            eq(OpenAIResponse.class)))
            .thenReturn(ResponseEntity.ok(openAIResponse));

        // Act
        List<Meal> meals = openAIService.generateMealPlan(consumer, 1);

        // Assert
        assertThat(meals).isNotEmpty();
        assertThat(meals).hasSize(5);
        assertThat(meals.get(0).getMealName()).isNotBlank();
    }

    @Test
    void generateMealPlan_whenOpenAIFails_returnsEmptyList() throws IOException {
        // Arrange
        Consumer consumer = createTestConsumer();
        when(restTemplate.exchange(anyString(), any(), any(), eq(OpenAIResponse.class)))
            .thenThrow(new RestClientException("API Error"));

        // Act
        List<Meal> meals = openAIService.generateMealPlan(consumer, 1);

        // Assert
        assertThat(meals).isEmpty();
    }

    private String createMockOpenAIResponse() {
        return """
            {
                "message": "Here is your meal plan",
                "meals": [
                    {"mealName": "Pasta Primavera", "ingredients": ["pasta", "vegetables"], "imgUrl": ""},
                    {"mealName": "Grilled Salmon", "ingredients": ["salmon", "lemon"], "imgUrl": ""},
                    {"mealName": "Caesar Salad", "ingredients": ["lettuce", "croutons"], "imgUrl": ""},
                    {"mealName": "Chicken Stir Fry", "ingredients": ["chicken", "rice"], "imgUrl": ""},
                    {"mealName": "Vegetable Soup", "ingredients": ["carrots", "celery"], "imgUrl": ""}
                ]
            }
            """;
    }

    private OpenAIResponse createOpenAIResponse(String content) {
        OpenAIResponse response = new OpenAIResponse();
        OpenAIResponse.Choice choice = new OpenAIResponse.Choice();
        OpenAIResponse.Message message = new OpenAIResponse.Message();
        message.setContent(content);
        choice.setMessage(message);
        response.setChoices(Collections.singletonList(choice));
        return response;
    }

    private Consumer createTestConsumer() {
        Consumer consumer = new Consumer();
        consumer.setId(UUID.randomUUID());
        consumer.setDietType("vegetarian");
        consumer.setAllergies(Set.of());
        consumer.setDislikes(Set.of());
        return consumer;
    }
}
```

### 6. TheMealDBService Tests

```java
@ExtendWith(MockitoExtension.class)
class TheMealDBServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TheMealDbServiceImpl mealDbService;

    @Test
    void searchMealsByName_returnsListOfMeals() {
        // Arrange
        String searchName = "Pasta";
        TheMealDbResponse response = createMockResponse(3);
        when(restTemplate.getForObject(anyString(), eq(TheMealDbResponse.class)))
            .thenReturn(response);

        // Act
        List<TheMealDbResponse.MealDto> results = mealDbService.searchMealsByName(searchName);

        // Assert
        assertThat(results).hasSize(3);
        verify(restTemplate).getForObject(contains(searchName), eq(TheMealDbResponse.class));
    }

    @Test
    void searchMealsByName_whenNoResults_returnsEmptyList() {
        // Arrange
        TheMealDbResponse response = new TheMealDbResponse();
        response.setMeals(null);
        when(restTemplate.getForObject(anyString(), eq(TheMealDbResponse.class)))
            .thenReturn(response);

        // Act
        List<TheMealDbResponse.MealDto> results = mealDbService.searchMealsByName("NoResults");

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    void searchMealsByName_whenApiReturnsNull_returnsEmptyList() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(TheMealDbResponse.class)))
            .thenReturn(null);

        // Act
        List<TheMealDbResponse.MealDto> results = mealDbService.searchMealsByName("Test");

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    void getMealById_returnsSpecificMeal() {
        // Arrange
        String mealId = "52772";
        TheMealDbResponse response = createMockResponse(1);
        when(restTemplate.getForObject(anyString(), eq(TheMealDbResponse.class)))
            .thenReturn(response);

        // Act
        TheMealDbResponse.MealDto result = mealDbService.getMealById(mealId);

        // Assert
        assertThat(result).isNotNull();
        verify(restTemplate).getForObject(contains(mealId), eq(TheMealDbResponse.class));
    }

    private TheMealDbResponse createMockResponse(int mealCount) {
        TheMealDbResponse response = new TheMealDbResponse();
        List<TheMealDbResponse.MealDto> meals = new ArrayList<>();
        for (int i = 0; i < mealCount; i++) {
            TheMealDbResponse.MealDto meal = new TheMealDbResponse.MealDto();
            meal.setIdMeal("id" + i);
            meal.setStrMeal("Meal " + i);
            meals.add(meal);
        }
        response.setMeals(meals);
        return response;
    }
}
```

---

## Integration Tests

### 1. MealPlanService Integration Test

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class MealPlanServiceIntegrationTest {

    @Autowired
    private MealPlanService mealPlanService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private WeeklyMealPlanRepository weeklyMealPlanRepository;

    @MockBean
    private OpenAIService openAIService; // Still mock external API

    @MockBean
    private EmailService emailService; // Mock email

    private Consumer testConsumer;

    @BeforeEach
    void setUp() {
        testConsumer = new Consumer();
        testConsumer.setEmail("integration@test.com");
        testConsumer.setName("Integration Test");
        testConsumer.setDietType("vegetarian");
        testConsumer.setAllergies(Set.of());
        testConsumer.setDislikes(Set.of());
        testConsumer = consumerService.save(testConsumer);
    }

    @Test
    void generateWeeklyMealPlan_persistsToDatabase() {
        // Arrange
        List<Meal> mockMeals = createMockMeals(5);
        when(openAIService.generateMealPlan(any(), eq(1))).thenReturn(mockMeals);
        when(openAIService.getLastGeneratedMessage()).thenReturn("Test message");

        // Act
        MealPlanResponse response = mealPlanService.generateWeeklyMealPlan(testConsumer);

        // Assert
        assertThat(response.getMealPlan().getId()).isNotNull();
        
        // Verify persistence
        List<WeeklyMealPlan> plans = weeklyMealPlanRepository
            .findByConsumerIdOrderByWeekStartDateDesc(testConsumer.getId());
        assertThat(plans).hasSize(1);
        assertThat(plans.get(0).getMeals()).hasSize(5);
    }

    @Test
    void getPlanHistory_returnsMultiplePlans() {
        // Arrange - create 3 plans
        List<Meal> mockMeals = createMockMeals(5);
        when(openAIService.generateMealPlan(any(), anyInt())).thenReturn(mockMeals);
        when(openAIService.getLastGeneratedMessage()).thenReturn("Test");
        
        mealPlanService.generateWeeklyMealPlan(testConsumer);
        mealPlanService.generateWeeklyMealPlan(testConsumer);
        mealPlanService.generateWeeklyMealPlan(testConsumer);

        // Act
        List<WeeklyMealPlan> history = mealPlanService.getPlanHistory(testConsumer.getId());

        // Assert
        assertThat(history).hasSize(3);
    }

    private List<Meal> createMockMeals(int count) {
        List<Meal> meals = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Meal meal = new Meal();
            meal.setMealName("Meal " + i);
            meal.setIngredients(Arrays.asList("ing1", "ing2"));
            meals.add(meal);
        }
        return meals;
    }
}
```

---

## TODO List

### Immediate (Sprint 1)

- [ ] **Fix EmailService method name typo** (`sendWeeklyMeanPlan` → `sendWeeklyMealPlan`)
- [ ] **Make MealCacheService thread-safe** (use ConcurrentHashMap or synchronized Set)
- [ ] **Add custom exceptions** (ConsumerNotFoundException, MealGenerationException enhancements)
- [ ] **Replace System.out.println with logger** in MealCacheService
- [ ] **Add constructor injection** to MealPlanService (replace @Autowired fields)
- [ ] **Add input validation** to all service methods (null checks, empty strings)
- [ ] **Write unit tests for EmailService** (currently no tests)
- [ ] **Write unit tests for MealCacheService** (currently no tests)
- [ ] **Write unit tests for TheMealDBService** (currently no tests)
- [ ] **Write unit tests for OpenAIService** (currently no tests)

### Short-term (Sprint 2)

- [ ] **Add retry logic** for OpenAI API calls (use Resilience4j)
- [ ] **Add retry logic** for TheMealDB API calls
- [ ] **Add URL encoding** for TheMealDB search parameters
- [ ] **Add response validation** for external API calls
- [ ] **Add timeout configuration** for RestTemplate
- [ ] **Implement async email sending** (@Async)
- [ ] **Add duplicate plan prevention** (check existing plan for week before generating)
- [ ] **Add integration tests** for MealPlanService
- [ ] **Add metrics/monitoring** (Micrometer + Actuator)
- [ ] **Fix chatgpt-prompt.txt hardcoded path** (use classpath resource)

### Medium-term (Sprint 3)

- [ ] **Implement cache eviction strategy** (use Caffeine cache with TTL)
- [ ] **Add cache size limits** to prevent memory issues
- [ ] **Make meal name lookup case-insensitive**
- [ ] **Add health checks** for external services
- [ ] **Add request/response logging interceptor**
- [ ] **Implement email retry queue** (for failed emails)
- [ ] **Add email templates in multiple languages**
- [ ] **Track OpenAI token usage** and costs
- [ ] **Add rate limiting** for external APIs
- [ ] **Add API response caching** for TheMealDB (reduce API calls)

### Long-term (Sprint 4+)

- [ ] **Add comprehensive E2E tests**
- [ ] **Implement feature toggles** (LaunchDarkly or similar)
- [ ] **Add performance monitoring dashboard** (Grafana)
- [ ] **Implement circuit breaker** for external APIs
- [ ] **Add API versioning strategy**
- [ ] **Create service health dashboard**
- [ ] **Implement message queue** for async operations (RabbitMQ/Kafka)
- [ ] **Add database query optimization** (add indexes, analyze slow queries)
- [ ] **Implement distributed caching** (Redis) for multi-instance deployments
- [ ] **Add A/B testing framework** for meal recommendations

---

## How to Run Tests

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Class
```bash
./mvnw test -Dtest=ConsumerServiceTest
```

### Run with Coverage Report
```bash
./mvnw clean test jacoco:report
# View report at: target/site/jacoco/index.html
```

### Run Integration Tests Only
```bash
./mvnw test -Dtest=*IntegrationTest
```

### Run with Specific Profile
```bash
./mvnw test -Dspring.profiles.active=test
```

---

## Test Naming Convention

```
methodName_stateUnderTest_expectedBehavior
```

Examples:
- `findByEmail_whenExists_returnsConsumer`
- `findByEmail_whenNotExists_throwsException`
- `generateMealPlan_withInvalidConsumer_throwsIllegalArgumentException`
- `sendEmail_whenDisabled_logsWarningAndSkips`

---

## Assertions Library (AssertJ)

Use AssertJ for more readable assertions:

```java
// Instead of JUnit assertions
assertEquals("test", actual);

// Use AssertJ
assertThat(actual).isEqualTo("test");
assertThat(list).hasSize(3).contains("item1");
assertThat(optional).isPresent().contains(expected);
```

Add to pom.xml if not present:
```xml
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Summary

### Current Coverage Status
- ✅ ConsumerService: 2 tests
- ✅ MealPlanService: 4 tests  
- ❌ EmailService: 0 tests
- ❌ MealCacheService: 0 tests
- ❌ TheMealDBService: 0 tests
- ❌ OpenAIService: 0 tests

### Estimated Test Count Needed
- ConsumerService: 8 tests (6 more)
- MealPlanService: 12 tests (8 more)
- EmailService: 6 tests (6 new)
- MealCacheService: 5 tests (5 new)
- TheMealDBService: 6 tests (6 new)
- OpenAIService: 8 tests (8 new)

**Total: ~45 tests needed for good coverage**

### Priority Order
1. Fix critical bugs (thread safety, naming)
2. Add missing unit tests
3. Add input validation
4. Implement retry logic
5. Add integration tests
6. Add monitoring/metrics
7. Performance optimization

---

**End of Analysis**
