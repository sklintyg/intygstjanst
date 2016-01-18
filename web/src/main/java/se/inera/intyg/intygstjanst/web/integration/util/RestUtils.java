/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.integration.util;

import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public final class RestUtils {

    private RestUtils() {
    }

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
