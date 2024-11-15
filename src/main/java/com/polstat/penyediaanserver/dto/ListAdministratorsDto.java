package com.polstat.penyediaanserver.dto;

import com.polstat.penyediaanserver.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListAdministratorsDto {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "Rehan")
    private String name;

    @Schema(example = "rehan@stis.ac.id")
    private String email;

    @Schema(example = "ADMINISTRATOR")
    private Role role;
}
