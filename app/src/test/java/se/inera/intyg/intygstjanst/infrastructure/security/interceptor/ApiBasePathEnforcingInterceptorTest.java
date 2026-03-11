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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
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

  @Mock
  private Appender<ILoggingEvent> mockAppender;

  @Captor
  private ArgumentCaptor<LoggingEvent> logCaptor;

  @BeforeEach
  void attachLogAppender() {
    final Logger logger = (Logger) LoggerFactory.getLogger(ApiBasePathEnforcingInterceptor.class);
    logger.addAppender(mockAppender);
  }

  @AfterEach
  void detachLogAppender() {
    final Logger logger = (Logger) LoggerFactory.getLogger(ApiBasePathEnforcingInterceptor.class);
    logger.detachAppender(mockAppender);
  }

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
    when(request.getServletPath()).thenReturn("/someapi");

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

  @Test
  void shouldLogWarnWhenBlockingUnannotatedController() throws Exception {
    when(handlerMethod.getBeanType()).thenAnswer(inv -> UnannotatedController.class);
    when(request.getServletPath()).thenReturn("/someapi");

    interceptor.preHandle(request, response, handlerMethod);

    verify(mockAppender).doAppend(logCaptor.capture());
    assertEquals(Level.WARN, logCaptor.getValue().getLevel());
  }

  @Test
  void shouldLogWarnWhenBlockingPathMismatch() throws Exception {
    when(handlerMethod.getBeanType()).thenAnswer(inv -> SinglePathController.class);
    when(request.getServletPath()).thenReturn("/wrongpath");

    interceptor.preHandle(request, response, handlerMethod);

    verify(mockAppender).doAppend(logCaptor.capture());
    assertEquals(Level.WARN, logCaptor.getValue().getLevel());
  }
}
