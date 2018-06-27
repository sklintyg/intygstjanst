/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.intygstjanst.persistence.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

public abstract class JpaConfigBase {

    static final String LIQUIBASE_SCRIPT = "changelog/changelog.xml";

    private static final Logger LOG = LoggerFactory.getLogger(JpaConfigBase.class);

    private static final int MAXIMUM_POOL_SIZE = 20;


    @Value("${hibernate.dialect}")
    private String hibernateDialect;
    @Value("${hibernate.hbm2ddl.auto}")
    private String hibernateHbm2ddl;
    @Value("${hibernate.show_sql}")
    private String hibernateShowSql;
    @Value("${hibernate.format_sql}")
    private String hibernateFormatSql;

    @Value("${db.driver}")
    private String databaseDriver;
    @Value("${db.url}")
    private String databaseUrl;
    @Value("${db.username}")
    private String databaseUsername;
    @Value("${db.password}")
    private String databasePassword;


    @Bean(name = "entityManagerFactory")
    LocalContainerEntityManagerFactoryBean entityManagerFactory(final DataSource dataSource) {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setPersistenceUnitName(JpaConstans.PERSISTANCE_UNIT_NAME);
        entityManagerFactoryBean.setPackagesToScan(JpaConstans.BASE_PACKAGE_TO_SCAN);
        entityManagerFactoryBean.setJpaProperties(additionalProperties());

        return entityManagerFactoryBean;
    }

    @Bean(name = "transactionManager")
    JpaTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @SuppressWarnings("ContextJavaBeanUnresolvedMethodsInspection")
    @Bean(name = "dataSource", destroyMethod = "shutdown")
    DataSource dataSource() {
        LOG.info("Initialize data-source with url: {}", databaseUrl);

        final HikariConfig dataSourceConfig = new HikariConfig();
        dataSourceConfig.setDriverClassName(databaseDriver);
        dataSourceConfig.setJdbcUrl(databaseUrl);
        dataSourceConfig.setUsername(databaseUsername);
        dataSourceConfig.setPassword(databasePassword);
        dataSourceConfig.setAutoCommit(false);
        dataSourceConfig.setConnectionTestQuery("SELECT 1");
        dataSourceConfig.setMaximumPoolSize(MAXIMUM_POOL_SIZE);

        return new HikariDataSource(dataSourceConfig);
    }

    Properties additionalProperties() {
        final Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", hibernateDialect);
        jpaProperties.put("hibernate.hbm2ddl.auto", hibernateHbm2ddl);
        jpaProperties.put("hibernate.show_sql", hibernateShowSql);
        jpaProperties.put("hibernate.format_sql", hibernateFormatSql);
        jpaProperties.put("hibernate.id.new_generator_mappings", false);
        jpaProperties.put("hibernate.enable_lazy_load_no_trans", false);

        return jpaProperties;
    }

}
