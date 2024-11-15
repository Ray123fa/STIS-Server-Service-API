package com.polstat.penyediaanserver.service;

import com.polstat.penyediaanserver.dto.ServerDto;
import com.polstat.penyediaanserver.entity.Server;
import com.polstat.penyediaanserver.entity.ServerAccount;
import com.polstat.penyediaanserver.entity.User;

public interface ServerService {
    Server requestServerAccess(ServerDto serverRequestDto, User user);
    Server approveServerRequest(Server server);
    Server rejectServerRequest(Server server, String reason);
    Server releaseServer(Server server);
    void terminateServer(Server server);

    ServerAccount createServerAccount(Server server, User user);
    String generateUsername(User user);
    String generatePassword();
}