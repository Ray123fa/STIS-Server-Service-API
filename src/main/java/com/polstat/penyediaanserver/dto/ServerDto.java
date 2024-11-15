package com.polstat.penyediaanserver.dto;

import com.polstat.penyediaanserver.enums.ServerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(example = "1")
    private Long id;
    private UserDto owner;

    @Schema(example = "Hosting project")
    private String purpose;

    @Schema(example = "PENDING")
    private ServerStatus status;

    private String reason;
    private Date createdAt;
    private Date updatedAt;
}
