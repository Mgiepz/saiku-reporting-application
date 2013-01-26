/*
 * Copyright (C) 2011 Marius Giepz
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 *
 */
package org.saiku.reporting.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfPageableModule;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlTableModule;
import org.pentaho.reporting.engine.classic.core.util.ReportParameterValues;
import org.pentaho.reporting.engine.classic.extensions.datasources.cda.CdaDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.cda.CdaQueryEntry;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.saiku.reporting.backend.exceptions.SaikuReportingException;
import org.saiku.reporting.backend.objects.dto.HtmlReport;
import org.saiku.reporting.backend.server.MetadataRepository;
import org.saiku.reporting.backend.server.SaikuPmdConnectionProvider;
import org.saiku.reporting.component.IReportingComponent;
import org.saiku.reporting.component.StandaloneReportingComponent;
import org.saiku.reporting.core.SaikuReportProcessor;
import org.saiku.reporting.core.model.FieldDefinition;
import org.saiku.reporting.core.model.ReportSpecification;
import org.saiku.reporting.core.model.types.DatasourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.metadata.MetadataConnection;
import pt.webdetails.cda.dataaccess.ColumnDefinition;
import pt.webdetails.cda.dataaccess.MqlDataAccess;
import pt.webdetails.cda.dataaccess.Parameter;
import pt.webdetails.cda.settings.CdaSettings;

public class ReportGeneratorService {
	
	private static final Logger log = LoggerFactory.getLogger(ReportGeneratorService.class);
	
	@Autowired
    private MetadataRepository metadataRepository;
	
	private URL repoURL;

	private IReportingComponent reportingComponent;
	
	public void setReportingComponent(IReportingComponent reportingComponent) {
		this.reportingComponent = reportingComponent;
	}

	private MasterReport prepareReport(ReportSpecification spec) throws SaikuReportingException, ResourceLoadingException, ResourceCreationException, ResourceKeyCreationException, MalformedURLException, ResourceException {
		
		 ClassicEngineBoot.getInstance().start();

	        SaikuReportProcessor saikuProcessor = new SaikuReportProcessor();

	        //Get the report template 
	        MasterReport mReport = getPrptTemplate(spec);

	        //generate the datasource and attach it to the report
	        DatasourceType dsType = spec.getDataSource().getType();
			
	        if(DatasourceType.CDA.equals(dsType)){
	        	generateCdaDatasource(mReport, spec);
	        }else if(DatasourceType.METADATA.equals(dsType)){
	        	generatePmdDatasource(mReport, spec);
	        }
	        
		return saikuProcessor.preProcessReport(mReport, spec);
	}

    /**
     * This method is the service entrypoint for rendering a report to html
     *
     * @param sessionId
     * @param spec
     * @param report
     * @param acceptedPage
     * @throws ResourceException
     * @throws MalformedURLException
     * @throws SaikuReportingException
     */
    public void renderReportHtml(ReportSpecification spec,
            HtmlReport report, Integer acceptedPage) throws ResourceException, MalformedURLException, SaikuReportingException {

        //preprocess the report and augment the spec with all infos from the template
        MasterReport output = prepareReport(spec); 

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Map<String, Object> reportParameters = null; //ParamUtils.getReportParameters("", spec);

        try {
            //let the engine process the report
            generateHtmlReport(output, stream, reportParameters, report, acceptedPage);
        } catch (Exception e) {
        	e.printStackTrace();
            throw new SaikuReportingException("failed to generate report");
        }

        //put the report and the augmented model into the dto
        String string = stream.toString();
        report.setReportModel(spec);
        report.setData(string);


    }

	public void renderReportPdf(ReportSpecification spec,
			ByteArrayOutputStream stream) throws ResourceLoadingException, ResourceCreationException, ResourceKeyCreationException, MalformedURLException, SaikuReportingException, ResourceException {
		
        //preprocess the report and augment the spec with all infos from the template
        MasterReport output = prepareReport(spec); 
        
        Map<String, Object> reportParameters = null; //ParamUtils.getReportParameters("", spec);

        try {
            //let the engine process the report
            generatePdfReport(output, stream, reportParameters);
        } catch (Exception e) {
        	e.printStackTrace();
            throw new SaikuReportingException("failed to generate pdf");
        }
		
	}

    private MasterReport getPrptTemplate(ReportSpecification spec) throws ResourceLoadingException,
            ResourceCreationException, ResourceKeyCreationException,
            MalformedURLException, ResourceException, SaikuReportingException {

        //prptProvider.getPrptTemplate(reportTemplate);
    	MasterReport mReport = null;

		try {
	        ResourceManager manager = new ResourceManager();
	        manager.registerDefaults();
	        FileSystemManager fsManager = VFS.getManager();
	        FileObject template;
			
			template = fsManager.resolveFile("res:cobalt_4_left_aligned_grid.prpt");

	        Resource res = manager.createDirectly(template.getURL(), MasterReport.class);  //mockup
	        mReport = (MasterReport) res.getResource();
	        
		} catch (FileSystemException e) {
			throw new SaikuReportingException(e);
		}

		return mReport;

    }

    /**
     * Generate report as html
     *
     * @param output
     * @param stream
     * @param report
     * @param acceptedPage
     * @param query2
     * @throws Exception
     */
    private void generateHtmlReport(MasterReport output, OutputStream stream,
            Map<String, Object> reportParameters, HtmlReport report, Integer acceptedPage) throws Exception {

        reportingComponent.setReport(output);
        reportingComponent.setPaginateOutput(true);
        reportingComponent.setInputs(reportParameters);
        reportingComponent.setDefaultOutputTarget(HtmlTableModule.TABLE_HTML_PAGE_EXPORT_TYPE);
        reportingComponent.setOutputTarget(HtmlTableModule.TABLE_HTML_PAGE_EXPORT_TYPE);
        reportingComponent.setDashboardMode(true);
        reportingComponent.setOutputStream(stream);
        reportingComponent.setAcceptedPage(acceptedPage);
        reportingComponent.validate();
        reportingComponent.execute();

        report.setCurrentPage(reportingComponent.getAcceptedPage());
        report.setPageCount(reportingComponent.getPageCount());

        //GenerateTest.storeReport(pentahoReportingPlugin.getReport());

    }
    
    

    /**
     * Generate the report as pdf
     *
     * @param output
     * @param stream
     * @param report
     * @param acceptedPage
     * @param query2
     * @throws Exception
     */
    @SuppressWarnings("unused")
	private void generatePdfReport(MasterReport output, OutputStream stream,
            Map<String, Object> reportParameters) throws Exception {

        final StandaloneReportingComponent reportingComponent = new StandaloneReportingComponent();
        reportingComponent.setReport(output);
        reportingComponent.setPaginateOutput(true);
        reportingComponent.setInputs(reportParameters);
        reportingComponent.setDefaultOutputTarget(PdfPageableModule.PDF_EXPORT_TYPE);
        reportingComponent.setOutputTarget(PdfPageableModule.PDF_EXPORT_TYPE);
        reportingComponent.setOutputStream(stream);
        reportingComponent.validate();
        reportingComponent.execute();

    }

 //----------------------------------------------------------------------------
 // move into factory   
    /**
     * Generate the metadata datource for the prpt
     *
     * @param sessionId
     *
     * @param mReport
     * @param spec
     */
    @SuppressWarnings("deprecation")
    private void generatePmdDatasource(MasterReport mReport, ReportSpecification spec) {

        PmdDataFactory pmd = new PmdDataFactory();
        metadataRepository.getMetadataDomainRepository();
        IMetadataDomainRepository repo = metadataRepository.getMetadataDomainRepository();
        SaikuPmdConnectionProvider pmdConnectionProvider = new SaikuPmdConnectionProvider(repo);

        String domainId = "";

        String mql = spec.getDataSource().getProperties().get("queryString");
        Pattern p = Pattern.compile("<domain_id>(.*?)</domain_id>");
        Matcher m = p.matcher(mql);
        if (m.find()) {
            domainId = m.group(1);
        }

        pmd.setDomainId(domainId);
        pmd.setConnectionProvider(pmdConnectionProvider);
        pmd.setXmiFile("pentaho2://" + domainId); //??? TODO: Standalone
        pmd.setQuery("MASTER_QUERY", mql);
        mReport.setDataFactory(pmd);
        mReport.setQuery("MASTER_QUERY");

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
    private void generateCdaDatasource(MasterReport mReport, ReportSpecification spec) throws SaikuReportingException {
    	
    	//First generate the data access and store it to vfs
        String domainId = "";

        String mql = spec.getDataSource().getProperties().get("queryString");
        Pattern p = Pattern.compile("<domain_id>(.*?)</domain_id>");
        Matcher m = p.matcher(mql);
        if (m.find()) {
            domainId = m.group(1);
        }
        
        mql = mql.replace("<![CDATA[", "").replace("]]>", "");

		// and then the calculated columns
        final Collection<ColumnDefinition> calculatedColumns = new ArrayList<ColumnDefinition>();
        
		for (FieldDefinition fieldDefinition : spec.getFieldDefinitions()) {
			if(fieldDefinition.getFormula()!=null){
				ColumnDefinition columnDef = new ColumnDefinition();
				columnDef.setName(fieldDefinition.getId());
				columnDef.setType(ColumnDefinition.TYPE.CALCULATED_COLUMN);
				columnDef.setFormula("=" + fieldDefinition.getFormula());
				calculatedColumns.add(columnDef);
			}
		}
        
		String id = spec.getDataSource().getId();
		CdaSettings cda = new CdaSettings(id, null);

		Connection connection = new MetadataConnection("1", domainId , domainId.split("/")[1]);
		MqlDataAccess dataAccess = new MqlDataAccess(id, id, "1",mql);
		dataAccess.setParameters(new ArrayList<Parameter>());
		dataAccess.getColumnDefinitions().clear();
		dataAccess.getColumnDefinitions().addAll(calculatedColumns);

		cda.addConnection(connection);
		cda.addDataAccess(dataAccess);

		storeCda(cda);
    	
    	//then generate the cda-extension data factory

        String queryId =  cda.getId(); //"MASTER_QUERY";
		
		CdaDataFactory f = new CdaDataFactory();        
		String baseUrlField = null;
		f.setBaseUrlField(baseUrlField);

		String baseUrl = "res:saiku-repository/" + cda.getId();
		f.setUsername(SaikuProperties.cdaUser);
		f.setPassword(SaikuProperties.cdaPassword);
		f.setBaseUrl(baseUrl);     
		f.setUseLocalCall(true);
		f.setQueryEntry(queryId, new CdaQueryEntry(queryId, queryId));


        mReport.setDataFactory(f);

		mReport.setQuery(queryId);

    }    
   
    protected ReportParameterValues getReportParameterValues(
            ReportSpecification model) {

        ReportParameterValues vals = new ReportParameterValues();

//        Map<String, Object> reportParameters = ParamUtils.getReportParameters("", model);
//
//        if (null != model) {
//            Collection<String> keyset = reportParameters.keySet();
//            for (Iterator<String> iterator = keyset.iterator(); iterator
//                    .hasNext();) {
//                String key = (String) iterator.next();
//                vals.put(key, reportParameters.get(key));
//            }
//        }
        return vals;
    }

	public void setMetadataRepository(MetadataRepository metadataRepository) {
		this.metadataRepository = metadataRepository;
	}

	public void storeCda(CdaSettings cda) throws SaikuReportingException {
		
		//TODO: wire that with spring
	 	setPath("res:saiku-repository");
		
		try { 
			String uri = repoURL.toURI().toString() + "/";
			if (uri != null && cda != null) {
				uri += cda.getId();
				File dsFile = new File(new URI(uri));
				if (dsFile.exists()) {
					dsFile.delete();
				}
				else {
					dsFile.createNewFile();
				}
				FileWriter fw = new FileWriter(dsFile);
	            fw.write(new String(cda.asXML()));
				fw.close();

			}
			else {
				throw new SaikuReportingException("Cannot save datasource because uri or datasource is null uri(" 
						+ (uri == null) + ")" );
			}
		}
		catch (Exception e) {
			throw new SaikuReportingException("Error saving datasource",e);
		}
	}
	
	public void setPath(String path) {

		FileSystemManager fileSystemManager;
		try {
			fileSystemManager = VFS.getManager();

			FileObject fileObject;
			fileObject = fileSystemManager.resolveFile(path);
			if (fileObject == null) {
				throw new IOException("File cannot be resolved: " + path);
			}
			if(!fileObject.exists()) {
				throw new IOException("File does not exist: " + path);
			}
			repoURL = fileObject.getURL();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
