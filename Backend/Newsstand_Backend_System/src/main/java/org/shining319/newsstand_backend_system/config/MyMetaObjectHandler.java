package org.shining319.newsstand_backend_system.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * @Author: shining319
 * @Date: 2026/2/8
 * @Description: MyBatis-Plus 自动填充器
 **/
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 在插入期间自动填充“createdAt”字段或者"stockInDate"字段
     *
     * @param metaObject the meta object to be filled
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        // this.strictInsertFill(metaObject, "stockInDate", LocalDateTime.class, LocalDateTime.now());
        // this.strictInsertFill(metaObject, "appliedAt", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 在更新期间自动填充“updateAt”字段。
     *
     * @param metaObject the meta object to be filled
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        // this.strictUpdateFill(metaObject, "readAt", LocalDateTime.class, LocalDateTime.now());

    }

}
