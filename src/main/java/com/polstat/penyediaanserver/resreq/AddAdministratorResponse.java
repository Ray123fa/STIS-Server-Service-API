package com.polstat.penyediaanserver.resreq;

import com.polstat.penyediaanserver.dto.AdministratorDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddAdministratorResponse {
    @Schema(defaultValue = "success")
    private String status;

    @Schema(defaultValue = "Administrator berhasil ditambahkan.")
    private String message;

    private AdministratorDto data;
}
