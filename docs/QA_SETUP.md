# Quality Assurance Setup

This project uses GitHub Actions for automated quality checks.

## What Runs Automatically

On every push/PR to `main` or `develop`:

1. **Tests** - All unit tests via JUnit
2. **Coverage** - Code coverage report with JaCoCo  
3. **Quality** - Checkstyle, SpotBugs, and PMD analysis

## Local Commands

```bash
# Run tests
./mvnw test

# Generate coverage report
./mvnw clean test jacoco:report
open target/site/jacoco/index.html

# Run quality checks
./mvnw checkstyle:check spotbugs:check pmd:check
```

## Setup

Add these plugins to your `pom.xml` before `</project>`:

```xml
<build>
    <plugins>
        <!-- JaCoCo -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- Checkstyle -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-checkstyle-plugin</artifactId>
            <version>3.3.1</version>
            <configuration>
                <configLocation>checkstyle.xml</configLocation>
                <failsOnError>false</failsOnError>
            </configuration>
        </plugin>

        <!-- SpotBugs -->
        <plugin>
            <groupId>com.github.spotbugs</groupId>
            <artifactId>spotbugs-maven-plugin</artifactId>
            <version>4.8.2.0</version>
            <configuration>
                <effort>Max</effort>
                <threshold>Low</threshold>
                <failOnError>false</failOnError>
            </configuration>
        </plugin>

        <!-- PMD -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-pmd-plugin</artifactId>
            <version>3.21.2</version>
            <configuration>
                <failOnViolation>false</failOnViolation>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Reports

After running locally, find reports at:
- Coverage: `target/site/jacoco/index.html`
- Checkstyle: `target/checkstyle-result.xml`
- SpotBugs: `target/spotbugsXml.xml`
- PMD: `target/pmd.xml`

On GitHub, download from: **Actions** → **Workflow Run** → **Artifacts**
