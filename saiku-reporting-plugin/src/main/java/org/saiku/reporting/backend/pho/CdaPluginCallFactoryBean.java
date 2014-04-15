package org.saiku.reporting.backend.pho;

import org.springframework.beans.factory.FactoryBean;

import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.plugincall.api.IPluginCall;

public class CdaPluginCallFactoryBean implements FactoryBean {

	@Override
	public Object getObject() throws Exception {
		return new InterPluginCall(InterPluginCall.CDA,"doQueryGet");
	}

	@Override
	public Class<IPluginCall> getObjectType() {
		return IPluginCall.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

}
