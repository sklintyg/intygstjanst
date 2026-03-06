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
package se.inera.intyg.infra.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class InternalApiFilter extends OncePerRequestFilter {

    private static final int FORBIDDEN = 403;

    @Value("${internal.api.port}")
    private int internalApiPort;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        final var localPort = request.getLocalPort();
        if (localPort == internalApiPort) {
            filterChain.doFilter(request, response);
        } else {
            final String path = getRequestPath(request);
            log.warn("Request was BLOCKED on port={} path={}", localPort, path);
            response.sendError(FORBIDDEN);
        }
    }

    private String getRequestPath(HttpServletRequest request) {
        final String contextPath = request.getContextPath();
        final String uri = request.getRequestURI();
        return contextPath.isEmpty() ? uri : uri.substring(contextPath.length());
    }
}
