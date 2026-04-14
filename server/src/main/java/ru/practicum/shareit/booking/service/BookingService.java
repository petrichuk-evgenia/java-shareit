package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.booking.model.BookingResponseDto;

import java.util.Collection;

public interface BookingService {

    BookingResponseDto createBooking(Long userId, BookingDto bookingDto);

    BookingResponseDto updateBooking(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto getBooking(Long userId,Long id);

    Collection<BookingResponseDto> getAllBookings(Long userId, String state);

    Collection<BookingResponseDto> getAllOwnerBookings(Long userId, String state);
}
