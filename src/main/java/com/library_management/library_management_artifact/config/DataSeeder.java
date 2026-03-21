package com.library_management.library_management_artifact.config;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.library_management.library_management_artifact.entity.Book;
import com.library_management.library_management_artifact.entity.BookItem;
import com.library_management.library_management_artifact.entity.BookItemStatus;
import com.library_management.library_management_artifact.entity.Category;
import com.library_management.library_management_artifact.entity.Role;
import com.library_management.library_management_artifact.entity.User;
import com.library_management.library_management_artifact.repository.BookItemRepository;
import com.library_management.library_management_artifact.repository.BookRepository;
import com.library_management.library_management_artifact.repository.CategoryRepository;
import com.library_management.library_management_artifact.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookItemRepository bookItemRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        userRepository.save(User.builder()
            .email("admin@library.com")
            .password(passwordEncoder.encode("admin123"))
            .fullName("Library Admin")
            .role(Role.ADMIN)
            .isVerified(true)
            .build());

        userRepository.save(User.builder()
            .email("john@example.com")
            .password(passwordEncoder.encode("member123"))
            .fullName("John Doe")
            .role(Role.MEMBER)
            .isVerified(true)
            .build());

        Category fiction    = categoryRepository.save(new Category("Fiction"));
        Category science    = categoryRepository.save(new Category("Science"));
        Category technology = categoryRepository.save(new Category("Technology"));
        Category selfHelp   = categoryRepository.save(new Category("Self-Help"));
        Category classic    = categoryRepository.save(new Category("Classic"));

        Book cleanCode = bookRepository.save(Book.builder()
            .isbn("978-0-13-468599-1")
            .title("Clean Code")
            .author("Robert C. Martin")
            .publisher("Prentice Hall")
            .publishedYear(2008)
            .description("A handbook of agile software craftsmanship.")
            .categories(Set.of(technology))
            .build());
        addItems(cleanCode, "CC", "T1-S1", 3);

        Book mockingbird = bookRepository.save(Book.builder()
            .isbn("978-0-06-112008-4")
            .title("To Kill a Mockingbird")
            .author("Harper Lee")
            .publishedYear(1960)
            .description("A story of racial injustice and childhood innocence.")
            .categories(Set.of(fiction, classic))
            .build());
        addItems(mockingbird, "TKM", "F2-S3", 2);

        Book gatsby = bookRepository.save(Book.builder()
            .isbn("978-0-7432-7356-5")
            .title("The Great Gatsby")
            .author("F. Scott Fitzgerald")
            .publishedYear(1925)
            .description("A tale of wealth, love, and the American Dream in the 1920s.")
            .categories(Set.of(fiction, classic))
            .build());
        addItems(gatsby, "GG", "F2-S4", 2);

        Book briefHistory = bookRepository.save(Book.builder()
            .isbn("978-0-14-028329-7")
            .title("A Brief History of Time")
            .author("Stephen Hawking")
            .publisher("Bantam Books")
            .publishedYear(1988)
            .description("An introduction to cosmology and the nature of the universe.")
            .categories(Set.of(science))
            .build());
        addItems(briefHistory, "BHT", "S3-S1", 2);

        Book sevenHabits = bookRepository.save(Book.builder()
            .isbn("978-0-7432-3781-6")
            .title("The 7 Habits of Highly Effective People")
            .author("Stephen R. Covey")
            .publisher("Free Press")
            .publishedYear(1989)
            .description("Principles of personal effectiveness and character ethics.")
            .categories(Set.of(selfHelp))
            .build());
        addItems(sevenHabits, "7H", "SH1-S2", 3);

        log.info("Seeded: 2 users, 5 categories, 5 books, 12 book items");
        log.info("Admin:  admin@library.com / admin123");
        log.info("Member: john@example.com  / member123");
    }

    private void addItems(Book book, String prefix, String locationCode, int count) {
        for (int i = 1; i <= count; i++) {
            bookItemRepository.save(BookItem.builder()
                .book(book)
                .itemCode(prefix + "-" + String.format("%03d", i))
                .locationCode(locationCode)
                .acquiredAt(LocalDate.of(2023, 1, 1))
                .status(BookItemStatus.AVAILABLE)
                .build());
        }
    }
}
