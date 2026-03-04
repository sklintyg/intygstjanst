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
package se.inera.intyg.intygstjanst.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Enforces an allowlist on port 8080 (the public/SOAP port).
 *
 * <h3>Design</h3>
 * <ul>
 *   <li>Port 8080 is the only port that is publicly reachable.
 *       It should serve SOAP endpoints (via CXFServlet at {@code /*}) and a limited set of
 *       public REST paths (e.g. {@code /api/…}).</li>
 *   <li>Port 8081 (internal REST) must NOT be reachable from outside — enforced at the
 *       network/load-balancer level AND by {@link se.inera.intyg.infra.security.filter.InternalApiFilter}
 *       which already blocks wrong-port requests on {@code /internalapi/*}.
 *       This filter does NOT touch port-8081 requests.</li>
 *   <li>Port 8082 (Actuator) is managed by Spring Boot's separate management server.
 *       Requests on 8082 never reach this filter because it runs in the main app context only.</li>
 * </ul>
 *
 * <h3>Allowlist logic (port 8080 only)</h3>
 * <p>A request is <em>allowed</em> if its {@code requestURI} (without context path) starts with
 * any prefix in {@code public.api.allowlist.prefixes}. All other paths on port 8080 return
 * {@code 403 Forbidden}.</p>
 */
@Component
public class PublicApiAllowlistFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(PublicApiAllowlistFilter.class);

    /**
     * The main public port. Only requests on this port are subject to the allowlist.
     */
    @Value("${server.port}")
    private int publicPort;

    /**
     * Comma-separated path prefixes (without context path) that are allowed on the public port.
     * Paths that do NOT start with any of these prefixes will receive a 403 response.
     * Default: {@code /api/,/resources/,/error,/actuator,/} — the trailing {@code /} acts as
     * a wildcard that permits SOAP paths without enumeration.
     */
    @Value("${public.api.allowlist.prefixes}")
    private String allowlistPrefixesRaw;

    private List<String> allowlistPrefixes;

    @Override
    protected void initFilterBean() {
        allowlistPrefixes = Arrays.stream(allowlistPrefixesRaw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
        LOG.info("PublicApiAllowlistFilter initialised: publicPort={}, allowedPrefixes={}",
            publicPort, allowlistPrefixes);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        final int localPort = request.getLocalPort();

        // Only enforce on the public port — pass through all other ports (8081, 8082, etc.)
        if (localPort != publicPort) {
            filterChain.doFilter(request, response);
            return;
        }

        final String path = getRequestPath(request);

        if (isAllowed(path)) {
            filterChain.doFilter(request, response);
        } else {
            LOG.warn("PublicApiAllowlistFilter: BLOCKED port={} path={}", localPort, path);
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                "Access to this path is not permitted on this port.");
        }
    }

    /**
     * Returns the path relative to the context root (strips the context path prefix).
     * Example: context=/inera-certificate, URI=/inera-certificate/api/foo → /api/foo
     */
    private String getRequestPath(HttpServletRequest request) {
        final String contextPath = request.getContextPath();
        final String uri = request.getRequestURI();
        return contextPath.isEmpty() ? uri : uri.substring(contextPath.length());
    }

    private boolean isAllowed(String path) {
        return allowlistPrefixes.stream().anyMatch(path::startsWith);
    }
}