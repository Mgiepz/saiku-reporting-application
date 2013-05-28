package org.saiku.reporting.backend.pho;

import pt.webdetails.cpf.SimpleContentGenerator;

public class SaikuReportingContentGenerator extends SimpleContentGenerator {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getPluginName() {
		return PluginConfig.PLUGIN_NAME;
	}

}
