# Test Scripts Conversion Plan

This document outlines the plan for moving standalone test scripts from the root directory to appropriate test directories and converting them to proper unit tests.

## 1. Standalone Test Scripts

The following standalone test scripts have been identified in the root directory:

| File | Target Directory | Test Class Name |
|------|-----------------|-----------------|
| test_authentication.kt | member-management/src/jvmTest/kotlin/at/mocode/members/test/ | AuthenticationTest |
| test_authentication_authorization.kt | api-gateway/src/jvmTest/kotlin/at/mocode/gateway/test/ | AuthenticationAuthorizationTest |
| test_validation.kt | shared-kernel/src/jvmTest/kotlin/at/mocode/validation/test/ | ValidationTest |
| database-integration-test.kt | shared-kernel/src/jvmTest/kotlin/at/mocode/shared/database/test/ | DatabaseIntegrationTest |

## 2. Conversion Guidelines

When converting the standalone scripts to proper unit tests, the following guidelines should be followed:

1. **Add proper test annotations**:
   - Use `@Test` for test methods
   - Use `@BeforeTest` for setup methods
   - Use `@AfterTest` for teardown methods

2. **Organize tests into test classes**:
   - Create a test class with a descriptive name
   - Group related tests into methods within the class
   - Use descriptive method names that explain what is being tested

3. **Use proper assertions**:
   - Replace `println` statements with proper assertions
   - Use `kotlin.test.assertEquals`, `kotlin.test.assertTrue`, etc.
   - Add meaningful error messages to assertions

4. **Set up test dependencies properly**:
   - Initialize dependencies in setup methods
   - Use mocks or test doubles where appropriate
   - Clean up resources in teardown methods

5. **Add proper package declarations**:
   - Use the package that corresponds to the target directory

## 3. Implementation Steps

### 3.1 test_authentication.kt → AuthenticationTest

1. Create the target directory if it doesn't exist
2. Create a new file AuthenticationTest.kt with the following structure:
   ```kotlin
   package at.mocode.members.test

   import at.mocode.members.domain.service.UserAuthorizationService
   import at.mocode.members.domain.service.JwtService
   import at.mocode.members.domain.service.AuthenticationService
   import at.mocode.members.infrastructure.repository.*
   import kotlin.test.*

   class AuthenticationTest {
       private lateinit var userRepository: UserRepositoryImpl
       private lateinit var userAuthorizationService: UserAuthorizationService
       private lateinit var jwtService: JwtService

       @BeforeTest
       fun setup() {
           userRepository = UserRepositoryImpl()
           val personRolleRepository = PersonRolleRepositoryImpl()
           val rolleRepository = RolleRepositoryImpl()
           val rolleBerechtigungRepository = RolleBerechtigungRepositoryImpl()
           val berechtigungRepository = BerechtigungRepositoryImpl()

           userAuthorizationService = UserAuthorizationService(
               userRepository,
               personRolleRepository,
               rolleRepository,
               rolleBerechtigungRepository,
               berechtigungRepository
           )

           jwtService = JwtService(userAuthorizationService)
       }

       @Test
       fun testUserAuthInfo() {
           val testUsers = userRepository.getAllUsers()
           assertNotEquals(0, testUsers.size, "Should have at least one test user")

           if (testUsers.isNotEmpty()) {
               val testUser = testUsers.first()
               val authInfo = userAuthorizationService.getUserAuthInfo(testUser.userId)
               assertNotNull(authInfo, "Auth info should not be null")
           }
       }

       @Test
       fun testJwtTokenGeneration() {
           val testUsers = userRepository.getAllUsers()
           if (testUsers.isNotEmpty()) {
               val testUser = testUsers.first()
               val tokenInfo = jwtService.generateToken(testUser)
               assertNotNull(tokenInfo.token, "Token should not be null")
               assertNotNull(tokenInfo.expiresAt, "Expiration date should not be null")
           }
       }

       @Test
       fun testTokenValidation() {
           val testUsers = userRepository.getAllUsers()
           if (testUsers.isNotEmpty()) {
               val testUser = testUsers.first()
               val tokenInfo = jwtService.generateToken(testUser)
               val payload = jwtService.validateToken(tokenInfo.token)
               assertNotNull(payload, "Payload should not be null")
           }
       }
   }
   ```

### 3.2 test_authentication_authorization.kt → AuthenticationAuthorizationTest

1. Create the target directory if it doesn't exist
2. Create a new file AuthenticationAuthorizationTest.kt with a similar structure to AuthenticationTest.kt
3. Convert the main function to test methods with proper assertions
4. Add setup and teardown methods as needed

### 3.3 test_validation.kt → ValidationTest

1. Create the target directory if it doesn't exist
2. Create a new file ValidationTest.kt with a similar structure
3. Convert the main function to test methods with proper assertions
4. Add setup and teardown methods as needed

### 3.4 database-integration-test.kt → DatabaseIntegrationTest

1. Create the target directory if it doesn't exist
2. Create a new file DatabaseIntegrationTest.kt with the following structure:
   ```kotlin
   package at.mocode.shared.database.test

   import at.mocode.gateway.config.configureDatabase
   import at.mocode.masterdata.domain.model.LandDefinition
   import at.mocode.masterdata.infrastructure.repository.LandRepositoryImpl
   import at.mocode.events.domain.model.Veranstaltung
   import at.mocode.events.infrastructure.repository.VeranstaltungRepositoryImpl
   import at.mocode.enums.SparteE
   import com.benasher44.uuid.uuid4
   import io.ktor.server.application.*
   import io.ktor.server.engine.*
   import io.ktor.server.netty.*
   import kotlinx.coroutines.runBlocking
   import kotlinx.datetime.Clock
   import kotlinx.datetime.LocalDate
   import org.jetbrains.exposed.sql.transactions.transaction
   import kotlin.test.*

   class DatabaseIntegrationTest {
       private lateinit var application: Application
       private lateinit var landRepository: LandRepositoryImpl
       private lateinit var eventRepository: VeranstaltungRepositoryImpl

       @BeforeTest
       fun setup() {
           val environment = applicationEngineEnvironment {
               config = MapApplicationConfig(
                   "database.url" to "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                   "database.user" to "sa",
                   "database.password" to ""
               )
           }

           application = Application(environment)
           application.configureDatabase()

           landRepository = LandRepositoryImpl()
           eventRepository = VeranstaltungRepositoryImpl()
       }

       @Test
       fun testMasterDataRepository() = runBlocking {
           transaction {
               // Create a test country
               val testCountry = LandDefinition(
                   landId = uuid4(),
                   isoAlpha2Code = "TS",
                   isoAlpha3Code = "TST",
                   isoNumerischerCode = "999",
                   nameDeutsch = "Testland",
                   nameEnglisch = "Testland",
                   wappenUrl = null,
                   istEuMitglied = false,
                   istEwrMitglied = false,
                   istAktiv = true,
                   sortierReihenfolge = 999,
                   createdAt = Clock.System.now(),
                   updatedAt = Clock.System.now()
               )

               // Save the test country
               val savedCountry = landRepository.save(testCountry)
               assertEquals("Testland", savedCountry.nameDeutsch, "Country name should match")

               // Retrieve the test country
               val retrievedCountry = landRepository.findByIsoAlpha2Code("TS")
               assertNotNull(retrievedCountry, "Retrieved country should not be null")
               assertEquals("Testland", retrievedCountry.nameDeutsch, "Retrieved country name should match")

               // Count active countries
               val activeCount = landRepository.countActive()
               assertTrue(activeCount > 0, "Should have at least one active country")

               // Clean up
               landRepository.delete(testCountry.landId)
           }
       }

       @Test
       fun testEventManagementRepository() = runBlocking {
           transaction {
               // Create a test event
               val testEvent = Veranstaltung(
                   name = "Test Veranstaltung",
                   beschreibung = "Eine Test-Veranstaltung für die Integration",
                   startDatum = LocalDate(2024, 8, 15),
                   endDatum = LocalDate(2024, 8, 17),
                   ort = "Test-Ort",
                   veranstalterVereinId = uuid4(),
                   sparten = listOf(SparteE.DRESSUR, SparteE.SPRINGEN),
                   istAktiv = true,
                   istOeffentlich = true,
                   maxTeilnehmer = 100,
                   anmeldeschluss = LocalDate(2024, 8, 1)
               )

               // Save the test event
               val savedEvent = eventRepository.save(testEvent)
               assertEquals("Test Veranstaltung", savedEvent.name, "Event name should match")

               // Retrieve the test event
               val retrievedEvent = eventRepository.findById(savedEvent.veranstaltungId)
               assertNotNull(retrievedEvent, "Retrieved event should not be null")
               assertEquals("Test Veranstaltung", retrievedEvent.name, "Retrieved event name should match")
               assertEquals(3, retrievedEvent.getDurationInDays(), "Event duration should be 3 days")
               assertTrue(retrievedEvent.isMultiDay(), "Event should be multi-day")

               // Test search functionality
               val searchResults = eventRepository.findByName("Test", 10)
               assertTrue(searchResults.isNotEmpty(), "Search should return at least one result")

               // Test public events
               val publicEvents = eventRepository.findPublicEvents(true)
               assertTrue(publicEvents.isNotEmpty(), "Should have at least one public event")

               // Count active events
               val activeEventCount = eventRepository.countActive()
               assertTrue(activeEventCount > 0, "Should have at least one active event")

               // Clean up event
               eventRepository.delete(savedEvent.veranstaltungId)
           }
       }
   }

   /**
    * Simple map-based application config for testing
    */
   class MapApplicationConfig(private val map: Map<String, String>) : ApplicationConfig {
       constructor(vararg pairs: Pair<String, String>) : this(pairs.toMap())

       override fun property(path: String): ApplicationConfigValue {
           return MapApplicationConfigValue(map[path])
       }

       override fun propertyOrNull(path: String): ApplicationConfigValue? {
           return map[path]?.let { MapApplicationConfigValue(it) }
       }

       override fun config(path: String): ApplicationConfig {
           return this
       }

       override fun configList(path: String): List<ApplicationConfig> {
           return emptyList()
       }

       override fun keys(): Set<String> {
           return map.keys
       }
   }

   class MapApplicationConfigValue(private val value: String?) : ApplicationConfigValue {
       override fun getString(): String = value ?: ""
       override fun getList(): List<String> = value?.split(",") ?: emptyList()
   }
   ```

## 4. Verification

After converting each test script:

1. Build the project to ensure there are no compilation errors
2. Run the tests to ensure they pass
3. Verify that the tests provide the same coverage as the original scripts
4. Remove the original scripts from the root directory

## 5. Documentation Update

Update the project documentation to reflect the new test organization:

1. Update README.md if it references the standalone test scripts
2. Update any other documentation that mentions the test scripts
