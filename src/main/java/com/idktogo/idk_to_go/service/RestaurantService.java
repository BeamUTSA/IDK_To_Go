package com.idktogo.idk_to_go.service;

import com.idktogo.idk_to_go.dao.HistoryDAO;
import com.idktogo.idk_to_go.dao.RestaurantDAO;

import java.util.Optional;

public final class RestaurantService {

    private RestaurantService() {}

    // Maps: like = +1, dislike = -1, no entry/neutral = 0
    private static int asScore(Integer liked) {
        return liked == null ? 0 : (liked > 0 ? 1 : -1);
    }

    public static void handleLike(int userId, int restaurantId) {
        applyReaction(userId, restaurantId, /*new*/ +1);
    }

    public static void handleDislike(int userId, int restaurantId) {
        applyReaction(userId, restaurantId, /*new*/ -1);
    }

    /**
     * Core transition logic:
     * - Reads current reaction (null -> no row OR liked is NULL).
     * - Persists new reaction to history.
     * - Adjusts likes/dislikes counts precisely.
     * - Adjusts net_score and weekly_likes by the true delta (new-old).
     */
    private static void applyReaction(int userId, int restaurantId, int newReaction) {
        // 1) Read current interaction (Optional.empty -> no row; present but null -> neutral)
        Optional<Integer> currentOpt = HistoryDAO.getInteractionType(userId, restaurantId);
        Integer current = currentOpt.orElse(null); // may be null
        int oldScore = asScore(current);
        int newScore = asScore(newReaction);

        // no change → do nothing
        if (oldScore == newScore) return;

        // 2) Upsert interaction first
        HistoryDAO.upsertInteraction(userId, restaurantId, newReaction);

        // 3) Adjust likes/dislikes counts
        // Transitions to handle:
        //   0 -> +1 : likes+1
        //   0 -> -1 : dislikes+1
        //  +1 -> -1 : likes-1, dislikes+1
        //  -1 -> +1 : dislikes-1, likes+1
        //   (we never neutralize here; only like/dislike endpoints)
        if (oldScore == 0 && newScore == 1) {
            RestaurantDAO.incrementLikes(restaurantId);
        } else if (oldScore == 0 && newScore == -1) {
            RestaurantDAO.incrementDislikes(restaurantId);
        } else if (oldScore == 1 && newScore == -1) {
            RestaurantDAO.decrementLikes(restaurantId);
            RestaurantDAO.incrementDislikes(restaurantId);
        } else if (oldScore == -1 && newScore == 1) {
            RestaurantDAO.decrementDislikes(restaurantId);
            RestaurantDAO.incrementLikes(restaurantId);
        }

        // 4) Adjust net_score and weekly_likes by the true delta
        int delta = newScore - oldScore; // e.g., -1 -> +1 gives +2
        RestaurantDAO.adjustNetAndWeekly(restaurantId, delta);
    }
}
