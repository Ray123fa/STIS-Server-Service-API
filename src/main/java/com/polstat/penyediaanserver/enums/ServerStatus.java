package com.polstat.penyediaanserver.enums;

public enum ServerStatus {
    PENDING,
    APPROVED,
    REJECTED,
    RELEASED;

    public static boolean isValidStatus(String status) {
        for (ServerStatus s : ServerStatus.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }
}