package com.idktogo.idk_to_go.model;

import java.time.LocalDateTime;

public record UserHistory(
        int id,
        int userId,
        int restaurantId,
        Integer liked,           // 1 = like, -1 = dislike, null = neutral
        LocalDateTime timestamp
) {}
