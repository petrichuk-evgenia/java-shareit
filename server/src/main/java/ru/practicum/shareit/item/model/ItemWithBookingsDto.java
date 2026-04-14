package ru.practicum.shareit.item.model;

import lombok.Data;
import ru.practicum.shareit.comments.model.CommentDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemWithBookingsDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
    private BookingInfo lastBooking;
    private BookingInfo nextBooking;
    private List<CommentDto> comments;

    @Data
    public static class BookingInfo {
        private Long id;
        private Long bookerId;
        private LocalDateTime start;
        private LocalDateTime end;
    }
}
