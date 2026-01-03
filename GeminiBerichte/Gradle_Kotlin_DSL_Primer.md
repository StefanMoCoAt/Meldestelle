# Gradle Kotlin DSL Primer

[Gradle](https://docs.gradle.org "Gradle Docs")

User Manual

*   9.2.1 Open Community Menu
    
    [9.2.1](/9.2.1)
    
    [8.5](/8.5)
    
    [7.6.2](/7.6.2)
    
    [6.9.4](/6.9.4)
    
    [All versions](https://gradle.org/releases/)
    

*   Theme
*   Community
    
    [Community Home](https://gradle.org/)
    
    [Community Forums](https://discuss.gradle.org/)
    
    [Community Plugins](https://plugins.gradle.org)
    
*   [DPE University](https://dpeuniversity.gradle.com/)
*   [Events](https://gradle.org/training/)
*   News
    
    [Newsletter](https://newsletter.gradle.org)
    
    [Blog](https://blog.gradle.org)
    
    [Twitter](https://twitter.com/gradle)
    
*   [Develocity](https://gradle.com/develocity)
*   [Github](https://github.com/gradle/gradle "Gradle on GitHub")

Search

### [Gradle User Manual](../userguide/userguide.html)

*   [Getting Started](../userguide/getting_started.html)

### Releases

*   [All Releases](https://gradle.org/releases/)
*   [Release Notes](../release-notes.html)
*   [Installing Gradle](../userguide/installation.html)
*   [Upgrading Gradle](#upgrading-gradle)
    *   [Within versions 9.x.y](../userguide/upgrading_version_9.html)
    *   [To version 9.0.0](../userguide/upgrading_major_version_9.html)
    *   [Within versions 8.x](../userguide/upgrading_version_8.html)
    *   [From version 7.x to 8.0](../userguide/upgrading_version_7.html)
    *   [From version 6.x to 7.0](../userguide/upgrading_version_6.html)
    *   [From version 5.x to 6.0](../userguide/upgrading_version_5.html)
    *   [From version 4.x to 5.0](../userguide/upgrading_version_4.html)
*   [Migrating to Gradle](#migrating-to-gradle)
    *   [from Maven](../userguide/migrating_from_maven.html)
    *   [from Ant](../userguide/migrating_from_ant.html)
*   [Compatibility Notes](../userguide/compatibility.html)
*   [Gradle's Feature Lifecycle](../userguide/feature_lifecycle.html)

### Gradle Fundamentals

*   [Running Gradle Builds](#running-introduction)
    *   [1\. Core Concepts](../userguide/gradle_basics.html)
    *   [2\. Wrapper Basics](../userguide/gradle_wrapper_basics.html)
    *   [3\. CLI Basics](../userguide/command_line_interface_basics.html)
    *   [4\. Settings File Basics](../userguide/settings_file_basics.html)
    *   [5\. Build File Basics](../userguide/build_file_basics.html)
    *   [6\. Dependencies Basics](../userguide/dependency_management_basics.html)
    *   [7\. Tasks Basics](../userguide/task_basics.html)
    *   [8\. Caching Basics](../userguide/gradle_optimizations.html)
    *   [9\. Plugins Basics](../userguide/plugin_basics.html)
    *   [10\. Build Scan Basics](../userguide/build_scans.html)
*   [Authoring Gradle Builds](#beyond-the-basics)
    *   [1\. Anatomy of a Gradle Build](../userguide/gradle_directories_intermediate.html)
    *   [2\. Structuring Multi-Project Builds](../userguide/multi_project_builds_intermediate.html)
    *   [3\. Gradle Build Lifecycle](../userguide/build_lifecycle_intermediate.html)
    *   [4\. Writing Build Scripts](../userguide/writing_build_scripts_intermediate.html)
    *   [5\. Gradle Managed Types](../userguide/gradle_managed_types_intermediate.html)
    *   [6\. Declaring Dependencies](../userguide/dependencies_intermediate.html)
    *   [7\. Creating and Registering Tasks](../userguide/writing_tasks_intermediate.html)
    *   [8\. Working with Plugins](../userguide/plugins_intermediate.html)
*   [Developing Gradle Plugins](#deep-dive)
    *   [1\. Plugin Introduction](../userguide/plugin_introduction_advanced.html)
    *   [2\. Pre-Compiled Script Plugins](../userguide/pre_compiled_script_plugin_advanced.html)
    *   [3\. Binary Plugins](../userguide/binary_plugin_advanced.html)
    *   [4\. Developing Binary Plugins](../userguide/developing_binary_plugin_advanced.html)
    *   [5\. Testing Binary Plugins](../userguide/testing_binary_plugin_advanced.html)
    *   [6\. Publishing Binary Plugins](../userguide/publishing_binary_plugin_advanced.html)

### Gradle Tutorials

*   Beginner Tutorial
    *   [1\. Initializing the Project](../userguide/part1_gradle_init.html)
    *   [2\. Running Tasks](../userguide/part2_gradle_tasks.html)
    *   [3\. Understanding Dependencies](../userguide/part3_gradle_dep_man.html)
    *   [4\. Applying Plugins](../userguide/part4_gradle_plugins.html)
    *   [5\. Exploring Incremental Builds](../userguide/part5_gradle_inc_builds.html)
    *   [6\. Enabling the Build Cache](../userguide/part6_gradle_caching.html)
*   Intermediate Tutorial
    *   [1\. Initializing the Project](../userguide/part1_gradle_init_project.html)
    *   [2\. Understanding the Build Lifecycle](../userguide/part2_build_lifecycle.html)
    *   [3\. Multi-Project Builds](../userguide/part3_multi_project_builds.html)
    *   [4\. Writing the Settings File](../userguide/part4_settings_file.html)
    *   [5\. Writing a Build Script](../userguide/part5_build_scripts.html)
    *   [6\. Writing Tasks](../userguide/part6_writing_tasks.html)
    *   [7\. Writing Plugins](../userguide/part7_writing_plugins.html)
*   Advanced Tutorial
    *   [1\. Initializing the Project](../userguide/part1_gradle_init_plugin.html)
    *   [2\. Adding an Extension](../userguide/part2_add_extension.html)
    *   [3\. Creating a Custom Task](../userguide/part3_create_custom_task.html)
    *   [4\. Writing a Unit Test](../userguide/part4_unit_test.html)
    *   [5\. Adding a DataFlow Action](../userguide/part5_add_dataflow_action.html)
    *   [6\. Writing a Functional Test](../userguide/part6_functional_test.html)
    *   [7\. Using a Consumer Project](../userguide/part7_use_consumer_project.html)
    *   [8\. Publish the Plugin](../userguide/part8_publish_locally.html)

### Gradle Reference

*   [Runtime and Configuration](#gradle-core)
    *   [Command-Line Interface](../userguide/command_line_interface.html)
    *   [Logging and Output](../userguide/logging.html)
    *   [Gradle Wrapper](../userguide/gradle_wrapper.html)
    *   [Gradle Daemon](../userguide/gradle_daemon.html)
    *   [Gradle Directories](../userguide/directory_layout.html)
    *   [Build Configuration](../userguide/build_environment.html)
    *   [Build Lifecycle](../userguide/build_lifecycle.html)
    *   [Build Scan](../userguide/inspect.html)
    *   [Continuous Builds](../userguide/continuous_builds.html)
    *   [File System Watching](../userguide/file_system_watching.html)
*   [DSLs and APIs](#dsl-and-apis)
    *   [Java API](../javadoc/index.html?overview-summary.html)
    *   [Groovy DSL Primer](../userguide/groovy_build_script_primer.html)
    *   [Groovy DSL](../dsl/index.html)
    *   [Kotlin DSL Primer](../userguide/kotlin_dsl.html)
    *   [Kotlin DSL](../kotlin-dsl/index.html)
    *   [Public APIs](../userguide/public_apis.html)
    *   [Default Script Imports](../userguide/default_script_imports.html)
    *   [Groovy to Kotlin DSL Migration](../userguide/migrating_from_groovy_to_kotlin_dsl.html)
*   [Best Practices](#best-practices)
    *   [Introduction](../userguide/best_practices.html)
    *   [Index](../userguide/best_practices_index.html)
    *   [General Best Practices](../userguide/best_practices_general.html)
    *   [Best Practices for Structuring Builds](../userguide/best_practices_structuring_builds.html)
    *   [Best Practices for Dependencies](../userguide/best_practices_dependencies.html)
    *   [Best Practices for Tasks](../userguide/best_practices_tasks.html)
    *   [Best Practices for Performance](../userguide/best_practices_performance.html)
    *   [Best Practices for Security](../userguide/best_practices_security.html)
*   [Core Plugins](#core-plugins)
    *   [List of Core Plugins](../userguide/plugin_reference.html)
*   [Dependency Management](#managing-dependencies)
    *   [Getting Started](../userguide/getting_started_dep_man.html)
    *   [Learning the Basics](#learning-the-basics-dependency-management)
        *   [1\. Declaring Dependencies](../userguide/declaring_dependencies.html)
        *   [2\. Dependency Configurations](../userguide/dependency_configurations.html)
        *   [3\. Declaring Repositories](../userguide/declaring_repositories.html)
        *   [4\. Centralizing Dependencies](../userguide/centralizing_dependencies.html)
        *   [5\. Dependency Constraints and Conflict Resolution](../userguide/dependency_constraints_conflicts.html)
    *   [Advanced Concepts](#understanding_dep_res)
        *   [1\. Dependency Resolution](../userguide/dependency_resolution.html)
        *   [2\. Graph Resolution](../userguide/graph_resolution.html)
        *   [3\. Variant Selection](../userguide/variant_aware_resolution.html)
        *   [4\. Artifact Resolution](../userguide/artifact_resolution.html)
    *   [Declaring Dependencies](#declaring-dependencies)
        *   [Declaring Dependencies](../userguide/declaring_dependencies_basics.html)
        *   [Viewing Dependencies](../userguide/viewing_debugging_dependencies.html)
        *   [Declaring Versions and Ranges](../userguide/dependency_versions.html)
        *   [Declaring Dependency Constraints](../userguide/dependency_constraints.html)
        *   [Creating Dependency Configurations](../userguide/declaring_configurations.html)
        *   [Gradle Distribution-Specific Dependencies](../userguide/gradle_dependencies.html)
        *   [Verifying Dependencies](../userguide/dependency_verification.html)
    *   [Declaring Repositories](#declaring-repositories)
        *   [Declaring Repositories](../userguide/declaring_repositories_basics.html)
        *   [Centralizing Repository Declarations](../userguide/centralizing_repositories.html)
        *   [Repository Types](../userguide/supported_repository_types.html)
        *   [Metadata Formats](../userguide/supported_metadata_formats.html)
        *   [Supported Protocols](../userguide/supported_repository_protocols.html)
        *   [Filtering Repository Content](../userguide/filtering_repository_content.html)
    *   [Centralizing Dependencies](#centralizing-dependencies)
        *   [Creating Platforms](../userguide/platforms.html)
        *   [Creating Version Catalogs](../userguide/version_catalogs.html)
        *   [Using Catalogs with Platforms](../userguide/centralizing_catalog_platform.html)
    *   [Managing Dependencies](#dependency-management)
        *   [Locking Versions](../userguide/dependency_locking.html)
        *   [Using Resolution Rules](../userguide/resolution_rules.html)
        *   [Modifying Dependency Metadata](../userguide/component_metadata_rules.html)
        *   [Caching Dependencies](../userguide/dependency_caching.html)
    *   [Controlling Dependency Resolution](#dependency-resolution)
        *   [Consistent Dependency Resolution](../userguide/dependency_resolution_consistency.html)
        *   [Resolving Specific Artifacts](../userguide/resolving_specific_artifacts.html)
        *   [Capabilities](../userguide/component_capabilities.html)
        *   [Variants and Attributes](../userguide/variant_attributes.html)
        *   [Artifact Views](../userguide/artifact_views.html)
        *   [Artifact Transforms](../userguide/artifact_transforms.html)
    *   [Publishing Libraries](#publishing)
        *   [Setting up Publishing](../userguide/publishing_setup.html)
        *   [Understanding Gradle Module Metadata](../userguide/publishing_gradle_module_metadata.html)
        *   [Signing Artifacts](../userguide/publishing_signing.html)
        *   [Customizing Publishing](../userguide/publishing_customization.html)
        *   [Maven Publish Plugin](../userguide/publishing_maven.html)
        *   [Ivy Publish Plugin](../userguide/publishing_ivy.html)
*   [Gradle Managed Types](#types-and-objects)
    *   [Lazy vs Eager Evaluation](../userguide/lazy_eager_evaluation.html)
    *   [Properties and Providers](../userguide/properties_providers.html)
    *   [Collections](../userguide/collections.html)
    *   [Services and Service Injection](../userguide/service_injection.html)
    *   [Dataflow Actions](../userguide/dataflow_actions.html)
    *   [Working with Files](../userguide/working_with_files.html)
*   [Structuring Builds](#build-structure)
    *   [Structuring and Organizing Projects](../userguide/organizing_gradle_projects.html)
    *   [Multi-Project Builds](../userguide/multi_project_builds.html)
    *   [Sharing Build Logic using buildSrc](../userguide/sharing_build_logic_between_subprojects.html)
    *   [Composite Builds](../userguide/composite_builds.html)
    *   [Configuration on Demand](../userguide/configuration_on_demand.html)
    *   [Isolated Projects](../userguide/isolated_projects.html)
*   [Task Development](#task-development)
    *   [Understanding Tasks](../userguide/more_about_tasks.html)
    *   [Controlling Task Execution](../userguide/controlling_task_execution.html)
    *   [Organizing Tasks](../userguide/organizing_tasks.html)
    *   [Implementing Custom Tasks](../userguide/implementing_custom_tasks.html)
    *   [Lazy Configuration](../userguide/lazy_configuration.html)
    *   [Parallel Task Execution](../userguide/worker_api.html)
    *   [Advanced Task Development](../userguide/custom_tasks.html)
    *   [Shared Build Services](../userguide/build_services.html)
*   [Plugin Development](#plugin-development)
    *   [Introduction to Plugins](../userguide/plugins.html)
    *   [Precompiled Script Plugins](../userguide/implementing_gradle_plugins_precompiled.html)
    *   [Convention Plugins](../userguide/implementing_gradle_plugins_convention.html)
    *   [Binary Plugins](../userguide/implementing_gradle_plugins_binary.html)
    *   [Testing Plugins](../userguide/testing_gradle_plugins.html)
    *   [Preparing to Publish](../userguide/preparing_to_publish.html)
    *   [Publishing Plugins](../userguide/publishing_gradle_plugins.html)
    *   [Reporting Plugin Problems](../userguide/reporting_problems.html)
    *   [Initialization Scripts](../userguide/init_scripts.html)
    *   [Testing with TestKit](../userguide/test_kit.html)
*   [Platforms](#platformst)
    *   [JVM Builds](#jvm)
        *   [Building Java & JVM projects](../userguide/building_java_projects.html)
        *   [Testing Java & JVM projects](../userguide/java_testing.html)
        *   [Java Toolchains](#java-toolchains)
            *   [Toolchains for JVM projects](../userguide/toolchains.html)
            *   [Toolchain Resolver Plugins](../userguide/toolchain_plugins.html)
        *   [Managing Dependencies](../userguide/dependency_management_for_java_projects.html)
        *   [JVM Plugins](#jvm-plugins)
            *   [Java Library Plugin](../userguide/java_library_plugin.html)
            *   [Java Application Plugin](../userguide/application_plugin.html)
            *   [Java Platform Plugin](../userguide/java_platform_plugin.html)
            *   [Groovy Plugin](../userguide/groovy_plugin.html)
            *   [Scala Plugin](../userguide/scala_plugin.html)
    *   [C++ Builds](#cpp)
        *   [Building C++ projects](../userguide/building_cpp_projects.html)
        *   [Testing C++ projects](../userguide/cpp_testing.html)
    *   [Swift Builds](#swift)
        *   [Building Swift projects](../userguide/building_swift_projects.html)
        *   [Testing Swift projects](../userguide/swift_testing.html)
*   [Other Topics](#advanced-topics)
    *   [Using Ant from Gradle](../userguide/ant.html)

### Optimizing Gradle Builds

*   [Improving Performance](../userguide/performance.html)
*   [Build Cache](#build-cache)
    *   [Enabling and Configuring](../userguide/build_cache.html)
    *   [Why use the Build Cache?](../userguide/build_cache_use_cases.html)
    *   [Understanding the Impact](../userguide/build_cache_performance.html)
    *   [Learning Basic Concepts](../userguide/build_cache_concepts.html)
    *   [Caching Java Project](../userguide/caching_java_projects.html)
    *   [Caching Android Project](../userguide/caching_android_projects.html)
    *   [Debugging Caching Issues](../userguide/build_cache_debugging.html)
    *   [Troubleshooting](../userguide/common_caching_problems.html)
*   [Configuration Cache](#configuration-cache)
    *   [How it Works](../userguide/configuration_cache.html)
    *   [Enabling and Configuring](../userguide/configuration_cache_enabling.html)
    *   [Requirements for your Build Logic](../userguide/configuration_cache_requirements.html)
    *   [Debugging and Troubleshooting](../userguide/configuration_cache_debugging.html)
    *   [Status](../userguide/configuration_cache_status.html)

### Integration

*   [Third-party Tools](../userguide/third_party_integration.html)
*   [APIs](#third-party-api)
    *   [Tooling API](../userguide/tooling_api.html)
    *   [Test Reporting API](../userguide/test_reporting_api.html)

### How-To-Guides

*   [Structuring Builds](#how-to-guides)
    *   [Convert a Single-Project Build to Multi-Project](../userguide/how_to_convert_single_build_to_multi_build.html)
*   [Dependency Management](#how-to)
    *   [How to Downgrade Transitive Dependencies](../userguide/how_to_downgrade_transitive_dependencies.html)
    *   [How to Upgrade Transitive Dependencies](../userguide/how_to_upgrade_transitive_dependencies.html)
    *   [How to Exclude Transitive Dependencies](../userguide/how_to_exclude_transitive_dependencies.html)
    *   [How to Prevent Accidental or Eager Dependency Upgrades](../userguide/how_to_prevent_accidental_dependency_upgrades.html)
    *   [How to Align Dependency Versions](../userguide/how_to_align_dependency_versions.html)
    *   [How to Share Outputs Between Projects](../userguide/how_to_share_outputs_between_projects.html)
    *   [How to Resolve Specific Artifacts from a Module Dependency](../userguide/how_to_resolve_specific_artifacts.html)
    *   [How to Use a Local Fork of a Module Dependency](../userguide/how_to_use_local_forks.html)
    *   [How to Fix Version Catalog Problems](../userguide/how_to_fix_version_catalog_problems.html)
    *   [How to Create Feature Variants of a Library](../userguide/how_to_create_feature_variants_of_a_library.html)

### Additional

*   [Samples](../samples/index.html)
*   [Glossary](../userguide/glossary.html)
*   [Single Page Version](../userguide/userguide_single.html)

# Gradle Kotlin DSL Primer

version 9.2.1

Contents

*   [Prerequisites](#kotdsl:prerequisites)
*   [IDE support](#sec:ide_support)
*   [Kotlin DSL scripts](#sec:scripts)
*   [Type-safe model accessors](#type-safe-accessors)
*   [Working with container objects](#kotdsl:containers)
*   [Working with runtime properties](#kotdsl:properties)
*   [Working with Gradle types](#kotdsl:types)
*   [Lazy property assignment](#kotdsl:assignment)
*   [Kotlin DSL Plugin](#sec:kotlin-dsl_plugin)
*   [Embedded Kotlin](#sec:kotlin)
*   [Interoperability](#sec:interoperability)
*   [Troubleshooting](#sec:troubleshooting)
*   [Limitations](#kotdsl:limitations)

Gradle’s Kotlin DSL offers an alternative to the traditional Groovy DSL, delivering an enhanced editing experience in supported IDEs with features like better content assist, refactoring, and documentation.

This chapter explores the key Kotlin DSL constructs and demonstrates how to use them to interact with the Gradle API.

If you are interested in migrating an existing Gradle build to the Kotlin DSL, please also check out the dedicated [migration page](migrating_from_groovy_to_kotlin_dsl.html#migrating_groovy_kotlin).

## [](#kotdsl:prerequisites)[Prerequisites](#kotdsl:prerequisites)

*   The embedded Kotlin compiler works on Linux, macOS, Windows, Cygwin, FreeBSD, and Solaris on x86-64 architectures.
    
*   Familiarity with Kotlin syntax and basic language features is recommended. Refer to the [Kotlin documentation](https://kotlinlang.org/docs/reference/) and [Kotlin Koans](https://kotlinlang.org/docs/tutorials/koans.html) to learn the basics.
    
*   Using the [`plugins {}`](plugins_intermediate.html#sec:plugins_block) block to declare Gradle plugins is highly recommended as it significantly improves the editing experience.
    

## [](#sec:ide_support)[IDE support](#sec:ide_support)

The Kotlin DSL is fully supported by IntelliJ IDEA and Android Studio. While other IDEs lack advanced tools for editing Kotlin DSL files, you can still import Kotlin-DSL-based builds and work with them as usual.

   

Build import

Syntax highlighting 1

Semantic editor 2

IntelliJ IDEA

**✓**

**✓**

**✓**

Android Studio

**✓**

**✓**

**✓**

Eclipse IDE

**✓**

**✓**

✖

CLion

**✓**

**✓**

✖

Apache NetBeans

**✓**

**✓**

✖

Visual Studio Code (LSP)

**✓**

**✓**

✖

Visual Studio

**✓**

✖

✖

1 Kotlin syntax highlighting in Gradle Kotlin DSL scripts  
2 Code completion, navigation to sources, documentation, refactorings etc…​ in Gradle Kotlin DSL scripts

As noted in the limitations, you must [import your project using the Gradle model](https://www.jetbrains.com/help/idea/gradle.html#gradle_import) to enable content assist and refactoring tools for Kotlin DSL scripts in IntelliJ IDEA.

Builds with slow configuration time might affect the IDE responsiveness, so please check out the [performance section](performance.html#performance_gradle) to help resolve such issues.

### [](#automatic_build_import_vs_automatic_reloading_of_script_dependencies)[Automatic build import vs. automatic reloading of script dependencies](#automatic_build_import_vs_automatic_reloading_of_script_dependencies)

Both IntelliJ IDEA and Android Studio will detect when you make changes to your build logic and offer two suggestions:

1.  Import the whole build again:
    
    ![IntelliJ IDEA](img/intellij-build-import-popup.png)
    
    ![IntelliJ IDEA](img/android-studio-build-sync-popup.png)
    
2.  Reload script dependencies when editing a build script:
    
    ![Reload script dependencies](img/intellij-script-dependencies-reload.png)
    

We recommend _disabling automatic build import_ while _enabling automatic reloading of script dependencies_. This approach provides early feedback when editing Gradle scripts while giving you control over when the entire build setup synchronizes with your IDE.

See the [Troubleshooting](#sec:troubleshooting) section to learn more.

## [](#sec:scripts)[Kotlin DSL scripts](#sec:scripts)

Just like its Groovy-based counterpart, the Kotlin DSL is built on Gradle’s Java API. Everything in a Kotlin DSL script is Kotlin code, compiled and executed by Gradle. Many of the objects, functions, and properties in your build scripts come from the Gradle API and the APIs of applied plugins.

Use the [Kotlin DSL reference](../kotlin-dsl/) search to explore available members.

### [](#script_file_names)[Script file names](#script_file_names)

*   Groovy DSL script files use the `.gradle` file name extension.
    
*   Kotlin DSL script files use the `.gradle.kts` file name extension.
    

To activate the Kotlin DSL, use the `.gradle.kts` extension for your build scripts instead of `.gradle`. This also applies to the [settings file](settings_file_basics.html#sec:settings_file_script) (e.g., `settings.gradle.kts`) and [initialization scripts](init_scripts.html#init_scripts).

You can mix Groovy DSL and Kotlin DSL scripts within the same build. For example, a Kotlin DSL build script can apply a Groovy DSL one, and different projects in a multi-project build can use either.

To improve IDE support, we recommend following these conventions:

*   Name settings scripts (or any script backed by a Gradle `Settings` object) using the pattern `*.settings.gradle.kts`. This includes script plugins applied from settings scripts.
    
*   Name [initialization scripts](init_scripts.html#init_scripts) using the pattern `*.init.gradle.kts` or simply `init.gradle.kts`.
    

This helps the IDE identify the object "backing" the script, whether it’s [Project](../dsl/org.gradle.api.Project.html), [Settings](../dsl/org.gradle.api.initialization.Settings.html), or [Gradle](../dsl/org.gradle.api.invocation.Gradle.html).

### [](#sec:implicit_imports)[Implicit imports](#sec:implicit_imports)

All Kotlin DSL build scripts come with implicit imports, including:

*   The [default Gradle API imports](default_script_imports.html#script-default-imports)
    
*   The Kotlin DSL API, which includes types from the following packages:
    
    *   `org.gradle.kotlin.dsl`
        
    *   `org.gradle.kotlin.dsl.plugins.dsl`
        
    *   `org.gradle.kotlin.dsl.precompile`
        
    *   `java.util.concurrent.Callable`
        
    *   `java.util.concurrent.TimeUnit`
        
    *   `java.math.BigDecimal`
        
    *   `java.math.BigInteger`
        
    *   `java.io.File`
        
    *   `javax.inject.Inject`
        
    

### [](#avoid_using_internal_kotlin_dsl_apis)[Avoid Using Internal Kotlin DSL APIs](#avoid_using_internal_kotlin_dsl_apis)

Using internal Kotlin DSL APIs in plugins and build scripts can break builds when either Gradle or plugins are updated.

The [Kotlin DSL API](../kotlin-dsl/) extends the public Gradle API with types listed in the [corresponding API docs](../kotlin-dsl/) found in the packages above (but not in their subpackages).

### [](#sec:compilation_warnings)[Compilation warnings](#sec:compilation_warnings)

Gradle Kotlin DSL scripts are compiled by Gradle during the [configuration phase](build_lifecycle_intermediate.html#build_lifecycle) of your build.

Deprecation warnings found by the Kotlin compiler are reported on the console when compiling the scripts:

```text
> Configure project :
w: build.gradle.kts:4:5: 'getter for uploadTaskName: String!' is deprecated. Deprecated in Java
```

It is possible to configure your build to fail on any warning emitted during script compilation by [setting](build_environment.html#sec:gradle_configuration_properties) the `org.gradle.kotlin.dsl.allWarningsAsErrors` Gradle property to `true`:

gradle.properties

```properties
org.gradle.kotlin.dsl.allWarningsAsErrors=true
```

## [](#type-safe-accessors)[Type-safe model accessors](#type-safe-accessors)

The Groovy DSL allows you to reference many build model elements by name, even if they are defined at runtime, such as named configurations or source sets.

For example, when the `Java` plugin is applied, you can access the `implementation` configuration via `configurations.implementation`.

The Kotlin DSL replaces this dynamic resolution with type-safe model accessors, which work with model elements contributed by plugins.

### [](#kotdsl:accessor_applicability)[Understanding when type-safe model accessors are available](#kotdsl:accessor_applicability)

The Kotlin DSL currently provides various sets of type-safe model accessors, each tailored to different scopes.

For the main project build scripts and precompiled project script plugins:

 

Type-safe model accessors

Example

Dependency and artifact configurations

`implementation` and `runtimeOnly` (contributed by the Java Plugin)

Project extensions and conventions, and extensions on them

`sourceSets`

Extensions on the `dependencies` and `repositories` containers, and extensions on them

`testImplementation` (contributed by the Java Plugin), `mavenCentral`

Elements in the `tasks` and `configurations` containers

`compileJava` (contributed by the Java Plugin), `test`

Elements in [project-extension containers](#kotdsl:containers)

Source sets contributed by the Java Plugin that are added to the `sourceSets` container: `sourceSets.main.java { setSrcDirs(listOf("src/main/java")) }`

For the main project settings script and precompiled settings script plugins:

 

Type-safe model accessors

Example

Project extensions and conventions, contributed by `Settings` plugins, and extensions on them

`pluginManagement`, `dependencyResolutionManagement`

Initialization scripts and script plugins do not have type-safe model accessors. These limitations will be removed in a future Gradle release.

The set of type-safe model accessors available is determined right before evaluating the script body, immediately after the `plugins {}` block. Model elements contributed after that point, such as configurations defined in your build script, **will not work** with type-safe model accessors:

build.gradle.kts

```kotlin
// Applies the Java plugin
plugins {
    id("java")
}

repositories {
    mavenCentral()
}

// Access to 'implementation' (contributed by the Java plugin) works here:
dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") // Add this if needed for runtime
}

// Add a custom configuration
configurations.create("customConfiguration")
// Type-safe accessors for 'customConfiguration' will NOT be available because it was created after the plugins block
dependencies {
    customConfiguration("com.google.guava:guava:32.1.2-jre") // ❌ Error: No type-safe accessor for 'customConfiguration'
}
```

However, this means you can use type-safe accessors for any model elements contributed by plugins that are _applied by parent projects_.

The following project build script demonstrates how you can access various configurations, extensions and other elements using type-safe accessors:

build.gradle.kts

```kotlin
plugins {
    `java-library`
}

dependencies {                              (1)
    api("junit:junit:4.13")
    implementation("junit:junit:4.13")
    testImplementation("junit:junit:4.13")
}

configurations {                            (1)
    implementation {
        resolutionStrategy.failOnVersionConflict()
    }
}

sourceSets {                                (2)
    main {                                  (3)
        java.srcDir("src/core/java")
    }
}

java {                                      (4)
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    test {                                  (5)
        testLogging.showExceptions = true
        useJUnit()
    }
}
```

**1**

Uses type-safe accessors for the `api`, `implementation` and `testImplementation` dependency configurations contributed by the [Java Library Plugin](java_library_plugin.html#java_library_plugin)

**2**

Uses an accessor to configure the `sourceSets` project extension

**3**

Uses an accessor to configure the `main` source set

**4**

Uses an accessor to configure the `java` source for the `main` source set

**5**

Uses an accessor to configure the `test` task

Your IDE is aware of the type-safe accessors and will include them in its suggestions.

This applies both at the top level of your build scripts, where most plugin extensions are added to the `Project` object, and within the blocks that configure an extension.

Note that accessors for elements of containers such as `configurations`, `tasks`, and `sourceSets` leverage Gradle’s [configuration avoidance APIs](lazy_configuration.html#lazy_configuration). For example, on `tasks`, accessors are of type `TaskProvider<T>` and provide a lazy reference and lazy configuration of the underlying task.

Here are some examples illustrating when configuration avoidance applies:

build.gradle.kts

```kotlin
tasks.test {
    // lazy configuration
    useJUnitPlatform()
}

// Lazy reference
val testProvider: TaskProvider<Test> = tasks.test

testProvider {
    // lazy configuration
}

// Eagerly realized Test task, defeats configuration avoidance if done out of a lazy context
val test: Test = tasks.test.get()
```

For all other containers, accessors for elements are of type `NamedDomainObjectProvider<T>`, providing the same behavior:

build.gradle.kts

```kotlin
val mainSourceSetProvider: NamedDomainObjectProvider<SourceSet> = sourceSets.named("main")
```

### [](#sec:kotlin_using_standard_api)[Understanding what to do when type-safe model accessors are not available](#sec:kotlin_using_standard_api)

Consider the sample build script shown above, which demonstrates the use of type-safe accessors. The following sample is identical, except it uses the `apply()` method to apply the plugin.

In this case, the build script cannot use type-safe accessors because the `apply()` call occurs in the body of the build script. You must use another techniques instead, as demonstrated here:

build.gradle.kts

```kotlin
apply(plugin = "java-library")

dependencies {
    "api"("junit:junit:4.13")
    "implementation"("junit:junit:4.13")
    "testImplementation"("junit:junit:4.13")
}

configurations {
    "implementation" {
        resolutionStrategy.failOnVersionConflict()
    }
}

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/core/java")
    }
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    named<Test>("test") {
        testLogging.showExceptions = true
    }
}
```

Type-safe accessors are unavailable for model elements contributed by the following:

*   Plugins applied via the `apply(plugin = "id")` method.
    
*   The project build script.
    
*   Script plugins, via `apply(from = "script-plugin.gradle.kts")`.
    
*   Plugins applied via [cross-project configuration](#sec:kotlin_cross_project_configuration).
    

You cannot use type-safe accessors in [binary Gradle plugins](implementing_gradle_plugins_binary.html#implementing_binary_plugins) implemented in Kotlin.

If you can’t find a type-safe accessor, _fall back to using the normal API_ for the corresponding types. To do so, you need to know the names and/or types of the configured model elements. We will now show you how these can be discovered by examining the script in detail.

#### [](#artifact_configurations)[Artifact configurations](#artifact_configurations)

The following sample demonstrates how to reference and configure artifact configurations without type-safe accessors:

build.gradle.kts

```kotlin
apply(plugin = "java-library")

dependencies {
    "api"("junit:junit:4.13")
    "implementation"("junit:junit:4.13")
    "testImplementation"("junit:junit:4.13")
}

configurations {
    "implementation" {
        resolutionStrategy.failOnVersionConflict()
    }
}
```

The code looks similar to that of the type-safe accessors, except that the configuration names are string literals. You can use string literals for configuration names in dependency declarations and within the `configurations {}` block.

While the IDE won’t be able to help you discover the available configurations, you can look them up either in the corresponding plugin’s documentation or by running `./gradlew dependencies`.

#### [](#project_extensions)[Project extensions](#project_extensions)

Project extensions have both a name and a unique type. However, the Kotlin DSL only needs to know the type to configure them.

The following sample shows the `sourceSets {}` and `java {}` blocks from the original example build script. The [`configure<T>()`](../kotlin-dsl/gradle/org.gradle.kotlin.dsl/configure.html) function is used with the corresponding type:

build.gradle.kts

```kotlin
apply(plugin = "java-library")

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/core/java")
    }
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
```

Note that `sourceSets` is a Gradle extension on `Project` of type `SourceSetContainer` and `java` is an extension on `Project` of type `JavaPluginExtension`.

You can discover available extensions by either reviewing the documentation for the applied plugins or running `./gradlew kotlinDslAccessorsReport`. The report generates the Kotlin code needed to access the model elements contributed by the applied plugins, providing both names and types.

As a last resort, you can check the plugin’s source code, though this should not be necessary in most cases.

You can also use the [`the<T>()`](../kotlin-dsl/gradle/org.gradle.kotlin.dsl/the.html) function if you only need a reference to the extension without configuring it, or if you want to perform a one-line configuration:

build.gradle.kts

```kotlin
the<SourceSetContainer>()["main"].java.srcDir("src/main/java")
```

The snippet above also demonstrates one way to configure elements of a project extension that is a container.

#### [](#elements_in_project_extension_containers)[Elements in project-extension containers](#elements_in_project_extension_containers)

Container-based project extensions, such as `SourceSetContainer`, allow you to configure the elements they hold.

In our sample build script, we want to configure a source set named `main` within the source set container. We can do this by using the [named()](../javadoc/org/gradle/api/NamedDomainObjectCollection.html#named-java.lang.String-) method instead of an accessor:

build.gradle.kts

```kotlin
apply(plugin = "java-library")

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/core/java")
    }
}
```

All elements within a container-based project extension have a name, so you can use this technique in all such cases.

For project extensions and conventions, you can discover what elements are present in any container by either checking the documentation for the applied plugins or by running `./gradlew kotlinDslAccessorsReport`.

As a last resort, you may also review the plugin’s source code to find out what it does.

#### [](#tasks)[Tasks](#tasks)

Tasks are not managed through a container-based project extension, but they are part of a container that behaves in a similar way.

This means that you can configure tasks in the same way as you do for source sets. The following example illustrates this approach:

build.gradle.kts

```kotlin
apply(plugin = "java-library")

tasks {
    named<Test>("test") {
        testLogging.showExceptions = true
    }
}
```

We are using the Gradle API to refer to tasks by name and type, rather than using accessors.

Note that it is necessary to specify the type of the task explicitly. If you don’t, the script won’t compile because the inferred type will be `Task`, not `Test`, and the `testLogging` property is specific to the `Test` task type.

However, you can omit the type if you only need to configure properties or call methods that are common to all tasks, i.e., those declared on the `Task` interface.

You can discover what tasks are available by running `./gradlew tasks`.

To find out the type of a given task, run `./gradlew help --task <taskName>`, as demonstrated here:

```text
❯ ./gradlew help --task test
...
Type
     Test (org.gradle.api.tasks.testing.Test)
```

The IDE can assist you with the required imports, so you only need the simple names of the types, without the package name part. In this case, there’s no need to import the `Test` task type, as it is part of the Gradle API and is therefore [imported implicitly](#sec:implicit_imports).

## [](#kotdsl:containers)[Working with container objects](#kotdsl:containers)

The Gradle build model makes extensive use of container objects (or simply "containers").

For example, `configurations` and `tasks` are containers that hold `Configuration` and `Task` objects, respectively. Community plugins also contribute containers, such as the `android.buildTypes` container contributed by the Android Plugin.

The Kotlin DSL provides multiple ways for build authors to interact with containers. We will explore each of these methods, using the `tasks` container as an example.

You can leverage the type-safe accessors described in [another section](#kotdsl:accessor_applicability) when configuring existing elements on supported containers. That section also explains which containers support type-safe accessors.

### [](#using_the_container_api)[Using the container API](#using_the_container_api)

All containers in Gradle implement [NamedDomainObjectContainer<DomainObjectType>](../dsl/org.gradle.api.NamedDomainObjectContainer.html#org.gradle.api.NamedDomainObjectContainer). Some containers can hold objects of different types and implement [PolymorphicDomainObjectContainer<BaseType>](../dsl/org.gradle.api.PolymorphicDomainObjectContainer.html#org.gradle.api.PolymorphicDomainObjectContainer). The simplest way to interact with containers is through these interfaces.

The following example demonstrates how you can use the [named()](../dsl/org.gradle.api.NamedDomainObjectContainer.html#org.gradle.api.NamedDomainObjectContainer:named\(java.lang.String\)) method to configure existing tasks, and the [register()](../dsl/org.gradle.api.NamedDomainObjectContainer.html#org.gradle.api.NamedDomainObjectContainer:register\(java.lang.String\)) method to create new tasks:

build.gradle.kts

```kotlin
tasks.named("check")                    (1)
tasks.register("myTask1")               (2)

tasks.named<JavaCompile>("compileJava") (3)
tasks.register<Copy>("myCopy1")         (4)

tasks.named("assemble") {               (5)
    dependsOn(":myTask1")
}
tasks.register("myTask2") {             (6)
    description = "Some meaningful words"
}

tasks.named<Test>("test") {             (7)
    testLogging.showStackTraces = true
}
tasks.register<Copy>("myCopy2") {       (8)
    from("source")
    into("destination")
}
```

**1**

Gets a reference of type `Task` to the existing task named `check`

**2**

Registers a new untyped task named `myTask1`

**3**

Gets a reference to the existing task named `compileJava` of type `JavaCompile`

**4**

Registers a new task named `myCopy1` of type `Copy`

**5**

Gets a reference to the existing (untyped) task named `assemble` and configures it — you can only configure properties and methods that are available on `Task` with this syntax

**6**

Registers a new untyped task named `myTask2` and configures it — you can only configure properties and methods that are available on `Task` in this case

**7**

Gets a reference to the existing task named `test` of type `Test` and configures it — in this case you have access to the properties and methods of the specified type

**8**

Registers a new task named `myCopy2` of type `Copy` and configures it

The above sample relies on the [configuration avoidance APIs](task_configuration_avoidance.html#task_configuration_avoidance). If you need or want to eagerly configure or register container elements, simply replace `named()` with `getByName()` and `register()` with `create()`.

### [](#using_kotlin_delegated_properties)[Using Kotlin delegated properties](#using_kotlin_delegated_properties)

Another way to interact with containers is via Kotlin delegated properties. These are particularly useful if you need a reference to a container element that you can use elsewhere in the build. Additionally, Kotlin delegated properties can easily be renamed via IDE refactoring.

The following example achieves the same result as the one in the previous section, but it uses delegated properties and reuses those references instead of string-literal task paths:

build.gradle.kts

```kotlin
val check by tasks.existing
val myTask1 by tasks.registering

val compileJava by tasks.existing(JavaCompile::class)
val myCopy1 by tasks.registering(Copy::class)

val assemble by tasks.existing {
    dependsOn(myTask1)  (1)
}
val myTask2 by tasks.registering {
    description = "Some meaningful words"
}

val test by tasks.existing(Test::class) {
    testLogging.showStackTraces = true
}
val myCopy2 by tasks.registering(Copy::class) {
    from("source")
    into("destination")
}
```

**1**

Uses the reference to the `myTask1` task rather than a task path

The above sample relies on the [configuration avoidance APIs](task_configuration_avoidance.html#task_configuration_avoidance). If you need or want to eagerly configure or register container elements, simply replace [`existing()`](../kotlin-dsl/gradle/org.gradle.kotlin.dsl/existing.html) with [`getting()`](../kotlin-dsl/gradle/org.gradle.kotlin.dsl/getting.html) and [`registering()`](../kotlin-dsl/gradle/org.gradle.kotlin.dsl/registering.html) with [`creating()`](../kotlin-dsl/gradle/org.gradle.kotlin.dsl/creating.html).

### [](#configuring_multiple_container_elements_together)[Configuring multiple container elements together](#configuring_multiple_container_elements_together)

When configuring several elements of a container, you can group interactions in a block to avoid repeating the container’s name on each interaction.

The following example demonstrates a combination of type-safe accessors, the container API, and Kotlin delegated properties:

build.gradle.kts

```kotlin
tasks {
    test {
        testLogging.showStackTraces = true
    }
    val myCheck by registering {
        doLast { /* assert on something meaningful */ }
    }
    check {
        dependsOn(myCheck)
    }
    register("myHelp") {
        doLast { /* do something helpful */ }
    }
}
```

## [](#kotdsl:properties)[Working with runtime properties](#kotdsl:properties)

Gradle has two main sources of properties defined at runtime: [_project properties_](build_environment.html#sec:project_properties) and [_extra properties_](writing_build_scripts_intermediate.html#sec:extra_properties).

The Kotlin DSL provides specific syntax for working with these property types, which we will explore in the following sections.

### [](#project_properties)[Project properties](#project_properties)

The Kotlin DSL allows you to access project properties by binding them via Kotlin delegated properties.

The following snippet demonstrates this technique for a couple of project properties, one of which _must_ be defined:

build.gradle.kts

```kotlin
val myProperty: String by project  (1)
val myNullableProperty: String? by project (2)
```

**1**

Makes the `myProperty` project property available via a `myProperty` delegated property — the project property must exist in this case, otherwise the build will fail when the build script attempts to use the `myProperty` value

**2**

Does the same for the `myNullableProperty` project property, but the build won’t fail on using the `myNullableProperty` value as long as you check for null (standard [Kotlin rules for null safety](https://kotlinlang.org/docs/reference/null-safety.html) apply)

The same approach works in both settings and initialization scripts, except you use `by settings` and `by gradle` respectively in place of `by project`.

### [](#extra_properties)[Extra properties](#extra_properties)

Extra properties are available on any object that implements the [ExtensionAware](../dsl/org.gradle.api.plugins.ExtensionAware.html#org.gradle.api.plugins.ExtensionAware) interface.

In Kotlin DSL, you can access and create extra properties via delegated properties, using the `by extra` syntax as demonstrated in the following sample:

build.gradle.kts

```kotlin
val myNewProperty by extra("initial value")  (1)
val myOtherNewProperty by extra { "calculated initial value" }  (2)

val myExtraProperty: String by extra  (3)
val myExtraNullableProperty: String? by extra  (4)
```

**1**

Creates a new extra property called `myNewProperty` in the current context (the project in this case) and initializes it with the value `"initial value"`, which also determines the property’s _type_

**2**

Create a new extra property whose initial value is calculated by the provided lambda

**3**

Binds an existing extra property from the current context (the project in this case) to a `myProperty` reference

**4**

Does the same as the previous line but allows the property to have a null value

This approach works for all Gradle scripts: project build scripts, script plugins, settings scripts, and initialization scripts.

You can also access extra properties on a root project from a subproject using the following syntax:

my-sub-project/build.gradle.kts

```kotlin
val myNewProperty: String by rootProject.extra  (1)
```

**1**

Binds the root project’s `myNewProperty` extra property to a reference of the same name

Extra properties aren’t just limited to projects. For example, `Task` extends `ExtensionAware`, so you can attach extra properties to tasks as well.

Here’s an example that defines a new `myNewTaskProperty` on the `test` task and then uses that property to initialize another task:

build.gradle.kts

```kotlin
tasks {
    test {
        val reportType by extra("dev")  (1)
        doLast {
            // Use 'suffix' for post-processing of reports
        }
    }

    register<Zip>("archiveTestReports") {
        val reportType: String by test.get().extra  (2)
        archiveAppendix = reportType
        from(test.get().reports.html.outputLocation)
    }
}
```

**1**

Creates a new `reportType` extra property on the `test` task

**2**

Makes the `test` task’s `reportType` extra property available to configure the `archiveTestReports` task

If you’re happy to use eager configuration rather than the configuration avoidance APIs, you could use a single, "global" property for the report type, like this:

build.gradle.kts

```kotlin
tasks.test {
    doLast { /* ... */ }
}

val testReportType by tasks.test.get().extra("dev")  (1)

tasks.create<Zip>("archiveTestsReports") {
    archiveAppendix = testReportType  (2)
    from(test.reports.html.outputLocation)
}
```

**1**

Creates and initializes an extra property on the `test` task, binding it to a "global" property

**2**

Uses the "global" property to initialize the `archiveTestReports` task

There is one last syntax for extra properties that treats `extra` as a map. We generally recommend against using this, as it bypasses Kotlin’s type checking and limits IDE support. However, it is more succinct than the delegated properties syntax and can be used if you only need to set an extra property without referencing it later.

Here is a simple example demonstrating how to set and read extra properties using the map syntax:

build.gradle.kts

```kotlin
extra["myNewProperty"] = "initial value"  (1)

tasks.register("myTask") {
    doLast {
        println("Property: ${project.extra["myNewProperty"]}")  (2)
    }
}
```

**1**

Creates a new project extra property called `myNewProperty` and sets its value

**2**

Reads the value from the project extra property we created — note the `project.` qualifier on `extra[…​]`, otherwise Gradle will assume we want to read an extra property from the _task_

## [](#kotdsl:types)[Working with Gradle types](#kotdsl:types)

`Property`, `Provider`, and `NamedDomainObjectProvider` are [types](properties_providers.html#properties_and_providers) that represent deferred and lazy evaluation of values and objects. The Kotlin DSL provides a specialized syntax for working with these types.

### [](#using_a_property)[Using a `Property`](#using_a_property)

A property represents a value that can be set and read lazily:

*   Setting a value: `property.set(value)` or `property = value`
    
*   Accessing the value: `property.get()`
    
*   Using the delegate syntax: `val propValue: String by property`
    

build.gradle.kts

```kotlin
val myProperty: Property<String> = project.objects.property(String::class.java)

myProperty.set("Hello, Gradle!") // Set the value
println(myProperty.get())        // Access the value

// Using delegate syntax
val propValue: String by myProperty
println(propValue)

// Using lazy syntax
myProperty = "Hi, Gradle!" // Set the value
println(myProperty.get())  // Access the value
```

### [](#using_a_provider)[Using a `Provider`](#using_a_provider)

A provider represents a read-only, lazily-evaluated value:

*   Accessing the value: `provider.get()`
    
*   Chaining: `provider.map { transform(it) }`
    

build.gradle.kts

```kotlin
val versionProvider: Provider<String> = project.provider { "1.0.0" }

println(versionProvider.get()) // Access the value

// Chaining transformations
val majorVersion: Provider<String> = versionProvider.map { it.split(".")[0] }
println(majorVersion.get()) // Prints: "1"
```

### [](#using_a_nameddomainobjectprovider)[Using a `NamedDomainObjectProvider`](#using_a_nameddomainobjectprovider)

A named domain object provider represents a lazily-evaluated named object from a Gradle container (like tasks or extensions):

*   Accessing the object: `namedObjectProvider.get()`
    
*   Configuring the object: `namedObjectProvider.configure { …​ }`
    

build.gradle.kts

```kotlin
val myTaskProvider: NamedDomainObjectProvider<Task> = tasks.named("build")

// Configuring the task
myTaskProvider.configure {
    doLast {
        println("Build task completed!")
    }
}

// Accessing the task
val myTask: Task = myTaskProvider.get()
```

## [](#kotdsl:assignment)[Lazy property assignment](#kotdsl:assignment)

Gradle’s Kotlin DSL supports lazy property assignment using the `=` operator.

Lazy property assignment reduces verbosity when [lazy properties](lazy_configuration.html#lazy_properties) are used. It works for properties that are publicly seen as `final` (without a setter) and have type `Property` or `ConfigurableFileCollection`. Since properties must be `final`, we generally recommend avoiding custom setters for properties with lazy types and, if possible, implementing such properties via an abstract getter.

Using the `=` operator is the preferred way to call `set()` in the Kotlin DSL:

build.gradle.kts

```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

abstract class WriteJavaVersionTask : DefaultTask() {
    @get:Input
    abstract val javaVersion: Property<String>
    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun execute() {
        output.get().asFile.writeText("Java version: ${javaVersion.get()}")
    }
}

tasks.register<WriteJavaVersionTask>("writeJavaVersion") {
    javaVersion.set("17") (1)
    javaVersion = "17" (2)
    javaVersion = java.toolchain.languageVersion.map { it.toString() } (3)
    output = layout.buildDirectory.file("writeJavaVersion/javaVersion.txt")
}
```

**1**

Set value with the `.set()` method

**2**

Set value with lazy property assignment using the `=` operator

**3**

The `=` operator can be used also for assigning lazy values

### [](#ide_support)[IDE support](#ide_support)

Lazy property assignment is supported from IntelliJ 2022.3 and from Android Studio Giraffe.

## [](#sec:kotlin-dsl_plugin)[Kotlin DSL Plugin](#sec:kotlin-dsl_plugin)

The Kotlin DSL Plugin provides a convenient way to develop Kotlin-based projects that contribute build logic. This includes [buildSrc projects](sharing_build_logic_between_subprojects.html#sec:using_buildsrc), [included builds](composite_builds.html#composite_builds), and [Gradle plugins](plugins.html#custom_plugins).

The plugin achieves this by doing the following:

*   Applies the [Kotlin Plugin](https://kotlinlang.org/docs/reference/using-gradle.html#targeting-the-jvm), which adds support for compiling Kotlin source files.
    
*   Adds the `kotlin-stdlib`, `kotlin-reflect`, and `gradleKotlinDsl()` dependencies to the `compileOnly` and `testImplementation` configurations, enabling the use of those Kotlin libraries and the Gradle API in your Kotlin code.
    
*   Configures the Kotlin compiler with the same settings used for Kotlin DSL scripts, ensuring consistency between your build logic and those scripts:
    
    *   Adds [Kotlin compiler arguments](#sec:kotlin_compiler_arguments),
        
    *   Registers the [SAM-with-receiver Kotlin compiler plugin](https://kotlinlang.org/docs/sam-with-receiver-plugin.html).
        
    
*   Enables support for [precompiled script plugins](plugins.html#sec:precompile_script_plugin).
    

Each Gradle release is meant to be used with a specific version of the `kotlin-dsl` plugin. Compatibility between arbitrary Gradle releases and `kotlin-dsl` plugin versions is not guaranteed. Using an unexpected version of the `kotlin-dsl` plugin will emit a warning and can cause hard-to-diagnose problems.

This is the basic configuration you need to use the plugin:

buildSrc/build.gradle.kts

```kotlin
plugins {
    `kotlin-dsl`
}

repositories {
    // The org.jetbrains.kotlin.jvm plugin requires a repository
    // where to download the Kotlin compiler dependencies from.
    mavenCentral()
}
```

The Kotlin DSL Plugin leverages [Java Toolchains](toolchains.html#toolchains). By default, the code will target Java 8. You can change that by defining a Java toolchain to be used by the project:

buildSrc/src/main/kotlin/myproject.java-conventions.gradle.kts

```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}
```

## [](#sec:kotlin)[Embedded Kotlin](#sec:kotlin)

Gradle embeds Kotlin in order to provide support for Kotlin-based scripts.

### [](#kotlin_versions)[Kotlin versions](#kotlin_versions)

Gradle ships with `kotlin-compiler-embeddable` plus matching versions of `kotlin-stdlib` and `kotlin-reflect` libraries. For details, see the Kotlin section of Gradle’s [compatibility matrix](compatibility.html#kotlin). The `kotlin` package from those modules is visible through the Gradle classpath.

The [compatibility](https://kotlinlang.org/docs/reference/compatibility.html) guarantees provided by Kotlin apply for both backward and forward compatibility.

#### [](#backward_compatibility)[Backward compatibility](#backward_compatibility)

Our approach is to only make backward-incompatible Kotlin upgrades with major Gradle releases. We clearly document the Kotlin version shipped with each release and announce upgrade plans ahead of major releases.

Plugin authors aiming to maintain compatibility with older Gradle versions must limit their API usage to what is supported by those versions. This is no different from working with any new API in Gradle. For example, if a new API for dependency resolution is introduced, a plugin must either drop support for older Gradle versions or organize its code to conditionally execute the new code path on compatible versions.

#### [](#forward_compatibility)[Forward compatibility](#forward_compatibility)

The primary compatibility concern lies between the external `kotlin-gradle-plugin` version and the `kotlin-stdlib` version shipped with Gradle. More broadly, this applies to any plugin that transitively depends on `kotlin-stdlib` and its version provided by Gradle. As long as the versions are compatible, everything should work as expected. This issue will diminish as the Kotlin language matures.

### [](#sec:kotlin_compiler_arguments)[Kotlin compiler arguments](#sec:kotlin_compiler_arguments)

The following Kotlin compiler arguments are used for compiling Kotlin DSL scripts, as well as Kotlin sources and scripts in projects with the `kotlin-dsl` plugin applied:

`-java-parameters`

Generate metadata for Java >= 1.8 reflection on method parameters. See [Kotlin/JVM compiler options](https://kotlinlang.org/docs/compiler-reference.html#kotlin-jvm-compiler-options) in the Kotlin documentation for more information.

`-Xjvm-default=all`

Makes all non-abstract members of Kotlin interfaces default for the Java classes implementing them. This is to provide a better interoperability with Java and Groovy for plugins written in Kotlin. See [Default methods in interfaces](https://kotlinlang.org/docs/java-to-kotlin-interop.html#default-methods-in-interfaces) in the Kotlin documentation for more information.

`-Xsam-conversions=class`

Sets up the implementation strategy for SAM (single abstract method) conversion to always generate anonymous classes, instead of using the `invokedynamic` JVM instruction. This is to provide a better support for configuration cache and incremental build. See [KT-44912](https://youtrack.jetbrains.com/issue/KT-44912) in the Kotlin issue tracker for more information.

`-Xjsr305=strict` & `-Xjspecify-annotations=strict`

Sets up Kotlin’s Java interoperability to strictly follow JSR-305 and JSpecify annotations for increased null safety. See [Calling Java code from Kotlin](https://kotlinlang.org/docs/reference/java-interop.html#compiler-configuration) in the Kotlin documentation for more information.

## [](#sec:interoperability)[Interoperability](#sec:interoperability)

When mixing languages in your build logic, you may have to cross language boundaries. An extreme example would be a build that uses tasks and plugins that are implemented in Java, Groovy and Kotlin, while also using both Kotlin DSL and Groovy DSL build scripts.

> Kotlin is designed with Java Interoperability in mind. Existing Java code can be called from Kotlin in a natural way, and Kotlin code can be used from Java rather smoothly as well.

— Kotlin reference documentation

Both [calling Java from Kotlin](https://kotlinlang.org/docs/reference/java-interop.html) and [calling Kotlin from Java](https://kotlinlang.org/docs/reference/java-to-kotlin-interop.html) are very well covered in the Kotlin reference documentation.

The same mostly applies to interoperability with Groovy code. In addition, the Kotlin DSL provides several ways to opt into Groovy semantics, which we look at next.

### [](#static_extensions)[Static extensions](#static_extensions)

Both the Groovy and Kotlin languages support extending existing classes via [Groovy Extension modules](https://groovy-lang.org/metaprogramming.html#_extension_modules) and [Kotlin extensions](https://kotlinlang.org/docs/reference/extensions.html).

To call a Kotlin extension function from Groovy, call it as a static function, passing the receiver as the first parameter:

build.gradle

```groovy
TheTargetTypeKt.kotlinExtensionFunction(receiver, "parameters", 42, aReference)
```

Kotlin extension functions are package-level functions. You can learn how to locate the name of the type declaring a given Kotlin extension in the [Package-Level Functions](https://kotlinlang.org/docs/reference/java-to-kotlin-interop.html#package-level-functions) section of the Kotlin reference documentation.

To call a Groovy extension method from Kotlin, the same approach applies: call it as a static function passing the receiver as the first parameter:

build.gradle.kts

```kotlin
TheTargetTypeGroovyExtension.groovyExtensionMethod(receiver, "parameters", 42, aReference)
```

### [](#named_parameters_and_default_arguments)[Named parameters and default arguments](#named_parameters_and_default_arguments)

Both the Groovy and Kotlin languages support named function parameters and default arguments, although they are implemented very differently. Kotlin has fully-fledged support for both, as described in the Kotlin language reference under [named arguments](https://kotlinlang.org/docs/reference/functions.html#named-arguments) and [default arguments](https://kotlinlang.org/docs/reference/functions.html#default-arguments). Groovy implements [named arguments](https://groovy-lang.org/objectorientation.html#_named_arguments) in a non-type-safe way based on a `Map<String, ?>` parameter, which means they cannot be combined with [default arguments](https://groovy-lang.org/objectorientation.html#_default_arguments). In other words, you can only use one or the other in Groovy for any given method.

#### [](#calling_kotlin_from_groovy)[Calling Kotlin from Groovy](#calling_kotlin_from_groovy)

To call a Kotlin function that has named arguments from Groovy, just use a normal method call with positional parameters:

build.gradle

```groovy
kotlinFunction("value1", "value2", 42)
```

There is no way to provide values by argument name.

To call a Kotlin function that has default arguments from Groovy, always pass values for all the function parameters.

#### [](#calling_groovy_from_kotlin)[Calling Groovy from Kotlin](#calling_groovy_from_kotlin)

To call a Groovy function with named arguments from Kotlin, you need to pass a `Map<String, ?>`, as shown in this example:

build.gradle.kts

```kotlin
groovyNamedArgumentTakingMethod(mapOf(
    "parameterName" to "value",
    "other" to 42,
    "and" to aReference))
```

To call a Groovy function with default arguments from Kotlin, always pass values for all the parameters.

### [](#groovy_closures_from_kotlin)[Groovy closures from Kotlin](#groovy_closures_from_kotlin)

You may sometimes have to call Groovy methods that take [Closure](https://groovy-lang.org/closures.html) arguments from Kotlin code. For example, some third-party plugins written in Groovy expect closure arguments.

Gradle plugins written in any language should prefer the type `Action<T>` type in place of closures. Groovy closures and Kotlin lambdas are automatically mapped to arguments of that type.

In order to provide a way to construct closures while preserving Kotlin’s strong typing, two helper methods exist:

*   `closureOf<T> {}`
    
*   `delegateClosureOf<T> {}`
    

Both methods are useful in different circumstances and depend upon the method you are passing the `Closure` instance into.

Some plugins expect simple closures, as with the [Bintray](https://plugins.gradle.org/plugin/com.jfrog.bintray) plugin:

build.gradle.kts

```kotlin
bintray {
    pkg(closureOf<PackageConfig> {
        // Config for the package here
    })
}
```

In other cases, like with the [Gretty Plugin](https://plugins.gradle.org/plugin/org.gretty) when configuring farms, the plugin expects a delegate closure:

build.gradle.kts

```kotlin
farms {
    farm("OldCoreWar", delegateClosureOf<FarmExtension> {
        // Config for the war here
    })
}
```

There sometimes isn’t a good way to tell, from looking at the source code, which version to use. Usually, if you get a `NullPointerException` with `closureOf<T> {}`, using `delegateClosureOf<T> {}` will resolve the problem.

These two utility functions are useful for _configuration closures_, but some plugins might expect Groovy closures for other purposes. The `KotlinClosure0` to `KotlinClosure2` types allows adapting Kotlin functions to Groovy closures with more flexibility:

build.gradle.kts

```kotlin
somePlugin {

    // Adapt parameter-less function
    takingParameterLessClosure(KotlinClosure0({
        "result"
    }))

    // Adapt unary function
    takingUnaryClosure(KotlinClosure1<String, String>({
        "result from single parameter $this"
    }))

    // Adapt binary function
    takingBinaryClosure(KotlinClosure2<String, String, String>({ a, b ->
        "result from parameters $a and $b"
    }))
}
```

### [](#the_kotlin_dsl_groovy_builder)[The Kotlin DSL Groovy Builder](#the_kotlin_dsl_groovy_builder)

If some plugin makes heavy use of [Groovy metaprogramming](https://groovy-lang.org/metaprogramming.html), then using it from Kotlin or Java or any statically-compiled language can be very cumbersome.

The Kotlin DSL provides a `withGroovyBuilder {}` utility extension that attaches the Groovy metaprogramming semantics to objects of type `Any`.

The following example demonstrates several features of the method on the object `target`:

build.gradle.kts

```kotlin
target.withGroovyBuilder {                                          (1)

    // GroovyObject methods available                               (2)
    if (hasProperty("foo")) { /*...*/ }
    val foo = getProperty("foo")
    setProperty("foo", "bar")
    invokeMethod("name", arrayOf("parameters", 42, aReference))

    // Kotlin DSL utilities
    "name"("parameters", 42, aReference)                            (3)
        "blockName" {                                               (4)
            // Same Groovy Builder semantics on `blockName`
        }
    "another"("name" to "example", "url" to "https://example.com/") (5)
}
```

**1**

The receiver is a [GroovyObject](https://docs.groovy-lang.org/latest/html/api/groovy/lang/GroovyObject.html) and provides Kotlin helpers

**2**

The `GroovyObject` API is available

**3**

Invoke the `methodName` method, passing some parameters

**4**

Configure the `blockName` property, maps to a `Closure` taking method invocation

**5**

Invoke `another` method taking named arguments, maps to a Groovy named arguments `Map<String, ?>` taking method invocation

### [](#using_a_groovy_script)[Using a Groovy script](#using_a_groovy_script)

Another option when dealing with problematic plugins that assume a Groovy DSL build script is to configure them in a Groovy DSL build script that is applied from the main Kotlin DSL build script:

dynamic-groovy-plugin-configuration.gradle

```groovy
native {    (1)
    dynamic {
        groovy as Usual
    }
}
```

build.gradle.kts

```kotlin
plugins {
    id("dynamic-groovy-plugin") version "1.0"   (2)
}
apply(from = "dynamic-groovy-plugin-configuration.gradle")  (3)
```

**1**

The Groovy script uses dynamic Groovy to configure plugin

**2**

The Kotlin build script requests and applies the plugin

**3**

The Kotlin build script applies the Groovy script

## [](#sec:troubleshooting)[Troubleshooting](#sec:troubleshooting)

The IDE support is provided by two components:

1.  Kotlin Plugin (used by IntelliJ IDEA/Android Studio).
    
2.  Gradle.
    

The level of support varies based on the versions of each.

If you encounter issues, first run `./gradlew tasks` from the command line to determine if the problem is specific to the IDE. If the issue persists on the command line, it likely originates from the build itself rather than IDE integration.

However, if the build runs successfully on the command line but your script editor reports errors, try restarting your IDE and invalidating its caches.

If the issue persists, and you suspect a problem with the Kotlin DSL script editor, try the following:

*   Run `./gradlew tasks` to gather more details.
    
*   Check the logs in one of these locations:
    
    *   `$HOME/Library/Logs/gradle-kotlin-dsl` on macOS
        
    *   `$HOME/.gradle-kotlin-dsl/log` on Linux
        
    *   `$HOME/AppData/Local/gradle-kotlin-dsl/log` on Windows
        
    
*   Report the issue on the [Gradle issue tracker](https://github.com/gradle/gradle/issues/), including as much detail as possible.
    

From version 5.1 onward, the log directory is automatically cleaned. Logs are checked periodically (at most, every 24 hours), and files are deleted if unused for 7 days.

If this doesn’t help pinpoint the problem, you can enable the `org.gradle.kotlin.dsl.logging.tapi` system property in your IDE. This causes the Gradle Daemon to log additional details in its log file located at `$HOME/.gradle/daemon`.

In IntelliJ IDEA, enable this property by navigating to `Help > Edit Custom VM Options…​` and adding: `-Dorg.gradle.kotlin.dsl.logging.tapi=true`.

For IDE problems outside the Kotlin DSL script editor, please open issues in the corresponding IDE’s issue tracker:

*   JetBrains’s IDEA issue tracker
    
*   Google’s Android Studio issue tracker
    

Lastly, if you face problems with Gradle itself or with the Kotlin DSL, please open issues on the [Gradle issue tracker](https://github.com/gradle/gradle/issues/).

## [](#kotdsl:limitations)[Limitations](#kotdsl:limitations)

*   The Kotlin DSL is [known to be slower than the Groovy DSL](https://github.com/gradle/gradle/issues/15886) on first use, for example with clean checkouts or on ephemeral continuous integration agents. Changing something in the _buildSrc_ directory also has an impact as it invalidates build-script caching. The main reason for this is the slower script compilation for Kotlin DSL.
    
*   In IntelliJ IDEA, you must [import your project from the Gradle model](https://www.jetbrains.com/help/idea/gradle.html#gradle_import) in order to get content assist and refactoring support for your Kotlin DSL build scripts.
    
*   Kotlin DSL script compilation avoidance has known issues. If you encounter problems, it can be disabled by [setting](build_environment.html#sec:gradle_system_properties) the `org.gradle.kotlin.dsl.scriptCompilationAvoidance` system property to `false`.
    
*   The Kotlin DSL will not support the `model {}` block, which is part of the [discontinued Gradle Software Model](https://blog.gradle.org/state-and-future-of-the-gradle-software-model).
    

If you run into trouble or discover a suspected bug, please report the issue in the [Gradle issue tracker](https://github.com/gradle/gradle/issues/).

Was this page helpful?

Additional Feedback:

You can [submit issues](https://github.com/gradle/gradle/issues/new?assignees=&labels=a%3Adocumentation%2Cto-triage&projects=&template=40_contributor_documentation.yml) directly on Github.

Submit Feedback

**Docs**

*   [Release Notes](/current/release-notes.html)
*   [Groovy DSL](/current/dsl/)
*   [Kotlin DSL](/current/kotlin-dsl/)
*   [Javadoc](/current/javadoc/)

**News**

*   [Blog](https://blog.gradle.org/)
*   [Newsletter](https://newsletter.gradle.org/)
*   [Twitter](https://twitter.com/gradle)
*   [Status](https://status.gradle.com/)

**Products**

*   [Develocity](https://gradle.com/develocity/)
*   [Build Scan®](https://gradle.com/develocity/product/build-scan/)
*   [Build Cache](https://gradle.com/build-cache/)
*   [Services](https://gradle.org/services/)

**Get Help**

*   [Forums](https://discuss.gradle.org/c/help-discuss)
*   [GitHub](https://github.com/gradle/)
*   [Events](https://gradle.org/training/)
*   [DPE University](https://dpeuniversity.gradle.com/)

##### Stay `UP-TO-DATE` on new features and news:

By entering your email, you agree to our [Terms](https://gradle.com/legal/terms-of-service/) and [Privacy Policy](https://gradle.com/legal/privacy/).

© 2025 Gradle, Inc. Gradle®, Develocity®, Build Scan®, and the Gradlephant logo are registered trademarks of Gradle, Inc.

[Gradle](https://gradle.com)

[Privacy](https://gradle.com/legal/privacy/) | [Terms of Service](https://gradle.com/legal/terms-of-service/)