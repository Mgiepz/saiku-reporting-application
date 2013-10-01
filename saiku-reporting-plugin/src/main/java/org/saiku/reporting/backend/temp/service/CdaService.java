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
package org.saiku.reporting.backend.temp.service;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.extensions.datasources.cda.CdaDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.cda.CdaQueryEntry;
import org.saiku.reporting.backend.exceptions.SaikuReportingException;
import org.saiku.reporting.backend.service.ICdaService;
import org.saiku.reporting.backend.service.SaikuProperties;
import org.saiku.reporting.backend.util.StringUtils;
import org.saiku.reporting.core.model.ReportSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.metadata.MetadataConnection;
import pt.webdetails.cda.dataaccess.MqlDataAccess;
import pt.webdetails.cda.dataaccess.Parameter;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cpf.IPluginCall;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.InterPluginCall.Plugin;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

public class CdaService implements ICdaService {

	private URL repoURL;

	private static final Logger log = LoggerFactory.getLogger(CdaService.class);

	@Autowired
	private IContentAccessFactory contentAccessFactory;

//	private IPluginCall cdaCall;

	private String cdaName = StringUtils.randomString20();

//	public void setCdaCall(IPluginCall cdaCall) {
//		this.cdaCall = cdaCall;
//	}

	/**
	 * Generate the cda datource for the prpt
	 *
	 * @param sessionId
	 *
	 * @param mReport
	 * @param spec
	 * @throws SaikuReportingException 
	 */
	@SuppressWarnings("deprecation")
	public void generateCdaDatasource(MasterReport mReport, ReportSpecification spec) throws SaikuReportingException {

		String mql = spec.getDataSource().getQueryString();
		CdaSettings cda = generateCda(spec.getDataSource().getId(), mql);

		String queryId =  cda.getId();

		CdaDataFactory f = new CdaDataFactory();        
		String baseUrlField = null;
		f.setBaseUrlField(baseUrlField);

		String baseUrl = "temporary/cda/" + cda.getId() +".cda";

		String fileName = cda.getId() + ".cda";

		f.setFile(fileName);
		f.setSolution("");
		f.setPath(SaikuProperties.temporaryPath + cda.getId() +".cda");
		f.setUsername(SaikuProperties.cdaUser);
		f.setPassword(SaikuProperties.cdaPassword);
		f.setBaseUrl(baseUrl);     
		f.setUseLocalCall(true);
		f.setQueryEntry(queryId, new CdaQueryEntry(queryId, queryId));

		mReport.setDataFactory(f);
		mReport.setQuery(queryId);

	}

	private CdaSettings generateCda(String id, String mql)
			throws SaikuReportingException {
		String domainId = "";        
		Pattern p = Pattern.compile("<domain_id>(.*?)</domain_id>");
		Matcher m = p.matcher(mql);
		if (m.find()) {
			domainId = m.group(1);
		}

		mql = mql.replace("<![CDATA[", "").replace("]]>", "");

		//		// and then the calculated columns
		//        final Collection<ColumnDefinition> calculatedColumns = new ArrayList<ColumnDefinition>();
		//        
		//		for (FieldDefinition fieldDefinition : spec.getFieldDefinitions()) {
		//			if(fieldDefinition.getFormula()!=null){
		//				ColumnDefinition columnDef = new ColumnDefinition();
		//				columnDef.setName(fieldDefinition.getId());
		//				columnDef.setType(ColumnDefinition.TYPE.CALCULATED_COLUMN);
		//				columnDef.setFormula("=" + fieldDefinition.getFormula());
		//				calculatedColumns.add(columnDef);
		//			}
		//		}
		//        
		CdaSettings cda = new CdaSettings(id, null);

		String xmiFile ="pentaho2://" + domainId + "/metadata.xmi";

		Connection connection = new MetadataConnection("1", domainId , xmiFile);
		MqlDataAccess dataAccess = new MqlDataAccess(id, id, "1",mql);
		dataAccess.setParameters(new ArrayList<Parameter>());
		dataAccess.getColumnDefinitions().clear();
		//dataAccess.getColumnDefinitions().addAll(calculatedColumns);

		cda.addConnection(connection);
		cda.addDataAccess(dataAccess);

		storeCda(cda);
		return cda;
	}    

	public void storeCda(CdaSettings cda) throws SaikuReportingException {

		String path = SaikuProperties.temporaryPath + cda.getId() +".cda";

		try{

			IUserContentAccess userAccess = contentAccessFactory.getUserContentAccess(null);

			if(userAccess.saveFile(path, new ByteArrayInputStream(cda.asXML().getBytes()))){
				log.debug("file '" + path + "' saved ok");
			}else {
				log.error("writeFile: failed saving " + path);
				throw new SaikuReportingException("Error saving cda datasource");
			}

		}catch(Exception e){
			e.printStackTrace();
			throw new SaikuReportingException("Error saving cda datasource");
		}

	}

	/*
	 * Executes an mql query and returns the result in cda-json format.
	 * Used to feed parameter widgets
	 */
	public String doMqlQuery(String mqlQueryString) {

		String uri = SaikuProperties.temporaryPath + "params" +".cda";

		try {

			//TODO:
			String tempName = "params";
			
			generateCda(tempName, mqlQueryString);

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("path", uri);
			params.put("dataAccessId", tempName);
			params.put("outputType", null);
			
	        InterPluginCall cdaCall = new InterPluginCall(new Plugin("cda","cda"), "doQueryGet", params);

			return cdaCall.call(); 

		} catch (SaikuReportingException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void setCdaCall(IPluginCall cdaCall) {
		// TODO Auto-generated method stub
	}


}
