package com.idktogo.idk_to_go.core;

import com.idktogo.idk_to_go.data.AppStorage;

public final class SessionManager {
    private SessionManager() {}

    private static final String KEY_ID   = "loggedInUserId";
    private static final String KEY_NAME = "loggedInUsername";

    public static void login(int userId, String username) {
        AppStorage.save(KEY_ID, String.valueOf(userId));
        AppStorage.save(KEY_NAME, username == null ? "" : username);
    }

    public static void logout() {
        AppStorage.remove(KEY_ID);
        AppStorage.remove(KEY_NAME);
    }

    public static boolean isLoggedIn() {
        return AppStorage.load(KEY_ID) != null;
    }

    public static Integer getUserId() {
        String s = AppStorage.load(KEY_ID);
        return (s == null) ? null : Integer.parseInt(s);
    }

    public static String getUsername() {
        return AppStorage.load(KEY_NAME);
    }
}
