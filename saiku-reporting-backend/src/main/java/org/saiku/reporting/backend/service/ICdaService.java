/*******************************************************************************
 * Copyright 2013 Marius Giepz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.saiku.reporting.backend.service;

import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.saiku.reporting.backend.exceptions.SaikuReportingException;
import org.saiku.reporting.core.model.ReportSpecification;

import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cpf.IPluginCall;

public interface ICdaService {

	public abstract void setCdaCall(IPluginCall cdaCall);

	/**
	 * Generate the cda datource for the prpt
	 *
	 * @param sessionId
	 *
	 * @param mReport
	 * @param spec
	 * @throws SaikuReportingException 
	 */
	public abstract void generateCdaDatasource(MasterReport mReport,
			ReportSpecification spec) throws SaikuReportingException;

	public abstract void storeCda(CdaSettings cda)
			throws SaikuReportingException;

	/*
	 * Executes an mql query and returns the result in cda-json format.
	 * Used to feed parameter widgets
	 */
	public abstract String doMqlQuery(String mqlQueryString);

}
