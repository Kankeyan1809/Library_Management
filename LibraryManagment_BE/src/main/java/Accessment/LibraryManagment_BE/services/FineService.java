package Accessment.LibraryManagment_BE.services;

import Accessment.LibraryManagment_BE.dtos.request.FineCalculationRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.StudentFineResponseDto;

public interface FineService {
    StudentFineResponseDto getStudentFine(Long studentId);
    StudentFineResponseDto calculateFine(Long studentId, FineCalculationRequestDto requestDto);
    StudentFineResponseDto payRecordFine(Long studentId, Long borrowRecordId);
    StudentFineResponseDto toggleRecordPayment(Long studentId, Long borrowRecordId);
}
