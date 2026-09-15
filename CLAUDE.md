# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Technology Stack
- **Backend:** Java 25, Spring Boot 4.x, Maven (3.9+)
- **Frontend:** Thymeleaf templates, HTML/CSS/JavaScript
- **Database:** MariaDB 11.4
- **Infrastructure:** Docker / Docker Compose (MariaDB, Spring App, Nginx, phpMyAdmin)
- **Security:** JWT-based authentication with role-based access control

## Environment & Architecture Rules
- **CRITICAL:** The `mariadb` container does NOT expose ports to the Windows host.
- All build, test, and database migration commands MUST be executed inside the running `spring_app` container using `docker compose exec`.
- Static assets and templates are mounted via volumes. Changes to these local directories sync immediately without container restart:
  - Static files: `src/main/resources/static` -> mapped to `/app/static/`
  - Thymeleaf templates: `src/main/resources/templates` -> mapped to `/app/templates/`

## Host Commands (Run on Windows)
- Spin up full infrastructure (build and start): `docker compose up -d --build`
- Stop environment: `docker compose down`
- View all service logs: `docker compose logs -f`
- View Spring application logs only: `docker compose logs -f app`
- View Nginx logs: `docker compose logs -f nginx_proxy`
- View phpMyAdmin logs: `docker compose logs -f phpmyadmin`
- Commit and push changes: `.\deploy.ps1` (see the script for details)
- Install JS dependencies (Run once): `npm install`
- Run all JS tests once (Vitest): `npm test`
- Run JS tests in watch mode (TDD): `npx vitest`
- Run JS tests with coverage: `npm run test:coverage`

## Container Commands (Execute inside the container)
Access container shell: `docker compose exec app bash`

- **Run Tests:** `docker compose exec app mvn test`
- **Run Specific Test Class:** `docker compose exec app mvn test -Dtest=<TestClassName>`
- **Run Specific Test Method:** `docker compose exec app mvn test -Dtest=<TestClassName>#<testMethod>`
- Build JAR & Lint check: `docker compose exec app mvn clean package -DskipTests`
- Checkstyle validation: `docker compose exec app mvn checkstyle:check`
- Generate Javadoc: `docker compose exec app mvn javadoc:javadoc`
- Run application in dev mode: `docker compose exec app mvn spring-boot:run`

## Code Standards & Style (Java 25 / Spring Boot 4.x)
- **Syntax:** Actively use modern Java 25 features (Pattern Matching, Records, Virtual Threads, Unnamed Classes, Sequenced Collections)
  - Example: Use `instanceof` pattern matching: `if (obj instanceof String s) { s.toUpperCase(); }`
  - Example: Use records for immutable data: `public record UserDto(Long id, String name) {}`
  - Example: Use virtual threads for concurrent operations: `Thread.startVirtualThread(() -> { /* task */ });`
- **Style:** Adhere to Google Java Style. Use constructor injection only. `@RequiredArgsConstructor` from Lombok is allowed; avoid field-level `@Autowired`.
- **Database:** Schema updates are managed automatically by Hibernate via `SPRING_JPA_HIBERNATE_DDL_AUTO: update`.
- **Validation:** Use Jakarta Validation annotations (`@Valid`, `@NotNull`, `@Size`, etc.) for input validation.
- **Security:** All endpoints should be secured with appropriate role checks unless explicitly public.
- **Lombok Usage:** Entities use `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@ToString`. Services and repositories typically don't use Lombok.

## JavaScript Standards & Modern Practices
- **Version & Syntax:** Use modern JavaScript (ES11 to ES15+). Prefer `const` and `let` over `var`. Actively use Optional Chaining (`?.`), Nullish Coalescing (`??`), Arrow Functions, and Object/Array Destructuring.
- **Modularity:** Organize scripts using **ES Modules (`import`/`export`)**. Declare module scripts in Thymeleaf via `<script type="module" thsrc="...">`. Avoid cluttering the global `window` scope.
  - Current migration state: Some files still use traditional patterns; new code should follow ES module guidelines.
- **Asynchronous Operations:** Use standard `async/await` syntax for all asynchronous logic. Wrap asynchronous blocks in `try/catch` for robust error handling. Do not use raw `.then().catch()` unless necessary.
- **DOM Manipulation:**
  - Prefer modern API options like `querySelector` and `querySelectorAll`.
  - Use `addEventListener` for bind actions. Strictly **avoid** inline HTML event attributes (e.g., `onclick="..."`).
  - Use `element.classList` for class styling instead of modifying `element.className`.
- **API Communication (Fetch API):**
  - Use native `fetch()` for backend requests (avoid jQuery AJAX or Axios unless required).
  - Explicitly append **JWT tokens** from local storage or cookies into HTTP headers (`Authorization: Bearer <token>`) for all secured backend paths.
- **Thymeleaf Integration:** Pass server-side variables safely into JavaScript using Thymeleaf natural data attributes: `<div id="config" th:data-api-url="${apiUrl}"></div>`. Read them in JS via `element.dataset.apiUrl`. Avoid rendering unsafe dynamic JS blocks using raw inlining (`/*[[...]]*/`) where possible.

## JavaScript Testing Standards (Vitest)
- **Framework:** Use **Vitest** for all JavaScript unit and integration tests.
- **Environment:** Use `jsdom` environment for tests interacting with the DOM. Include `// @vitest-environment jsdom` at the top of the test file if needed.
- **File Naming:** Name test files as `*.test.js` and place them in `src/main/resources/static/js/` directory following the feature structure (e.g., `src/main/resources/static/js/work-results.test.js`).
- **Mocking & Fetch:**
  - Mock all backend API calls (e.g., via `vi.spyOn(global, 'fetch')` or `vi.stubGlobal('fetch', ...)`). Never allow JS tests to make real HTTP requests to Spring Boot.
  - Always mock the JWT token retrieval logic (LocalStorage/Cookies) to simulate authorized states.
- **DOM Assertions:**
  - Prepare the virtual DOM before testing UI changes by setting `document.body.innerHTML`.
  - Use modern assertions: `expect(element).toBeInTheDocument()`, `expect(element.classList.contains('active')).toBe(true)`.
- **Test Structure:** Group tests using `describe()`, state behavior with `it('should...')`, and clean up the DOM/mocks in `afterEach()` using `vi.restoreAllMocks()` and `document.body.innerHTML = ''`.
- **Vitest Configuration:** See `vitest.config.js` for configuration details. Tests are configured to use jsdom environment and match files ending in `.test.js`.

## Application Structure
- **Controllers:** Handle HTTP requests and return Thymeleaf views or JSON responses
- **Services:** Contain business logic and transaction management
- **Repositories:** Spring Data JPA repositories for database access
- **Models:** JPA entities representing database tables
- **DTOs:** Data Transfer Objects for communication between layers
- **Security:** JWT authentication, role-based access control, password encoding
- **Config:** Application configuration, CORS, security configuration

## Key Features by Role
Based on the HomeController routing logic:
- **ADMINISTRATOR/MANAGER:** Employee management (`/employees`)
- **TECHNOLOGIST:** Model management (`/models`)
- **MASTER:** Order management (`/orders`)
- **EMPLOYEE:** Work result submission (`/work-results`)
- **ACCOUNTANT:** Salary management (`/salary`)

## Common Development Tasks

### Adding a New Feature
1. Create/modify entity classes in `src/main/java/ee/jvm/nirgi_java/classes/`
2. Create repository interface extending `JpaRepository` in `src/main/java/ee/jvm/nirgi_java/repository/`
3. Create service class in `src/main/java/ee/jvm/nirgi_java/service/`
4. Create controller in `src/main/java/ee/jvm/nirgi_java/controller/`
5. Create Thymeleaf template in `src/main/resources/templates/`
6. Add any needed static resources (CSS/JS) in `src/main/resources/static/`
7. Update HomeController role-based redirects if needed
8. Write unit tests for new components
9. Run full test suite before committing

### Working with Database
- Entities use Lombok annotations (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@ToString`)
- Relationships use standard JPA annotations (`@OneToMany`, `@ManyToOne`, etc.)
- Custom queries can be added to repository interfaces using Spring Data query methods or `@Query` annotations
- Database migrations are handled automatically by Hibernate (update mode)

### Running Tests Effectively
- **Unit Tests:** Focus on service layer logic, use Mockito for mocking dependencies
- **Integration Tests:** Test controller endpoints with `@WebMvcTest` or `@SpringBootTest`
- **Repository Tests:** Test data access layer with `@DataJpaTest`
- **Test Naming Convention:** Use descriptive names like `shouldReturnErrorWhenInputIsInvalid`
- **Coverage:** Aim for high coverage on service and controller layers

### Available Test Commands
```bash
# Run all tests (backend)
docker compose exec app mvn test

# Run tests for specific package (backend)
docker compose exec app mvn test -Dtest=ee.jvm.nirgi_java.service.*;

# Run tests with coverage report (backend)
docker compose exec app mvn test jacoco:report

# Skip tests for faster builds (backend)
docker compose exec app mvn clean package -DskipTests

# Run JS tests locally on Windows host
npm test          # Single run
npx vitest        # Watch mode
npm run test:coverage  # With coverage report

# Run specific JS test file
npx vitest src/main/resources/static/js/work-results.test.js

# Run JS tests in specific directory
npx vitest src/main/resources/static/js/__tests__/
```

### Docker Development Tips
- For rapid frontend development, changes to `src/main/resources/static/` and `src/main/resources/templates/` are immediately reflected due to volume mounts
- For backend changes, you can use `docker compose exec app mvn spring-boot:run` for hot reloading (if configured) or rebuild/restart as needed
- To view logs for a specific service: `docker compose logs -f <service-name>` (e.g., `docker compose logs -f app`)
- To execute one-off commands in the app container: `docker compose exec app <command>` (e.g., `docker compose exec app ls -la`)
- When debugging database issues, you can access the MariaDB container: `docker compose exec mariadb mysql -u${MARIADB_USER} -p${MARIADB_PASSWORD} ${MARIADB_DATABASE}`

## Important Files & Directories
- `.env` - Environment variables for Docker containers (database credentials, etc.)
- `Dockerfile` - Spring application Docker configuration
- `nginx/` - Nginx configuration and SSL certificates
- `mariadb_data/` - Persistent MariaDB data volume
- `target/` - Compiled classes and generated JAR (gitignored)
- `vitest.config.js` - Vitest configuration for JavaScript testing

## Agent Guidelines & Constraints
1. ALWAYS run container tests (`docker compose exec app mvn test`) before committing or finalizing logic changes.
2. Do NOT change dependency versions in `pom.xml` without explicit user permission.
3. **CRITICAL:** Keep your responses, explanations, and code comments in **Russian**.
4. When making changes to static resources or templates, remember they are mounted via volumes - changes appear immediately.
5. Ensure all new endpoints are properly secured with role-based access control.
6. Follow existing patterns for dependency injection (constructor injection preferred).
7. Write meaningful commit messages that explain the why, not just the what.

## Troubleshooting
- If the application fails to start, check logs with `docker compose logs -f app`
- Database connection issues: Verify `.env` file contains correct credentials
- Port conflicts: Ensure ports 80, 443 (Nginx) and 3306 (MariaDB, though not exposed) are free
- Template changes not appearing: Verify you're editing files in `src/main/resources/templates/` (not the container paths)
- Static resource cache issues: Thymeleaf cache is disabled in development (`spring.thymeleaf.cache=false`)
- JavaScript test failures: Ensure Vitest is installed (`npm install`) and check `vitest.config.js` for configuration
- Backend test failures: Check that you're running tests inside the container with `docker compose exec app mvn test`
- Role-based access issues: Verify that endpoints have appropriate `@PreAuthorize` or role checks in controllers