package Accessment.LibraryManagment_BE.mapper;


import Accessment.LibraryManagment_BE.dtos.request.BookRequestDto;
import Accessment.LibraryManagment_BE.dtos.request.StudentRequestDto;
import Accessment.LibraryManagment_BE.dtos.response.BookResponseDto;
import Accessment.LibraryManagment_BE.dtos.response.StudentResponseDto;
import Accessment.LibraryManagment_BE.entity.Book;
import Accessment.LibraryManagment_BE.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookMapper {

    Book toEntity(BookRequestDto bookRequestDto);

    @Mapping(source = "bookId",target = "bookId")
    BookResponseDto toDto(Book book);

    void updateEntityfromDto(BookRequestDto bookRequestDto ,@MappingTarget Book book);
}
