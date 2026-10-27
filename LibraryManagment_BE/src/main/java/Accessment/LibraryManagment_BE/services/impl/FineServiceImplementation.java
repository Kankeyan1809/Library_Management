package Accessment.LibraryManagment_BE.services.impl;

import Accessment.LibraryManagment_BE.dtos.request.FineCalculationRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.FineDetailDto;
import Accessment.LibraryManagment_BE.dtos.response.StudentFineResponseDto;
import Accessment.LibraryManagment_BE.entity.BorrowRecord;
import Accessment.LibraryManagment_BE.entity.Student;
import Accessment.LibraryManagment_BE.exception.BusinessException;
import Accessment.LibraryManagment_BE.repositories.BorrowRecordRepository;
import Accessment.LibraryManagment_BE.repositories.StudentRepository;
import Accessment.LibraryManagment_BE.services.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FineServiceImplementation implements FineService {

    private final StudentRepository studentRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    @Override
    @Transactional
    public StudentFineResponseDto getStudentFine(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("Student not found"));

        List<BorrowRecord> records = borrowRecordRepository.findByStudentStudentId(studentId);
        records.sort(Comparator.comparing(BorrowRecord::getBorrowRecordId));

        LocalDate today = LocalDate.now();
        List<FineDetailDto> fineDetails = new ArrayList<>();

        double grandTotalFine = 0.0;
        double grandTotalPaid = 0.0;
        double grandTotalUnpaid = 0.0;

        for (BorrowRecord record : records) {
            long borrowedQty = record.getInitialQuantity() != null && record.getInitialQuantity() > 0
                    ? record.getInitialQuantity()
                    : (record.getNumberOfBooks() != null && record.getNumberOfBooks() > 0 ? record.getNumberOfBooks() : 1L);

            long outstandingQty = record.getReturnDate() != null
                    ? 0L
                    : (record.getNumberOfBooks() != null ? record.getNumberOfBooks() : 0L);

            LocalDate borrowDate = record.getBorrowDate();
            LocalDate dueDate = borrowDate != null ? borrowDate.plusDays(3) : null;
            LocalDate returnDate = record.getReturnDate();
            LocalDate effectiveDate = returnDate != null ? returnDate : today;

            long daysSinceBorrowing = borrowDate != null
                    ? Math.max(0, ChronoUnit.DAYS.between(borrowDate, effectiveDate))
                    : 0L;

            long lateDays = dueDate != null && effectiveDate.isAfter(dueDate)
                    ? Math.max(0, ChronoUnit.DAYS.between(dueDate, effectiveDate))
                    : 0L;

            double fineForOneBook = lateDays * 10.0;
            double baseFine = borrowedQty * 50.0;
            double lateFine = borrowedQty * lateDays * 10.0;
            double totalFine = baseFine + lateFine;

            double paidAmount = record.getPaidAmount() != null
                    ? record.getPaidAmount()
                    : (Boolean.TRUE.equals(record.getPaid()) ? totalFine : 0.0);

            paidAmount = Math.max(0.0, Math.min(totalFine, paidAmount));
            boolean isPaid = paidAmount >= totalFine;
            double unpaidAmount = Math.max(0.0, totalFine - paidAmount);

            grandTotalFine += totalFine;
            grandTotalPaid += paidAmount;
            grandTotalUnpaid += unpaidAmount;

            String paymentStatus;
            if (isPaid) {
                paymentStatus = "PAID";
            } else if (paidAmount > 0.0) {
                paymentStatus = "PARTIALLY_PAID";
            } else {
                paymentStatus = "UNPAID";
            }

            record.setFineAmount(totalFine);
            record.setPaidAmount(paidAmount);
            record.setBalanceAmount(unpaidAmount);
            record.setPaymentStatus(paymentStatus);
            record.setPaid(isPaid);
            borrowRecordRepository.saveAndFlush(record);

            String bookTitle = record.getBook() != null ? record.getBook().getBookTitle() : null;
            Long bookId = record.getBook() != null ? record.getBook().getBookId() : null;

            fineDetails.add(FineDetailDto.builder()
                    .borrowRecordId(record.getBorrowRecordId())
                    .bookId(bookId)
                    .bookTitle(bookTitle)
                    .borrowedQuantity(borrowedQty)
                    .borrowDate(borrowDate)
                    .dueDate(dueDate)
                    .returnDate(returnDate)
                    .daysSinceBorrowing(daysSinceBorrowing)
                    .outstandingQuantity(outstandingQty)
                    .lateDays(lateDays)
                    .fineForOneBook(fineForOneBook)
                    .baseFine(baseFine)
                    .lateFine(lateFine)
                    .totalFine(totalFine)
                    .paidAmount(paidAmount)
                    .unpaidAmount(unpaidAmount)
                    .paymentStatus(paymentStatus)
                    .paid(isPaid)
                    .build());
        }

        // Update student's fine amount in DB to match total fine paid
        student.setFineAmount((int) Math.round(grandTotalPaid));
        studentRepository.saveAndFlush(student);

        return StudentFineResponseDto.builder()
                .studentId(student.getStudentId())
                .studentName(student.getStudentName())
                .grandTotalFine(grandTotalFine)
                .grandTotalPaid(grandTotalPaid)
                .grandTotalUnpaid(grandTotalUnpaid)
                .records(fineDetails)
                .build();
    }

    @Override
    @Transactional
    public StudentFineResponseDto calculateFine(Long studentId, FineCalculationRequestDto requestDto) {
        StudentFineResponseDto response = getStudentFine(studentId);

        // If custom lateDays was provided and student had no records, calculate a hypothetical fine
        if (requestDto != null && requestDto.getLateDays() != null && response.getRecords().isEmpty()) {
            int lateDays = requestDto.getLateDays();
            double base = 50.0;
            double late = lateDays * 10.0;
            double total = base + late;

            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new BusinessException("Student not found"));
            student.setFineAmount((int) Math.round(total));
            studentRepository.save(student);

            response.setGrandTotalFine(total);
            response.setGrandTotalUnpaid(total);
        }

        return response;
    }

    @Override
    @Transactional
    public StudentFineResponseDto payRecordFine(Long studentId, Long borrowRecordId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new BusinessException("Borrow record not found"));

        if (!record.getStudent().getStudentId().equals(studentId)) {
            throw new BusinessException("Borrow record does not belong to student #" + studentId);
        }

        long borrowedQty = record.getInitialQuantity() != null && record.getInitialQuantity() > 0
                ? record.getInitialQuantity()
                : (record.getNumberOfBooks() != null && record.getNumberOfBooks() > 0 ? record.getNumberOfBooks() : 1L);
        LocalDate borrowDate = record.getBorrowDate();
        LocalDate dueDate = borrowDate != null ? borrowDate.plusDays(3) : null;
        LocalDate returnDate = record.getReturnDate();
        LocalDate effectiveDate = returnDate != null ? returnDate : LocalDate.now();
        long lateDays = (dueDate != null && effectiveDate.isAfter(dueDate))
                ? Math.max(0, ChronoUnit.DAYS.between(dueDate, effectiveDate))
                : 0L;
        double totalFine = (borrowedQty * 50.0) + (borrowedQty * lateDays * 10.0);

        boolean alreadyPaid = Boolean.TRUE.equals(record.getPaid()) ||
                (record.getPaidAmount() != null && record.getPaidAmount() >= totalFine && totalFine > 0);
        if (alreadyPaid) {
            throw new BusinessException("Fine has already been fully paid for this record. Additional payments are not allowed.");
        }

        record.setPaid(true);
        record.setPaidAmount(totalFine);
        record.setFineAmount(totalFine);
        record.setBalanceAmount(0.0);
        record.setPaymentStatus("PAID");
        borrowRecordRepository.saveAndFlush(record);

        return getStudentFine(studentId);
    }

    @Override
    @Transactional
    public StudentFineResponseDto toggleRecordPayment(Long studentId, Long borrowRecordId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new BusinessException("Borrow record not found"));

        if (!record.getStudent().getStudentId().equals(studentId)) {
            throw new BusinessException("Borrow record does not belong to student #" + studentId);
        }

        long borrowedQty = record.getInitialQuantity() != null && record.getInitialQuantity() > 0
                ? record.getInitialQuantity()
                : (record.getNumberOfBooks() != null && record.getNumberOfBooks() > 0 ? record.getNumberOfBooks() : 1L);
        LocalDate borrowDate = record.getBorrowDate();
        LocalDate dueDate = borrowDate != null ? borrowDate.plusDays(3) : null;
        LocalDate returnDate = record.getReturnDate();
        LocalDate effectiveDate = returnDate != null ? returnDate : LocalDate.now();
        long lateDays = (dueDate != null && effectiveDate.isAfter(dueDate))
                ? Math.max(0, ChronoUnit.DAYS.between(dueDate, effectiveDate))
                : 0L;
        double totalFine = (borrowedQty * 50.0) + (borrowedQty * lateDays * 10.0);

        boolean currentPaid = Boolean.TRUE.equals(record.getPaid()) ||
                (record.getPaidAmount() != null && record.getPaidAmount() >= totalFine && totalFine > 0);

        if (currentPaid) {
            record.setPaid(false);
            record.setPaidAmount(0.0);
            record.setBalanceAmount(totalFine);
            record.setPaymentStatus("UNPAID");
        } else {
            record.setPaid(true);
            record.setPaidAmount(totalFine);
            record.setBalanceAmount(0.0);
            record.setPaymentStatus("PAID");
        }
        record.setFineAmount(totalFine);
        borrowRecordRepository.saveAndFlush(record);

        return getStudentFine(studentId);
    }
}
