package ru.practicum.shareit.comments.model;

import lombok.Data;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;

    private String text;

    private Item item;

    private String authorName;

    private LocalDateTime created;

}
