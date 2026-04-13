package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void shouldFindBookingsByBookerIdAndStatus() {
        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@yandex.ru");
        userRepository.save(booker);

        User user = new User();
        user.setName("User");
        user.setEmail("user@yandex.ru");
        userRepository.save(user);

        Item item = new Item();
        item.setName("Drill");
        item.setAvailable(true);
        item.setDescription("Drill for metal");
        item.setOwner(user);
        itemRepository.save(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);
        bookingRepository.save(booking);

        List<Booking> result = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                booker.getId(), Status.WAITING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Status.WAITING);
    }
}