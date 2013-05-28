package org.saiku.reporting.backend.pho;

import javax.servlet.ServletException;

import org.codehaus.enunciate.modules.jersey.EnunciateJerseyServletContainer;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;

import pt.webdetails.cpf.SpringEnabledContentGenerator;

public class ServletAdapterContentGenerator extends SpringEnabledContentGenerator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String PLUGIN_ID = PluginConfig.PLUGIN_NAME;

	private static EnunciateJerseyServletContainer servlet;

	public ServletAdapterContentGenerator() throws ServletException {

		super();

		final ClassLoader origLoader = Thread.currentThread()
				.getContextClassLoader();


		final PluginClassLoader tempLoader = (PluginClassLoader) pm.getClassLoader(PLUGIN_ID);

		try {
			
			Thread.currentThread().setContextClassLoader(tempLoader);
			servlet = (EnunciateJerseyServletContainer) pluginContext.getBean("enunciatePluginServlet");
			servlet.init(new MutableServletConfig("ServletAdapterContentGenerator"));
			
		} finally {
			Thread.currentThread().setContextClassLoader(origLoader);
		}
	}

	@Override
	public String getPluginName() {
		return PluginConfig.PLUGIN_NAME;
	}

}
