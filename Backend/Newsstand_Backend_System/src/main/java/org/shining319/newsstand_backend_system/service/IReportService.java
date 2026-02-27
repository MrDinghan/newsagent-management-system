package org.shining319.newsstand_backend_system.service;

import org.shining319.newsstand_backend_system.dto.response.DailyReportVO;

import java.time.LocalDate;

/**
 * @Author: shining319
 * @Date: 2026/2/27
 * @Description: 报表服务接口
 **/
public interface IReportService {

    /**
     * 查询指定日期的销售日报
     * 若当日无记录，返回空数据结构（金额为0，列表为空），不抛出异常
     *
     * @param date 查询日期（缺省为今日）
     * @return 日报统计数据
     */
    DailyReportVO getDailyReport(LocalDate date);
}
