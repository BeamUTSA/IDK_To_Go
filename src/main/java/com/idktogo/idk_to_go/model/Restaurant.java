package com.idktogo.idk_to_go.model;

public record Restaurant(
        int id,
        String name,
        String category,
        String location,
        int likes,
        int dislikes,
        int netScore,
        int weeklyLikes,
        String logo
) {}
