# ADR-0008: Multiplatform Client Applications

## Status

Accepted

## Context

Our system requires client applications for different user roles and platforms:

1. Desktop applications for administrators and event organizers who need rich functionality
2. Web applications for members and horse owners who need to access the system from various devices
3. Potential future mobile applications for on-the-go access

Developing and maintaining separate codebases for each platform would require:
- Duplicate implementation of business logic and UI components
- Multiple teams with different platform expertise
- Coordination to ensure consistent user experience across platforms
- Higher maintenance costs as features and fixes would need to be implemented multiple times

We needed a solution that would allow us to share code across platforms while still providing a native-like experience on each platform.

## Decision

We decided to use Kotlin Multiplatform and Compose Multiplatform for our client applications:

1. **Kotlin Multiplatform**: Allows sharing of business logic, data models, and API client code across platforms
2. **Compose Multiplatform**: Provides a declarative UI framework that works across desktop, web, and mobile platforms

Our implementation includes:
- **common-ui**: Shared UI components and business logic
- **desktop-app**: Desktop application for administrators and event organizers
- **web-app**: Web application for members and horse owners

The architecture follows a Model-View-ViewModel (MVVM) pattern:
- **Model**: Shared data models and repository implementations
- **ViewModel**: Shared business logic and state management
- **View**: Platform-specific UI implementations using Compose Multiplatform

We use a modular approach where platform-specific code is minimized and most of the code is shared across platforms.

## Consequences

### Positive

- **Code sharing**: Significant portions of code are shared across platforms, reducing duplication
- **Consistent user experience**: UI components and behavior are consistent across platforms
- **Single language**: Kotlin is used for all platforms, simplifying development
- **Reduced maintenance**: Fixes and features can be implemented once and applied across platforms
- **Team efficiency**: Developers can work on multiple platforms with the same skill set

### Negative

- **Learning curve**: Kotlin Multiplatform and Compose Multiplatform have a learning curve
- **Maturity**: Compose Multiplatform is still evolving, especially for web targets
- **Performance considerations**: There may be performance overhead compared to platform-native solutions
- **Platform-specific features**: Some platform-specific features may be harder to implement
- **Debugging complexity**: Debugging across platforms can be more complex

### Neutral

- **Build system complexity**: The build system is more complex with multiplatform targets
- **Dependency management**: Managing dependencies across platforms requires careful consideration

## Alternatives Considered

### Separate Native Applications

We considered developing separate native applications for each platform (Java/JavaFX for desktop, JavaScript/React for web). This would have provided the best performance and access to platform features but would have required duplicate implementation of business logic and UI components.

### React Native

We considered using React Native for mobile and web, with a separate desktop application. This would have allowed code sharing between mobile and web but would still have required a separate desktop solution and would have required JavaScript expertise.

### Flutter

We considered using Flutter for all platforms. Flutter provides good cross-platform support but would have required learning Dart and would have had less integration with our Kotlin-based backend services.

## References

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform Documentation](https://www.jetbrains.com/lp/compose-multiplatform/)
- [MVVM Architecture Pattern](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel)
- [Kotlin Multiplatform Mobile](https://kotlinlang.org/lp/mobile/)
