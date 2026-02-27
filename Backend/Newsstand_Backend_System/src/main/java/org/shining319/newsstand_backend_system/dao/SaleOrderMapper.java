package org.shining319.newsstand_backend_system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.shining319.newsstand_backend_system.entity.SaleOrder;

import org.shining319.newsstand_backend_system.dto.response.TopProductVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Author: shining319
 * @Date: 2026/2/25
 * @Description: 销售订单Mapper接口
 **/
@Mapper
public interface SaleOrderMapper extends BaseMapper<SaleOrder> {

    /**
     * 插入销售订单（使用自定义 TypeHandler 处理 UUID）
     *
     * @param order 销售订单实体
     * @return 影响的行数
     */
    int insertOrder(SaleOrder order);

    /**
     * 按 ID 查询订单（含明细列表，使用 collection ResultMap）
     *
     * @param id 订单 ID
     * @return 含明细列表的完整订单，不存在返回 null
     */
    SaleOrder selectOrderWithItems(@Param("id") String id);

    /**
     * 分页查询销售历史（按时间倒序，使用自定义 TypeHandler 处理 UUID）
     *
     * @param page      分页对象
     * @param startDate 开始时间（可选）
     * @param endDate   结束时间（可选）
     * @return 分页订单列表（不含明细）
     */
    IPage<SaleOrder> selectOrderPage(
            Page<SaleOrder> page,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // ===== 日报查询相关 =====

    /**
     * 查询指定时间范围内的订单总数
     *
     * @param startDateTime 开始时间（当日 00:00:00）
     * @param endDateTime   结束时间（当日 23:59:59.999999999）
     * @return 订单总数
     */
    Integer selectDailyOrderCount(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /**
     * 查询指定时间范围内的总销售额
     *
     * @param startDateTime 开始时间（当日 00:00:00）
     * @param endDateTime   结束时间（当日 23:59:59.999999999）
     * @return 总销售额（无记录时为0）
     */
    BigDecimal selectDailyTotalAmount(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /**
     * 查询指定时间范围内 TOP5 畅销商品（按数量降序）
     * 使用 sale_items.product_name 快照字段，不关联 products 表
     *
     * @param startDateTime 开始时间
     * @param endDateTime   结束时间
     * @return TOP5 畅销商品列表（最多5条，无记录时为空列表）
     */
    List<TopProductVO> selectTopProducts(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /**
     * 查询指定时间范围内各类别（NEWSPAPER/MAGAZINE）的销售金额
     * 关联 products 表获取商品类型（不过滤 deleted，支持已软删除商品的历史数据）
     *
     * @param startDateTime 开始时间
     * @param endDateTime   结束时间
     * @return 各类别销售金额列表，Map 包含 productType 和 amount
     */
    List<Map<String, Object>> selectCategorySales(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
