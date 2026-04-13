package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.*;

public class BookingMapper {

    public static BookingResponseDto toResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(Status.valueOf(booking.getStatus().name()));

        BookingBookerDto bookerDto = new BookingBookerDto();
        bookerDto.setId(booking.getBooker().getId());
        dto.setBooker(bookerDto);

        BookingItemDto itemDto = new BookingItemDto();
        itemDto.setId(booking.getItem().getId());
        itemDto.setName(booking.getItem().getName());
        dto.setItem(itemDto);

        return dto;
    }
}
