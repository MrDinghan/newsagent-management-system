package org.shining319.newsstand_backend_system.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.shining319.newsstand_backend_system.dao.ProductMapper;
import org.shining319.newsstand_backend_system.dao.SaleItemMapper;
import org.shining319.newsstand_backend_system.dao.SaleOrderMapper;
import org.shining319.newsstand_backend_system.dto.request.CreateSaleRequest;
import org.shining319.newsstand_backend_system.dto.request.SaleItemRequest;
import org.shining319.newsstand_backend_system.entity.Product;
import org.shining319.newsstand_backend_system.entity.SaleItem;
import org.shining319.newsstand_backend_system.entity.SaleOrder;
import org.shining319.newsstand_backend_system.exception.BusinessException;
import org.shining319.newsstand_backend_system.exception.NotFoundException;
import org.shining319.newsstand_backend_system.service.ISaleService;
import org.shining319.newsstand_backend_system.util.OrderNumberGenerator;
import org.shining319.newsstand_backend_system.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: shining319
 * @Date: 2026/2/26
 * @Description: 销售服务实现类
 **/
@Slf4j
@Service
public class SaleServiceImpl implements ISaleService {

    private final ProductMapper productMapper;
    private final SaleOrderMapper saleOrderMapper;
    private final SaleItemMapper saleItemMapper;
    private final OrderNumberGenerator orderNumberGenerator;

    @Autowired
    public SaleServiceImpl(ProductMapper productMapper,
                           SaleOrderMapper saleOrderMapper,
                           SaleItemMapper saleItemMapper,
                           OrderNumberGenerator orderNumberGenerator) {
        this.productMapper = productMapper;
        this.saleOrderMapper = saleOrderMapper;
        this.saleItemMapper = saleItemMapper;
        this.orderNumberGenerator = orderNumberGenerator;
    }

    /**
     * 创建销售订单
     * 流程：验证库存 → 扣减库存（乐观锁）→ 创建订单及明细 → 返回完整订单
     *
     * @param request 创建销售订单请求（含购物车商品列表）
     * @return 创建的完整销售订单（含明细列表）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SaleOrder createSale(CreateSaleRequest request) {
        List<SaleItemRequest> itemRequests = request.getItems();

        // 1. 检查重复 productId（重复条目会导致乐观锁冲突，给出清晰的错误提示）
        long distinctCount = itemRequests.stream()
                .map(SaleItemRequest::getProductId)
                .distinct()
                .count();
        if (distinctCount < itemRequests.size()) {
            throw new BusinessException("Duplicate productId found in items, please merge quantities for the same product");
        }

        // 2. 验证所有商品存在且库存充足（二次验证）
        List<Product> products = new ArrayList<>(itemRequests.size());
        for (SaleItemRequest itemRequest : itemRequests) {
            Product product = productMapper.selectProductById(itemRequest.getProductId());
            if (product == null) {
                throw new NotFoundException("Product not found: id=" + itemRequest.getProductId());
            }
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new BusinessException(
                        "Insufficient stock for product '" + product.getName() + "': " +
                        "required=" + itemRequest.getQuantity() + ", available=" + product.getStock()
                );
            }
            products.add(product);
        }

        // 3. 逐一扣减库存（乐观锁）
        LocalDateTime now = LocalDateTime.now().withNano(0);
        for (int i = 0; i < itemRequests.size(); i++) {
            SaleItemRequest itemRequest = itemRequests.get(i);
            Product product = products.get(i);
            int newStock = product.getStock() - itemRequest.getQuantity();
            int affected = productMapper.adjustStockById(
                    product.getId(), newStock, product.getVersion(), now);
            if (affected == 0) {
                throw new BusinessException(
                        "Stock update failed due to concurrent conflict for product '" +
                        product.getName() + "', please retry"
                );
            }
        }

        // 4. 构建销售明细列表，计算订单总金额
        String orderId = UuidUtil.generateUuidV7();
        List<SaleItem> saleItems = new ArrayList<>(itemRequests.size());
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (int i = 0; i < itemRequests.size(); i++) {
            SaleItemRequest itemRequest = itemRequests.get(i);
            Product product = products.get(i);
            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            SaleItem saleItem = new SaleItem();
            saleItem.setId(UuidUtil.generateUuidV7());
            saleItem.setOrderId(orderId);
            saleItem.setProductId(product.getId());
            saleItem.setProductName(product.getName());
            saleItem.setUnitPrice(product.getPrice());
            saleItem.setQuantity(itemRequest.getQuantity());
            saleItem.setSubtotal(subtotal);

            saleItems.add(saleItem);
            totalAmount = totalAmount.add(subtotal);
            totalQuantity += itemRequest.getQuantity();
        }

        // 5. 创建销售订单
        SaleOrder order = new SaleOrder();
        order.setId(orderId);
        order.setOrderNumber(orderNumberGenerator.generate());
        order.setTotalAmount(totalAmount);
        order.setItemCount(itemRequests.size());
        order.setTotalQuantity(totalQuantity);
        saleOrderMapper.insertOrder(order);

        // 6. 批量插入销售明细
        saleItemMapper.insertItems(saleItems);

        // 7. 返回完整订单（含明细，通过selectOrderWithItems查询确保数据与DB一致）
        SaleOrder createdOrder = saleOrderMapper.selectOrderWithItems(orderId);
        log.info("销售订单创建成功: orderId={}, orderNumber={}, totalAmount={}, itemCount={}",
                orderId, order.getOrderNumber(), totalAmount, itemRequests.size());
        return createdOrder;
    }
}
