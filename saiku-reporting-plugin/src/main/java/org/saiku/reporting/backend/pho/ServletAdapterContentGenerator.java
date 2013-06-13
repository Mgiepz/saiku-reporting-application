package org.saiku.reporting.backend.pho;

import java.io.File;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.modules.jersey.EnunciateJerseyServletContainer;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.security.wrapper.SavedRequestAwareWrapper;

@SuppressWarnings("serial")
public class ServletAdapterContentGenerator extends BaseContentGenerator {

	private static final Log logger = LogFactory
			.getLog(ServletAdapterContentGenerator.class);

	private IPluginManager pm = PentahoSystem.get(IPluginManager.class);

	private static ConfigurableApplicationContext appContext;

	private static final String PLUGIN_ID = PluginConfig.PLUGIN_NAME;

	private static EnunciateJerseyServletContainer servlet;

	public ServletAdapterContentGenerator() throws ServletException {
		
		final ClassLoader origLoader = Thread.currentThread()
				.getContextClassLoader();

		final PluginClassLoader tempLoader = (PluginClassLoader) pm
				.getClassLoader(PLUGIN_ID);

		try {

			//PentahoSessionManager.setSessionData(PentahoSessionHolder.getSession());
			
			Thread.currentThread().setContextClassLoader(tempLoader);

			if (appContext == null) {
				appContext = getSpringBeanFactory();

				servlet = (EnunciateJerseyServletContainer) appContext
						.getBean("enunciatePluginServlet");
				servlet.init(new MutableServletConfig(
						"ServletAdapterContentGenerator"));
			}
		} finally {
			Thread.currentThread().setContextClassLoader(origLoader);
		}
	}

	@SuppressWarnings("nls")
	@Override
	public void createContent() throws Exception {
		Object requestOrWrapper = this.parameterProviders.get("path")
				.getParameter("httprequest");
		HttpServletRequest request = null;
		if (requestOrWrapper instanceof SavedRequestAwareWrapper) {
			request = (HttpServletRequest) ((SavedRequestAwareWrapper) requestOrWrapper)
					.getRequest();
		} else {
			request = (HttpServletRequest) requestOrWrapper;
		}
		HttpServletResponse response = (HttpServletResponse) this.parameterProviders
				.get("path").getParameter("httpresponse");

		final ClassLoader origLoader = Thread.currentThread()
				.getContextClassLoader();
		final PluginClassLoader tempLoader = (PluginClassLoader) pm
				.getClassLoader(PLUGIN_ID);
		try {
			Thread.currentThread().setContextClassLoader(tempLoader);
			servlet.service(request, response);
		} finally {
			Thread.currentThread().setContextClassLoader(origLoader);
		}
	}

	@Override
	public Log getLogger() {
		return logger;
	}

	private ConfigurableApplicationContext getSpringBeanFactory() {
		final PluginClassLoader loader = (PluginClassLoader) pm
				.getClassLoader(PLUGIN_ID);
		logger.warn(loader.getPluginDir());
		File f = new File(loader.getPluginDir(), "plugin.spring.xml"); //$NON-NLS-1$
		if (f.exists()) {
			logger.debug("Found plugin spring file @ " + f.getAbsolutePath()); //$NON-NLS-1$
			ConfigurableApplicationContext context = new FileSystemXmlApplicationContext(
					"file:" + f.getAbsolutePath()) { //$NON-NLS-1$
				@Override
				protected void initBeanDefinitionReader(
						XmlBeanDefinitionReader beanDefinitionReader) {

					beanDefinitionReader.setBeanClassLoader(loader);
				}

				@Override
				protected void prepareBeanFactory(
						ConfigurableListableBeanFactory clBeanFactory) {
					super.prepareBeanFactory(clBeanFactory);
					clBeanFactory.setBeanClassLoader(loader);
				}

				/**
				 * Critically important to override this and return the desired
				 * CL
				 **/
				@Override
				public ClassLoader getClassLoader() {
					return loader;
				}
			};
			return context;
		}
		throw new IllegalStateException("no plugin.spring.xml file found"); //$NON-NLS-1$
	}

}
