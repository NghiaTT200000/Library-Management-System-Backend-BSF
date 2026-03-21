package com.library_management.library_management_artifact.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library_management.library_management_artifact.dto.request.BookItemRequest;
import com.library_management.library_management_artifact.dto.response.BookItemDetailResponse;
import com.library_management.library_management_artifact.dto.response.BookItemResponse;
import com.library_management.library_management_artifact.entity.BookItem;
import com.library_management.library_management_artifact.entity.BookItemStatus;
import com.library_management.library_management_artifact.exception.BadRequestException;
import com.library_management.library_management_artifact.exception.ResourceNotFoundException;
import com.library_management.library_management_artifact.mapper.BookItemDetailMapper;
import com.library_management.library_management_artifact.mapper.BookItemMapper;
import com.library_management.library_management_artifact.repository.BookItemRepository;
import com.library_management.library_management_artifact.repository.BookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookItemService {

    private final BookItemRepository bookItemRepository;
    private final BookRepository bookRepository;
    private final BookItemMapper bookItemMapper;
    private final BookItemDetailMapper bookItemDetailMapper;

    @Transactional(readOnly = true)
    public Page<BookItemResponse> getAll(String bookIsbn, String itemCode, String status, Pageable pageable) {
        Specification<BookItem> spec = Specification
                .where(bookIsbnContains(bookIsbn))
                .and(itemCodeContains(itemCode))
                .and(statusEquals(status));
        return bookItemRepository.findAll(spec, pageable).map(bookItemMapper::toResponse);
    }

    private Specification<BookItem> bookIsbnContains(String isbn) {
        return (root, query, cb) -> isbn == null || isbn.isBlank() ? null
                : cb.like(root.get("book").get("isbn"), "%" + isbn + "%");
    }

    private Specification<BookItem> itemCodeContains(String code) {
        return (root, query, cb) -> code == null || code.isBlank() ? null
                : cb.like(cb.lower(root.get("itemCode")), "%" + code.toLowerCase() + "%");
    }

    private Specification<BookItem> statusEquals(String status) {
        if (status == null || status.isBlank()) return (root, query, cb) -> null;
        try {
            BookItemStatus s = BookItemStatus.valueOf(status);
            return (root, query, cb) -> cb.equal(root.get("status"), s);
        } catch (IllegalArgumentException e) {
            return (root, query, cb) -> null;
        }
    }

    @Transactional(readOnly = true)
    public List<BookItemResponse> getByBookIsbn(String isbn) {
        return bookItemMapper.toResponseList(bookItemRepository.findByBookIsbn(isbn));
    }

    @Transactional(readOnly = true)
    public BookItemDetailResponse getById(UUID id) {
        return bookItemDetailMapper.toDetailResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public BookItemDetailResponse getByItemCode(String itemCode) {
        return bookItemDetailMapper.toDetailResponse(
                bookItemRepository.findByItemCode(itemCode)
                        .orElseThrow(() -> new ResourceNotFoundException("Book item not found")));
    }

    @Transactional
    public BookItemResponse create(BookItemRequest request) {
        if (bookItemRepository.existsByItemCode(request.getItemCode())) {
            throw new BadRequestException("Item code already exists");
        }

        BookItem item = BookItem.builder()
                .book(bookRepository.findById(request.getBookIsbn())
                        .orElseThrow(() -> new ResourceNotFoundException("Book not found")))
                .itemCode(request.getItemCode())
                .locationCode(request.getLocationCode())
                .description(request.getDescription())
                .acquiredAt(request.getAcquiredAt())
                .build();

        return bookItemMapper.toResponse(bookItemRepository.save(item));
    }

    @Transactional
    public BookItemResponse update(UUID id, BookItemRequest request) {
        BookItem item = findOrThrow(id);

        if (!item.getItemCode().equals(request.getItemCode())
                && bookItemRepository.existsByItemCode(request.getItemCode())) {
            throw new BadRequestException("Item code already exists");
        }

        item.setItemCode(request.getItemCode());
        item.setLocationCode(request.getLocationCode());
        item.setDescription(request.getDescription());
        item.setAcquiredAt(request.getAcquiredAt());

        return bookItemMapper.toResponse(bookItemRepository.save(item));
    }

    @Transactional
    public void delete(UUID id) {
        BookItem item = findOrThrow(id);
        if (item.getStatus() == BookItemStatus.BORROWED) {
            throw new BadRequestException("Cannot retire a book item that is currently borrowed");
        }
        item.setStatus(BookItemStatus.RETIRED);
        bookItemRepository.save(item);
    }

    @Transactional
    public BookItemResponse activate(UUID id) {
        BookItem item = findOrThrow(id);
        if (item.getStatus() != BookItemStatus.RETIRED) {
            throw new BadRequestException("Only retired book items can be reactivated");
        }
        item.setStatus(BookItemStatus.AVAILABLE);
        return bookItemMapper.toResponse(bookItemRepository.save(item));
    }

    private BookItem findOrThrow(UUID id) {
        return bookItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book item not found"));
    }
}
