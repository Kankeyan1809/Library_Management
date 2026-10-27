package Accessment.LibraryManagment_BE.dtos.request;


import lombok.Data;

import java.time.LocalDate;

@Data
public class BorrowRecordRequestDto {

    private Long borrowRecordId;

    private Long studentId;

    private Long bookId;

    private LocalDate borrowDate;

    private LocalDate returnDate;

    private Long numberOfBooks;

    private Long initialQuantity;

    private Boolean paid;

    private Double paidAmount;

    private String paymentStatus;
}
