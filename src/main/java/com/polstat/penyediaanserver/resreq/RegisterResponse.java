package com.polstat.penyediaanserver.resreq;

import com.polstat.penyediaanserver.dto.MahasiswaDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    @Schema(defaultValue = "success")
    private String status;

    @Schema(defaultValue = "Registrasi berhasil dilakukan.")
    private String message;

    private MahasiswaDto data;
}
