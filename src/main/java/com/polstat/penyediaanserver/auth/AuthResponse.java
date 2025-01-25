package com.polstat.penyediaanserver.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private static final String jwt = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJleGFtcGxlQHN0aXMuYW" +
            "MuaWQiLCJyb2xlIjoiTUFIQVNJU1dBIiwiaXNzIjoiUG9sc3RhdCIsIm" +
            "lhdCI6MTczMTUxNTQ5OCwiZXhwIjoxNzMxNjAxODk4fQ.X5SctKzDMV" +
            "jQ5quFUvSsPdtIcCtLb3MCzr2nPRrvt3egkwNkNRXr6WjR6OmWpoUh0" +
            "avFZpmOtfXhOs73KKaqWQ";

    @Schema(example = "Rayhan")
    private String name;

    @Schema(example = "rehan@stis.ac.id")
    private String email;

    @Schema(example = "MAHASISWA")
    private String role;

    @Schema(example = jwt)
    private String accessToken;
}
