package org.shining319.newsstand_backend_system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.shining319.newsstand_backend_system.dto.request.CreateProductRequest;
import org.shining319.newsstand_backend_system.dto.request.QueryProductRequest;
import org.shining319.newsstand_backend_system.dto.request.UpdateProductRequest;
import org.shining319.newsstand_backend_system.entity.Product;

/**
 * @Author: shining319
 * @Date: 2026/2/9
 * @Description: 产品服务接口
 * 继承MyBatis-Plus的IService，提供标准CRUD方法
 **/
public interface IProductService extends IService<Product> {

    /**
     * 创建产品
     *
     * @param request 创建产品请求
     * @return 创建的产品实体
     * @throws org.shining319.newsstand_backend_system.exception.ConflictException 当产品名称已存在时
     */
    Product createProduct(CreateProductRequest request);

    /**
     * 分页查询产品列表
     * 不返回已删除产品，按创建时间倒序排列
     *
     * @param request 查询请求（包含page, size, type）
     * @return MyBatis-Plus分页对象
     */
    Page<Product> queryProducts(QueryProductRequest request);

    /**
     * 更新产品信息（不包含库存）
     *
     * @param id      产品ID
     * @param request 更新请求（name, type, price）
     * @return 更新后的产品实体
     * @throws org.shining319.newsstand_backend_system.exception.NotFoundException 当产品不存在时
     */
    Product updateProduct(String id, UpdateProductRequest request);
}
