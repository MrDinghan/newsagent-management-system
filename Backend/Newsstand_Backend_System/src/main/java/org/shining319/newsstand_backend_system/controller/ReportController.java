package org.shining319.newsstand_backend_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.shining319.newsstand_backend_system.dto.response.DailyReportVO;
import org.shining319.newsstand_backend_system.dto.response.Result;
import org.shining319.newsstand_backend_system.exception.GlobalExceptionHandler;
import org.shining319.newsstand_backend_system.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * @Author: shining319
 * @Date: 2026/2/27
 * @Description: 报表控制器
 **/
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Report", description = "Sales report related interfaces")
public class ReportController {

    private final IReportService reportService;

    @Autowired
    public ReportController(IReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 查询指定日期的销售日报
     *
     * @param date 查询日期（可选，格式 yyyy-MM-dd，缺省为今日）
     * @return 日报统计数据
     */
    @GetMapping("/daily")
    @Operation(
            summary = "Get daily sales report",
            description = "Get sales statistics for a specific date, including total amount, order count, " +
                    "top 5 best-selling products, and sales breakdown by category (NEWSPAPER/MAGAZINE). " +
                    "If no sales records exist for the date, returns an empty data structure (amounts as 0, lists empty) " +
                    "rather than 404. Defaults to today if date parameter is not provided."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Query successful (returns empty data structure if no records for the date)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DailyReportVOResult.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid date format (expected yyyy-MM-dd)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GlobalExceptionHandler.ValidationExceptionResult.class)
                    )
            )
    })
    @Parameters({
            @Parameter(
                    name = "date",
                    description = "Query date (format: yyyy-MM-dd, defaults to today if not provided)",
                    example = "2026-02-27",
                    schema = @Schema(type = "string", format = "date")
            )
    })
    public Result<DailyReportVO> getDailyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        DailyReportVO report = reportService.getDailyReport(queryDate);
        return Result.ok(report);
    }

    /**
     * Swagger文档用的响应包装类
     * 用于在OpenAPI文档中正确显示 Result<DailyReportVO> 的结构
     */
    @Schema(description = "Daily sales report response")
    private static class DailyReportVOResult extends Result<DailyReportVO> {
        @Schema(description = "日报统计数据")
        @Override
        public DailyReportVO getData() {
            return super.getData();
        }
    }
}
