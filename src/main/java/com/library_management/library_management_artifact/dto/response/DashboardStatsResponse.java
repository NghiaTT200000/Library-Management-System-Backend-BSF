package com.library_management.library_management_artifact.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalUsers;
    private long totalBooks;
    private long totalBookItems;
    private long activeBorrowings;
    private List<BorrowRecordDetailResponse> recentBorrowings;
}
