package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comments.model.CommentDto;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemServiceImpl itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String baseUrl = "/items";

    @Test
    public void shouldGetItemById() throws Exception {
        ItemWithBookingsDto responseDto = createItemWithBookingsDto();

        when(itemService.get(1L)).thenReturn(responseDto);

        mockMvc.perform(get(baseUrl + "/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.lastBooking.id").value(101))
                .andExpect(jsonPath("$.nextBooking").doesNotExist())
                .andExpect(jsonPath("$.comments").isArray());
    }

    @Test
    public void shouldCreateItem() throws Exception {
        ItemDto requestDto = new ItemDto();
        requestDto.setName("Drill");
        requestDto.setDescription("Powerful");
        requestDto.setAvailable(true);

        ItemDto responseDto = new ItemDto();
        responseDto.setId(1L);
        responseDto.setName("Drill");
        responseDto.setDescription("Powerful");
        responseDto.setAvailable(true);

        when(itemService.create(any(), eq(1L))).thenReturn(responseDto);

        mockMvc.perform(post(baseUrl)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    public void shouldUpdateItem() throws Exception {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("New name");

        ItemDto updated = new ItemDto();
        updated.setId(1L);
        updated.setName("New name");

        when(itemService.update(eq(1L), any(), eq(1L))).thenReturn(updated);

        mockMvc.perform(patch(baseUrl + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"));
    }

    @Test
    public void shouldGetUserItems() throws Exception {
        ItemWithBookingsDto item = createItemWithBookingsDto();

        when(itemService.getItemsWithBookings(1L)).thenReturn(List.of(item));

        mockMvc.perform(get(baseUrl)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    public void shouldSearchItems() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Drill");

        when(itemService.search("drill")).thenReturn(List.of(item));

        mockMvc.perform(get(baseUrl + "/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    public void shouldCreateComment() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Good tool!");

        CommentDto saved = new CommentDto();
        saved.setId(1L);
        saved.setText("Good tool!");
        saved.setAuthorName("John");
        saved.setCreated(LocalDateTime.now());

        when(itemService.createComment(eq(1L), eq(1L), any())).thenReturn(saved);

        mockMvc.perform(post(baseUrl + "/1/comment")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Good tool!"))
                .andExpect(jsonPath("$.authorName").value("John"));
    }

    private ItemWithBookingsDto createItemWithBookingsDto() {
        ItemWithBookingsDto dto = new ItemWithBookingsDto();
        dto.setId(1L);
        dto.setName("Drill");
        dto.setDescription("Powerful");
        dto.setAvailable(true);

        ItemWithBookingsDto.BookingInfo last = new ItemWithBookingsDto.BookingInfo();
        last.setId(101L);
        last.setBookerId(2L);
        dto.setLastBooking(last);
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Good tool!");
        commentDto.setAuthorName("John");
        commentDto.setCreated(LocalDateTime.now());
        commentDto.setId(1L);
        List<CommentDto> comments = List.of(commentDto);
        dto.setComments(comments);

        return dto;
    }
}
