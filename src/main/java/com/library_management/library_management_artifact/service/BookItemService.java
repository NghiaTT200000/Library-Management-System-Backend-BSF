package com.library_management.library_management_artifact.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library_management.library_management_artifact.dto.request.BookItemRequest;
import com.library_management.library_management_artifact.dto.response.BookItemDetailResponse;
import com.library_management.library_management_artifact.dto.response.BookItemResponse;
import com.library_management.library_management_artifact.entity.BookItem;
import com.library_management.library_management_artifact.entity.BookItemStatus;
import com.library_management.library_management_artifact.entity.ItemCondition;
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
    public List<BookItemResponse> getByBookId(UUID bookId) {
        return bookItemMapper.toResponseList(bookItemRepository.findByBookId(bookId));
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
                .book(bookRepository.findById(request.getBookId())
                        .orElseThrow(() -> new ResourceNotFoundException("Book not found")))
                .itemCode(request.getItemCode())
                .locationCode(request.getLocationCode())
                .description(request.getDescription())
                .acquiredAt(request.getAcquiredAt())
                .condition(request.getCondition() != null ? request.getCondition() : ItemCondition.GOOD)
                .status(request.getStatus() != null ? request.getStatus() : BookItemStatus.AVAILABLE)
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
        if (request.getCondition() != null) item.setCondition(request.getCondition());
        if (request.getStatus() != null) item.setStatus(request.getStatus());

        return bookItemMapper.toResponse(bookItemRepository.save(item));
    }

    @Transactional
    public void delete(UUID id) {
        bookItemRepository.delete(findOrThrow(id));
    }

    private BookItem findOrThrow(UUID id) {
        return bookItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book item not found"));
    }
}
