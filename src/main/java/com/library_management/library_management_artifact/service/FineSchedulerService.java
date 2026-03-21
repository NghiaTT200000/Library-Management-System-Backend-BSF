package com.library_management.library_management_artifact.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library_management.library_management_artifact.config.AppProperties;
import com.library_management.library_management_artifact.entity.BorrowRecord;
import com.library_management.library_management_artifact.entity.BorrowStatus;
import com.library_management.library_management_artifact.entity.Fine;
import com.library_management.library_management_artifact.entity.FineStatus;
import com.library_management.library_management_artifact.repository.BorrowRecordRepository;
import com.library_management.library_management_artifact.repository.FineRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FineSchedulerService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final FineRepository fineRepository;
    private final EmailService emailService;
    private final AppProperties appProperties;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void processFines() {
        LocalDate today = LocalDate.now();
        AppProperties.Fine cfg = appProperties.getFine();

        List<BorrowRecord> newlyOverdue =
                borrowRecordRepository.findAllByStatusAndDueDateBefore(BorrowStatus.ACTIVE, today);

        List<BorrowRecord> alreadyOverdue =
                borrowRecordRepository.findAllByStatus(BorrowStatus.OVERDUE);

        log.info("Fine scheduler: {} newly overdue, {} already overdue", newlyOverdue.size(), alreadyOverdue.size());

        for (BorrowRecord record : newlyOverdue) {
            long daysLate = ChronoUnit.DAYS.between(record.getDueDate(), today);
            long daysOverdue = daysLate - cfg.getGracePeriodDays();
            if (daysOverdue <= 0) continue;

            record.setStatus(BorrowStatus.OVERDUE);
            borrowRecordRepository.save(record);

            double amount = Math.min(daysOverdue * cfg.getRatePerDay(), cfg.getMaxAmount());
            String bookTitle = record.getBookItem().getBook().getTitle();

            Fine fine = Fine.builder()
                    .borrowRecord(record)
                    .user(record.getUser())
                    .daysOverdue((int) daysOverdue)
                    .amount(BigDecimal.valueOf(amount))
                    .status(FineStatus.UNPAID)
                    .build();
            fineRepository.save(fine);
            log.info("Created fine for borrow record {} — {} days overdue, {} VND", record.getId(), daysOverdue, (long) amount);
            emailService.sendFineCreatedEmail(record.getUser().getEmail(), record.getUser().getFullName(), bookTitle, (int) daysOverdue, amount);
        }

        for (BorrowRecord record : alreadyOverdue) {
            long daysLate = ChronoUnit.DAYS.between(record.getDueDate(), today);
            long daysOverdue = daysLate - cfg.getGracePeriodDays();
            if (daysOverdue <= 0) continue;

            double amount = Math.min(daysOverdue * cfg.getRatePerDay(), cfg.getMaxAmount());

            fineRepository.findByBorrowRecordId(record.getId()).ifPresent(fine -> {
                if (fine.getStatus() == FineStatus.UNPAID) {
                    fine.setDaysOverdue((int) daysOverdue);
                    fine.setAmount(BigDecimal.valueOf(amount));
                    fineRepository.save(fine);
                    log.info("Updated fine {} — now {} days overdue, {} VND", fine.getId(), daysOverdue, (long) amount);
                }
            });
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void sendUnpaidFineReminders() {
        List<Fine> unpaidFines = fineRepository.findAllByStatus(FineStatus.UNPAID);

        log.info("Fine reminder scheduler: found {} unpaid fines", unpaidFines.size());
        
        for (Fine fine : unpaidFines) {
            String bookTitle = fine.getBorrowRecord().getBookItem().getBook().getTitle();
            emailService.sendUnpaidFineReminderEmail(
                    fine.getUser().getEmail(),
                    fine.getUser().getFullName(),
                    bookTitle,
                    fine.getDaysOverdue(),
                    fine.getAmount().doubleValue()
            );
        }
    }
}
