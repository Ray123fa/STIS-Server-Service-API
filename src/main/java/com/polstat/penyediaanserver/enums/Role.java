package com.polstat.penyediaanserver.enums;

public enum Role {
    ADMINISTRATOR,
    MAHASISWA;

    public static boolean isValidRole(String role) {
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
}