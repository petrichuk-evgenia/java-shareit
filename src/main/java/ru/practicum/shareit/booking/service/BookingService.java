package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(Long userId, BookingDto bookingDto);

    BookingResponseDto approve(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto findById(Long userId, Long bookingId);

    List<BookingResponseDto> findAllByUser(Long userId, BookingState state);

    List<BookingResponseDto> findAllByOwner(Long userId, BookingState state);
}
