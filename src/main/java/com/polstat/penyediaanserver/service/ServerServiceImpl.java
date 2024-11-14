package com.polstat.penyediaanserver.service;

import com.polstat.penyediaanserver.dto.ServerDto;
import com.polstat.penyediaanserver.entity.Server;
import com.polstat.penyediaanserver.entity.ServerAccount;
import com.polstat.penyediaanserver.entity.User;
import com.polstat.penyediaanserver.enums.ServerStatus;
import com.polstat.penyediaanserver.repository.ServerAccountRepository;
import com.polstat.penyediaanserver.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<Server> getServerById(Long id) {
        return serverRepository.findById(id);
    }

    @Override
    public Server approveServerRequest(Server server) {
        server.setStatus(ServerStatus.APPROVED);
        return serverRepository.save(server);
    }

    @Override
    public Server rejectServerRequest(Server server, String reason) {
        server.setStatus(ServerStatus.REJECTED);
        server.setReason(reason);
        return serverRepository.save(server);
    }

    @Override
    public Page<Server> getServerRequestsByOwner(User user, Pageable pageable) {
        return serverRepository.findByOwner(user, pageable);
    }

    @Override
    public Server releaseServer(Server server) {
        server.setStatus(ServerStatus.RELEASED);
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
        return UUID.randomUUID().toString().substring(0, 8);
    }
}