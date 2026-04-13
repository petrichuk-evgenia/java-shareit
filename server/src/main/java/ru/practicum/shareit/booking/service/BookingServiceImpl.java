package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponseDto createBooking(Long userId, BookingDto bookingDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нельзя забронировать свою вещь");
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);

        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toResponseDto(saved);
    }

    @Override
    public BookingResponseDto updateBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Только владелец может подтверждать бронирование");
        }

        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Можно подтверждать только ожидающие бронирования");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        Booking updated = bookingRepository.save(booking);
        return BookingMapper.toResponseDto(updated);
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        Long bookerId = booking.getBooker().getId();
        Long itemId = booking.getItem().getId();
        boolean isOwner = itemRepository.existsByIdAndOwnerId(itemId, userId);

        if (!bookerId.equals(userId) && !isOwner) {
            throw new NotFoundException("Доступ запрещён");
        }

        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public Collection<BookingResponseDto> getAllBookings(Long userId, String stateName) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        State state = State.from(stateName);
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBookerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    userId, LocalDateTime.now(), LocalDateTime.now());
            case PAST -> bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
            case WAITING -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                    userId, Status.WAITING);
            case REJECTED -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                    userId, Status.REJECTED);
        };
        return bookings.stream().map(BookingMapper::toResponseDto).collect(Collectors.toList());
    }

    @Override
    public Collection<BookingResponseDto> getAllOwnerBookings(Long userId, String stateName) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        State state = State.from(stateName);
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    userId, LocalDateTime.now(), LocalDateTime.now());
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case FUTURE ->
                    bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                    userId, Status.WAITING);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                    userId, Status.REJECTED);
        };
        return bookings.stream().map(BookingMapper::toResponseDto).collect(Collectors.toList());
    }
}
