/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Enforces base-path access control declared via {@link ApiBasePath}.
 *
 * <p>When a controller class carries {@code @ApiBasePath}, only requests
 * whose {@code servletPath} matches one of the declared values are allowed.
 * All other requests receive a 404 response, mirroring the old JAX-RS
 * per-address server configuration.</p>
 *
 * <p>Controllers without {@code @ApiBasePath} are not restricted.</p>
 */
@Component
public class ApiBasePathEnforcingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        final var annotation = handlerMethod.getBeanType().getAnnotation(ApiBasePath.class);
        if (annotation == null) {
            return true;
        }

        final var servletPath = request.getServletPath();
        final var allowed = Arrays.asList(annotation.value());
        if (allowed.contains(servletPath)) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return false;
    }
}