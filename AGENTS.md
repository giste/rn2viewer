# Custom Instructions for Android Studio Agent

## Language and Style
- **Language**: Always generate code, variable names, and documentation (KDoc/comments) in English.
- **Tone**: Professional and concise.

## Architecture: Clean Architecture & SOLID
- **Layer Separation**: Strictly follow the separation between Data, Domain, and Presentation layers.
- **Domain Layer**:
    - Must be platform-independent (pure Kotlin/Java).
    - Use "UseCases" for business logic.
- **Presentation Layer**:
    - Use MVVM (ViewModel + StateFlow/SharedFlow).
    - Follow Jetpack Compose best practices for UI.
- **Data Layer**:
    - Use the Repository Pattern for data abstraction.

[//]: # (    - Implement Mappers to convert Data Transfer Objects &#40;DTOs&#41; to Domain Models.)

## Code Standards
- **Naming**: Follow official Kotlin style guides (PascalCase for classes, camelCase for functions/variables).
- **Dependency Injection**: Use Hilt for all dependency management.
- **Error Handling**: Use a functional approach (like `Result<T>` or a custom `Either` type) instead of throwing exceptions for business errors.
- **Asynchrony**: Use Kotlin Coroutines and Flows exclusively.

## Testing
- Ensure code is highly testable.
- Favor constructor injection to facilitate mocking.
