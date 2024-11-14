package com.polstat.penyediaanserver.mapper;

import com.polstat.penyediaanserver.dto.ServerDto;
import com.polstat.penyediaanserver.dto.UserDto;
import com.polstat.penyediaanserver.entity.Server;
import com.polstat.penyediaanserver.entity.User;

public class ServerMapper {

    public static Server mapToServer(ServerDto serverDto) {
        User owner = User.builder()
                .id(serverDto.getOwner().getId())
                .name(serverDto.getOwner().getName())
                .email(serverDto.getOwner().getEmail())
                .role(serverDto.getOwner().getRole())
                .build();

        return Server.builder()
                .id(serverDto.getId())
                .owner(owner)
                .purpose(serverDto.getPurpose())
                .status(serverDto.getStatus())
                .reason(serverDto.getReason())
                .createdAt(serverDto.getCreatedAt())
                .updatedAt(serverDto.getUpdatedAt())
                .build();
    }

    public static ServerDto mapToServerDto(Server server) {
        UserDto ownerDto = UserDto.builder()
                .id(server.getOwner().getId())
                .name(server.getOwner().getName())
                .email(server.getOwner().getEmail())
                .role(server.getOwner().getRole())
                .build();

        return ServerDto.builder()
                .id(server.getId())
                .owner(ownerDto)
                .purpose(server.getPurpose())
                .status(server.getStatus())
                .reason(server.getReason())
                .createdAt(server.getCreatedAt())
                .updatedAt(server.getUpdatedAt())
                .build();
    }
}