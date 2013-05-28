package org.saiku.reporting.backend.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.extensions.datasources.cda.CdaDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.cda.CdaQueryEntry;
import org.saiku.reporting.backend.exceptions.SaikuReportingException;
import org.saiku.reporting.core.model.ReportSpecification;
import org.springframework.beans.factory.annotation.Autowired;

import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.metadata.MetadataConnection;
import pt.webdetails.cda.dataaccess.MqlDataAccess;
import pt.webdetails.cda.dataaccess.Parameter;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cpf.repository.IRepositoryAccess;

public class CdaService {
	
	private URL repoURL;
	
	@Autowired
	private IRepositoryAccess repositoryAccess;


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
    	
    	//First generate the data access and store it to vfs
        String domainId = "";

        String mql = spec.getDataSource().getProperties().get("queryString");
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
		String id = spec.getDataSource().getId();
		CdaSettings cda = new CdaSettings(id, null);

		Connection connection = new MetadataConnection("1", domainId , domainId.split("/")[1]);
		MqlDataAccess dataAccess = new MqlDataAccess(id, id, "1",mql);
		dataAccess.setParameters(new ArrayList<Parameter>());
		dataAccess.getColumnDefinitions().clear();
		//dataAccess.getColumnDefinitions().addAll(calculatedColumns);

		cda.addConnection(connection);
		cda.addDataAccess(dataAccess);

		storeCda(cda);

        String queryId =  cda.getId();
		
		CdaDataFactory f = new CdaDataFactory();        
		String baseUrlField = null;
		f.setBaseUrlField(baseUrlField);

		String baseUrl = "saiku:/" + cda.getId();
		f.setUsername(SaikuProperties.cdaUser);
		f.setPassword(SaikuProperties.cdaPassword);
		f.setBaseUrl(baseUrl);     
		f.setUseLocalCall(true);
		f.setQueryEntry(queryId, new CdaQueryEntry(queryId, queryId));


        mReport.setDataFactory(f);

		mReport.setQuery(queryId);

    }    
    
	public void storeCda(CdaSettings cda) throws SaikuReportingException {
		
		String uri = "temp/" + cda.getId();
		
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
		// TODO Auto-generated method stub
		return null;
	}
   
	
}
