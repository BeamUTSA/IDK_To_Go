package com.idktogo.idk_to_go.service;

import com.idktogo.idk_to_go.dao.HistoryDAO;
import com.idktogo.idk_to_go.model.UserHistory;

import java.util.List;

public class UserHistoryService {

    private static final UserHistoryService INSTANCE = new UserHistoryService();
    private boolean historyTrackingEnabled = true;

    private UserHistoryService() {
        // Private constructor: use getInstance()
    }

    public static UserHistoryService getInstance() {
        return INSTANCE;
    }

    public boolean isHistoryTrackingEnabled() {
        return historyTrackingEnabled;
    }

    public void setHistoryTrackingEnabled(boolean enabled) {
        this.historyTrackingEnabled = enabled;
    }

    public boolean recordAction(int userId, int restaurantId, boolean liked) {
        if (!historyTrackingEnabled) {
            return true;
        }
        int likedValue = liked ? 1 : -1;
        return HistoryDAO.upsertInteraction(userId, restaurantId, likedValue);
    }


    public List<UserHistory> getUserHistory(int userId) {
        return HistoryDAO.getHistoryForUser(userId);
    }

    public boolean clearUserHistory(int userId) {
        return HistoryDAO.deleteHistoryForUser(userId);
    }
}
