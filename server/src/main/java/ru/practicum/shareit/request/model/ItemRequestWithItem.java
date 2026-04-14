package ru.practicum.shareit.request.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestWithItem {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemInfo> items;

    @Data
    public static class ItemInfo {
        private Long id;
        private String name;
        private Long ownerId;
    }
}
