# IDK To Go
IDK To Go is a JavaFX-based desktop application designed to help users decide where to eat by combining user interaction, restaurant tracking, popularity ranking, and a future AI-driven quiz system. The project follows an MVC-inspired architecture and uses a local SQLite database for persistent storage.

## Features
### Core Functionality
- User account system with login, registration, profile updates, and deletion
- Persistent session handling and theme memory
- Restaurant viewing with score, trending, and user history tracking
- Options menu for appearance settings, account management, and logout
- Admin-only panel for elevated management features
- Local database (SQLite) storing users, restaurants, likes, history, and scores

### UI and Navigation
- Navigation helper class for scene switching and stage injection
- ThemeManager for global dark/light theme support
- FXML-based view structure styled with CSS
- Logical separation of controllers for maintainability

### Upcoming / Planned
- AI-generated food decision quiz
- Cloud sync or online DB migration
- Mobile deployment or JavaFX port alternatives
- Extended admin features and analytics dashboards

## Technology Stack
| Component       | Technology |
|-----------------|------------|
| Language        | Java 21 |
| UI Framework    | JavaFX 21 |
| Build Tool      | Maven |
| Database        | SQLite (via JDBC) |
| Architecture    | MVC-style separation (`controller`, `dao`, `model`, `core`) |
| Styling         | JavaFX CSS |
| Version Control | Git / GitHub |

## Project Structure
src/main/java/com/idktogo/idk_to_go/
│
├─ controller/         # FXML controllers
├─ core/               # Navigation, ThemeManager, app bootstrap utilities
├─ dao/                # Database access and queries
├─ model/              # Records / data models
├─ service/            # Business logic and history tracking
│
├─ Main.java           # Application entry point
│
src/main/resources/com/idktogo/idk_to_go/
│
├─ *.fxml              # View files
├─ styles/             # CSS stylesheets
├─ images/             # UI assets and logos
├─ db/app.db           # SQLite database

## Database Schema (SQLite)
Tables include:
users
restaurants
menu
userhistory

A full SQL schema is stored in:
src/main/resources/com/idktogo/idk_to_go/db/schema.sql

## Build and Run
### Prerequisites
- Java 21+
- Maven 3.9+
- Any IDE supporting JavaFX (IntelliJ recommended)

### Run from IDE
1. Open the project in IntelliJ
2. Ensure Maven loads dependencies
3. Run Main.java

### Run from command line
mvn clean install
mvn javafx:run

## Maven Configuration
JavaFX modules are handled with org.openjfx plugin.
Dependencies include SQLite JDBC and JavaFX controls.

## Git Workflow
1. Fork or clone repository
2. Create a feature branch
3. Submit pull request to main

Example:
git checkout -b feature/new-ui
git commit -m "Added updated UI layout for main screen"
git push origin feature/new-ui

## Contributing
Contributions are NOT Welcome. This is a Team specific project for an Application Programming Course.

## License
This repo currently has no assigned license. You may not redistribute or modify without permission unless a license is later added.

## Author / Maintainer
BeamUTSA  
University of Texas at San Antonio  

## Roadmap
- Implement AI quiz generator
- Add API support for live restaurant data
- Replace local SQLite with remote database option
- Package app into native installer formats (MSI/DMG)
