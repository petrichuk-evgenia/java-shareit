package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") @Positive(message = "ID пользователя должен быть положительным") Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        if (userId == null) {
            throw new ValidationException("X-Sharer-User-Id header не может быть пустым");
        }
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") @Positive(message = "ID пользователя должен быть положительным") Long userId,
                          @PathVariable @Positive(message = "ID вещи должен быть положительным") Long itemId,
                          @RequestBody ItemDto itemDto) {
        if (userId == null) {
            throw new ValidationException("X-Sharer-User-Id header не может быть пустым");
        }
        if (itemId == null) {
            throw new ValidationException("ID вещи не может быть пустым");
        }
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable @Positive(message = "ID вещи должен быть положительным") Long itemId) {
        if (itemId == null) {
            throw new ValidationException("ID вещи не может быть пустым");
        }
        return itemService.findById(itemId);
    }

    @GetMapping
    public List<ItemDto> findAllByOwnerId(@RequestHeader("X-Sharer-User-Id") @Positive(message = "ID пользователя должен быть положительным") Long userId) {
        if (userId == null) {
            throw new ValidationException("X-Sharer-User-Id header не может быть пустым");
        }
        return itemService.findAllByOwnerId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(required = false) String text) {
        if (text == null || text.isBlank()) {
            return List.of(); // Пустой список
        }
        return itemService.search(text);
    }
}
