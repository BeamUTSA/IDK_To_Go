package com.idktogo.idk_to_go.model;

import java.sql.Timestamp;

public record User(
        int id,
        String username,
        String email,
        String firstName,
        String lastName,
        String password,
        boolean isAdmin,
        Timestamp createdAt
) {}
