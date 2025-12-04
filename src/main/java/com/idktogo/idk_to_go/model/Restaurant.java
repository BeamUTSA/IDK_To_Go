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
) {
    // Default constructor
    public Restaurant() {
        this(0, "", "", "", 0, 0, 0, 0, "");
    }

    // Copy constructor for updating specific fields
    public Restaurant withLikes(int newLikes) {
        return new Restaurant(id, name, category, location, newLikes, dislikes, netScore, weeklyLikes, logo);
    }

    public Restaurant withDislikes(int newDislikes) {
        return new Restaurant(id, name, category, location, likes, newDislikes, netScore, weeklyLikes, logo);
    }

    public Restaurant withNetScore(int newNetScore) {
        return new Restaurant(id, name, category, location, likes, dislikes, newNetScore, weeklyLikes, logo);
    }

    public Restaurant withWeeklyLikes(int newWeeklyLikes) {
        return new Restaurant(id, name, category, location, likes, dislikes, netScore, newWeeklyLikes, logo);
    }
}
