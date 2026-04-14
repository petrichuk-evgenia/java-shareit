package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.booking.service.BookingServiceImpl;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingServiceImpl bookingService;

    @Test
    public void shouldApproveBooking() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(Status.APPROVED);
        responseDto.setItem(BookingItemDto.builder().id(1L).name("Item").build());
        responseDto.setBooker(BookingBookerDto.builder().id(1L).build());
        responseDto.setStart(LocalDateTime.of(2026, 4, 1, 10, 0));
        responseDto.setEnd(LocalDateTime.of(2026, 4, 2, 10, 0));

        when(bookingService.updateBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.item.id").exists())
                .andExpect(jsonPath("$.booker.id").exists());
    }

    @Test
    public void shouldCreateBooking() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto response = new BookingResponseDto();
        response.setId(1L);
        response.setStatus(Status.WAITING);
        response.setStart(dto.getStart());
        response.setEnd(dto.getEnd());

        when(bookingService.createBooking(anyLong(), any())).thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }
}