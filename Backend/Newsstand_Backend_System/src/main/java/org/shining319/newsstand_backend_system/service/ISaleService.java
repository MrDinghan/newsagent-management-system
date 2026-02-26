package org.shining319.newsstand_backend_system.service;

import org.shining319.newsstand_backend_system.dto.request.CreateSaleRequest;
import org.shining319.newsstand_backend_system.entity.SaleOrder;

/**
 * @Author: shining319
 * @Date: 2026/2/26
 * @Description: 销售服务接口
 **/
public interface ISaleService {

    /**
     * 创建销售订单
     * 验证库存 → 扣减库存 → 创建订单及明细
     *
     * @param request 创建销售订单请求（含购物车商品列表）
     * @return 创建的完整销售订单（含明细列表）
     * @throws org.shining319.newsstand_backend_system.exception.NotFoundException 当某个商品不存在时
     * @throws org.shining319.newsstand_backend_system.exception.BusinessException 当某个商品库存不足或乐观锁冲突时
     */
    SaleOrder createSale(CreateSaleRequest request);
}
