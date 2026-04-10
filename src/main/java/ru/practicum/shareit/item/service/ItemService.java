package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> findAllByOwnerId(Long ownerId);

    ItemDto findById(Long id);

    ItemDto findById(Long itemId, Long userId);

    ItemDto create(Long ownerId, ItemDto itemDto);

    ItemDto update(Long ownerId, Long itemId, ItemDto itemDto);

    List<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, CreateCommentDto createCommentDto);
}
