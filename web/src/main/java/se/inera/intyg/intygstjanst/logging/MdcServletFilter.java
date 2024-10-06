package se.inera.intyg.intygstjanst.logging;

import static se.inera.intyg.intygstjanst.logging.MdcLogConstants.MDC_SESSION_ID_KEY;
import static se.inera.intyg.intygstjanst.logging.MdcLogConstants.MDC_SPAN_ID_KEY;
import static se.inera.intyg.intygstjanst.logging.MdcLogConstants.MDC_TRACE_ID_KEY;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@Component
public class MdcServletFilter implements Filter {

  @Autowired
  private MdcHelper mdcHelper;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      if (request instanceof HttpServletRequest http) {
        MDC.put(MDC_SESSION_ID_KEY, mdcHelper.sessionId(http));
        MDC.put(MDC_TRACE_ID_KEY, mdcHelper.traceId(http));
        MDC.put(MDC_SPAN_ID_KEY, mdcHelper.spanId());
      }
      chain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }

  @Override
  public void init(FilterConfig filterConfig) {
    SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
        filterConfig.getServletContext());
  }
}