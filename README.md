# Health Response Application

## Overview

The Health Response Application is a Java Swing desktop application designed to manage and conduct Health Risk Assessments (HRAs). The application allows users to register, log in, take assessments, view their history, and receive results regarding their risk level. Administrators can manage users, questions, sponsors, and view overall reports.

The application is built on the Java Swing platform, utilizes the FlatLaf library for a modern user interface, and connects to a MySQL database for data storage.

## Key Features

### For Users (USER Role)

*   **Registration & Login:** Create a new account and log into the system.
*   **Take Assessment:**
    *   Answer a series of health-related questions.
    *   Question types include: single choice, multiple choice, and text input.
*   **View Results:**
    *   Receive a total score after completing an assessment.
    *   View the risk level determined based on the score.
*   **View Assessment History:** Track previous assessment attempts.
*   **User Dashboard:** Main interface displaying available user functions.

### For Administrators (ADMIN Role)

*   **Admin Dashboard:** Overview interface with management functions.
*   **User Management:**
    *   Add, edit, and delete user accounts.
    *   Assign roles and sponsors to users.
*   **Question Management:**
    *   Add, edit, and delete questions in the HRA system.
    *   Define question types, content, options (if any), and corresponding scores for each option or text-based question.
*   **Sponsor Management:**
    *   Add, edit, and delete sponsor information.
    *   View a list of users sponsored by a specific sponsor.
*   **View Global Reports:**
    *   Chart displaying user risk level distribution.
    *   Chart displaying response distribution for specific questions.
    *   Chart showing the number of users per sponsor.
    *   Other general statistics.

## Application Architecture

The application follows a 3-tier architecture:

1.  **Presentation Tier (UI):**
    *   Package: `com.kimquyen.healthapp.ui`
    *   Built using Java Swing.
    *   Uses `CardLayout` to manage functional screens.
    *   The FlatLaf library is used for an enhanced user interface.
    *   `UIConstants` for managing interface constants (colors, fonts).

2.  **Business Logic Tier (Service):**
    *   Package: `com.kimquyen.healthapp.service`
    *   Includes Service classes responsible for handling business logic and coordinating operations between the UI and DAO.
    *   Passwords are hashed using the BCrypt algorithm (`BCryptPasswordHashingServiceImpl`).

3.  **Data Access Tier (DAO):**
    *   Package: `com.kimquyen.healthapp.dao`
    *   DAO classes are responsible for direct interaction with the MySQL database via JDBC.
    *   Uses `PreparedStatement` to prevent SQL Injection vulnerabilities.

### Other Components

*   **Model Tier:**
    *   Package: `com.kimquyen.healthapp.model`
    *   Contains POJO classes representing data entities (e.g., `Account`, `UserData`, `HraQuestion`).
*   **Utility Tier:**
    *   Package: `com.kimquyen.healthapp.util`
    *   Contains utility classes such as `DatabaseUtil` (DB connection management), `SessionManager` (login session management), `ValidationUtil` (input validation).
*   **Configuration:**
    *   Package: `com.kimquyen.healthapp.config`
    *   `DatabaseConfig` contains database connection configuration information.

## Technologies Used

*   **Language:** Java (JDK 8+)
*   **User Interface (UI):** Java Swing
*   **UI Library:** FlatLaf (for a modern look and feel)
*   **Database:** MySQL (Managed with SQLyog or similar)
*   **DB Connectivity:** JDBC (MySQL Connector/J)
*   **Password Hashing:** jBCrypt
*   **Charts/Reports:** JFreeChart

## Setup and Running the Application

### System Requirements

*   JDK 8 or higher.
*   MySQL Server.
*   A MySQL management tool (e.g., SQLyog, MySQL Workbench, DBeaver).
*   Eclipse IDE (or other compatible Java IDE).

### Installation Steps

1.  **Database Setup:**
    *   Start your MySQL Server.
    *   Using SQLyog (or a similar tool), create a database named `hihi`.
    *   Execute SQL files (if provided, or manually create tables based on the source code analysis - see *Inferred Database Structure* below).
    *   **Important:** Configure the database connection information in the `src/com/kimquyen/healthapp/config/DatabaseConfig.java` file to match your MySQL environment (DB_URL, DB_USER, DB_PASSWORD).

2.  **Project Setup in Eclipse:**
    *   Create a new Java Project in Eclipse.
    *   Copy the entire source code into the `src` directory of the project.
    *   Add the necessary JAR libraries to the project's Build Path:
        *   MySQL Connector/J (e.g., `mysql-connector-java-8.x.x.jar`)
        *   jBCrypt (e.g., `jbcrypt-0.4.jar`)
        *   JFreeChart (e.g., `jfreechart-1.5.x.jar`, `jcommon-1.0.x.jar`)
        *   FlatLaf (e.g., `flatlaf-x.x.jar`)
        (You can find and download these JAR files from Maven Central or official sources.)
        *How to add JARs in Eclipse:* Right-click on Project > Properties > Java Build Path > Libraries (tab) > Add External JARs...

3.  **Create Initial Data (Optional but Recommended):**
    *   **Admin Account:** Run `src/com/kimquyen/healthapp/util/PasswordHasherUtil.java` to generate a hashed password for the admin account. Then, manually insert a record into the `account` and `users_data` tables for the admin.
    *   **Sample Questions:** Insert a few sample questions into the `hra_qna_scores` table to enable assessments.
    *   **Sample User Accounts:** If you have data in `users_data` without corresponding `account` entries, you can run `src/com/kimquyen/healthapp/util/AccountGeneratorUtil.java` to automatically create accounts for these UserData entries.

### Running the Application

*   In Eclipse, navigate to the `src/com/kimquyen/healthapp/MainApp.java` file.
*   Right-click on `MainApp.java` > Run As > Java Application.
*   The login screen will appear.

## Inferred Database Structure (from source code)

*   **`users_data`**: Stores user profile information.
    *   `id` (INT, PK, AI)
    *   `name` (VARCHAR)
    *   `sponsor_id` (INT, FK - may reference `sponsor_data.id`, can be NULL)
    *   `created_at` (TIMESTAMP)
*   **`account`**: Stores login credentials.
    *   `id` (INT, PK, AI - may not be necessary if `username` is PK)
    *   `username` (VARCHAR, UNIQUE)
    *   `password` (VARCHAR - stores hashed password)
    *   `role` (VARCHAR - e.g., 'ADMIN', 'USER')
    *   `user_data_fk_id` (INT, FK - references `users_data.id`)
*   **`sponsor_data`**: Stores sponsor information.
    *   `id` (INT, PK, AI)
    *   `name` (VARCHAR, UNIQUE)
*   **`hra_qna_scores`**: Stores questions, options, and scores.
    *   `question_id` (INT) - Not AI, managed by code.
    *   `type` (VARCHAR - e.g., 'SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TEXT_INPUT')
    *   `title` (VARCHAR, NULLABLE)
    *   `text` (TEXT - question content)
    *   `options` (VARCHAR, NULLABLE - option content)
    *   `score` (INT, NULLABLE - score for the option or general score for text questions)
    *   *Primary key could be (`question_id`, `options`) if `options` is not null for choice-based questions, or a separate auto-incrementing ID per row might be needed.*
*   **`hra_responses`**: Stores user responses to questions.
    *   `id` (INT, PK, AI)
    *   `user_id` (INT, FK - references `users_data.id`)
    *   `question_id` (INT, FK - references `hra_qna_scores.question_id`)
    *   `response` (TEXT - user's answer)
    *   `created_at` (TIMESTAMP)
*   **`user_assessment_attempts`**: Stores a summary of each assessment attempt.
    *   `attempt_id` (INT, PK, AI)
    *   `user_data_id` (INT, FK - references `users_data.id`)
    *   `assessment_date` (TIMESTAMP)
    *   `total_score` (INT)
    *   `risk_level` (VARCHAR)

## Author

*   Kim Quyen and Dai Loi

## Additional Notes

*   This application was analyzed based on the provided source code.
*   Database details (exact column names, data types, constraints) are inferred and should be verified by examining the actual schema in SQLyog or a DDL (Data Definition Language) file if available.