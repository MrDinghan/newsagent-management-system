package org.shining319.newsstand_backend_system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * @Author: shining319
 * @Date: 2026/2/27
 * @Description: 查询销售历史请求
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Query sale history request")
public class QuerySaleHistoryRequest {

    @Schema(
            description = "页码（从0开始，0表示第一页）",
            example = "0",
            defaultValue = "0"
    )
    @Min(value = 0, message = "页码必须大于等于0")
    private Integer page = 0;

    @Schema(
            description = "每页数量（1-100之间）",
            example = "20",
            defaultValue = "20"
    )
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size = 20;

    @Schema(
            description = "开始日期（格式：yyyy-MM-dd，可选）",
            example = "2026-02-01",
            type = "string",
            format = "date"
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Schema(
            description = "结束日期（格式：yyyy-MM-dd，可选）",
            example = "2026-02-28",
            type = "string",
            format = "date"
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}
