package com.polstat.penyediaanserver.service;

import com.polstat.penyediaanserver.dto.UserDto;
import com.polstat.penyediaanserver.enums.Role;

public interface UserService {
    UserDto createUser(UserDto user, Role role);
    UserDto updateUser(Long userId, UserDto userDto);
    boolean updateUserEmail(String currentEmail, String newEmail);
    boolean updateUserPassword(String email, String newPassword);
    void updateUserRole(Long userId, String newRole);
    boolean deleteUserByEmail(String email);
    UserDto getUserByEmail(String email);
}
