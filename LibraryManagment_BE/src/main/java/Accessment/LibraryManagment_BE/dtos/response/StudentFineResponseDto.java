package Accessment.LibraryManagment_BE.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentFineResponseDto {
    private Long studentId;
    private String studentName;
    private Double grandTotalFine;
    private Double grandTotalPaid;
    private Double grandTotalUnpaid;
    private List<FineDetailDto> records;
}
