package com.library_management.library_management_artifact.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library_management.library_management_artifact.dto.response.FineDetailResponse;
import com.library_management.library_management_artifact.entity.Fine;
import com.library_management.library_management_artifact.entity.FineStatus;
import com.library_management.library_management_artifact.entity.Role;
import com.library_management.library_management_artifact.entity.User;
import com.library_management.library_management_artifact.exception.BadRequestException;
import com.library_management.library_management_artifact.exception.ForbiddenException;
import com.library_management.library_management_artifact.exception.ResourceNotFoundException;
import com.library_management.library_management_artifact.mapper.FineDetailMapper;
import com.library_management.library_management_artifact.repository.FineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FineService {

    private final FineRepository fineRepository;
    private final FineDetailMapper fineDetailMapper;

    public Page<FineDetailResponse> getAll(String userEmail, String itemCode, String bookTitle, Pageable pageable) {
        return fineRepository.searchAll(userEmail, itemCode, bookTitle, pageable)
                .map(fineDetailMapper::toDetailResponse);
    }

    public Page<FineDetailResponse> getMyFines(User currentUser, String itemCode, String bookTitle, Pageable pageable) {
        return fineRepository.searchByUser(currentUser.getId(), itemCode, bookTitle, pageable)
                .map(fineDetailMapper::toDetailResponse);
    }

    @Transactional
    public FineDetailResponse pay(UUID id) {
        Fine fine = findOrThrow(id);
        if (fine.getStatus() == FineStatus.PAID) {
            throw new BadRequestException("This fine has already been paid");
        }
        fine.setStatus(FineStatus.PAID);
        return fineDetailMapper.toDetailResponse(fineRepository.save(fine));
    }

    public FineDetailResponse getByIdForUser(UUID id, User currentUser) {
        Fine fine = findOrThrow(id);
        if (currentUser.getRole() != Role.ADMIN && !fine.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only view your own fines");
        }
        return fineDetailMapper.toDetailResponse(fine);
    }

    private Fine findOrThrow(UUID id) {
        return fineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found"));
    }
}
