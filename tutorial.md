# 📚 Library Management System — Spring Boot Setup Guide

---

## 1. Create the Project

Go to **https://start.spring.io** and configure:

| Field | Value |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.2.3 |
| Group | com.library |
| Artifact | library-backend |
| Packaging | Jar |
| Java | 17 |

**Dependencies to add:**
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL Driver
- Validation
- Lombok

Click **Generate** → unzip → open in IntelliJ or VS Code.

Then manually add to `pom.xml` (JWT + OkHttp not on Spring Initializr):

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- HTTP client for Claude + Voyage AI -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>
```

---

## 2. Configure the Database

Create your PostgreSQL database:
```bash
createdb library_db
```

`src/main/resources/application.yml`:
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/library_db
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: update       # auto-creates/updates tables from your entities
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

app:
  jwt:
    secret: ${JWT_SECRET:3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b}
    expiration-ms: 86400000          # 1 day
    refresh-expiration-ms: 604800000 # 7 days
  claude:
    api-key: ${CLAUDE_API_KEY}
    base-url: https://api.anthropic.com
    model: claude-sonnet-4-20250514
    max-tokens: 1024
  voyage:
    api-key: ${CLAUDE_API_KEY}       # same key works for Voyage AI
    model: voyage-3
  rag:
    top-k: 5
  fine:
    rate-per-day: 0.50
    max-amount: 10.00
    grace-period-days: 2
    loan-period-days: 14
```

---

## 3. Package Structure

```
src/main/java/com/library/
├── LibraryApplication.java
│
├── config/
│   ├── ClaudeConfig.java          # @ConfigurationProperties for Claude + Voyage
│   ├── SecurityConfig.java        # JWT filter chain, CORS, role rules
│   └── DataSeeder.java            # CommandLineRunner — seeds DB on first run
│
├── entity/
│   ├── User.java                  # implements UserDetails
│   ├── Book.java                  # has Set<Category> @ManyToMany
│   ├── Category.java
│   ├── BorrowRecord.java
│   ├── Fine.java
│   ├── Role.java                  # enum: ADMIN, MEMBER
│   ├── BorrowStatus.java          # enum: ACTIVE, RETURNED, OVERDUE
│   └── FineStatus.java            # enum: UNPAID, PAID, WAIVED
│
├── repository/
│   ├── UserRepository.java
│   ├── BookRepository.java
│   ├── CategoryRepository.java
│   ├── BorrowRecordRepository.java
│   └── FineRepository.java
│
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── BookRequest.java       # includes List<Long> categoryIds
│   │   └── CategoryRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── BookResponse.java      # includes List<String> categories
│       ├── CategoryResponse.java
│       ├── BorrowResponse.java
│       ├── FineResponse.java
│       ├── SummaryResponse.java   # AI
│       └── RecommendationResponse.java  # AI
│
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   ├── ForbiddenException.java
│   └── GlobalExceptionHandler.java
│
├── security/
│   ├── JwtUtils.java
│   └── JwtAuthenticationFilter.java
│
└── service/
    ├── AuthService.java           # also implements UserDetailsService
    ├── BookService.java           # CRUD + triggers async embedding
    ├── CategoryService.java
    ├── BorrowService.java         # borrow/return + fine calculation
    ├── FineService.java
    ├── EmbeddingService.java      # Voyage AI calls + cosine similarity
    ├── ClaudeService.java         # Claude API calls
    └── AiService.java             # recommend() + getSummary()
```

---

## 4. How JPA Creates Your Tables

With `ddl-auto: update`, **Spring creates your SQL tables automatically from your Java entities**. You do NOT need to run SQL manually.

Here's how each table maps:

### `users` table ← `User.java`
```java
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;          // → full_name (snake_case auto-converted)

    private String phoneNumber;       // → phone_number

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.MEMBER;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;  // → created_at
}
```

### `books` table ← `Book.java`
```java
@Entity
@Table(name = "books")
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String isbn;

    @Column(nullable = false) private String title;
    @Column(nullable = false) private String author;
    private String publisher;
    private Integer publishedYear;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String coverImageUrl;

    @Column(nullable = false) @Builder.Default
    private Integer totalCopies = 1;

    @Column(nullable = false) @Builder.Default
    private Integer availableCopies = 1;

    // RAG fields
    @Column(columnDefinition = "TEXT") private String descriptionEmbedding;
    @Column(columnDefinition = "TEXT") private String aiSummary;

    // Many-to-many with Category
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_categories",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### `categories` table ← `Category.java`
```java
@Entity
@Table(name = "categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private LocalDateTime createdAt;
}
```
> The `book_categories` join table is **auto-created** by the `@JoinTable` annotation — no separate entity needed.

### `borrow_records` table ← `BorrowRecord.java`
```java
@Entity
@Table(name = "borrow_records")
public class BorrowRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private LocalDate borrowedAt;
    private LocalDate dueDate;
    private LocalDate returnedAt;    // null until returned

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BorrowStatus status = BorrowStatus.ACTIVE;

    private LocalDateTime createdAt;
}
```

### `fines` table ← `Fine.java`
```java
@Entity
@Table(name = "fines")
public class Fine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "borrow_record_id", nullable = false)
    private BorrowRecord borrowRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer daysOverdue;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FineStatus status = FineStatus.UNPAID;

    private LocalDateTime createdAt;
}
```

---

## 5. Data Seeder (CommandLineRunner)

This runs once on startup and seeds the database only if it's empty:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return; // skip if already seeded

        // --- Users ---
        User admin = userRepository.save(User.builder()
            .email("admin@library.com")
            .password(passwordEncoder.encode("admin123"))
            .fullName("Library Admin")
            .role(Role.ADMIN)
            .build());

        User member = userRepository.save(User.builder()
            .email("john@example.com")
            .password(passwordEncoder.encode("member123"))
            .fullName("John Doe")
            .role(Role.MEMBER)
            .build());

        // --- Categories ---
        Category fiction    = categoryRepository.save(new Category("Fiction"));
        Category science    = categoryRepository.save(new Category("Science"));
        Category technology = categoryRepository.save(new Category("Technology"));
        Category selfHelp   = categoryRepository.save(new Category("Self-Help"));
        Category classic    = categoryRepository.save(new Category("Classic"));

        // --- Books ---
        bookRepository.save(Book.builder()
            .isbn("978-0-13-468599-1")
            .title("Clean Code")
            .author("Robert C. Martin")
            .publisher("Prentice Hall")
            .publishedYear(2008)
            .description("A handbook of agile software craftsmanship...")
            .totalCopies(3).availableCopies(3)
            .categories(Set.of(technology))
            .build());

        bookRepository.save(Book.builder()
            .isbn("978-0-06-112008-4")
            .title("To Kill a Mockingbird")
            .author("Harper Lee")
            .publishedYear(1960)
            .description("A story of racial injustice and childhood innocence...")
            .totalCopies(2).availableCopies(2)
            .categories(Set.of(fiction, classic))
            .build());

        // ... add more books

        log.info("Seeded: 2 users, 5 categories, 2 books");
        log.info("Admin: admin@library.com / admin123");
        log.info("Member: john@example.com / member123");
    }
}
```

---

## 6. API Endpoints

### Auth
| Method | Endpoint | Access | Body |
|---|---|---|---|
| POST | `/api/auth/register` | Public | `{ email, password, fullName }` |
| POST | `/api/auth/login` | Public | `{ email, password }` |

### Books
| Method | Endpoint | Access | Notes |
|---|---|---|---|
| GET | `/api/books` | Public | `?search=clean&categoryId=1&page=0&size=10` |
| GET | `/api/books/{id}` | Public | Includes categories |
| POST | `/api/books` | ADMIN | Body includes `categoryIds: [1, 2]` |
| PUT | `/api/books/{id}` | ADMIN | Re-embeds if description changed |
| DELETE | `/api/books/{id}` | ADMIN | |

### Categories
| Method | Endpoint | Access | Notes |
|---|---|---|---|
| GET | `/api/categories` | Public | List all |
| POST | `/api/categories` | ADMIN | `{ name }` |
| PUT | `/api/categories/{id}` | ADMIN | Rename |
| DELETE | `/api/categories/{id}` | ADMIN | |

### Borrowing
| Method | Endpoint | Access | Notes |
|---|---|---|---|
| POST | `/api/borrows/borrow/{bookId}` | MEMBER | Checks availability + fine status |
| POST | `/api/borrows/return/{borrowId}` | MEMBER | Auto-calculates fine if overdue |
| GET | `/api/borrows/my` | MEMBER | My active + history |
| GET | `/api/borrows` | ADMIN | All records, filterable |

### Fines
| Method | Endpoint | Access | Notes |
|---|---|---|---|
| GET | `/api/fines/my` | MEMBER | My unpaid fines |
| GET | `/api/fines` | ADMIN | All fines |
| PATCH | `/api/fines/{id}/pay` | MEMBER | Mark as paid |
| PATCH | `/api/fines/{id}/waive` | ADMIN | Waive a fine |

### AI (RAG)
| Method | Endpoint | Access | Notes |
|---|---|---|---|
| GET | `/api/ai/recommend?q=...&limit=5` | MEMBER | Semantic book recommendations |
| GET | `/api/ai/books/{id}/summary` | MEMBER | Claude summary (cached) |

---

## 7. Build Order (recommended)

Follow this order so each layer builds on the previous:

```
1. Entities + Enums          → JPA auto-creates tables
2. Repositories              → just interfaces, zero code
3. Security (JWT)            → JwtUtils, Filter, SecurityConfig
4. AuthService + Controller  → register/login working
5. CategoryService + Controller
6. BookService + Controller  → CRUD without AI first
7. BorrowService + Controller
8. FineService + Controller
9. EmbeddingService          → Voyage AI
10. ClaudeService            → Claude API
11. AiService + AiController → wire RAG together
12. DataSeeder               → seed test data
```

---

## 8. Testing the API

Use **Postman** or **curl**:

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123","fullName":"Test User"}'

# Login → copy the accessToken
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@library.com","password":"admin123"}'

# Get books (public)
curl http://localhost:8080/api/books

# AI recommend (need token)
curl -H "Authorization: Bearer YOUR_TOKEN" \
  "http://localhost:8080/api/ai/recommend?q=books+about+software+engineering"
```