package com.polstat.penyediaanserver.resreq;

import com.polstat.penyediaanserver.dto.ListUsersDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListUsersResponse {
    @Schema(defaultValue = "success")
    private String status;

    private List<ListUsersDto> data;

    @Schema(example = "1")
    private int totalPages;

    @Schema(example = "0")
    private int page;

    @Schema(example = "1")
    private long totalElements;
}
