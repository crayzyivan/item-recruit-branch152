package com.item.framework.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * MySQL主数据源配置类
 * 
 * 配置MySQL作为主数据源，处理主要的业务数据
 * 包括Job、Candidate等核心业务表
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-28
 */
@Slf4j
@Configuration
@MapperScan(basePackages = "com.item.mapper", sqlSessionFactoryRef = "mysqlSqlSessionFactory")
public class MysqlDataSourceConfig {

    @Primary
    @Bean(name = "mysqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public HikariDataSource mysqlDataSource() {
        log.info(" init MySQL dataSoure...");
        return new HikariDataSource();
    }

    @Bean(name = "mysqlMybatisPlusInterceptor")
    public MybatisPlusInterceptor mysqlMybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }


    @Primary
    @Bean(name = "mysqlSqlSessionFactory")
    public SqlSessionFactory mysqlSqlSessionFactory(@Qualifier("mysqlDataSource") DataSource dataSource,
                                                     @Qualifier("mysqlMybatisPlusInterceptor") MybatisPlusInterceptor interceptor,
                                                     MybatisFillConfig mybatisFillConfig) throws Exception {
        // 使用MyBatis Plus的SqlSessionFactoryBean
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        
        // 设置MyBatis Plus插件
        factoryBean.setPlugins(interceptor);
        
        // 设置全局配置，包括自动填充处理器
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(mybatisFillConfig);
        factoryBean.setGlobalConfig(globalConfig);
        
        return factoryBean.getObject();
    }

    @Primary
    @Bean(name = "mysqlTransactionManager")
    public DataSourceTransactionManager mysqlTransactionManager(@Qualifier("mysqlDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Primary
    @Bean(name = "mysqlSqlSessionTemplate")
    public SqlSessionTemplate mysqlSqlSessionTemplate(@Qualifier("mysqlSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
