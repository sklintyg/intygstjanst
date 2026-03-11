/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.infrastructure.security.interceptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

@ExtendWith(MockitoExtension.class)
class ApiBasePathEnforcingInterceptorTest {

  @InjectMocks
  private ApiBasePathEnforcingInterceptor interceptor;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HandlerMethod handlerMethod;

  @ApiBasePath("/internalapi")
  static class SinglePathController {}

  @ApiBasePath({"/internalapi", "/externalapi"})
  static class MultiPathController {}

  static class UnannotatedController {}

  @Test
  void shouldAllowWhenHandlerIsNotHandlerMethod() throws Exception {
    final var result = interceptor.preHandle(request, response, new Object());

    assertTrue(result);
  }

  @Test
  void shouldBlockAndSendNotFoundWhenControllerHasNoApiBasePathAnnotation() throws Exception {
    when(handlerMethod.getBeanType()).thenAnswer(inv -> UnannotatedController.class);

    final var result = interceptor.preHandle(request, response, handlerMethod);

    assertFalse(result);
    verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  void shouldAllowWhenServletPathMatchesAnnotatedPath() throws Exception {
    when(handlerMethod.getBeanType()).thenAnswer(inv -> SinglePathController.class);
    when(request.getServletPath()).thenReturn("/internalapi");

    final var result = interceptor.preHandle(request, response, handlerMethod);

    assertTrue(result);
  }

  @Test
  void shouldBlockAndSendNotFoundWhenServletPathDoesNotMatch() throws Exception {
    when(handlerMethod.getBeanType()).thenAnswer(inv -> SinglePathController.class);
    when(request.getServletPath()).thenReturn("/publicapi");

    final var result = interceptor.preHandle(request, response, handlerMethod);

    assertFalse(result);
    verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  void shouldAllowWhenServletPathMatchesOneOfMultiplePaths() throws Exception {
    when(handlerMethod.getBeanType()).thenAnswer(inv -> MultiPathController.class);
    when(request.getServletPath()).thenReturn("/externalapi");

    final var result = interceptor.preHandle(request, response, handlerMethod);

    assertTrue(result);
  }

  @Test
  void shouldBlockAndSendNotFoundWhenServletPathMatchesNoneOfMultiplePaths() throws Exception {
    when(handlerMethod.getBeanType()).thenAnswer(inv -> MultiPathController.class);
    when(request.getServletPath()).thenReturn("/unknownapi");

    final var result = interceptor.preHandle(request, response, handlerMethod);

    assertFalse(result);
    verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
  }
}
