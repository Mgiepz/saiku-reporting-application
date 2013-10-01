/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.saiku.reporting.backend.temp.cpf;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cpf.IPluginCall;

public abstract class AbstractInterPluginCall implements IPluginCall {

	protected static final Log logger = LogFactory.getLog(AbstractInterPluginCall.class);

	protected String plugin;
	protected String method;

	protected Map<String, Object> requestParameters;

	public AbstractInterPluginCall(){}


	/**
	 * Creates a new call.
	 * @param plugin the plugin to call
	 * @param method 
	 */
	public AbstractInterPluginCall(String plugin, String method){    
		init(plugin, method, new HashMap<String, Object>());
	}

	public AbstractInterPluginCall(String plugin, String method, Map<String, Object> params) {
		init(plugin, method, params);    
	}

	public void init(String plugin, String method, Map<String, Object> params) {
		if (plugin == null) {
			throw new IllegalArgumentException("Plugin must be specified");
		}

		this.plugin = plugin;
		this.method = method;
		if (this.requestParameters == null) {
			this.requestParameters = new HashMap<String, Object>();
		}
		this.requestParameters.putAll(
				params != null
				? params
						: new HashMap<String, Object>());
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public abstract String call();

}
