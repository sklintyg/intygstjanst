package se.inera.certificate.integration.util;

import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class RestUtils {

    public static String entityAsString(Response response) {
        try {
            if (response.hasEntity()) {
                return IOUtils.toString((InputStream) response.getEntity());
            }
            return null;
        } catch (IOException ioe) {
            throw Throwables.propagate(ioe);
        }
    }
}
