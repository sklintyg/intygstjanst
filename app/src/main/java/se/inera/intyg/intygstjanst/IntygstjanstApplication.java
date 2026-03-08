package se.inera.intyg.intygstjanst;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication(
    scanBasePackages = {
        "se.inera.intyg.intygstjanst",
        "se.inera.intyg.common.support.modules.support.api",
        "se.inera.intyg.common.services",
        "se.inera.intyg.common",
        "se.inera.intyg.common.support.services",
        "se.inera.intyg.common.util.integration.json"
    }
)
public class IntygstjanstApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntygstjanstApplication.class, args);
    }
}