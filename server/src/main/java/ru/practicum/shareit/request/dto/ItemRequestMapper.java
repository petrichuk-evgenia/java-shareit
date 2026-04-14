package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestWithItem;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequestDto toDto(ItemRequest request) {
        if (request == null) return null;

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        dto.setUserId(request.getRequestor().getId());
        return dto;
    }

    public static ItemRequestWithItem toWithItemsDto(ItemRequest request, List<Item> items) {
        if (request == null) return null;

        ItemRequestWithItem dto = new ItemRequestWithItem();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        List<ItemRequestWithItem.ItemInfo> itemDtos = items.stream()
                .map(item -> {
                    ItemRequestWithItem.ItemInfo info = new ItemRequestWithItem.ItemInfo();
                    info.setId(item.getId());
                    info.setName(item.getName());
                    info.setOwnerId(item.getOwner().getId());
                    return info;
                })
                .collect(Collectors.toList());
        dto.setItems(itemDtos);
        return dto;
    }
}
