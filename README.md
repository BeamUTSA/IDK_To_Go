# IDK To Go

IDK To Go is a JavaFX-based desktop application that helps users decide where to eat by combining user interaction, restaurant tracking, and popularity-based scoring.  
It follows an MVC-inspired architecture and uses a remote MySQL database (hosted on Railway) with asynchronous DAO operations.

---

## Features

### Core Functionality
- User account system with registration, login, updates, and deletion  
- Persistent session handling through SessionManager  
- Restaurant browsing with menus, likes/dislikes, and user history tracking  
- Admin panel for managing restaurants, menus, and weekly stats  
- Asynchronous MySQL operations using CompletableFuture  
- Secure JDBC logic with parameterized PreparedStatements  

### UI and Navigation
- FXML-based interface styled with JavaFX CSS  
- Navigation helper for scene management  
- ThemeManager for light/dark theme switching  
- Modular controller structure for maintainability  

### Planned / Upcoming
- AI-generated food decision quiz  
- Mobile or web deployment  
- Admin analytics dashboard  
- Cloud-based user data syncing  

---

## Technology Stack

| Component | Technology |
|------------|-------------|
| Language | Java 21 |
| UI Framework | JavaFX 21 |
| Build Tool | Maven |
| Database | MySQL (Railway) |
| Database Access | JDBC with HikariCP |
| Architecture | MVC-style (controller / dao / model / service / core) |
| Async Handling | CompletableFuture |
| Styling | JavaFX CSS |
| Version Control | Git / GitHub |

---

## Project Structure

```
src/main/java/com/idktogo/idk_to_go/
│
├─ controller/        # JavaFX controllers
├─ core/              # Navigation, ThemeManager, SessionManager, DatabaseConnector
├─ dao/               # Async DAO classes for database CRUD logic
├─ model/             # Record-based immutable entities
├─ service/           # Business logic and history management
│
├─ Main.java          # Application entry point
│
src/main/resources/com/idktogo/idk_to_go/
│
├─ *.fxml             # View files
├─ styles/            # CSS stylesheets
├─ images/            # Logos and UI assets
├─ db/schema.sql      # Database schema (MySQL)
```

---

## Database Schema (MySQL)

The application uses the following schema:

| Table | Purpose |
|--------|----------|
| users | Stores user accounts and admin flag |
| restaurants | Contains restaurant info, scores, and stats |
| menu_items | Restaurant menus with item names and prices |
| user_history | Tracks user interactions (likes/dislikes) |

The full schema is located at:  
`src/main/resources/com/idktogo/idk_to_go/db/schema.sql`

---

## Build and Run

### Prerequisites
- Java 21 or later  
- Maven 3.9 or later  
- Internet connection (for Railway MySQL access)  
- IntelliJ IDEA (recommended)

### Run from IDE
1. Open the project in IntelliJ.  
2. Wait for Maven to import dependencies.  
3. Run `Main.java`.  
   You should see:  
   ```
   Connected to Railway MySQL successfully!
   Application started successfully!
   ```

### Run from Command Line
```
mvn clean install
mvn javafx:run
```

---

## Maven Dependencies

Key dependencies included in `pom.xml`:

| Dependency | Purpose |
|-------------|----------|
| org.openjfx:javafx-controls | UI components |
| org.openjfx:javafx-fxml | FXML loader support |
| com.mysql:mysql-connector-j | MySQL JDBC driver |
| com.zaxxer:HikariCP | Connection pooling |
| org.slf4j:slf4j-simple | Logging |
| org.junit.jupiter:junit-jupiter | Testing |

---

## Git Workflow

1. Clone or fork the repository  
2. Create a feature branch  
3. Commit and push your changes  
4. Submit a pull request to `main`

Example:
```
git checkout -b feature/new-ui
git commit -m "Added updated UI layout for main screen"
git push origin feature/new-ui
```

---

## Contributing

Contributions are currently closed.  
This project is part of an Application Programming course at the University of Texas at San Antonio.  
External contributions require instructor or team approval.

---

## License

No license currently assigned.  
You may not redistribute or modify without explicit permission unless a license is added in the future.

---

## Author / Maintainer

**BeamUTSA**  
University of Texas at San Antonio  
Application Programming Project — IDK To Go  

---

## Roadmap

- Implement AI-based food quiz  
- Add live analytics for admins  
- Extend remote cloud database features  
- Package application as a native installer (MSI/DMG/AppImage)
