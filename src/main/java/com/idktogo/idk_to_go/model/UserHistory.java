package com.idktogo.idk_to_go.model;

import java.time.LocalDateTime;

public record UserHistory(
        int id,
        int userId,
        int restaurantId,
        Integer liked,               // Integer instead of int for null safety
        LocalDateTime timestamp
) {}
