package org.shining319.newsstand_backend_system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.shining319.newsstand_backend_system.entity.SaleOrder;

import java.time.LocalDateTime;

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
}
