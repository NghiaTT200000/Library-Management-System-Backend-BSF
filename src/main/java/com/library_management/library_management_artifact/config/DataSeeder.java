package com.library_management.library_management_artifact.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.library_management.library_management_artifact.entity.Book;
import com.library_management.library_management_artifact.entity.Category;
import com.library_management.library_management_artifact.entity.Role;
import com.library_management.library_management_artifact.entity.User;
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

        bookRepository.save(Book.builder()
            .isbn("978-0-7432-7356-5")
            .title("The Great Gatsby")
            .author("F. Scott Fitzgerald")
            .publishedYear(1925)
            .description("A tale of wealth, love, and the American Dream in the 1920s.")
            .totalCopies(2).availableCopies(2)
            .categories(Set.of(fiction, classic))
            .build());

        bookRepository.save(Book.builder()
            .isbn("978-0-14-028329-7")
            .title("A Brief History of Time")
            .author("Stephen Hawking")
            .publisher("Bantam Books")
            .publishedYear(1988)
            .description("An introduction to cosmology and the nature of the universe.")
            .totalCopies(2).availableCopies(2)
            .categories(Set.of(science))
            .build());

        bookRepository.save(Book.builder()
            .isbn("978-0-7432-3781-6")
            .title("The 7 Habits of Highly Effective People")
            .author("Stephen R. Covey")
            .publisher("Free Press")
            .publishedYear(1989)
            .description("Principles of personal effectiveness and character ethics.")
            .totalCopies(3).availableCopies(3)
            .categories(Set.of(selfHelp))
            .build());

        log.info("Seeded: 2 users, 5 categories, 5 books");
        log.info("Admin:  admin@library.com / admin123");
        log.info("Member: john@example.com  / member123");
    }
}
