package se.inera.intyg.intygstjanst.logging;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PerformanceLogging {

    String eventType();

    String eventAction();

    boolean isActive() default true;

}
