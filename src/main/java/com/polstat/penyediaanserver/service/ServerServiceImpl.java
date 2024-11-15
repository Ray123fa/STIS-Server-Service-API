package com.polstat.penyediaanserver.service;

import com.polstat.penyediaanserver.dto.ServerDto;
import com.polstat.penyediaanserver.entity.Server;
import com.polstat.penyediaanserver.entity.ServerAccount;
import com.polstat.penyediaanserver.entity.User;
import com.polstat.penyediaanserver.enums.ServerStatus;
import com.polstat.penyediaanserver.repository.ServerAccountRepository;
import com.polstat.penyediaanserver.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;

@Service
public class ServerServiceImpl implements ServerService {
    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private ServerAccountRepository serverAccountRepository;

    @Override
    public Server requestServerAccess(ServerDto ServerDto, User user) {
        Server server = Server.builder()
                .owner(user)
                .purpose(ServerDto.getPurpose())
                .status(ServerStatus.PENDING)
                .createdAt(new Date())
                .build();
        return serverRepository.save(server);
    }

    @Override
    public Server approveServerRequest(Server server) {
        server.setStatus(ServerStatus.APPROVED);
        server.setUpdatedAt(new Date());
        return serverRepository.save(server);
    }

    @Override
    public Server rejectServerRequest(Server server, String reason) {
        server.setStatus(ServerStatus.REJECTED);
        server.setReason(reason);
        server.setUpdatedAt(new Date());
        return serverRepository.save(server);
    }

    @Override
    public Server releaseServer(Server server) {
        server.setStatus(ServerStatus.RELEASED);
        server.setUpdatedAt(new Date());
        return serverRepository.save(server);
    }

    @Override
    public void terminateServer(Server server) {
        serverRepository.delete(server);
    }

    @Override
    public ServerAccount createServerAccount(Server server, User user) {
        String username = generateUsername(user);
        String password = generatePassword();

        ServerAccount serverAccount = ServerAccount.builder()
                .server(server)
                .owner(user)
                .username(username)
                .password(password)
                .build();

        return serverAccountRepository.save(serverAccount);
    }

    @Override
    public String generateUsername(User user) {
        String baseUsername = user.getEmail().split("@")[0];
        String uniqueUsername = baseUsername;
        int counter = 1;

        while (serverAccountRepository.existsByUsername(uniqueUsername)) {
            uniqueUsername = baseUsername + "_" + counter;
            counter++;
        }

        return uniqueUsername;
    }

    @Override
    public String generatePassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }

        return password.toString();
    }
}