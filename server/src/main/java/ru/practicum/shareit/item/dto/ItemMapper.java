package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comments.dto.CommentMapper;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemWithBookingsDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-controllers.
 */

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        if (item == null) return null;

        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        if (item.getRequest() != null) {
            dto.setRequestId(item.getRequest().getId());
        }
        return dto;
    }

    public static Item toItem(ItemDto itemDto, User user) {
        if (itemDto == null) return null;

        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(user);
        return item;
    }

    public static ItemWithBookingsDto toItemWithBookingsDto(
            Item item,
            Booking lastBooking,
            Booking nextBooking,
            List<Comment> comments) {

        ItemWithBookingsDto dto = new ItemWithBookingsDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setRequestId(item.getRequest() != null ? item.getRequest().getId() : null);

        if (lastBooking != null) {
            ItemWithBookingsDto.BookingInfo last = new ItemWithBookingsDto.BookingInfo();
            last.setId(lastBooking.getId());
            last.setBookerId(lastBooking.getBooker().getId());
            last.setStart(lastBooking.getStart());
            last.setEnd(lastBooking.getEnd());
            dto.setLastBooking(last);
        }

        if (nextBooking != null) {
            ItemWithBookingsDto.BookingInfo next = new ItemWithBookingsDto.BookingInfo();
            next.setId(nextBooking.getId());
            next.setBookerId(nextBooking.getBooker().getId());
            next.setStart(nextBooking.getStart());
            next.setEnd(nextBooking.getEnd());
            dto.setNextBooking(next);
        }

        dto.setComments(comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList()));

        return dto;
    }

}
