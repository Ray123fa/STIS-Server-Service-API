package com.polstat.penyediaanserver.service;

import com.polstat.penyediaanserver.dto.UserDto;
import com.polstat.penyediaanserver.entity.User;
import com.polstat.penyediaanserver.enums.Role;
import com.polstat.penyediaanserver.exception.EmailAlreadyExistsException;
import com.polstat.penyediaanserver.exception.InvalidEmailDomainException;
import com.polstat.penyediaanserver.exception.UserNotExistException;
import com.polstat.penyediaanserver.mapper.UserMapper;
import com.polstat.penyediaanserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.polstat.penyediaanserver.enums.Role.ADMINISTRATOR;
import static com.polstat.penyediaanserver.enums.Role.MAHASISWA;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDto createUser(UserDto userDto) {
        // Cek apakah email sudah terdaftar
        if (userRepository.findByEmail(userDto.getEmail()) != null) {
            throw new EmailAlreadyExistsException("Email sudah terdaftar. Gunakan email lain.");
        }

        // Cek apakah email berdomain STIS
        if (!userDto.getEmail().endsWith("@stis.ac.id")) {
            throw new InvalidEmailDomainException("Hanya email berdomain stis.ac.id yang diperbolehkan.");
        }

        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userDto.setRole(MAHASISWA);

        User user = userRepository.save(UserMapper.mapToUser(userDto));
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException("Pengguna tidak ditemukan"));
        existingUser.setName(userDto.getName());
        existingUser.setEmail(userDto.getEmail());

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
    public UserDto addAdministrator(UserDto userDto) {
        // Cek apakah email sudah terdaftar
        if (userRepository.findByEmail(userDto.getEmail()) != null) {
            throw new EmailAlreadyExistsException("Email sudah terdaftar. Gunakan email lain.");
        }

        // Cek apakah email berdomain STIS
        if (!userDto.getEmail().endsWith("@stis.ac.id")) {
            throw new InvalidEmailDomainException("Hanya email berdomain stis.ac.id yang diperbolehkan.");
        }

        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userDto.setRole(ADMINISTRATOR);

        User user = userRepository.save(UserMapper.mapToUser(userDto));
        return UserMapper.mapToUserDto(user);
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
