package Accessment.LibraryManagment_BE.controllers;


import Accessment.LibraryManagment_BE.dtos.response.LibrarySummaryResponseDto;
import Accessment.LibraryManagment_BE.services.LibrarySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/library/summary")
public class LibrarySummaryController {

    private final LibrarySummaryService librarySummaryService;

    @GetMapping
    public LibrarySummaryResponseDto getSummary() {
        return librarySummaryService.getSummary();
    }
}
