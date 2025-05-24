package com.opsbeach.virima.core.hibernate;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class HibernateConfig {
    private final JpaProperties jpaProperties;

    public HibernateConfig(JpaProperties jpaProperties) {
        this.jpaProperties = jpaProperties;
    }

    @Bean
    JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource/*,
                                                                MultiTenantConnectionProvider multiTenantConnectionProviderImpl,
                                                                CurrentTenantIdentifierResolver currentTenantIdentifierResolverImpl*/) {
        Map<String, Object> jpaPropertiesMap = new HashMap<>(jpaProperties.getProperties());
        //jpaPropertiesMap.put(AvailableSettings.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
        //jpaPropertiesMap.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProviderImpl);
        //jpaPropertiesMap.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolverImpl);
        //jpaPropertiesMap.put(AvailableSettings.DEFAULT_SCHEMA, "user");
        jpaPropertiesMap.put(AvailableSettings.FORMAT_SQL, Boolean.TRUE);
        // jpaPropertiesMap.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, "com.vladmihalcea.hibernate.type.util.CamelCaseToSnakeCaseNamingStrategy");
        var localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(dataSource);
        localContainerEntityManagerFactoryBean.setPackagesToScan("com.opsbeach*");
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(this.jpaVendorAdapter());
        localContainerEntityManagerFactoryBean.setJpaPropertyMap(jpaPropertiesMap);
        return localContainerEntityManagerFactoryBean;
    }
}