package com.polstat.penyediaanserver.config;

import com.polstat.penyediaanserver.entity.User;
import com.polstat.penyediaanserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

import static com.polstat.penyediaanserver.enums.Role.ADMINISTRATOR;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Cek apakah akun admin sudah ada
        if (userRepository.findByEmail("unit-ti@stis.ac.id") == null) {
            User admin = new User();
            admin.setName("UNIT TI");
            admin.setEmail("unit-ti@stis.ac.id");
            admin.setPassword(passwordEncoder.encode("unit-ti-stis"));
            admin.setRole(ADMINISTRATOR);

            // Simpan ke database
            userRepository.save(admin);
            System.out.println("Akun administrator berhasil dibuat.");
        } else {
            System.out.println("Akun administrator sudah ada.");
        }
    }
}
