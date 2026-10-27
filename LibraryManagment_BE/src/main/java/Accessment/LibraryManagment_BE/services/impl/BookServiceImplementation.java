package Accessment.LibraryManagment_BE.services.impl;

import Accessment.LibraryManagment_BE.dtos.request.BookRequestDto;
import Accessment.LibraryManagment_BE.dtos.request.StudentRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.BookResponseDto;
import Accessment.LibraryManagment_BE.dtos.response.StudentResponseDto;
import Accessment.LibraryManagment_BE.entity.Book;
import Accessment.LibraryManagment_BE.entity.Student;
import Accessment.LibraryManagment_BE.mapper.BookMapper;
import Accessment.LibraryManagment_BE.mapper.StudentMapper;
import Accessment.LibraryManagment_BE.repositories.BookRepository;
import Accessment.LibraryManagment_BE.repositories.StudentRepository;
import Accessment.LibraryManagment_BE.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class BookServiceImplementation implements BookService {

    private final BookRepository bookRepository;


    private final BookMapper bookMapper;

    @Override
    public BookResponseDto registerBook(BookRequestDto bookRequestDto){
        Book book = bookMapper.toEntity(bookRequestDto);

        Book saved= bookRepository.save(book);

        return bookMapper.toDto(saved);
    }
    @Override
    public List<BookResponseDto> getAllBooks(){
        List<Book> books=bookRepository.findAll();
        return books.stream().map(bookMapper::toDto).toList();
    }
}
