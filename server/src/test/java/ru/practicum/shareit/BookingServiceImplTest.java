package ru.practicum.shareit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.booking.model.BookingResponseDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BookingServiceImplTest {

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@yandex.ru");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@yandex.ru");
        booker = userRepository.save(booker);

        item = new Item();
        item.setName("Drill");
        item.setDescription("Powerful");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }

    @Test
    void shouldApproveBooking() {
        BookingDto dto = createBookingDto();
        var created = bookingService.createBooking(booker.getId(), dto);

        var approved = bookingService.updateBooking(owner.getId(), created.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void shouldRejectBooking() {
        BookingDto dto = createBookingDto();
        var created = bookingService.createBooking(booker.getId(), dto);

        var rejected = bookingService.updateBooking(owner.getId(), created.getId(), false);

        assertThat(rejected.getStatus()).isEqualTo(Status.REJECTED);
    }

    @Test
    void shouldThrowExceptionWhenNotOwnerUpdatesBooking() {
        BookingDto dto = createBookingDto();
        var created = bookingService.createBooking(booker.getId(), dto);

        Assertions.assertThatThrownBy(() -> bookingService.updateBooking(booker.getId(), created.getId(), true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Только владелец может подтверждать");
    }

    @Test
    void shouldThrowExceptionWhenUpdateNotWaitingBooking() {
        BookingDto dto = createBookingDto();
        var created = bookingService.createBooking(booker.getId(), dto);
        bookingService.updateBooking(owner.getId(), created.getId(), true);

        Assertions.assertThatThrownBy(() -> bookingService.updateBooking(owner.getId(), created.getId(), false))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Можно подтверждать только ожидающие");
    }

    @Test
    void shouldGetBookingByBooker() {
        BookingDto dto = createBookingDto();
        var created = bookingService.createBooking(booker.getId(), dto);

        var result = bookingService.getBooking(booker.getId(), created.getId());

        assertThat(result.getId()).isEqualTo(created.getId());
    }

    @Test
    void shouldGetBookingByOwner() {
        BookingDto dto = createBookingDto();
        var created = bookingService.createBooking(booker.getId(), dto);

        var result = bookingService.getBooking(owner.getId(), created.getId());

        assertThat(result.getId()).isEqualTo(created.getId());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotBookerOrOwner() {
        User stranger = new User();
        stranger.setName("Stranger");
        stranger.setEmail("stranger@yandex.ru");
        User savedStranger = userRepository.save(stranger);

        BookingDto dto = createBookingDto();
        var created = bookingService.createBooking(booker.getId(), dto);

        Assertions.assertThatThrownBy(() -> bookingService.getBooking(savedStranger.getId(), created.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Доступ запрещён");
    }

    @Test
    void shouldThrowExceptionWhenBookingNotFound() {
        Assertions.assertThatThrownBy(() -> bookingService.getBooking(booker.getId(), 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Бронирование не найдено");
    }

    @Test
    void shouldGetAllBookingsForBooker() {
        createBooking(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        createBooking(LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4));

        Collection<BookingResponseDto> bookings = bookingService.getAllBookings(booker.getId(), "ALL");

        assertThat(bookings).hasSize(2);
    }

    @Test
    void shouldGetCurrentBookings() {
        createBooking(LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));
        createBooking(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        Collection<BookingResponseDto> bookings = bookingService.getAllBookings(booker.getId(), "CURRENT");

        assertThat(bookings).hasSize(1);
        assertThat(bookings.iterator().next().getStatus()).isEqualTo(Status.WAITING);
    }

    @Test
    void shouldGetPastBookings() {
        createBooking(LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        createBooking(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        Collection<BookingResponseDto> bookings = bookingService.getAllBookings(booker.getId(), "PAST");

        assertThat(bookings).hasSize(1);
    }

    @Test
    void shouldGetFutureBookings() {
        createBooking(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        createBooking(LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));

        Collection<BookingResponseDto> bookings = bookingService.getAllBookings(booker.getId(), "FUTURE");

        assertThat(bookings).hasSize(1);
    }

    @Test
    void shouldGetWaitingBookings() {
        createBooking(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        createBooking(LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4));

        Collection<BookingResponseDto> bookings = bookingService.getAllBookings(booker.getId(), "WAITING");

        assertThat(bookings).hasSize(2);
        assertThat(bookings).allMatch(b -> b.getStatus().equals(Status.WAITING));
    }

    @Test
    void shouldGetRejectedBookings() {
        BookingResponseDto rejected = createBooking(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        bookingService.updateBooking(owner.getId(), rejected.getId(), false);

        Collection<BookingResponseDto> bookings = bookingService.getAllBookings(booker.getId(), "REJECTED");

        assertThat(bookings).hasSize(1);
        assertThat(bookings.iterator().next().getStatus()).isEqualTo(Status.REJECTED);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundInGetAllBookings() {
        Assertions.assertThatThrownBy(() -> bookingService.getAllBookings(999L, "ALL"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id=999 не найден");
    }

    @Test
    void shouldGetAllOwnerBookings() {
        createBooking(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        createBooking(LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4));

        Collection<BookingResponseDto> bookings = bookingService.getAllOwnerBookings(owner.getId(), "ALL");

        assertThat(bookings).hasSize(2);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundInGetAllOwnerBookings() {
        Assertions.assertThatThrownBy(() -> bookingService.getAllOwnerBookings(999L, "ALL"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id=999 не найден");
    }

    private BookingDto createBookingDto() {
        BookingDto dto = new BookingDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        return dto;
    }

    private BookingResponseDto createBooking(LocalDateTime start, LocalDateTime end) {
        BookingDto dto = new BookingDto();
        dto.setItemId(item.getId());
        dto.setStart(start);
        dto.setEnd(end);
        return bookingService.createBooking(booker.getId(), dto);
    }
}

