package com.polstat.penyediaanserver.dto;

import com.polstat.penyediaanserver.enums.ServerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerDto {
    private Long id;
    private UserDto owner;
    private String purpose;
    private ServerStatus status;
    private String reason;
    private Date createdAt;
    private Date updatedAt;
}
