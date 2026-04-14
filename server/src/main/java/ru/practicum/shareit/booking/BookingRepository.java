package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, Status status);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId, LocalDateTime now, LocalDateTime now2);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, Status status);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId, LocalDateTime now, LocalDateTime now2);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findByItemAndStatusAndStartBeforeOrderByEndDesc(Item item, Status status, LocalDateTime now);

    List<Booking> findByItemAndStatusAndStartAfterOrderByStartAsc(Item item, Status status, LocalDateTime now);

    boolean existsByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime now);
}
