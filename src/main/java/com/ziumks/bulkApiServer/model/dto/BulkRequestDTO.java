package com.ziumks.bulkApiServer.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "bulk Request Dto")
public class BulkRequestDTO {

    @Schema(description = "테이블 명", required = true)
    private String tableName;
    @Schema(description = "만들 인덱스 명", required = true)
    private String indexName;
    @Schema(description = "인덱스 doc Id ,\"\" 일 시 uuid로 docId 생성되어 들어감 " , required = true ,defaultValue = "")
    private String docId;

    @Schema(description = "인덱스 저장할 data List (json)", required = true)
    private List<Map<String,Object>> dataList;

}
