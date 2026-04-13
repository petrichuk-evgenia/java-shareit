package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.item.model.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = {ObjectMapper.class})
public class ItemDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeAndDeserializeItemDto() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Drill");
        dto.setDescription("Powerful drill");
        dto.setAvailable(true);
        dto.setRequestId(5L);

        String json = objectMapper.writeValueAsString(dto);
        ItemDto parsed = objectMapper.readValue(json, ItemDto.class);

        assertThat(parsed.getId()).isEqualTo(1L);
        assertThat(parsed.getName()).isEqualTo("Drill");
        assertThat(parsed.getDescription()).isEqualTo("Powerful drill");
        assertThat(parsed.getAvailable()).isTrue();
        assertThat(parsed.getRequestId()).isEqualTo(5L);
    }
}