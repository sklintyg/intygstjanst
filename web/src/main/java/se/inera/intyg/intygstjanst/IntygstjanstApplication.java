package se.inera.intyg.intygstjanst;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

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
        RedisAutoConfiguration.class
    }
)
@EntityScan(basePackages = "se.inera.intyg.intygstjanst.persistence.model")
@EnableJpaRepositories(basePackages = "se.inera.intyg.intygstjanst.persistence.model.dao")
public class IntygstjanstApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntygstjanstApplication.class, args);
    }
}