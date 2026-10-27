package Accessment.LibraryManagment_BE.controllers;


import Accessment.LibraryManagment_BE.dtos.request.BorrowRecordRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.BorrowRecordResponseDto;
import Accessment.LibraryManagment_BE.dtos.response.StudentResponseDto;
import Accessment.LibraryManagment_BE.services.BorrowRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")

public class BorrowRecordController {
    private final BorrowRecordService borrowRecordService;


    @PostMapping("/borrow")
    public BorrowRecordResponseDto  borrowBook(@RequestBody BorrowRecordRequestDto borrowRecordRequestDto){
        return borrowRecordService.borrowBook(borrowRecordRequestDto);
    }
    @GetMapping("/borrow")
    public List<BorrowRecordResponseDto> getAllStudents(){

        return borrowRecordService.getborrowRecords();
    }

    @PostMapping("/return")
    public BorrowRecordResponseDto returnBook(@RequestBody BorrowRecordRequestDto borrowRecordRequestDto){
        return borrowRecordService.returnBook(borrowRecordRequestDto);
    }



}

