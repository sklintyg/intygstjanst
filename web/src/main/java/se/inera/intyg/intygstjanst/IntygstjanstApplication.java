package se.inera.intyg.intygstjanst;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(
    scanBasePackages = {
        "se.inera.intyg.intygstjanst",
        "se.inera.intyg.infra.integration.intygproxyservice",
        "se.inera.intyg.infra.pu.integration.intygproxyservice",
        "se.inera.intyg.common.support.modules.support.api",
        "se.inera.intyg.common.services",
        "se.inera.intyg.common",
        "se.inera.intyg.common.support.services",
        "se.inera.intyg.common.util.integration.json"
    },
    exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        LiquibaseAutoConfiguration.class,
        ActiveMQAutoConfiguration.class,
        RedisAutoConfiguration.class
    }
)
public class IntygstjanstApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntygstjanstApplication.class, args);
    }
}