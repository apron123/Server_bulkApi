package com.ziumks.bulkApiServer.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Bulk Api Response")
public class BulkResponseDTO {
    @Schema(description = "응답 코드",example = "200")
    private int responseCode;

    @Schema(description = "테이블 명")
    private String tableName;

    @Schema(description = "인덱스 명")
    private String indexName;

    @Schema(description = "실패 시 실패 이유 msg")
    private String msg;


}
