package Accessment.LibraryManagment_BE.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineDetailDto {
    private Long borrowRecordId;
    private Long bookId;
    private String bookTitle;
    private Long borrowedQuantity;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Long daysSinceBorrowing;
    private Long outstandingQuantity;
    private Long lateDays;
    private Double fineForOneBook;
    private Double baseFine;
    private Double lateFine;
    private Double totalFine;
    private Double paidAmount;
    private Double unpaidAmount;
    private String paymentStatus;
    private Boolean paid;
}
