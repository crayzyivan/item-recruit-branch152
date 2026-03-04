package com.item.framework.config;

import com.baomidou.mybatisplus.annotation.DbType;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * PostgreSQL数据源配置类
 * 
 * 配置PostgreSQL数据源、SqlSessionFactory、事务管理器等
 * 专门用于处理PostgreSQL数据库操作，与MySQL数据源分离
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-28  14:13
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@MapperScan(basePackages = "com.item.pgmapper", sqlSessionFactoryRef = "pgSqlSessionFactory")
public class PostgresDataSourceConfig {

    @Bean(name = "pgDataSource")
    @ConfigurationProperties(prefix = "spring.postgresql.datasource")
    public HikariDataSource pgDataSource() {
        log.info("🔧 init PostgreSQL dataSource...");
        return new HikariDataSource();
    }

    @Bean(name = "pgMybatisPlusInterceptor")
    public MybatisPlusInterceptor pgMybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }


    @Bean(name = "pgSqlSessionFactory")
    public SqlSessionFactory pgSqlSessionFactory(@Qualifier("pgDataSource") DataSource dataSource,
                                                  @Qualifier("pgMybatisPlusInterceptor") MybatisPlusInterceptor interceptor) throws Exception {
        // 使用MyBatis Plus的SqlSessionFactoryBean
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        
        // 设置MyBatis Plus插件
        factoryBean.setPlugins(interceptor);
        
        // PostgreSQL 不使用逻辑删除，全局配置通过 Nacos 自动应用
        
        // 如果有 mapper XML 文件，可以设置：
        // factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/pg/*.xml"));
        return factoryBean.getObject();
    }

    @Bean(name = "pgTransactionManager")
    public DataSourceTransactionManager pgTransactionManager(@Qualifier("pgDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "pgSqlSessionTemplate")
    public SqlSessionTemplate pgSqlSessionTemplate(@Qualifier("pgSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}