# IDK To Go

**IDK To Go** is a robust JavaFX-based desktop application designed to simplify the decision-making process for choosing a place to eat. It empowers users by combining personalized restaurant tracking, popularity-based scoring, and an intuitive interface to help them discover their next favorite meal. Built with an MVC-inspired architecture, the application leverages a remote MySQL database (hosted on Railway) for persistent data storage, utilizing asynchronous DAO operations for a responsive user experience.

---

## Features

### Core Functionality
*   **User Management:** Comprehensive user account system including registration, secure login, profile updates, and account deletion.
*   **Persistent Sessions:** Seamless user experience with session handling managed by the `SessionManager`.
*   **Restaurant Discovery:** Browse restaurants, view detailed menus, and interact by liking or disliking establishments.
*   **User History Tracking:** Keeps a record of user interactions and preferences to inform future recommendations.
*   **Admin Panel:** Dedicated interface for administrators to manage restaurants, update menus, and view weekly statistics.
*   **Asynchronous Database Operations:** Utilizes `CompletableFuture` for non-blocking MySQL interactions, ensuring a smooth UI.
*   **Secure Data Handling:** Implements secure JDBC logic with parameterized `PreparedStatements` to prevent SQL injection.
*   **AI-Powered Decision Quiz:** An intelligent quiz that helps users decide where to eat based on their preferences.

### User Interface & Navigation
*   **Modern UI:** FXML-based interface with a clean, modern design, styled using JavaFX CSS.
*   **Intuitive Navigation:** A dedicated `Navigation` helper class for efficient scene management.
*   **Theme Customization:** `ThemeManager` allows users to switch between light and dark themes.
*   **Modular Design:** Controllers are structured modularly for enhanced maintainability and scalability.

---

## Technology Stack

| Component           | Technology                                  | Purpose                                     |
|---------------------|---------------------------------------------|---------------------------------------------|
| **Language**        | Java 21                                     | Core programming language                   |
| **UI Framework**    | JavaFX 21                                   | Rich desktop application interface          |
| **Build Tool**      | Maven                                       | Project management and build automation     |
| **Database**        | MySQL (hosted on Railway)                   | Relational database for data persistence    |
| **DB Access**       | JDBC with Commons DBCP2                     | Database connectivity and connection pooling|
| **Architecture**    | MVC-style (Controller / DAO / Model / Service / Core) | Structured application design               |
| **Async Handling**  | CompletableFuture                           | Non-blocking asynchronous operations        |
| **Styling**         | JavaFX CSS                                  | Customizing UI appearance                   |
| **Version Control** | Git / GitHub                                | Source code management                      |
| **Utility Library** | Google Guava                                | General-purpose utility functions           |
| **AI Integration**  | OpenAI Java Client                          | Interacting with OpenAI APIs                |
| **JSON Processing** | Jackson Databind, org.json                  | Handling JSON data structures               |

---

## Project Structure

```
src/main/java/com/idktogo/idk_to_go/
│
├─ controller/        # JavaFX controllers for managing UI interactions
├─ core/              # Core utilities: Navigation, ThemeManager, SessionManager, DatabaseConnector
├─ dao/               # Asynchronous Data Access Objects for database CRUD operations
├─ model/             # Record-based immutable entities representing data structures
├─ service/           # Business logic and user history management
│
├─ Main.java          # Application entry point and primary launcher
│
src/main/resources/com/idktogo/idk_to_go/
│
├─ *.fxml             # FXML layout files defining the user interface views
├─ styles/            # CSS stylesheets for application theming and styling
├─ images/            # Logos, icons, and other UI assets
├─ db/schema.sql      # SQL script for defining the MySQL database schema
```

---

## Database Schema (MySQL)

The application utilizes a relational database with the following key tables:

| Table        | Purpose                                            |
|--------------|----------------------------------------------------|
| `users`      | Stores user accounts, credentials, and admin status|
| `restaurants`| Contains restaurant information, scores, and stats |
| `menu_items` | Details of menu items for each restaurant          |
| `user_history`| Tracks user interactions (likes/dislikes) with restaurants |

The complete database schema can be found at:  
`src/main/resources/com/idktogo/idk_to_go/db/schema.sql`

---

## Build and Run

### Prerequisites
*   **Java Development Kit (JDK):** Version 21 or later.
*   **Apache Maven:** Version 3.9 or later.
*   **Internet Connection:** Required for accessing the remote Railway MySQL database.
*   **Integrated Development Environment (IDE):** IntelliJ IDEA is recommended for optimal development experience.

### Run from IDE (IntelliJ IDEA)
1.  Open the project in IntelliJ IDEA.
2.  Allow Maven to automatically import all necessary dependencies.
3.  Locate and run the `Main.java` file (e.g., right-click -> Run 'Main.main()').
    Upon successful startup, you should observe console output similar to:
    ```
    Connected to Railway MySQL successfully!
    Application started successfully!
    ```

### Run from Command Line
To build and run the application using Maven from your terminal:

```bash
# Clean the project and install dependencies
mvn clean install

# Run the JavaFX application
mvn javafx:run
```

---

## Git Workflow

For contributors, please follow this standard workflow:

1.  **Clone or Fork:** Obtain a copy of the repository.
2.  **Create Feature Branch:** Branch off from `main` for new features or bug fixes.
3.  **Commit Changes:** Make your changes and commit them with descriptive messages.
4.  **Push to Origin:** Push your feature branch to the remote repository.
5.  **Submit Pull Request:** Open a pull request targeting the `main` branch.

Example:
```bash
git checkout -b feature/new-ui
git commit -m "Added updated UI layout for main screen"
git push origin feature/new-ui
```

---

## Contributing

Contributions to this project are currently closed. This project is developed as part of an Application Programming course at the University of Texas at San Antonio. Any external contributions would require explicit approval from the instructor or the project team.

---

## License

This project currently does not have an assigned open-source license. Redistribution or modification without explicit permission is prohibited. A license may be added in the future.

---

## Author / Maintainer

**Tiem**  
University of Texas at San Antonio  
Application Programming Project — IDK To Go  
