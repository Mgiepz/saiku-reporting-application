package org.saiku.reporting.backend.pho;

import java.lang.reflect.Field;
import java.util.Map;

import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.StreamConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.PluginEnvironment;

public class SaikuReportingLifecycleListener implements
		IPluginLifecycleListener {

	private static final String SRPT = "srpt";

	@Override
	public void init() throws PluginLifecycleException {
		
		PluginEnvironment.init(PentahoPluginEnvironment.getInstance());
		//CdaEngine.init(new PentahoCdaEnvironment());

		/*
		 * Thnx Mysticfall for coming up with that hack!
		 */
		
		Logger log = LoggerFactory.getLogger(getClass());

		if (log.isInfoEnabled()) {
			log.info("Trying to register a content converter.");
		}

		try {
			DefaultExportHandler handler = PentahoSystem
					.get(DefaultExportHandler.class);

			Field field = handler.getClass().getDeclaredField("converters");
			field.setAccessible(true);

			try {
				@SuppressWarnings("unchecked")
				Map<String, Converter> converters = (Map<String, Converter>) field
						.get(handler);

				Converter streamConverter = null;

				for (Converter converter : converters.values()) {
					if (converter instanceof StreamConverter) {
						streamConverter = converter;
					}
				}

				if (streamConverter != null) {
					if (log.isInfoEnabled()) {
						log.info(String
								.format("Registering converter for extension '%s' : %s",
										SRPT, streamConverter));
					}
					converters.put(SRPT, streamConverter);
				}

			} finally {
				field.setAccessible(false);
			}
		} catch (Exception e) {
			if (log.isWarnEnabled()) {
				log.warn(String.format(
						"Failed to register a converter for extension '%s' : "
								+ e, SRPT), e);
			}
		}
	}

	@Override
	public void loaded() throws PluginLifecycleException {

	}

	@Override
	public void unLoaded() throws PluginLifecycleException {
		// TODO Auto-generated method stub

	}

}
