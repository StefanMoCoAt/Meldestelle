# SQLDelight Integration in Compose Multiplatform

This guide shows how to integrate SQLDelight in a Compose Multiplatform project with Koin dependency injection.

## Step 1: Add Dependencies

Add below dependencies In `gradle/libs.versions.toml`:

```toml
[versions]
sqldelight = "2.0.1"
koin = "3.5.3"

[libraries]
sqldelight-driver-sqlite = { module = "app.cash.sqldelight:sqlite-driver", version.ref = "sqldelight" }
sqldelight-driver-android = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-driver-native = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }

[plugins]
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
```

In `build.gradle.kts` (project level):

```kotlin
plugins {
  alias(libs.plugins.sqldelight) apply false
}
```

In `shared/build.gradle.kts`:

```kotlin
plugins {
  alias(libs.plugins.sqldelight)
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.koin.core)
      implementation(libs.sqldelight.driver.sqlite)
    }

    androidMain.dependencies {
      implementation(libs.koin.android)
      implementation(libs.sqldelight.driver.android)
    }

    iosMain.dependencies {
      implementation(libs.sqldelight.driver.native)
    }

    desktopMain.dependencies {
      implementation(libs.sqldelight.driver.sqlite)
    }
  }
}

sqldelight {
  databases {
    create("AppDatabase") {
      packageName.set("com.example.database")
    }
  }
}

```

##Step 2: Create SQL Schema

**Create directory structure:**

`shared/src/commonMain/sqldelight/com/example/database/`

Create `User.sq` file:

```sql
CREATE TABLE User
(
  id       INTEGER PRIMARY KEY AUTOINCREMENT,
  name     TEXT NOT NULL,
  imageUrl TEXT
);

-- Insert a new user
insertUser
:
INSERT INTO User(name, imageUrl)
VALUES (?, ?);

-- Get all users
getAllUsers
:
SELECT *
FROM User;

-- Get user by ID
getUserById
:
SELECT *
FROM User
WHERE id = ?;

-- Update user
updateUser
:
UPDATE User
SET name     = ?,
    imageUrl = ?
WHERE id = ?;

-- Delete user
deleteUser
:
DELETE
FROM User
WHERE id = ?;

-- Delete all users
deleteAllUsers
:
DELETE
FROM User;
```

## Step 3: Create Database Driver Interface

In `shared/src/commonMain/kotlin/database/DatabaseDriverFactory.kt`:

```kotlin
package com.example.database

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
  fun createDriver(): SqlDriver
}
```

## Step 4: Platform-Specific Implementations

### Android —

`shared/src/androidMain/kotlin/database/DatabaseDriverFactory.android.kt`:

```kotlin
package com.example.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
  actual fun createDriver(): SqlDriver {
    return AndroidSqliteDriver(
      schema = AppDatabase.Schema,
      context = context,
      name = "app.db"
    )
  }
}
```

### iOS —

`shared/src/iosMain/kotlin/database/DatabaseDriverFactory.ios.kt`:

```kotlin
package com.example.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
  actual fun createDriver(): SqlDriver {
    return NativeSqliteDriver(
      schema = AppDatabase.Schema,
      name = "app.db"
    )
  }
}

```

### Desktop —

`shared/src/desktopMain/kotlin/database/DatabaseDriverFactory.desktop.kt`:

```kotlin
package com.example.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual class DatabaseDriverFactory {
  actual fun createDriver(): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    AppDatabase.Schema.create(driver)
    return driver
  }
}

```

## Step 5: Create Repository

In `shared/src/commonMain/kotlin/repository/UserRepository.kt`:

```kotlin
package com.example.repository

import com.example.database.AppDatabase
import com.example.database.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class UserRepository(private val database: AppDatabase) {

  private val queries = database.userQueries

  suspend fun insertUser(name: String, imageUrl: String?) = withContext(Dispatchers.IO) {
    queries.insertUser(name, imageUrl)
  }

  suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
    queries.getAllUsers().executeAsList()
  }

  suspend fun getUserById(id: Long): User? = withContext(Dispatchers.IO) {
    queries.getUserById(id).executeAsOneOrNull()
  }

  suspend fun updateUser(id: Long, name: String, imageUrl: String?) = withContext(Dispatchers.IO) {
    queries.updateUser(name, imageUrl, id)
  }

  suspend fun deleteUser(id: Long) = withContext(Dispatchers.IO) {
    queries.deleteUser(id)
  }

  suspend fun deleteAllUsers() = withContext(Dispatchers.IO) {
    queries.deleteAllUsers()
  }
}

```

## Step 6: Setup Koin Modules

In `shared/src/commonMain/kotlin/di/DatabaseModule.kt`:

```kotlin
package com.example.di

import com.example.database.AppDatabase
import com.example.database.DatabaseDriverFactory
import com.example.repository.UserRepository
import org.koin.dsl.module

val databaseModule = module {
  single { DatabaseDriverFactory() }
  single { AppDatabase(get<DatabaseDriverFactory>().createDriver()) }
  single { UserRepository(get()) }
}

```

### Platform-specific modules

### Android —

`shared/src/androidMain/kotlin/di/PlatformModule.android.kt`:

```kotlin
package com.example.di

import com.example.database.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
  single { DatabaseDriverFactory(androidContext()) }
}

```

### iOS —

`shared/src/iosMain/kotlin/di/PlatformModule.ios.kt`:

```kotlin
package com.example.di

import com.example.database.DatabaseDriverFactory
import org.koin.dsl.module

actual val platformModule = module {
  single { DatabaseDriverFactory() }
}

```

### Desktop —

`shared/src/desktopMain/kotlin/di/PlatformModule.desktop.kt`:

```kotlin
package com.example.di

import com.example.database.DatabaseDriverFactory
import org.koin.dsl.module

actual val platformModule = module {
  single { DatabaseDriverFactory() }
}

```

### Common module declaration —

`shared/src/commonMain/kotlin/di/PlatformModule.kt`:

```kotlin
package com.example.di

import org.koin.core.module.Module

expect val platformModule: Module

```

## Step 7: Initialize Koin

In `shared/src/commonMain/kotlin/di/KoinInit.kt`:

```kotlin
package com.example.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
  appDeclaration()
  modules(
    platformModule,
    databaseModule
  )
}

```

## Step 8: Platform Initialization

### Android —

In `MainActivity.kt`:

```kotlin
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    initKoin {
      androidContext(this@MainActivity)
    }

    setContent {
      App()
    }
  }
}

```

### iOS —

In `iosApp/iosApp/iOSApp.swift`:

```kotlin
import SwiftUI
import shared

@main
struct iOSApp : App {

  init() {
    KoinInitKt.doInitKoin()
  }

  var body: some Scene {
  WindowGroup {
    ContentView()
  }
}
}

```

### Desktop —

In `desktopApp/src/jvmMain/kotlin/main.kt`:

```kotlin
fun main() {
  initKoin()

  application {
    Window(onCloseRequest = ::exitApplication) {
      App()
    }
  }
}

```

## Step 9: Use in Compose

### Create VieModel —

In `shared/src/commonMain/kotlin/viewmodel/UserViewModel.kt`:

```kotlin
package com.example.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.User
import com.example.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

  var users by mutableStateOf<List<User>>(emptyList())
    private set

  var isLoading by mutableStateOf(false)
    private set

  init {
    loadUsers()
  }

  fun loadUsers() {
    viewModelScope.launch {
      isLoading = true
      users = userRepository.getAllUsers()
      isLoading = false
    }
  }

  fun addUser(name: String, imageUrl: String?) {
    viewModelScope.launch {
      userRepository.insertUser(name, imageUrl)
      loadUsers()
    }
  }

  fun deleteUser(id: Long) {
    viewModelScope.launch {
      userRepository.deleteUser(id)
      loadUsers()
    }
  }
}

```

Use in Compose Screen:

```kotlin
@Composable
fun UserScreen() {
  val userViewModel: UserViewModel = koinInject()

  LazyColumn {
    items(userViewModel.users) { user ->
      UserItem(
        user = user,
        onDelete = { userViewModel.deleteUser(user.id) }
      )
    }
  }
}

@Composable
fun UserItem(user: User, onDelete: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = user.name,
      modifier = Modifier.weight(1f)
    )

    Button(onClick = onDelete) {
      Text("Delete")
    }
  }
}

```

### That’s It!

You now have SQLDelight fully integrated in your Compose Multiplatform project with:

- Database working on Android, iOS, and Desktop
- Koin dependency injection setup
- Repository pattern for clean architecture
- Ready-to-use User table with CRUD operations

The database will automatically handle platform-specific implementations while sharing the same business logic across
all platforms.
