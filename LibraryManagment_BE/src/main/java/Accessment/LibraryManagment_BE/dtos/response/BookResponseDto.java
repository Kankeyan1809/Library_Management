package Accessment.LibraryManagment_BE.dtos.response;


import lombok.Data;

@Data
public class BookResponseDto {

    private Long bookId;

    private String bookTitle;

    private String author;

    private String category;

    private Integer availableQuantity;
}
