package org.shining319.newsstand_backend_system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.shining319.newsstand_backend_system.entity.SaleItem;

import java.util.List;

/**
 * @Author: shining319
 * @Date: 2026/2/25
 * @Description: 销售明细Mapper接口
 **/
@Mapper
public interface SaleItemMapper extends BaseMapper<SaleItem> {

    /**
     * 批量插入销售明细（使用自定义 TypeHandler 处理 UUID）
     *
     * @param items 销售明细列表
     * @return 影响的行数
     */
    int insertItems(@Param("items") List<SaleItem> items);

    /**
     * 按订单 ID 查询所有明细（使用自定义 TypeHandler 处理 UUID）
     *
     * @param orderId 订单 ID
     * @return 该订单的所有明细列表
     */
    List<SaleItem> selectItemsByOrderId(@Param("orderId") String orderId);
}
