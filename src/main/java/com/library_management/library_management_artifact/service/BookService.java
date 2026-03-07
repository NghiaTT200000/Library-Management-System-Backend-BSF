package com.library_management.library_management_artifact.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import com.library_management.library_management_artifact.entity.BookItemStatus;
import com.library_management.library_management_artifact.entity.Category;
import com.library_management.library_management_artifact.exception.BadRequestException;
import com.library_management.library_management_artifact.exception.ResourceNotFoundException;
import com.library_management.library_management_artifact.mapper.BookDetailMapper;
import com.library_management.library_management_artifact.mapper.BookMapper;
import com.library_management.library_management_artifact.repository.BookRepository;
import com.library_management.library_management_artifact.repository.CategoryRepository;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;
    private final BookDetailMapper bookDetailMapper;
    private final Cloudinary cloudinary;
    private final AppProperties appProperties;

    public Page<BookResponse> getAll(String search, String author, String categoryName,
                                     Boolean available, Pageable pageable) {
        Specification<Book> spec = Specification
                .where(titleContains(search))
                .and(authorContains(author))
                .and(inCategory(categoryName))
                .and(hasAvailableItems(available));

        return bookRepository.findAll(spec, pageable).map(bookMapper::toResponse);
    }

    public BookResponse getById(UUID id) {
        return bookMapper.toResponse(findOrThrow(id));
    }

    public BookDetailResponse getDetailById(UUID id) {
        return bookDetailMapper.toDetailResponse(findOrThrow(id));
    }

    @Transactional
    public BookResponse create(BookRequest request, MultipartFile file) {
        Book book = bookMapper.toEntity(request);
        book.setCategories(resolveCategories(request.getCategoryNames()));
        book = bookRepository.save(book);
        if (file != null && !file.isEmpty()) {
            book.setCoverImageUrl(uploadImage(book.getId(), file));
            book = bookRepository.save(book);
        }
        return bookMapper.toResponse(book);
    }

    @Transactional
    public BookResponse update(UUID id, BookRequest request, MultipartFile file) {
        Book book = findOrThrow(id);
        bookMapper.updateEntity(request, book);
        book.setCategories(resolveCategories(request.getCategoryNames()));
        if (file != null && !file.isEmpty()) {
            book.setCoverImageUrl(uploadImage(id, file));
        }
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public void delete(UUID id) {
        bookRepository.delete(findOrThrow(id));
    }

    private String uploadImage(UUID bookId, MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("File must be an image");
        }
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", appProperties.getCloudinary().getUploadFolder(),
                            "public_id", bookId.toString(),
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

    private Specification<Book> hasAvailableItems(Boolean available) {
        return (root, query, cb) -> {
            if (available == null || !available) return null;
            if (query != null) query.distinct(true);
            var items = root.join("items", JoinType.INNER);
            return cb.equal(items.get("status"), BookItemStatus.AVAILABLE);
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
