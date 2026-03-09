package com.library_management.library_management_artifact.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library_management.library_management_artifact.config.AppProperties;
import com.library_management.library_management_artifact.dto.request.BorrowRequest;
import com.library_management.library_management_artifact.dto.response.BorrowRecordDetailResponse;
import com.library_management.library_management_artifact.entity.BookItem;
import com.library_management.library_management_artifact.entity.BookItemStatus;
import com.library_management.library_management_artifact.entity.BorrowRecord;
import com.library_management.library_management_artifact.entity.BorrowStatus;
import com.library_management.library_management_artifact.entity.Role;
import com.library_management.library_management_artifact.entity.User;
import com.library_management.library_management_artifact.exception.BadRequestException;
import com.library_management.library_management_artifact.exception.ForbiddenException;
import com.library_management.library_management_artifact.exception.ResourceNotFoundException;
import com.library_management.library_management_artifact.mapper.BorrowRecordDetailMapper;
import com.library_management.library_management_artifact.repository.BookItemRepository;
import com.library_management.library_management_artifact.repository.BorrowRecordRepository;
import com.library_management.library_management_artifact.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookItemRepository bookItemRepository;
    private final UserRepository userRepository;
    private final BorrowRecordDetailMapper borrowRecordDetailMapper;
    private final AppProperties appProperties;

    @Transactional
    public BorrowRecordDetailResponse borrow(BorrowRequest request) {
        User borrowUser = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BookItem item = bookItemRepository.findById(request.getBookItemId())
            .orElseThrow(() -> new ResourceNotFoundException("Book item not found"));

        if (item.getStatus() != BookItemStatus.AVAILABLE) {
            throw new BadRequestException("Book item is not available for borrowing");
        }

        LocalDate today = LocalDate.now();
        BorrowRecord record = BorrowRecord.builder()
                .user(borrowUser)
                .bookItem(item)
                .borrowedAt(today)
                .dueDate(today.plusDays(appProperties.getFine().getLoanPeriodDays()))
                .build();

        item.setStatus(BookItemStatus.BORROWED);
        bookItemRepository.save(item);

        return borrowRecordDetailMapper.toDetailResponse(borrowRecordRepository.save(record));
    }

    @Transactional
    public BorrowRecordDetailResponse returnBook(UUID recordId) {
        BorrowRecord record = findOrThrow(recordId);

        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new BadRequestException("This book has already been returned");
        }

        LocalDate today = LocalDate.now();
        record.setReturnedAt(today);
        record.setStatus(BorrowStatus.RETURNED);

        BookItem item = record.getBookItem();
        item.setStatus(BookItemStatus.AVAILABLE);
        bookItemRepository.save(item);

        return borrowRecordDetailMapper.toDetailResponse(borrowRecordRepository.save(record));
    }

    public Page<BorrowRecordDetailResponse> getAll(Pageable pageable) {
        return borrowRecordRepository.findAll(pageable)
                .map(borrowRecordDetailMapper::toDetailResponse);
    }

    public Page<BorrowRecordDetailResponse> getMyBorrows(User currentUser, Pageable pageable) {
        return borrowRecordRepository.findByUserId(currentUser.getId(), pageable)
                .map(borrowRecordDetailMapper::toDetailResponse);
    }

    public BorrowRecordDetailResponse getById(UUID id, User currentUser) {
        BorrowRecord record = findOrThrow(id);
        if (currentUser.getRole() != Role.ADMIN && !record.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only view your own borrow records");
        }
        return borrowRecordDetailMapper.toDetailResponse(record);
    }

    public List<BorrowRecordDetailResponse> getByItemId(UUID itemId) {
        return borrowRecordRepository.findByBookItemId(itemId).stream()
                .map(borrowRecordDetailMapper::toDetailResponse)
                .toList();
    }

    @Transactional
    public BorrowRecordDetailResponse borrowOverdue(UUID bookItemId, UUID userId, int daysOverdueBy) {
        User borrowUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BookItem item = bookItemRepository.findById(bookItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Book item not found"));

        if (item.getStatus() != BookItemStatus.AVAILABLE) {
            throw new BadRequestException("Book item is not available for borrowing");
        }

        LocalDate dueDate = LocalDate.now().minusDays(daysOverdueBy);
        BorrowRecord record = BorrowRecord.builder()
                .user(borrowUser)
                .bookItem(item)
                .borrowedAt(dueDate.minusDays(appProperties.getFine().getLoanPeriodDays()))
                .dueDate(dueDate)
                .build();

        item.setStatus(BookItemStatus.BORROWED);
        bookItemRepository.save(item);

        return borrowRecordDetailMapper.toDetailResponse(borrowRecordRepository.save(record));
    }

    private BorrowRecord findOrThrow(UUID id) {
        return borrowRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found"));
    }
}
