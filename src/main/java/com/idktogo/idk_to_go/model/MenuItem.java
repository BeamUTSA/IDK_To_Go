package com.idktogo.idk_to_go.model;

public record MenuItem(
        int id,
        int restaurantId,
        String itemName,
        double price
) {}
