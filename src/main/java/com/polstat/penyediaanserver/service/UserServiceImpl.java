package com.polstat.penyediaanserver.service;

import com.polstat.penyediaanserver.dto.UserDto;
import com.polstat.penyediaanserver.entity.User;
import com.polstat.penyediaanserver.enums.Role;
import com.polstat.penyediaanserver.exception.EmailAlreadyExistsException;
import com.polstat.penyediaanserver.exception.UserNotExistException;
import com.polstat.penyediaanserver.mapper.UserMapper;
import com.polstat.penyediaanserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDto createUser(UserDto userDto, Role role) {
        // Cek apakah email sudah terdaftar
        if (userRepository.findByEmail(userDto.getEmail()) != null) {
            throw new EmailAlreadyExistsException("Email sudah terdaftar. Gunakan email lain.");
        }

        userDto.setName(userDto.getName());
        userDto.setEmail(userDto.getEmail());
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userDto.setRole(role);

        User user = userRepository.save(UserMapper.mapToUser(userDto));
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException("Pengguna tidak ditemukan"));
        existingUser.setName(userDto.getName());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));

        User updatedUser = userRepository.save(existingUser);
        return UserMapper.mapToUserDto(updatedUser);
    }

    @Override
    public boolean updateUserEmail(String currentEmail, String newEmail) {
        User user = userRepository.findByEmail(currentEmail);
        if (user != null) {
            user.setEmail(newEmail);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateUserPassword(String currentEmail, String newPassword) {
        User user = userRepository.findByEmail(currentEmail);
        if (user != null) {
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void updateUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException("Pengguna tidak ditemukan"));
        user.setRole(Role.valueOf(newRole));
        userRepository.save(user);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public boolean deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            userRepository.delete(user);
            return true;
        }
        return false;
    }
}
