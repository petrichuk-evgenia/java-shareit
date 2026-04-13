package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comments.model.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemWithBookingsDto;

import java.util.Collection;


public interface ItemService {

    ItemDto create(ItemDto item, Long userId);

    ItemWithBookingsDto get(Long item);

    ItemDto update(Long itemId, ItemDto item, Long userId);

    Collection<ItemDto> search(String text);

    Collection<ItemWithBookingsDto> getItemsWithBookings(Long userId);

    CommentDto createComment(Long userId, Long itemId, CommentDto comment);
}
