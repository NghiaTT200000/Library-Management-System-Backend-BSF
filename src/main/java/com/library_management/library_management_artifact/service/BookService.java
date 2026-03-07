package com.library_management.library_management_artifact.service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library_management.library_management_artifact.dto.request.BookRequest;
import com.library_management.library_management_artifact.dto.response.BookResponse;
import com.library_management.library_management_artifact.entity.Book;
import com.library_management.library_management_artifact.entity.Category;
import com.library_management.library_management_artifact.exception.ResourceNotFoundException;
import com.library_management.library_management_artifact.mapper.BookMapper;
import com.library_management.library_management_artifact.repository.BookRepository;
import com.library_management.library_management_artifact.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;

    public Page<BookResponse> getAll(String search, String author, String categoryName, Pageable pageable) {
        Specification<Book> spec = Specification
                .where(titleContains(search))
                .and(authorContains(author))
                .and(inCategory(categoryName));

        return bookRepository.findAll(spec, pageable).map(bookMapper::toResponse);
    }

    public BookResponse getById(UUID id) {
        return bookMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        Book book = bookMapper.toEntity(request);
        book.setAvailableCopies(request.getTotalCopies());
        book.setCategories(resolveCategories(request.getCategoryNames()));
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse update(UUID id, BookRequest request) {
        Book book = findOrThrow(id);

        int diff = request.getTotalCopies() - book.getTotalCopies();
        book.setAvailableCopies(Math.max(0, book.getAvailableCopies() + diff));

        bookMapper.updateEntity(request, book);
        book.setCategories(resolveCategories(request.getCategoryNames()));
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public void delete(UUID id) {
        bookRepository.delete(findOrThrow(id));
    }

    private Specification<Book> titleContains(String q) {
        return (root, query, cb) -> q == null || q.isBlank() ? null
                : cb.like(cb.lower(root.get("title")), "%" + q.toLowerCase() + "%");
    }

    private Specification<Book> authorContains(String q) {
        return (root, query, cb) -> q == null || q.isBlank() ? null
                : cb.like(cb.lower(root.get("author")), "%" + q.toLowerCase() + "%");
    }

    private Specification<Book> inCategory(String categoryName) {
        return (root, query, cb) -> {
            if (categoryName == null || categoryName.isBlank()) return null;
            var categories = root.join("categories");
            return cb.equal(cb.lower(categories.get("name")), categoryName.toLowerCase());
        };
    }

    private Book findOrThrow(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
    }

    private Set<Category> resolveCategories(Set<String> names) {
        if (names == null || names.isEmpty()) return new HashSet<>();

        return names.stream()
                .map(name -> categoryRepository.findByName(name)
                        .orElseGet(() -> categoryRepository.save(
                                Category.builder().name(name).build())))
                .collect(Collectors.toSet());
    }
}
