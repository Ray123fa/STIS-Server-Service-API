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
public class GetMyServersDto {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "Hosting project")
    private String purpose;

    @Schema(example = "PENDING")
    private String status;
}
