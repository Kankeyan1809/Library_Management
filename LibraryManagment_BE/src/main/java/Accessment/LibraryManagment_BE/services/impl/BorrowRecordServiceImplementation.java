package Accessment.LibraryManagment_BE.services.impl;

import Accessment.LibraryManagment_BE.dtos.request.BorrowRecordRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.BorrowRecordResponseDto;
import Accessment.LibraryManagment_BE.entity.Book;
import Accessment.LibraryManagment_BE.entity.BorrowRecord;
import Accessment.LibraryManagment_BE.entity.Student;
import Accessment.LibraryManagment_BE.exception.BusinessException;
import Accessment.LibraryManagment_BE.mapper.BorrowRecordMapper;
import Accessment.LibraryManagment_BE.repositories.BookRepository;
import Accessment.LibraryManagment_BE.repositories.BorrowRecordRepository;
import Accessment.LibraryManagment_BE.repositories.StudentRepository;
import Accessment.LibraryManagment_BE.services.BorrowRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RequiredArgsConstructor
@Service

public class BorrowRecordServiceImplementation implements BorrowRecordService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BorrowRecordMapper borrowRecordMapper;
    private final StudentRepository studentRepository;
    private final BookRepository bookRepository;
    private final Accessment.LibraryManagment_BE.services.FineService fineService;


    @Override
    public BorrowRecordResponseDto borrowBook(BorrowRecordRequestDto borrowRecordRequestDto) {
        Student student = studentRepository.findById(borrowRecordRequestDto.getStudentId()).orElseThrow(() -> new BusinessException("Student Not Found"));

        Book book = bookRepository.findById(borrowRecordRequestDto.getBookId()).orElseThrow(() -> new BusinessException("Book Not Found"));

        BorrowRecord record = borrowRecordMapper.toEntity(borrowRecordRequestDto);

        record.setStudent(student);
        record.setBook(book);

        List<BorrowRecord> records =
                borrowRecordRepository
                        .findByStudentStudentId(
                                borrowRecordRequestDto.getStudentId()
                        );

        long alreadyBorrowed = records.stream()
                .mapToLong(BorrowRecord::getNumberOfBooks)
                .sum();



        if (alreadyBorrowed + borrowRecordRequestDto.getNumberOfBooks() > 5) {
            throw new BusinessException(
                    "A student cannot borrow more than 5 books"
            );



        }
        if (book.getAvailableQuantity()
                < borrowRecordRequestDto.getNumberOfBooks().intValue()) {

            throw new BusinessException(
                    "Not enough books available"
            );
        }
        book.setAvailableQuantity(
                book.getAvailableQuantity()
                        - borrowRecordRequestDto.getNumberOfBooks().intValue()
        );

        bookRepository.save(book);






        record.setInitialQuantity(borrowRecordRequestDto.getNumberOfBooks());
        record.setPaid(false);

        BorrowRecord saved = borrowRecordRepository.save(record);

        return borrowRecordMapper.toDto(saved);
    }
    @Override
    public List<BorrowRecordResponseDto> getborrowRecords(){
        List<BorrowRecord> records=borrowRecordRepository.findAll();
        LocalDate today = LocalDate.now();
        return records.stream().map(record -> {
            BorrowRecordResponseDto dto = borrowRecordMapper.toDto(record);
            long borrowedQty = record.getInitialQuantity() != null && record.getInitialQuantity() > 0
                    ? record.getInitialQuantity()
                    : (record.getNumberOfBooks() != null && record.getNumberOfBooks() > 0 ? record.getNumberOfBooks() : 1L);
            LocalDate borrowDate = record.getBorrowDate();
            LocalDate dueDate = borrowDate != null ? borrowDate.plusDays(3) : null;
            LocalDate returnDate = record.getReturnDate();
            LocalDate effectiveDate = returnDate != null ? returnDate : today;
            long lateDays = (dueDate != null && effectiveDate.isAfter(dueDate))
                    ? Math.max(0, ChronoUnit.DAYS.between(dueDate, effectiveDate))
                    : 0L;
            double totalFine = record.getFineAmount() != null && record.getFineAmount() > 0
                    ? record.getFineAmount()
                    : (borrowedQty * 50.0) + (borrowedQty * lateDays * 10.0);
            double paidAmount = record.getPaidAmount() != null
                    ? record.getPaidAmount()
                    : (Boolean.TRUE.equals(record.getPaid()) ? totalFine : 0.0);
            double balance = Math.max(0.0, totalFine - paidAmount);
            String status = record.getPaymentStatus();
            if (status == null || status.isEmpty()) {
                if (paidAmount >= totalFine && totalFine > 0) {
                    status = "PAID";
                } else if (paidAmount > 0) {
                    status = "PARTIALLY_PAID";
                } else {
                    status = "UNPAID";
                }
            }
            dto.setFineAmount(totalFine);
            dto.setPaidAmount(paidAmount);
            dto.setBalanceAmount(balance);
            dto.setOutstandingBalance(balance);
            dto.setPaymentStatus(status);
            dto.setPaid("PAID".equalsIgnoreCase(status) || Boolean.TRUE.equals(record.getPaid()));
            return dto;
        }).toList();
    }
    @Override
    @Transactional
    public BorrowRecordResponseDto returnBook(BorrowRecordRequestDto dto) {

        BorrowRecord borrowRecord;
        if (dto.getBorrowRecordId() != null) {
            borrowRecord = borrowRecordRepository.findById(dto.getBorrowRecordId())
                    .orElseThrow(() -> new BusinessException("Borrow record not found with ID #" + dto.getBorrowRecordId()));
        } else {
            borrowRecord = borrowRecordRepository
                    .findFirstByStudentStudentIdAndBookBookIdAndReturnDateIsNull(
                            dto.getStudentId(),
                            dto.getBookId()
                    )
                    .orElseThrow(() ->
                            new BusinessException("Borrow record not found"));
        }

        if (dto.getStudentId() != null && !borrowRecord.getStudent().getStudentId().equals(dto.getStudentId())) {
            throw new BusinessException("Borrow record does not belong to student #" + dto.getStudentId());
        }

        if (dto.getNumberOfBooks() == null || dto.getNumberOfBooks() <= 0) {
            throw new BusinessException(
                    "Number of books must be greater than zero");
        }

        if (dto.getNumberOfBooks() > borrowRecord.getNumberOfBooks()) {
            throw new BusinessException(
                    "Cannot return more books than borrowed");
        }

        if (dto.getPaidAmount() != null && dto.getPaidAmount() < 0) {
            throw new BusinessException("Payment amount cannot be negative");
        }

        if (borrowRecord.getInitialQuantity() == null) {
            borrowRecord.setInitialQuantity(borrowRecord.getNumberOfBooks());
        }

        Book book = borrowRecord.getBook();

        book.setAvailableQuantity(
                book.getAvailableQuantity() + dto.getNumberOfBooks()
        );

        bookRepository.save(book);

        borrowRecord.setNumberOfBooks(
                borrowRecord.getNumberOfBooks() - dto.getNumberOfBooks()
        );

        if (borrowRecord.getNumberOfBooks() == 0) {
            borrowRecord.setReturnDate(dto.getReturnDate() != null ? dto.getReturnDate() : LocalDate.now());
        }

        // Calculate fine details for this record according to existing business rules
        LocalDate borrowDate = borrowRecord.getBorrowDate();
        LocalDate dueDate = borrowDate != null ? borrowDate.plusDays(3) : null;
        LocalDate effectiveDate = dto.getReturnDate() != null ? dto.getReturnDate() : LocalDate.now();
        long lateDays = (dueDate != null && effectiveDate.isAfter(dueDate))
                ? Math.max(0, ChronoUnit.DAYS.between(dueDate, effectiveDate))
                : 0L;
        long borrowedQty = borrowRecord.getInitialQuantity() != null && borrowRecord.getInitialQuantity() > 0
                ? borrowRecord.getInitialQuantity()
                : (dto.getNumberOfBooks() != null ? dto.getNumberOfBooks() : 1L);
        double totalFine = (borrowedQty * 50.0) + (borrowedQty * lateDays * 10.0);

        double existingPaid = borrowRecord.getPaidAmount() != null
                ? borrowRecord.getPaidAmount()
                : (Boolean.TRUE.equals(borrowRecord.getPaid()) ? totalFine : 0.0);
        double currentOutstanding = Math.max(0.0, totalFine - existingPaid);

        double newPaid = existingPaid;
        String paymentStatus;
        boolean paid;

        // 8. If the fine amount is zero, handle the return without requiring a payment.
        if (totalFine == 0.0) {
            newPaid = 0.0;
            paymentStatus = "PAID";
            paid = true;
        } else {
            if (dto.getPaidAmount() != null) {
                double payment = dto.getPaidAmount();
                if (payment < 0) {
                    throw new BusinessException("Payment amount cannot be negative");
                }
                // 10. Reject additional payments once the fine has been fully paid.
                if (existingPaid >= totalFine && payment > 0) {
                    throw new BusinessException("Fine has already been fully paid for this record. Additional payments are not allowed.");
                }
                // 4. When a payment is submitted, validate that the amount is positive and does not exceed the outstanding balance.
                if (payment > currentOutstanding + 0.001) {
                    throw new BusinessException("Payment amount (" + payment + ") cannot exceed the outstanding balance (" + currentOutstanding + ")");
                }

                newPaid = existingPaid + payment;
                newPaid = Math.min(totalFine, Math.max(0.0, newPaid));
            } else if (dto.getPaid() != null) {
                if (Boolean.TRUE.equals(dto.getPaid())) {
                    if (existingPaid >= totalFine && totalFine > 0) {
                        newPaid = totalFine;
                    } else {
                        newPaid = totalFine;
                    }
                } else {
                    newPaid = existingPaid;
                }
            }

            // 5. If the total amount paid equals the total fine due, set the payment status to PAID.
            // 6. If only part of the fine is paid, keep the status as PARTIALLY_PAID.
            // 7. If no payment has been made and a fine is outstanding, retain the appropriate unpaid status.
            if (newPaid >= totalFine - 0.001) {
                paymentStatus = "PAID";
                paid = true;
                newPaid = totalFine;
            } else if (newPaid > 0.0) {
                paymentStatus = "PARTIALLY_PAID";
                paid = false;
            } else {
                paymentStatus = "UNPAID";
                paid = false;
            }
        }

        // 11. Prevent duplicate payment requests and ensure the balance cannot become negative.
        double balanceAmount = Math.max(0.0, totalFine - newPaid);

        // 9. Save the payment, updated balance, payment status, and relevant student information consistently in a database transaction.
        borrowRecord.setFineAmount(totalFine);
        borrowRecord.setPaidAmount(newPaid);
        borrowRecord.setBalanceAmount(balanceAmount);
        borrowRecord.setPaymentStatus(paymentStatus);
        borrowRecord.setPaid(paid);

        borrowRecordRepository.saveAndFlush(borrowRecord);

        // Recalculate student fine amount and persist in student entity
        if (borrowRecord.getStudent() != null && borrowRecord.getStudent().getStudentId() != null) {
            try {
                fineService.getStudentFine(borrowRecord.getStudent().getStudentId());
            } catch (Exception e) {
                Student student = studentRepository.findById(borrowRecord.getStudent().getStudentId()).orElse(null);
                if (student != null && dto.getPaidAmount() != null && dto.getPaidAmount() > 0) {
                    int currentFine = student.getFineAmount() != null ? student.getFineAmount() : 0;
                    student.setFineAmount(currentFine + (int) Math.round(dto.getPaidAmount()));
                    studentRepository.saveAndFlush(student);
                }
            }
        }

        // 12. Return the updated borrow record, fine amount, amount paid, outstanding balance
        BorrowRecordResponseDto responseDto = borrowRecordMapper.toDto(borrowRecord);
        responseDto.setFineAmount(totalFine);
        responseDto.setPaidAmount(newPaid);
        responseDto.setBalanceAmount(balanceAmount);
        responseDto.setOutstandingBalance(balanceAmount);
        responseDto.setPaymentStatus(paymentStatus);
        responseDto.setPaid(paid);

        return responseDto;
    }


}