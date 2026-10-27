package Accessment.LibraryManagment_BE.dtos.response;


import lombok.Data;

import java.time.LocalDate;

@Data
public class BorrowRecordResponseDto {

    private Long borrowRecordId;

    private LocalDate borrowDate;

    private LocalDate returnDate;

    private Long numberOfBooks;

    private Long initialQuantity;

    private Boolean paid;

    private Double paidAmount;

    private Double fineAmount;

    private Double balanceAmount;

    private Double outstandingBalance;

    private String paymentStatus;

    private Long studentId;

    private Long bookId;
}
