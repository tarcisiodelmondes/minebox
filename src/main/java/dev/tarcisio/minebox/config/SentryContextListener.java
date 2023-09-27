package dev.tarcisio.minebox.config;

import io.sentry.Sentry;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class SentryContextListener implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    Sentry.init();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    Sentry.close();
  }
}
