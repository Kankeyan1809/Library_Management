
package Accessment.LibraryManagment_BE.mapper;
import Accessment.LibraryManagment_BE.dtos.request.BorrowRecordRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.BorrowRecordResponseDto;
import Accessment.LibraryManagment_BE.entity.BorrowRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BorrowRecordMapper {

    @Mapping(target = "borrowRecordId", ignore = true)
//    @Mapping(target = "borrowDate", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "book", ignore = true)
    BorrowRecord toEntity(BorrowRecordRequestDto borrowRecordRequestDto);

    @Mapping(source = "student.studentId", target = "studentId")
    @Mapping(source = "book.bookId", target = "bookId")
    @Mapping(source = "balanceAmount", target = "outstandingBalance")
    BorrowRecordResponseDto toDto(BorrowRecord borrowRecord);
}