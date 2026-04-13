package ru.practicum.shareit.comments.dto;

import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.comments.model.CommentDto;

public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setItem(comment.getItem());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setCreated(comment.getCreated());
        return dto;
    }
}
