package org.shining319.newsstand_backend_system.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shining319.newsstand_backend_system.dao.SaleOrderMapper;
import org.shining319.newsstand_backend_system.dto.response.CategorySalesVO;
import org.shining319.newsstand_backend_system.dto.response.DailyReportVO;
import org.shining319.newsstand_backend_system.dto.response.TopProductVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @Author: shining319
 * @Date: 2026/2/27
 * @Description: ReportServiceImpl单元测试
 * 测试范围：日期转换、数据组装、类别占比计算、空数据/null处理
 * 不测试：HTTP层、数据库实际操作
 **/
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private SaleOrderMapper saleOrderMapper;

    @InjectMocks
    private ReportServiceImpl reportService;

    // ==================== B3.1.1: 正常查询 ====================

    @Test
    @DisplayName("查询日报 - 有数据，返回正确统计结果")
    void testGetDailyReport_WithData() {
        // Given
        LocalDate date = LocalDate.of(2026, 2, 27);
        LocalDateTime startDT = date.atStartOfDay();
        LocalDateTime endDT = date.atTime(LocalTime.MAX);

        List<TopProductVO> topProducts = List.of(
                new TopProductVO("人民日报", 10),
                new TopProductVO("读者", 5)
        );
        List<Map<String, Object>> categorySalesRaw = List.of(
                Map.of("productType", "NEWSPAPER", "amount", new BigDecimal("25.00")),
                Map.of("productType", "MAGAZINE", "amount", new BigDecimal("50.00"))
        );

        when(saleOrderMapper.selectDailyOrderCount(startDT, endDT)).thenReturn(8);
        when(saleOrderMapper.selectDailyTotalAmount(startDT, endDT)).thenReturn(new BigDecimal("75.00"));
        when(saleOrderMapper.selectTopProducts(startDT, endDT)).thenReturn(topProducts);
        when(saleOrderMapper.selectCategorySales(startDT, endDT)).thenReturn(categorySalesRaw);

        // When
        DailyReportVO result = reportService.getDailyReport(date);

        // Then
        assertThat(result.getDate()).isEqualTo(date);
        assertThat(result.getOrderCount()).isEqualTo(8);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("75.00");
        assertThat(result.getTopProducts()).hasSize(2);
        assertThat(result.getTopProducts().get(0).getProductName()).isEqualTo("人民日报");
        assertThat(result.getTopProducts().get(0).getTotalQuantity()).isEqualTo(10);
        assertThat(result.getCategorySales().getNewspaperAmount()).isEqualByComparingTo("25.00");
        assertThat(result.getCategorySales().getMagazineAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("查询日报 - 历史日期，日期转换正确（atStartOfDay/atTime(LocalTime.MAX)）")
    void testGetDailyReport_HistoricalDate_CorrectDateConversion() {
        // Given
        LocalDate date = LocalDate.of(2025, 12, 31);
        LocalDateTime expectedStart = date.atStartOfDay();           // 2025-12-31T00:00:00
        LocalDateTime expectedEnd = date.atTime(LocalTime.MAX);      // 2025-12-31T23:59:59.999999999

        when(saleOrderMapper.selectDailyOrderCount(expectedStart, expectedEnd)).thenReturn(3);
        when(saleOrderMapper.selectDailyTotalAmount(expectedStart, expectedEnd)).thenReturn(new BigDecimal("30.00"));
        when(saleOrderMapper.selectTopProducts(expectedStart, expectedEnd)).thenReturn(Collections.emptyList());
        when(saleOrderMapper.selectCategorySales(expectedStart, expectedEnd)).thenReturn(Collections.emptyList());

        // When
        DailyReportVO result = reportService.getDailyReport(date);

        // Then: 验证日期传递正确
        assertThat(result.getDate()).isEqualTo(date);
        assertThat(result.getOrderCount()).isEqualTo(3);
        verify(saleOrderMapper).selectDailyOrderCount(expectedStart, expectedEnd);
        verify(saleOrderMapper).selectDailyTotalAmount(expectedStart, expectedEnd);
    }

    // ==================== B3.1.1: 空数据处理 ====================

    @Test
    @DisplayName("查询日报 - 无销售记录，返回零值结构（不抛异常）")
    void testGetDailyReport_EmptyData() {
        // Given
        LocalDate date = LocalDate.of(2026, 1, 1);
        LocalDateTime startDT = date.atStartOfDay();
        LocalDateTime endDT = date.atTime(LocalTime.MAX);

        when(saleOrderMapper.selectDailyOrderCount(startDT, endDT)).thenReturn(0);
        when(saleOrderMapper.selectDailyTotalAmount(startDT, endDT)).thenReturn(BigDecimal.ZERO);
        when(saleOrderMapper.selectTopProducts(startDT, endDT)).thenReturn(Collections.emptyList());
        when(saleOrderMapper.selectCategorySales(startDT, endDT)).thenReturn(Collections.emptyList());

        // When
        DailyReportVO result = reportService.getDailyReport(date);

        // Then: 返回空数据结构而非null或抛异常
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isEqualTo(date);
        assertThat(result.getOrderCount()).isEqualTo(0);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTopProducts()).isEmpty();
        assertThat(result.getCategorySales()).isNotNull();
        assertThat(result.getCategorySales().getNewspaperAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getCategorySales().getMagazineAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getCategorySales().getNewspaperPercentage()).isEqualTo(0.0);
        assertThat(result.getCategorySales().getMagazinePercentage()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("查询日报 - Mapper返回null（orderCount/totalAmount/topProducts为null），默认为0/空列表")
    void testGetDailyReport_NullMapperResults() {
        // Given: Mapper returns null (edge case for some DB drivers)
        LocalDate date = LocalDate.of(2026, 2, 27);
        LocalDateTime startDT = date.atStartOfDay();
        LocalDateTime endDT = date.atTime(LocalTime.MAX);

        when(saleOrderMapper.selectDailyOrderCount(startDT, endDT)).thenReturn(null);
        when(saleOrderMapper.selectDailyTotalAmount(startDT, endDT)).thenReturn(null);
        when(saleOrderMapper.selectTopProducts(startDT, endDT)).thenReturn(null);
        when(saleOrderMapper.selectCategorySales(startDT, endDT)).thenReturn(null);

        // When
        DailyReportVO result = reportService.getDailyReport(date);

        // Then: null 应被处理为默认值
        assertThat(result.getOrderCount()).isEqualTo(0);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTopProducts()).isEmpty();
    }

    // ==================== B3.1.1: 类别占比计算 ====================

    @Test
    @DisplayName("查询日报 - NEWSPAPER+MAGAZINE均有销售，占比计算正确（四舍五入，2位小数）")
    void testGetDailyReport_CategoryPercentageCalculation() {
        // Given: NEWSPAPER=25.00, MAGAZINE=75.00, total=100.00 → 25% / 75%
        LocalDate date = LocalDate.of(2026, 2, 27);
        LocalDateTime startDT = date.atStartOfDay();
        LocalDateTime endDT = date.atTime(LocalTime.MAX);

        List<Map<String, Object>> categorySalesRaw = List.of(
                Map.of("productType", "NEWSPAPER", "amount", new BigDecimal("25.00")),
                Map.of("productType", "MAGAZINE", "amount", new BigDecimal("75.00"))
        );

        when(saleOrderMapper.selectDailyOrderCount(startDT, endDT)).thenReturn(10);
        when(saleOrderMapper.selectDailyTotalAmount(startDT, endDT)).thenReturn(new BigDecimal("100.00"));
        when(saleOrderMapper.selectTopProducts(startDT, endDT)).thenReturn(Collections.emptyList());
        when(saleOrderMapper.selectCategorySales(startDT, endDT)).thenReturn(categorySalesRaw);

        // When
        DailyReportVO result = reportService.getDailyReport(date);

        // Then
        CategorySalesVO categorySales = result.getCategorySales();
        assertThat(categorySales.getNewspaperAmount()).isEqualByComparingTo("25.00");
        assertThat(categorySales.getMagazineAmount()).isEqualByComparingTo("75.00");
        assertThat(categorySales.getNewspaperPercentage()).isEqualTo(25.0);
        assertThat(categorySales.getMagazinePercentage()).isEqualTo(75.0);
    }

    @Test
    @DisplayName("查询日报 - 仅NEWSPAPER类别有销售（MAGAZINE为0），占比为100%/0%")
    void testGetDailyReport_OnlyNewspaper() {
        // Given: only NEWSPAPER records
        LocalDate date = LocalDate.of(2026, 2, 27);
        LocalDateTime startDT = date.atStartOfDay();
        LocalDateTime endDT = date.atTime(LocalTime.MAX);

        List<Map<String, Object>> categorySalesRaw = List.of(
                Map.of("productType", "NEWSPAPER", "amount", new BigDecimal("60.00"))
        );

        when(saleOrderMapper.selectDailyOrderCount(startDT, endDT)).thenReturn(5);
        when(saleOrderMapper.selectDailyTotalAmount(startDT, endDT)).thenReturn(new BigDecimal("60.00"));
        when(saleOrderMapper.selectTopProducts(startDT, endDT)).thenReturn(Collections.emptyList());
        when(saleOrderMapper.selectCategorySales(startDT, endDT)).thenReturn(categorySalesRaw);

        // When
        DailyReportVO result = reportService.getDailyReport(date);

        // Then
        CategorySalesVO categorySales = result.getCategorySales();
        assertThat(categorySales.getNewspaperAmount()).isEqualByComparingTo("60.00");
        assertThat(categorySales.getMagazineAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(categorySales.getNewspaperPercentage()).isEqualTo(100.0);
        assertThat(categorySales.getMagazinePercentage()).isEqualTo(0.0);
    }
}
