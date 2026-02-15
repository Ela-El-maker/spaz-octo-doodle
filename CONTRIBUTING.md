# Contributing to Spazoodle

Thank you for your interest in contributing to Spazoodle! We welcome contributions from the community to help improve this Android alarm application.

## Code of Conduct

This project follows a code of conduct to ensure a welcoming environment for all contributors. By participating, you agree to:

- Be respectful and inclusive
- Focus on constructive feedback
- Accept responsibility for mistakes
- Show empathy towards other contributors
- Help create a positive community

## How to Contribute

### Reporting Issues

If you find a bug or have a feature request:

1. Check existing issues to avoid duplicates
2. Use the issue templates when available
3. Provide detailed information including:
   - Steps to reproduce (for bugs)
   - Expected vs actual behavior
   - Android version and device information
   - App version

### Contributing Code

1. **Fork the repository** and create a feature branch from `main`
2. **Follow the coding standards** outlined below
3. **Write tests** for new features and bug fixes
4. **Ensure all tests pass** before submitting
5. **Update documentation** if needed
6. **Submit a pull request** with a clear description

### Development Setup

1. Clone the repository
2. Open in Android Studio
3. Ensure you have the required Android SDK versions
4. Run `./gradlew build` to verify setup
5. Run tests with `./gradlew test`

## Coding Standards

### Kotlin Style

- Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use meaningful variable and function names

### Architecture

- Follow Clean Architecture principles
- Separate concerns into domain, data, and presentation layers
- Use dependency injection with Hilt
- Write testable code with clear interfaces

### Testing

- Write unit tests for business logic
- Write integration tests for data layer
- Write UI tests for critical user flows
- Aim for good test coverage (>80%)

### Commit Messages

- Use clear, descriptive commit messages
- Start with a verb (Add, Fix, Update, Remove, etc.)
- Keep the first line under 50 characters
- Add detailed description if needed

Example:

```
Add alarm scheduling validation

- Validate alarm time is in the future
- Show user-friendly error messages
- Add unit tests for validation logic
```

## Pull Request Process

1. **Update the README.md** if your changes affect user-facing functionality
2. **Update documentation** in the `docs/` directory for architectural changes
3. **Ensure CI passes** all checks
4. **Request review** from maintainers
5. **Address feedback** and make necessary changes

## Areas for Contribution

- Bug fixes and stability improvements
- UI/UX enhancements
- Performance optimizations
- New features (please discuss first)
- Documentation improvements
- Test coverage expansion

## Getting Help

- Check the [documentation](docs/) for detailed information
- Join discussions in GitHub issues
- Contact maintainers for guidance

Thank you for contributing to Spazoodle! 🎉
