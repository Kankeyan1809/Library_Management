package Accessment.LibraryManagment_BE.services;

import Accessment.LibraryManagment_BE.dtos.request.BookRequestDto;
import Accessment.LibraryManagment_BE.dtos.request.StudentRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.BookResponseDto;
import Accessment.LibraryManagment_BE.dtos.response.StudentResponseDto;

import java.util.List;

public interface BookService {

   BookResponseDto registerBook(BookRequestDto bookRequestDto);

    List<BookResponseDto> getAllBooks();

//    BookResponseDto updateBook(Long id,BookRequestDto bookRequestDto);
//
//    void deleteBook(Long id);
//
//    BookResponseDto getById(Long id);
}
