package se.inera.certificate.logging;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;

public class LogbackConfiguratorContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogbackConfiguratorContextListener.class);

    private static final String CLASSPATH = "classpath:";
    /**
     * initialize logback with external configuration file.
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        String logbackConfigurationUri = getConfigurationUri(servletContextEvent);
        Resource logbackConfigurationResource = getConfigurationResource(logbackConfigurationUri);
        if (logbackConfigurationResource.exists()) {
            LOGGER.info("Found logback configuration " + logbackConfigurationResource.getDescription()
                    + " - Overriding default configuration");
            JoranConfigurator configurator = new JoranConfigurator();
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.reset();
            configurator.setContext(loggerContext);
            try {
                try {
                    File logbackConfigurationFile = logbackConfigurationResource.getFile();
                    configurator.doConfigure(logbackConfigurationFile);
                } catch (IOException e) {
                    // If the resource is not available as a file (i.e. if it is bundled in
                    // a jar on the classpath), load it from an InputStream instead
                    configurator.doConfigure(logbackConfigurationResource.getInputStream());
                }
                LOGGER.info("default configuration overrided by logback configuration "
                        + logbackConfigurationResource.getDescription());
            } catch (Exception exception) {
                try {
                    new ContextInitializer(loggerContext).autoConfig();
                } catch (JoranException e) {
                    BasicConfigurator.configureDefaultContext();
                    LOGGER.error("Can't configure default configuration", exception);
                }
                LOGGER.error(
                        "Can't configure logback with specified " + logbackConfigurationResource.getDescription()
                                + " - Keep default configuration", exception);
            }
        } else {
            LOGGER.error("Can't read logback configuration specified file at "
                    + logbackConfigurationResource.getDescription() + " - Keep default configuration");
        }
    }

    private Resource getConfigurationResource(String logbackConfigurationUri) {
        Resource logbackConfigurationResource;
        if (logbackConfigurationUri.startsWith(CLASSPATH)) {
            logbackConfigurationResource = new ClassPathResource(logbackConfigurationUri.substring(CLASSPATH.length()));
        } else {
            logbackConfigurationResource = new FileSystemResource(logbackConfigurationUri);
        }
        return logbackConfigurationResource;
    }

    private String getConfigurationUri(ServletContextEvent servletContextEvent) {
        String defaultUri = CLASSPATH + "logback.xml";
        String logbackConfigParameter = servletContextEvent.getServletContext().getInitParameter("logbackConfigParameter");
        if (logbackConfigParameter!= null && !logbackConfigParameter.isEmpty()) {
            return System.getProperty(logbackConfigParameter, defaultUri);
        } else {
            return defaultUri;            
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
