package ru.practicum.shareit.item;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comments.model.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemServiceImpl itemService;

    public ItemController(ItemServiceImpl itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto createItem(@RequestBody ItemDto item, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.create(item, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId, @RequestBody ItemDto item, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.update(itemId, item, userId);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto getItemById(@PathVariable Long itemId) {
        return itemService.get(itemId);
    }

    @GetMapping
    public Collection<ItemWithBookingsDto> getUserItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemsWithBookings(userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> getItemsBySearch(@RequestParam String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId, @RequestBody CommentDto comment) {
        return itemService.createComment(itemId, userId, comment);
    }
}
