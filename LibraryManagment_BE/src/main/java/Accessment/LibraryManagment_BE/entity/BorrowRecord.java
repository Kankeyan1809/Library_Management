package Accessment.LibraryManagment_BE.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long borrowRecordId;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private Long numberOfBooks;
    private Long initialQuantity;
    private Boolean paid = false;
    private Double paidAmount = 0.0;
    private Double fineAmount = 0.0;
    private Double balanceAmount = 0.0;
    private String paymentStatus = "UNPAID";

    //BorrowRecord <--> Book
    @ManyToOne
    @JoinColumn(name = "bookId")
    private Book book;

    //BorrowRecord <--> Student
    @ManyToOne
    @JoinColumn(name = "studentId")
    private Student student;

}

