# Contributing to Shopping

Thank you for your interest in contributing to the Shopping plugin! This document provides guidelines and information for contributors.

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [How to Contribute](#how-to-contribute)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Submitting Changes](#submitting-changes)
- [Reporting Issues](#reporting-issues)

## Code of Conduct

This project follows a simple code of conduct:
- Be respectful and inclusive
- Focus on constructive feedback
- Help create a positive community

## How to Contribute

### Types of Contributions
- **Bug fixes** - Fix existing issues
- **Features** - Add new functionality
- **Documentation** - Improve documentation
- **Testing** - Add or improve tests

### Getting Started
1. Fork the repository
2. Create a feature branch from `main`
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## Development Setup

### Prerequisites
- **Java 21** or higher
- **Gradle 8.5** or higher
- **Minecraft Server** (Paper/Spigot 1.21+) for testing
- **Economy Plugin** (Vault + EssentialsX/CMI)

### Setup Steps
```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/Shopping.git
cd Shopping

# Build the project
./gradlew build

# Test in development environment
./gradlew runServer  # If you have a test server setup
```

### Project Structure
```
src/main/java/me/touchie771/shopping/
├── Shopping.java              # Main plugin class
├── FeesManager.java           # Fee and tax system
├── SecurityManager.java       # Item restrictions
├── shop/                      # Shop system
│   ├── ShopCommand.java
│   ├── ShopHandler.java
│   ├── ShopMenuListener.java
│   └── ShopItem.java
└── auction/                   # Auction system
    ├── AuctionCommand.java
    ├── AuctionHandler.java
    ├── AuctionMenuListener.java
    ├── AuctionTask.java
    └── AuctionItem.java

src/main/resources/
├── plugin.yml                 # Plugin configuration
├── fees.yml                   # Fee settings
├── security.yml               # Item restrictions
└── *.yml                      # Data files
```

## Coding Standards

### Java Code Style
- Use **UTF-8** encoding
- Follow standard Java naming conventions
- Use meaningful variable and method names
- Add documentation for public methods
- Keep methods focused on single responsibilities
- Use final for immutable variables where appropriate

### Code Quality
- **No emojis** in code or comments
- Write **maintainable code** with clear logic
- Handle exceptions appropriately
- Use logging for debugging (not print statements)
- Keep the codebase clean and well-organized

### Commit Messages
Use clear, descriptive commit messages:
```
feat: add configurable fees system
fix: resolve auction completion bug
docs: update README with fee examples
refactor: improve shop menu performance
```

### Pull Request Guidelines
- **One feature/fix per PR** - Keep changes focused
- **Descriptive title** - Clearly explain what the PR does
- **Detailed description** - Explain the problem and solution
- **Test your changes** - Ensure everything works
- **Update documentation** - If needed
- **Squash commits** - Clean up commit history

## Testing

### Manual Testing
1. **Compile** the plugin: `./gradlew build`
2. **Install** on test server with Vault and economy plugin
3. **Test features**:
   - Shop listings and purchases
   - Auction creation and bidding
   - Fee calculations and tax collection
   - Permission checks
   - Edge cases (insufficient funds, etc.)

### Test Checklist
- [ ] Shop selling works with fees
- [ ] Shop buying works with taxes
- [ ] Auction starting works with fees
- [ ] Auction completion works with taxes
- [ ] Permissions are respected
- [ ] Error messages are clear
- [ ] Data saves/loads correctly
- [ ] No console errors

## Submitting Changes

### Pull Request Process
1. **Ensure tests pass** and code compiles
2. **Update documentation** if needed
3. **Write clear commit messages**
4. **Create pull request** with detailed description
5. **Respond to feedback** and make requested changes
6. **Wait for review** and approval

### PR Template
```
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Refactoring

## Testing
- [ ] Tested on local server
- [ ] All existing functionality works
- [ ] New features work as expected

## Additional Notes
Any additional context or screenshots
```

## Reporting Issues

### Bug Reports
Please include:
- **Minecraft version** and server type (Paper/Spigot)
- **Plugin version**
- **Steps to reproduce** the issue
- **Expected behavior**
- **Actual behavior**
- **Error logs** if applicable
- **Configuration files** if relevant

### Feature Requests
Please include:
- **Clear description** of the feature
- **Use case** - why is this needed?
- **Implementation ideas** if you have them
- **Mockups** or examples if applicable

## Questions?

If you have questions about contributing:
- Check existing [issues](https://github.com/Touchie771/Shopping/issues) and [discussions](https://github.com/Touchie771/Shopping/discussions)
- Open a [discussion](https://github.com/Touchie771/Shopping/discussions) for questions
- Contact the maintainer

Thank you for contributing to Shopping! 🎉
