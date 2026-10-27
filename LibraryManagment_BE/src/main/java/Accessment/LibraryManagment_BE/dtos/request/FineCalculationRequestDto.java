package Accessment.LibraryManagment_BE.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FineCalculationRequestDto {
    private Integer lateDays;
}
