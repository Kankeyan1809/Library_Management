package Accessment.LibraryManagment_BE.services;


import Accessment.LibraryManagment_BE.dtos.request.BorrowRecordRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.BorrowRecordResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface BorrowRecordService {

    BorrowRecordResponseDto borrowBook(BorrowRecordRequestDto borrowRecordRequestDto);




    List<BorrowRecordResponseDto> getborrowRecords();

    BorrowRecordResponseDto returnBook(BorrowRecordRequestDto dto);
}
