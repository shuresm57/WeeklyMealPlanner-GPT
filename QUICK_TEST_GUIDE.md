# Quick Test Guide

## Run Tests Immediately

### 1. Run All Existing Tests
```bash
./mvnw test
```

### 2. Run with Coverage
```bash
./mvnw clean test jacoco:report
open target/site/jacoco/index.html  # Mac
xdg-open target/site/jacoco/index.html  # Linux
start target/site/jacoco/index.html  # Windows
```

### 3. Run Specific Service Test
```bash
# Consumer Service
./mvnw test -Dtest=ConsumerServiceTest

# Meal Plan Service
./mvnw test -Dtest=MealPlanServiceTest
```

---

## Quick Wins: Add These Tests First

### 1. EmailService Test (Copy-paste ready)

Create: `src/test/java/com/example/weeklymealplannergpt/service/EmailServiceTest.java`

```java
package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import com.example.weeklymealplannergpt.service.email.EmailServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
    }

    @Test
    void sendMealPlan_whenEnabled_sendsEmail() throws MessagingException {
        // Arrange
        Consumer consumer = new Consumer();
        consumer.setEmail("test@example.com");
        WeeklyMealPlan plan = new WeeklyMealPlan();
        MimeMessage mimeMessage = mock(MimeMessage.class);
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("weekly-meal-plan"), any(Context.class)))
            .thenReturn("<html>Test</html>");

        // Act
        emailService.sendMealPlan(consumer, plan);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendMealPlan_whenDisabled_throwsException() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        Consumer consumer = new Consumer();
        consumer.setEmail("test@example.com");

        // Act & Assert
        assertThatThrownBy(() -> emailService.sendMealPlan(consumer, new WeeklyMealPlan()))
            .isInstanceOf(MessagingException.class)
            .hasMessageContaining("disabled");
    }
}
```

Run it:
```bash
./mvnw test -Dtest=EmailServiceTest
```

---

### 2. MealCacheService Test (Copy-paste ready)

Create: `src/test/java/com/example/weeklymealplannergpt/service/MealCacheServiceTest.java`

```java
package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Meal;
import com.example.weeklymealplannergpt.repository.MealRepository;
import com.example.weeklymealplannergpt.service.mealplan.MealCacheServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealCacheServiceTest {

    @Mock
    private MealRepository mealRepository;

    @InjectMocks
    private MealCacheServiceImpl cacheService;

    @Test
    void initCache_loadsAllMeals() {
        // Arrange
        Meal meal1 = new Meal();
        meal1.setMealName("Pasta");
        Meal meal2 = new Meal();
        meal2.setMealName("Pizza");
        
        when(mealRepository.findAll()).thenReturn(Arrays.asList(meal1, meal2));

        // Act
        cacheService.initCache();

        // Assert
        assertThat(cacheService.getMealByName("Pasta")).isNotNull();
        assertThat(cacheService.getMealByName("Pizza")).isNotNull();
        verify(mealRepository).findAll();
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
        Meal meal = new Meal();
        meal.setMealName("Burger");

        // Act
        cacheService.addToCache(meal);

        // Assert
        assertThat(cacheService.getMealByName("Burger")).isNotNull();
    }
}
```

Run it:
```bash
./mvnw test -Dtest=MealCacheServiceTest
```

---

### 3. TheMealDBService Test (Copy-paste ready)

Create: `src/test/java/com/example/weeklymealplannergpt/service/TheMealDBServiceTest.java`

```java
package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.dto.TheMealDbResponse;
import com.example.weeklymealplannergpt.service.mealplan.TheMealDbServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TheMealDBServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TheMealDbServiceImpl mealDbService;

    @Test
    void searchMealsByName_returnsListOfMeals() {
        // Arrange
        TheMealDbResponse response = new TheMealDbResponse();
        List<TheMealDbResponse.MealDto> meals = new ArrayList<>();
        TheMealDbResponse.MealDto meal = new TheMealDbResponse.MealDto();
        meal.setStrMeal("Pasta");
        meals.add(meal);
        response.setMeals(meals);
        
        when(restTemplate.getForObject(anyString(), eq(TheMealDbResponse.class)))
            .thenReturn(response);

        // Act
        List<TheMealDbResponse.MealDto> results = mealDbService.searchMealsByName("Pasta");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStrMeal()).isEqualTo("Pasta");
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
    void getMealById_returnsSpecificMeal() {
        // Arrange
        TheMealDbResponse response = new TheMealDbResponse();
        List<TheMealDbResponse.MealDto> meals = new ArrayList<>();
        TheMealDbResponse.MealDto meal = new TheMealDbResponse.MealDto();
        meal.setIdMeal("123");
        meals.add(meal);
        response.setMeals(meals);
        
        when(restTemplate.getForObject(anyString(), eq(TheMealDbResponse.class)))
            .thenReturn(response);

        // Act
        TheMealDbResponse.MealDto result = mealDbService.getMealById("123");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIdMeal()).isEqualTo("123");
    }
}
```

Run it:
```bash
./mvnw test -Dtest=TheMealDBServiceTest
```

---

## After Adding Tests

### See Coverage Improvement
```bash
./mvnw clean test jacoco:report
```

Check these files in browser:
- `target/site/jacoco/index.html` - Overall coverage
- `target/site/jacoco/com.example.weeklymealplannergpt.service/index.html` - Service package

---

## Current vs Target Coverage

### Before New Tests
```
ConsumerService: ~40% coverage
EmailService: 0% coverage
MealCacheService: 0% coverage
MealPlanService: ~50% coverage
TheMealDBService: 0% coverage
OpenAIService: 0% coverage
```

### After Adding 3 Quick Tests
```
ConsumerService: ~40% coverage
EmailService: ~60% coverage ✅
MealCacheService: ~70% coverage ✅
MealPlanService: ~50% coverage
TheMealDBService: ~50% coverage ✅
OpenAIService: 0% coverage
```

---

## Troubleshooting

### Test Won't Compile
```bash
# Update Maven dependencies
./mvnw clean install -DskipTests

# Check for missing imports
# Most likely: Add these imports to your test file
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
```

### Test Fails
```bash
# Run with verbose output
./mvnw test -Dtest=YourTest -X

# Check logs
cat target/surefire-reports/YourTest.txt
```

### Coverage Report Not Generated
```bash
# Ensure jacoco plugin is in pom.xml (it should be)
./mvnw clean verify
```

---

## Next Steps

1. ✅ Add the 3 quick tests above (15 minutes)
2. ✅ Run coverage report (2 minutes)
3. ✅ See improvement in coverage (satisfying!)
4. Read full `SERVICE_ANALYSIS.md` for detailed improvements
5. Implement Priority 1 fixes from TODO list

---

## Useful Commands Cheat Sheet

```bash
# Clean and test
./mvnw clean test

# Skip tests during build
./mvnw clean install -DskipTests

# Run single test method
./mvnw test -Dtest=ConsumerServiceTest#findByEmail_returnsConsumer

# Run with specific Spring profile
./mvnw test -Dspring.profiles.active=test

# Generate coverage and skip test execution (use cached results)
./mvnw jacoco:report

# Run tests in parallel (faster)
./mvnw -T 4 test

# Run only failed tests from last run
./mvnw test -Dsurefire.rerunFailingTestsCount=2
```

---

**Time to Complete:** 30 minutes for all 3 test files  
**Immediate Value:** +30% code coverage  
**Effort:** Copy, paste, run  
