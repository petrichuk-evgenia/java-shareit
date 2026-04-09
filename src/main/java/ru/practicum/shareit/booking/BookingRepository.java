package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Найти бронирования по id букера
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    @Query("SELECT b FROM Booking b JOIN b.item i WHERE i.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findByOwnerId(@Param("ownerId") Long ownerId);

    // Найти текущие бронирования
    @Query("SELECT b FROM Booking b WHERE b.bookerId = :userId AND b.start <= :now AND b.end >= :now ORDER BY b.start DESC")
    List<Booking> findCurrentByBookerId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Найти будущие бронирования
    @Query("SELECT b FROM Booking b WHERE b.bookerId = :userId AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findFutureByBookerId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Найти прошедшие бронирования
    @Query("SELECT b FROM Booking b WHERE b.bookerId = :userId AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findPastByBookerId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Найти бронирования по статусу
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    // Проверить, бронировал ли пользователь вещь
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.bookerId = :userId AND b.item.id = :itemId AND b.end < :now AND b.status = :status")
    boolean existsByBookerIdAndItemIdAndEndBeforeAndStatus(@Param("userId") Long userId,
                                                           @Param("itemId") Long itemId,
                                                           @Param("now") LocalDateTime now,
                                                           @Param("status") BookingStatus status);

    // Найти последнее бронирование вещи
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.start < :now ORDER BY b.start DESC")
    List<Booking> findLastBookingByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    // Найти следующее бронирование вещи
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.start > :now ORDER BY b.start ASC")
    List<Booking> findNextBookingByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);
}