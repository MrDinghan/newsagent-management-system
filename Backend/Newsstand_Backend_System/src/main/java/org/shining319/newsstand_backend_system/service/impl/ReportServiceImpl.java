package org.shining319.newsstand_backend_system.service.impl;

import org.shining319.newsstand_backend_system.dao.SaleOrderMapper;
import org.shining319.newsstand_backend_system.dto.response.CategorySalesVO;
import org.shining319.newsstand_backend_system.dto.response.DailyReportVO;
import org.shining319.newsstand_backend_system.dto.response.TopProductVO;
import org.shining319.newsstand_backend_system.service.IReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Author: shining319
 * @Date: 2026/2/27
 * @Description: 报表服务实现类
 **/
@Service
public class ReportServiceImpl implements IReportService {

    private final SaleOrderMapper saleOrderMapper;

    @Autowired
    public ReportServiceImpl(SaleOrderMapper saleOrderMapper) {
        this.saleOrderMapper = saleOrderMapper;
    }

    /**
     * 查询指定日期的销售日报
     * 若当日无记录，返回空数据结构（金额为0，列表为空），不抛出异常
     *
     * @param date 查询日期（缺省为今日）
     * @return 日报统计数据
     */
    @Override
    @Transactional(readOnly = true)
    public DailyReportVO getDailyReport(LocalDate date) {
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

        // 1. 查询订单数和总销售额
        Integer orderCount = saleOrderMapper.selectDailyOrderCount(startDateTime, endDateTime);
        BigDecimal totalAmount = saleOrderMapper.selectDailyTotalAmount(startDateTime, endDateTime);

        // 2. 查询 TOP5 畅销商品
        List<TopProductVO> topProducts = saleOrderMapper.selectTopProducts(startDateTime, endDateTime);

        // 3. 查询各类别销售金额，并计算占比
        List<Map<String, Object>> categorySalesRaw = saleOrderMapper.selectCategorySales(startDateTime, endDateTime);
        CategorySalesVO categorySales = buildCategorySalesVO(categorySalesRaw, totalAmount);

        // 4. 组装日报
        DailyReportVO report = new DailyReportVO();
        report.setDate(date);
        report.setOrderCount(orderCount != null ? orderCount : 0);
        report.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        report.setTopProducts(topProducts != null ? topProducts : Collections.emptyList());
        report.setCategorySales(categorySales);

        return report;
    }

    /**
     * 从原始类别查询结果中构建 CategorySalesVO，计算各类别金额和占比
     *
     * @param categorySalesRaw 原始查询结果（每行含 productType 和 amount）
     * @param totalAmount      当日总销售额
     * @return 类别销售VO
     */
    private CategorySalesVO buildCategorySalesVO(List<Map<String, Object>> categorySalesRaw, BigDecimal totalAmount) {
        BigDecimal newspaperAmount = BigDecimal.ZERO;
        BigDecimal magazineAmount = BigDecimal.ZERO;

        if (categorySalesRaw != null) {
            for (Map<String, Object> row : categorySalesRaw) {
                String type = (String) row.get("productType");
                Object amountObj = row.get("amount");
                BigDecimal amount = amountObj instanceof BigDecimal
                        ? (BigDecimal) amountObj
                        : new BigDecimal(amountObj.toString());

                if ("NEWSPAPER".equals(type)) {
                    newspaperAmount = amount;
                } else if ("MAGAZINE".equals(type)) {
                    magazineAmount = amount;
                }
            }
        }

        // 计算占比（totalAmount为0时占比为0，避免除零）
        double newspaperPercentage = 0.0;
        double magazinePercentage = 0.0;
        if (totalAmount != null && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            newspaperPercentage = newspaperAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalAmount, 2, RoundingMode.HALF_UP)
                    .doubleValue();
            magazinePercentage = magazineAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalAmount, 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return new CategorySalesVO(newspaperAmount, magazineAmount, newspaperPercentage, magazinePercentage);
    }
}
