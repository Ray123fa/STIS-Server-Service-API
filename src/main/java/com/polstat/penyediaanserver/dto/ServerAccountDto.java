package com.polstat.penyediaanserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerAccountDto {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "rehan")
    private String username;

    @Schema(example = "0531b3b6")
    private String password;
}
