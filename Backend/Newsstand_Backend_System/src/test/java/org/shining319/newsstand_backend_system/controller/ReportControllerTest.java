package org.shining319.newsstand_backend_system.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.shining319.newsstand_backend_system.dao.ProductMapper;
import org.shining319.newsstand_backend_system.dao.SaleItemMapper;
import org.shining319.newsstand_backend_system.dao.SaleOrderMapper;
import org.shining319.newsstand_backend_system.dto.response.CategorySalesVO;
import org.shining319.newsstand_backend_system.dto.response.DailyReportVO;
import org.shining319.newsstand_backend_system.dto.response.TopProductVO;
import org.shining319.newsstand_backend_system.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @Author: shining319
 * @Date: 2026/2/27
 * @Description: ReportController单元测试
 * 测试范围：HTTP请求/响应处理、日期参数解析、异常转HTTP状态码、Service调用验证
 * 不测试：业务逻辑细节（留给ReportServiceImplTest）
 **/
@WebMvcTest(controllers = ReportController.class,
        excludeAutoConfiguration = {
                com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
        })
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReportService reportService;

    @MockitoBean
    private ProductMapper productMapper;

    @MockitoBean
    private SaleOrderMapper saleOrderMapper;

    @MockitoBean
    private SaleItemMapper saleItemMapper;

    /**
     * 构建Mock Service返回的DailyReportVO
     */
    private DailyReportVO buildMockReport(LocalDate date) {
        TopProductVO top1 = new TopProductVO("人民日报", 10);
        TopProductVO top2 = new TopProductVO("读者", 5);
        CategorySalesVO categorySales = new CategorySalesVO(
                new BigDecimal("25.00"), new BigDecimal("50.00"), 33.33, 66.67);

        DailyReportVO report = new DailyReportVO();
        report.setDate(date);
        report.setTotalAmount(new BigDecimal("75.00"));
        report.setOrderCount(8);
        report.setTopProducts(List.of(top1, top2));
        report.setCategorySales(categorySales);
        return report;
    }

    // ==================== B3.1.1: 指定日期查询 ====================

    @Test
    @DisplayName("查询日报 - 指定日期成功（200 OK，返回统计数据）")
    void testGetDailyReport_Success_WithDate() throws Exception {
        // Given
        LocalDate queryDate = LocalDate.of(2026, 2, 26);
        when(reportService.getDailyReport(queryDate)).thenReturn(buildMockReport(queryDate));

        // When & Then
        mockMvc.perform(get("/api/reports/daily")
                        .param("date", "2026-02-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.date").value("2026-02-26"))
                .andExpect(jsonPath("$.data.totalAmount").value(75.00))
                .andExpect(jsonPath("$.data.orderCount").value(8))
                .andExpect(jsonPath("$.data.topProducts").isArray())
                .andExpect(jsonPath("$.data.topProducts.length()").value(2))
                .andExpect(jsonPath("$.data.topProducts[0].productName").value("人民日报"))
                .andExpect(jsonPath("$.data.topProducts[0].totalQuantity").value(10))
                .andExpect(jsonPath("$.data.categorySales.newspaperAmount").value(25.00))
                .andExpect(jsonPath("$.data.categorySales.magazineAmount").value(50.00));

        verify(reportService, times(1)).getDailyReport(queryDate);
    }

    @Test
    @DisplayName("查询日报 - 不传日期参数（缺省今日，200 OK）")
    void testGetDailyReport_Success_DefaultDate() throws Exception {
        // Given: no date param, controller defaults to LocalDate.now()
        LocalDate today = LocalDate.now();
        when(reportService.getDailyReport(today)).thenReturn(buildMockReport(today));

        // When & Then
        mockMvc.perform(get("/api/reports/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderCount").value(8));

        verify(reportService, times(1)).getDailyReport(today);
    }

    // ==================== B3.1.1: 无销售记录（空数据结构） ====================

    @Test
    @DisplayName("查询日报 - 当日无销售记录（返回空数据结构，200 OK，不返回404）")
    void testGetDailyReport_EmptyData() throws Exception {
        // Given: no records for the date → empty structure, NOT 404
        LocalDate queryDate = LocalDate.of(2026, 1, 1);
        DailyReportVO emptyReport = new DailyReportVO();
        emptyReport.setDate(queryDate);
        emptyReport.setTotalAmount(BigDecimal.ZERO);
        emptyReport.setOrderCount(0);
        emptyReport.setTopProducts(Collections.emptyList());
        emptyReport.setCategorySales(new CategorySalesVO(
                BigDecimal.ZERO, BigDecimal.ZERO, 0.0, 0.0));
        when(reportService.getDailyReport(queryDate)).thenReturn(emptyReport);

        // When & Then
        mockMvc.perform(get("/api/reports/daily")
                        .param("date", "2026-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderCount").value(0))
                .andExpect(jsonPath("$.data.totalAmount").value(0))
                .andExpect(jsonPath("$.data.topProducts").isArray())
                .andExpect(jsonPath("$.data.topProducts.length()").value(0))
                .andExpect(jsonPath("$.data.categorySales.newspaperAmount").value(0))
                .andExpect(jsonPath("$.data.categorySales.magazineAmount").value(0));

        verify(reportService, times(1)).getDailyReport(queryDate);
    }

    // ==================== B3.1.1: 日期格式校验 ====================

    @Test
    @DisplayName("查询日报 - 日期格式错误（非yyyy-MM-dd）→ 400 Bad Request")
    void testGetDailyReport_InvalidDateFormat() throws Exception {
        // When & Then: "invalid-date" cannot be parsed as LocalDate
        mockMvc.perform(get("/api/reports/daily")
                        .param("date", "invalid-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").exists());

        verify(reportService, never()).getDailyReport(any());
    }
}
