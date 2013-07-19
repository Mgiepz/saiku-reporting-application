package org.saiku.reporting.backend.service;

import java.util.Map;

import pt.webdetails.cpf.IPluginCall;
import pt.webdetails.cpf.plugin.CorePlugin;

public class FakeCdaCall implements IPluginCall {

	@Override
	public void init(CorePlugin plugin, String method,
			Map<String, Object> params) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMethod(String method) {
		// TODO Auto-generated method stub

	}

	@Override
	public String call() {
		
		return null;
	}

}
