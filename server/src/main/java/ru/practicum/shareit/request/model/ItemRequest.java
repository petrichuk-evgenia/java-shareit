package ru.practicum.shareit.request.model;

import jakarta.persistence.*;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@Table(name = "requests")
@Entity
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "description")
    private String description;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requestor_id", nullable = false)
    private User requestor;
    @Column(name = "created")
    private LocalDateTime created;

}
