package com.polstat.penyediaanserver.service;

import com.polstat.penyediaanserver.dto.ServerDto;
import com.polstat.penyediaanserver.entity.Server;
import com.polstat.penyediaanserver.entity.ServerAccount;
import com.polstat.penyediaanserver.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ServerService {
    Server requestServerAccess(ServerDto serverRequestDto, User user);
    Optional<Server> getServerById(Long id);
    Server approveServerRequest(Server server);
    Server rejectServerRequest(Server server, String reason);
    Page<Server> getServerRequestsByOwner(User user, Pageable pageable);
    Server releaseServer(Server server);
    void terminateServer(Server server);

    ServerAccount createServerAccount(Server server, User user);
    String generateUsername(User user);
    String generatePassword();
}