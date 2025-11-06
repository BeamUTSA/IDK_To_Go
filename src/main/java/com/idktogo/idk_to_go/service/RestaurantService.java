package com.idktogo.idk_to_go.service;

import com.idktogo.idk_to_go.dao.HistoryDAO;
import com.idktogo.idk_to_go.dao.RestaurantDAO;
import java.util.concurrent.CompletableFuture;

public final class RestaurantService {
    private RestaurantService() {}

    private static int asScore(Integer liked) {
        return liked == null ? 0 : (liked > 0 ? 1 : -1);
    }

    public static CompletableFuture<Void> handleLike(int userId, int restaurantId) {
        return applyReaction(userId, restaurantId, +1);
    }

    public static CompletableFuture<Void> handleDislike(int userId, int restaurantId) {
        return applyReaction(userId, restaurantId, -1);
    }

    private static CompletableFuture<Void> applyReaction(int userId, int restaurantId, int newReaction) {
        return HistoryDAO.getInteractionType(userId, restaurantId).thenCompose(currentOpt -> {
            Integer current = currentOpt.orElse(null);
            int oldScore = asScore(current);
            int newScore = asScore(newReaction);
            if (oldScore == newScore) return CompletableFuture.completedFuture(null);

            return HistoryDAO.upsertInteraction(userId, restaurantId, newReaction).thenRun(() -> {
                if (oldScore == 0 && newScore == 1) RestaurantDAO.incrementLikes(restaurantId);
                else if (oldScore == 0 && newScore == -1) RestaurantDAO.incrementDislikes(restaurantId);
                else if (oldScore == 1 && newScore == -1) {
                    RestaurantDAO.decrementLikes(restaurantId);
                    RestaurantDAO.incrementDislikes(restaurantId);
                } else if (oldScore == -1 && newScore == 1) {
                    RestaurantDAO.decrementDislikes(restaurantId);
                    RestaurantDAO.incrementLikes(restaurantId);
                }
                RestaurantDAO.adjustNetAndWeekly(restaurantId, newScore - oldScore);
            });
        });
    }
}
