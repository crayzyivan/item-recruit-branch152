package com.item.pgmapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.item.pgentity.PgCurrency;
import org.apache.ibatis.annotations.Mapper;

/**
 * PostgreSQL货币Mapper接口
 * 对应common.currencies表
 * 
 * @author system
 */
@Mapper
public interface PgCurrencyMapper extends BaseMapper<PgCurrency> {
    
    /**
     * 根据货币名称查询货币信息
     * 
     * @param name 货币名称
     * @return 货币实体
     */
    default PgCurrency selectByName(String name) {
        return selectOne(
            new LambdaQueryWrapper<PgCurrency>()
                .eq(PgCurrency::getName, name)
                .last("LIMIT 1")
        );
    }
    
    /**
     * 根据货币符号查询货币信息
     * 
     * @param symbol 货币符号
     * @return 货币实体
     */
    default PgCurrency selectBySymbol(String symbol) {
        return selectOne(
            new LambdaQueryWrapper<PgCurrency>()
                .eq(PgCurrency::getSymbol, symbol)
                .last("LIMIT 1")
        );
    }
}
