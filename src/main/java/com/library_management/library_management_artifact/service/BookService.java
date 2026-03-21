package com.library_management.library_management_artifact.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.library_management.library_management_artifact.config.AppProperties;
import com.library_management.library_management_artifact.dto.request.BookRequest;
import com.library_management.library_management_artifact.dto.response.BookDetailResponse;
import com.library_management.library_management_artifact.dto.response.BookResponse;
import com.library_management.library_management_artifact.entity.Book;
import com.library_management.library_management_artifact.entity.Category;
import com.library_management.library_management_artifact.exception.BadRequestException;
import com.library_management.library_management_artifact.exception.ResourceNotFoundException;
import com.library_management.library_management_artifact.mapper.BookDetailMapper;
import com.library_management.library_management_artifact.mapper.BookMapper;
import com.library_management.library_management_artifact.repository.BookRepository;
import com.library_management.library_management_artifact.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;
    private final BookDetailMapper bookDetailMapper;
    private final Cloudinary cloudinary;
    private final AppProperties appProperties;

    public Page<BookResponse> getAll(String search, String isbn, String author, String categoryName, Pageable pageable) {
        Specification<Book> spec = Specification
                .where(titleContains(search))
                .and(isbnContains(isbn))
                .and(authorContains(author))
                .and(inCategory(categoryName));

        return bookRepository.findAll(spec, pageable).map(bookMapper::toResponse);
    }

    public BookDetailResponse getById(String isbn) {
        return bookDetailMapper.toDetailResponse(findOrThrow(isbn));
    }

    @Transactional
    public BookDetailResponse create(BookRequest request, MultipartFile file) {
        if (bookRepository.existsById(request.getIsbn())) {
            throw new BadRequestException("ISBN already exists: " + request.getIsbn());
        }
        Book book = bookMapper.toEntity(request);
        book.setCategories(resolveCategories(request.getCategoryNames()));
        book = bookRepository.save(book);
        if (file != null && !file.isEmpty()) {
            book.setCoverImageUrl(uploadImage(book.getIsbn(), file));
            book = bookRepository.save(book);
        }
        return bookDetailMapper.toDetailResponse(book);
    }

    @Transactional
    public BookDetailResponse update(String isbn, BookRequest request, MultipartFile file) {
        Book book = findOrThrow(isbn);
        bookMapper.updateEntity(request, book);
        book.setCategories(resolveCategories(request.getCategoryNames()));
        if (file != null && !file.isEmpty()) {
            book.setCoverImageUrl(uploadImage(isbn, file));
        }
        return bookDetailMapper.toDetailResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse activate(String isbn) {
        Book book = findOrThrow(isbn);
        book.setActive(true);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse deactivate(String isbn) {
        Book book = findOrThrow(isbn);
        book.setActive(false);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public void delete(String isbn) {
        bookRepository.delete(findOrThrow(isbn));
    }

    private String uploadImage(String bookIsbn, MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("File must be an image");
        }
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", appProperties.getCloudinary().getUploadFolder(),
                            "public_id", bookIsbn,
                            "overwrite", true));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new BadRequestException("Image upload failed: " + e.getMessage());
        }
    }

    private Specification<Book> titleContains(String q) {
        return (root, query, cb) -> q == null || q.isBlank() ? null
                : cb.like(cb.lower(root.get("title")), "%" + q.toLowerCase() + "%");
    }

    private Specification<Book> isbnContains(String q) {
        return (root, query, cb) -> q == null || q.isBlank() ? null
                : cb.like(root.get("isbn"), "%" + q + "%");
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

    private Book findOrThrow(String isbn) {
        return bookRepository.findById(isbn)
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
