package com.idktogo.idk_to_go.service;

import com.idktogo.idk_to_go.dao.HistoryDAO;
import com.idktogo.idk_to_go.model.UserHistory;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class HistoryService {
    public HistoryService() {}

    public static CompletableFuture<List<UserHistory>> listByUser(int userId) {
        return HistoryDAO.listByUser(userId);
    }

    public static CompletableFuture<Integer> clearUserHistory(int userId) {
        return HistoryDAO.deleteAllForUser(userId);
    }

    public static CompletableFuture<Void> clearAllHistory() {
        return HistoryDAO.deleteAllForAllUsers();
    }
}
