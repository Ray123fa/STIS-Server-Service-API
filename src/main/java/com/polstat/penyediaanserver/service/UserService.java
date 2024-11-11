package com.polstat.penyediaanserver.service;

import com.polstat.penyediaanserver.dto.UserDto;

public interface UserService {
    public UserDto createUser(UserDto user);
    public UserDto updateUser(Long userId, UserDto userDto);
    boolean updateUserEmail(String currentEmail, String newEmail);
    boolean updateUserPassword(String email, String newPassword);
    boolean deleteUserByEmail(String email);

//    Administrator
    public UserDto addAdministrator(UserDto user);
    void updateUserRole(Long userId, String newRole);

    public UserDto getUserByEmail(String email);
}
