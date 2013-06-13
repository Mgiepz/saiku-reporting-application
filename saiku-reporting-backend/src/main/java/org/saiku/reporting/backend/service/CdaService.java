package org.saiku.reporting.backend.service;

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
import pt.webdetails.cpf.plugin.CorePlugin;
import pt.webdetails.cpf.repository.IRepositoryAccess;

public class CdaService {
	
	private URL repoURL;
	
	private static final Logger log = LoggerFactory.getLogger(CdaService.class);

	private IRepositoryAccess repositoryAccess;

	private IPluginCall cdaCall;


//	public String doMqlQuery(String mqlQueryString) {
//		
//        String domainId = "";
//
//        Pattern p = Pattern.compile("<domain_id>(.*?)</domain_id>");
//        Matcher m = p.matcher(mqlQueryString);
//        if (m.find()) {
//            domainId = m.group(1);
//        }
//        
//       mqlQueryString = mqlQueryString.replace("<![CDATA[", "").replace("]]>", "");
//
//		String id = "xxx";
//		
//        CdaSettings cda = new CdaSettings(id, null);
//
//		Connection connection = new MetadataConnection("1", domainId , domainId.split("/")[1]);
//
//		MqlDataAccess dataAccess = new MqlDataAccess(id, id, "1",mqlQueryString);
//		dataAccess.setParameters(new ArrayList<Parameter>());
//		dataAccess.getColumnDefinitions().clear();
//
//		cda.addConnection(connection);
//		cda.addDataAccess(dataAccess);
//
//		//sstoreCda(cda);
//	}
	
    public void setCdaCall(IPluginCall cdaCall) {
		this.cdaCall = cdaCall;
	}

	public void setRepositoryAccess(IRepositoryAccess repositoryAccess) {
		this.repositoryAccess = repositoryAccess;
	}

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

		String baseUrl = "saiku:/" + cda.getId();
		String fileName = cda.getId() + ".cda";
		String uri = "plugin-samples/cda/" + fileName;
		
		f.setFile(fileName);
		f.setPath("cda");
		f.setSolution("plugin-samples");
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

		Connection connection = new MetadataConnection("1", domainId , domainId.split("/")[1]);
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
		
		String uri = "plugin-samples/cda/" + cda.getId() +".cda";

		if(repositoryAccess.canWrite(uri)){
			try {
				repositoryAccess.publishFile(uri, new String(cda.asXML()), true);
			} catch (Exception e) {
				e.printStackTrace();
				throw new SaikuReportingException("Error saving cda datasource", e);		
			}
		}else{
			throw new SaikuReportingException("Error saving cda datasource");
		}

	}

	/*
	 * Executes an mql query and returns the result in cda-json format.
	 * Used to feed parameter widgets
	 */
	public String doMqlQuery(String mqlQueryString) {
		
		try {
			generateCda("xxx", mqlQueryString);

		    Map<String, Object> params = new HashMap<String, Object>();
		    params.put("path", "plugin-samples/cda/xxx.cda");
		    params.put("dataAccessId", "xxx");
		    params.put("outputType", null);
		    cdaCall.init(CorePlugin.CDA, "doQuery", params);
    
		    return cdaCall.call(); 
			
		} catch (SaikuReportingException e) {
			e.printStackTrace();
		}

		return null;
	}
   
	
}
