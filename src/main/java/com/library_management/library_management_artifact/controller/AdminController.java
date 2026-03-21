package com.library_management.library_management_artifact.controller;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library_management.library_management_artifact.constant.ApiMessage;
import com.library_management.library_management_artifact.dto.request.CreateUserRequest;
import com.library_management.library_management_artifact.dto.response.ApiResponse;
import com.library_management.library_management_artifact.dto.response.BorrowRecordDetailResponse;
import com.library_management.library_management_artifact.dto.response.DashboardStatsResponse;
import com.library_management.library_management_artifact.dto.response.UserResponse;
import com.library_management.library_management_artifact.repository.BookItemRepository;
import com.library_management.library_management_artifact.repository.BookRepository;
import com.library_management.library_management_artifact.service.AuthService;
import com.library_management.library_management_artifact.service.BorrowService;
import com.library_management.library_management_artifact.service.FineSchedulerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AuthService authService;
    private final BorrowService borrowService;
    private final FineSchedulerService fineSchedulerService;
    private final BookRepository bookRepository;
    private final BookItemRepository bookItemRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboard() {
        DashboardStatsResponse data = DashboardStatsResponse.builder()
                .totalUsers(authService.countUsers())
                .totalBooks(bookRepository.count())
                .totalBookItems(bookItemRepository.count())
                .activeBorrowings(borrowService.countActiveBorrows())
                .recentBorrowings(borrowService.getRecentBorrows(5))
                .build();
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats fetched", data));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse data = authService.createUser(request);
        return ResponseEntity
                .status(ApiMessage.USER_CREATED.getStatus())
                .body(ApiResponse.success(ApiMessage.USER_CREATED.getMessage(), data));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> data = authService.getAllUsers();
        return ResponseEntity
                .status(ApiMessage.USERS_FETCHED.getStatus())
                .body(ApiResponse.success(ApiMessage.USERS_FETCHED.getMessage(), data));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        authService.deactivateUser(id);
        return ResponseEntity
                .status(ApiMessage.USER_DEACTIVATED.getStatus())
                .body(ApiResponse.success(ApiMessage.USER_DEACTIVATED.getMessage()));
    }

    @PostMapping("/test/overdue-borrow")
    public ResponseEntity<ApiResponse<BorrowRecordDetailResponse>> createOverdueBorrow(
            @RequestParam UUID bookItemId,
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "5") int daysOverdueBy
    ) {
        BorrowRecordDetailResponse data = borrowService.borrowOverdue(bookItemId, userId, daysOverdueBy);
        return ResponseEntity.ok(ApiResponse.success("Overdue borrow record created for testing", data));
    }

    @PostMapping("/scheduler/process-fines")
    public ResponseEntity<ApiResponse<Void>> triggerProcessFines() {
        fineSchedulerService.processFines();
        return ResponseEntity.ok(ApiResponse.success("Fine processing job triggered successfully"));
    }

    @PostMapping("/scheduler/send-reminders")
    public ResponseEntity<ApiResponse<Void>> triggerSendReminders() {
        fineSchedulerService.sendUnpaidFineReminders();
        return ResponseEntity.ok(ApiResponse.success("Unpaid fine reminder job triggered successfully"));
    }
}
