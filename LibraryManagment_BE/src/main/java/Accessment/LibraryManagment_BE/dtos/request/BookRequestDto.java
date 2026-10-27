package Accessment.LibraryManagment_BE.dtos.request;


import lombok.Data;

@Data
public class BookRequestDto {

    private String bookTitle;

    private String author;

    private String category;

    private Integer availableQuantity;
}
