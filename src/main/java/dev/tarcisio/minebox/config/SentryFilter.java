package dev.tarcisio.minebox.config;

import java.io.IOException;

import io.sentry.Sentry;
import io.sentry.SentryLevel;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = "/api/*")
public class SentryFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      chain.doFilter(request, response);
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      int status = httpResponse.getStatus();

      if (status / 100 == 5) {
        Sentry.captureMessage("Application error: code=" + status, SentryLevel.ERROR);
      }
    } catch (Throwable t) {
      Sentry.captureException(t);
      throw t;
    }
  }

}
