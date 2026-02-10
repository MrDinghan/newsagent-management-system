package org.shining319.newsstand_backend_system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.shining319.newsstand_backend_system.entity.Product;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: 产品Mapper接口
 **/
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    // 继承 BaseMapper，自动获得 CRUD 方法

    /**
     * 插入产品（使用自定义 TypeHandler 处理 UUID）
     *
     * @param product 产品实体
     * @return 影响的行数
     */
    int insertProduct(Product product);

    /**
     * 分页查询产品（使用自定义 TypeHandler 处理 UUID）
     *
     * @param page 分页对象
     * @param deleted 是否已删除
     * @param type 产品类型（可选）
     * @return 分页结果
     */
    IPage<Product> selectPageWithTypeHandler(
            Page<Product> page,
            @Param("deleted") Boolean deleted,
            @Param("type") String type
    );
}
