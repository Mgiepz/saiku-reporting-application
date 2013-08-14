package org.saiku.reporting.backend.server;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextHolder implements ApplicationContextAware {

	  private static ApplicationContext CONTEXT;

	  public void setApplicationContext(ApplicationContext context) throws BeansException {
	    CONTEXT = context;
	  }

	  public static Object getBean(String beanName) {
	    return CONTEXT.getBean(beanName);
	  }
	}
