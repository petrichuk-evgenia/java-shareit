package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestWithItem;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;

import java.util.Collection;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestServiceImpl itemRequestService;

    public ItemRequestController(ItemRequestServiceImpl itemRequestService) {
        this.itemRequestService = itemRequestService;
    }


    @PostMapping
    public ItemRequestDto createRequest(@RequestBody ItemRequestDto itemRequestDto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.createRequest(itemRequestDto, userId);
    }

    @GetMapping
    public Collection<ItemRequestWithItem> getRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getRequests(userId);
    }

    @GetMapping(path = "/all")
    public Collection<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllRequests(userId);
    }

    @GetMapping(path = "/{requestId}")
    public ItemRequestWithItem getRequestById(@PathVariable Long requestId,@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getRequestById(requestId,userId);
    }
}
