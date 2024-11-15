package com.polstat.penyediaanserver.resreq;

import com.polstat.penyediaanserver.entity.ServerAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalServerResponse {
    @Schema(defaultValue = "success")
    private String status;

    @Schema(defaultValue = "Pengajuan server berhasil disetujui.")
    private String message;

    @Schema(example = "{ \"username\": \"rehan\", \"password\": \"0531b3b6\" }")
    private ServerAccount account;
}
