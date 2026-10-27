package Accessment.LibraryManagment_BE.controllers;

import Accessment.LibraryManagment_BE.dtos.request.BookRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.BookResponseDto;
import Accessment.LibraryManagment_BE.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping()
    public BookResponseDto registerbook(@RequestBody BookRequestDto bookRequestDto){
        BookResponseDto book = bookService.registerBook(bookRequestDto);

        return book;
    }
    @GetMapping()
    public List<BookResponseDto> getAllBooks(){
        return bookService.getAllBooks();
    }
}
