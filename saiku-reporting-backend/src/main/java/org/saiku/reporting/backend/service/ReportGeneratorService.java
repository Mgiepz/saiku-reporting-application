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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.marshal.MarshallableResultSet;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.query.model.*;
import org.pentaho.metadata.query.model.util.QueryXmlHelper;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfPageableModule;
import org.pentaho.reporting.engine.classic.core.modules.output.table.csv.CSVTableModule;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlTableModule;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xls.ExcelTableModule;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriter;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriterException;
import org.pentaho.reporting.engine.classic.core.util.ReportParameterValues;
import org.pentaho.reporting.engine.classic.extensions.datasources.pmd.PmdDataFactory;
import org.pentaho.reporting.libraries.resourceloader.ResourceCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.saiku.reporting.backend.component.IReportingComponent;
import org.saiku.reporting.backend.exceptions.SaikuReportingException;
import org.saiku.reporting.backend.objects.dto.HtmlReport;
import org.saiku.reporting.backend.server.MetadataRepository;
import org.saiku.reporting.backend.server.SaikuPmdConnectionProvider;
import org.saiku.reporting.backend.util.FileExtensionFilter;
import org.saiku.reporting.backend.util.ReportModelLogger;
import org.saiku.reporting.core.SaikuReportPreProcessorUtil;
import org.saiku.reporting.core.SaikuReportProcessor;
import org.saiku.reporting.core.model.ReportSpecification;
import org.saiku.reporting.core.model.TemplateDefinition;
import org.saiku.reporting.core.model.types.DatasourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

public class ReportGeneratorService {

	private static final Logger log = LoggerFactory
			.getLogger(ReportGeneratorService.class);

	@Autowired
	private MetadataRepository metadataRepository;

	//@Autowired
	//private ICdaService cdaService;

	private IReportingComponent reportingComponent;

	@Autowired
	private IContentAccessFactory contentAccessFactory;

	public void setReportingComponent(IReportingComponent reportingComponent) {
		this.reportingComponent = reportingComponent;
	}

	private MasterReport prepareReport(ReportSpecification spec)
			throws SaikuReportingException, ResourceLoadingException,
			ResourceCreationException, ResourceKeyCreationException,
			MalformedURLException, ResourceException {

		ClassicEngineBoot.getInstance().start();

		SaikuReportProcessor saikuProcessor = new SaikuReportProcessor();

		// Get the report template
		MasterReport mReport = getPrptTemplate(spec);

		// generate the datasource and attach it to the report
		DatasourceType dsType = spec.getDataSource().getType();

		if (DatasourceType.CDA.equals(dsType)) {
		//	cdaService.generateCdaDatasource(mReport, spec);
		} else if (DatasourceType.METADATA.equals(dsType)) {
			generatePmdDatasource(mReport, spec);
		}

		try {
			return saikuProcessor.preProcessReport(mReport, spec);
		} catch (BundleWriterException e) {
			throw new SaikuReportingException(e);
		}
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
	public void renderReportHtml(ReportSpecification spec, HtmlReport report,
			Integer acceptedPage) throws ResourceException,
			MalformedURLException, SaikuReportingException {

		// preprocess the report and augment the spec with all infos from the
		// template
		MasterReport output = prepareReport(spec);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Map<String, Object> reportParameters = null; // ParamUtils.getReportParameters("",
		// spec);

		try {
			// let the engine process the report
			generateHtmlReport(output, stream, reportParameters, report,
					acceptedPage);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SaikuReportingException("failed to generate report");
		}

		// put the report and the augmented model into the dto
		String string = stream.toString();

		if(string==null) ReportModelLogger.log(spec, log);

		report.setReportModel(spec);
		report.setData(string);

	}

	public void saveReport(String path, ReportSpecification spec)
			throws SaikuReportingException {

		try{

			MasterReport output = prepareReport(spec);

			IUserContentAccess userAccess = contentAccessFactory.getUserContentAccess("/");

			final ByteArrayOutputStream prptContent = new ByteArrayOutputStream();

			BundleWriter.writeReportToZipStream(output, prptContent);

			if(userAccess.saveFile(path, new ByteArrayInputStream(prptContent.toByteArray()))){
				log.debug("file '" + path + "' saved ok");
			}else {
				log.error("writeFile: failed saving " + path);
				throw new SaikuReportingException("Error saving cda datasource");
			}

		}catch(Exception e){
			e.printStackTrace();
			throw new SaikuReportingException("Error saving srpt");
		}

	}

	public void renderReportPdf(ReportSpecification spec,
			ByteArrayOutputStream stream) throws ResourceLoadingException,
			ResourceCreationException, ResourceKeyCreationException,
			MalformedURLException, SaikuReportingException, ResourceException {

		// preprocess the report and augment the spec with all infos from the
		// template
		MasterReport output = prepareReport(spec);

		Map<String, Object> reportParameters = null; // ParamUtils.getReportParameters("",
		// spec);

		try {
			// let the engine process the report
			generatePdfReport(output, stream, reportParameters);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SaikuReportingException("failed to generate pdf");
		}

	}

    public void renderReportXls(ReportSpecification spec,
                                ByteArrayOutputStream stream) throws ResourceLoadingException,
            ResourceCreationException, ResourceKeyCreationException,
            MalformedURLException, SaikuReportingException, ResourceException {

        // preprocess the report and augment the spec with all infos from the
        // template
        MasterReport output = prepareReport(spec);

        Map<String, Object> reportParameters = null; // ParamUtils.getReportParameters("",
        // spec);

        try {
            // let the engine process the report
            generateXlsReport(output, stream, reportParameters);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SaikuReportingException("failed to generate xls");
        }

    }

	private MasterReport getPrptTemplate(ReportSpecification spec)
			throws SaikuReportingException {

		String path = "resources/templates/" + SaikuProperties.defaultPrptTemplate;

		IReadAccess access = contentAccessFactory.getPluginSystemReader(null);

		try {
			InputStream is = access.getFileInputStream(path);
			reportingComponent.setReportDefinitionInputStream(is);

			return reportingComponent.getReport();
		} catch (ResourceException e) {
			// if it cannot be loaded it should try to load a default
			e.printStackTrace();
			throw new SaikuReportingException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new SaikuReportingException(e);
		}

	}
	

	public ReportSpecification loadReport(String path) {

		IUserContentAccess userAccess = contentAccessFactory.getUserContentAccess(null);
		
		try {
			InputStream is = userAccess.getFileInputStream(path);
			reportingComponent.setReportDefinitionInputStream(is);

			MasterReport reportBundle = reportingComponent.getReport();
			
			ReportSpecification reportSpecification = SaikuReportPreProcessorUtil.loadReportSpecification(reportBundle, reportBundle.getResourceManager());
			return reportSpecification;
			
		} catch (Exception e) {
			// if it cannot be loaded it should try to load a default
			e.printStackTrace();
			log.error("loadReport: failed loading " + path);
		}
		return null;

	}


	public List<TemplateDefinition> getTemplatesFromRepository(String path) {

		path = "resources/templates";

		List<TemplateDefinition> templateList = new ArrayList<TemplateDefinition>();

		IReadAccess access = contentAccessFactory.getPluginSystemReader(null);

		List<IBasicFile> fileList = access.listFiles(path, new FileExtensionFilter(new String[] {".prpt"})); 

		for (IBasicFile file : fileList) {
			TemplateDefinition thisTemplate = new TemplateDefinition(file.getFullPath(),file.getName());
			templateList.add(thisTemplate);
		}

		return templateList;

	}

	public void getImg(String name, OutputStream out){

		String path = "resources/templates";

		String fileName = name.endsWith(".png") ? name : name + ".png";
		
		IReadAccess access = contentAccessFactory.getPluginSystemReader(null);

		try {
			InputStream image = access.getFileInputStream(path + "/" + fileName);
			IOUtils.copy(image, out);

		} catch (IOException e) {
			log.info("image " + name + ".png not found");
			e.printStackTrace();
		}

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
			Map<String, Object> reportParameters, HtmlReport report,
			Integer acceptedPage) throws Exception {

		reportingComponent.setReport(output);
		reportingComponent.setPaginateOutput(true);
		reportingComponent.setInputs(reportParameters);
		reportingComponent
		.setDefaultOutputTarget(HtmlTableModule.TABLE_HTML_PAGE_EXPORT_TYPE);
		reportingComponent
		.setOutputTarget(HtmlTableModule.TABLE_HTML_PAGE_EXPORT_TYPE);
		reportingComponent.setDashboardMode(true);
		reportingComponent.setOutputStream(stream);
		reportingComponent.setAcceptedPage(acceptedPage);
		reportingComponent.validate();
		reportingComponent.execute();

		report.setCurrentPage(reportingComponent.getAcceptedPage());
		report.setPageCount(reportingComponent.getPageCount());

		// GenerateTest.storeReport(pentahoReportingPlugin.getReport());

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

		reportingComponent.setReport(output);
		reportingComponent.setPaginateOutput(true);
		reportingComponent.setInputs(reportParameters);
		reportingComponent.setDefaultOutputTarget(PdfPageableModule.PDF_EXPORT_TYPE);
		reportingComponent.setOutputTarget(PdfPageableModule.PDF_EXPORT_TYPE);
		reportingComponent.setOutputStream(stream);
		reportingComponent.validate();
		reportingComponent.execute();

	}

    /**
     * Generate the report as xls
     *
     * @param output
     * @param stream
     * @param report
     * @param acceptedPage
     * @param query2
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private void generateXlsReport(MasterReport output, OutputStream stream,
                                   Map<String, Object> reportParameters) throws Exception {

        reportingComponent.setReport(output);
        reportingComponent.setPaginateOutput(true);
        reportingComponent.setInputs(reportParameters);
        reportingComponent.setDefaultOutputTarget(ExcelTableModule.EXCEL_FLOW_EXPORT_TYPE);
        reportingComponent.setOutputTarget(ExcelTableModule.EXCEL_FLOW_EXPORT_TYPE);
        reportingComponent.setOutputStream(stream);
        reportingComponent.validate();
        reportingComponent.execute();

    }

    /**
     * Generate the report as csv
     *
     * @param output
     * @param stream
     * @param report
     * @param acceptedPage
     * @param query2
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private void generateCsvReport(MasterReport output, OutputStream stream,
                                   Map<String, Object> reportParameters) throws Exception {

        reportingComponent.setReport(output);
        reportingComponent.setPaginateOutput(true);
        reportingComponent.setInputs(reportParameters);
        reportingComponent.setDefaultOutputTarget(CSVTableModule.TABLE_CSV_STREAM_EXPORT_TYPE);
        reportingComponent.setOutputTarget(CSVTableModule.TABLE_CSV_STREAM_EXPORT_TYPE);
        reportingComponent.setOutputStream(stream);
        reportingComponent.validate();
        reportingComponent.execute();

    }
	// ----------------------------------------------------------------------------
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
	private void generatePmdDatasource(MasterReport mReport,
			ReportSpecification spec) {

		PmdDataFactory pmd = new PmdDataFactory();
		metadataRepository.getMetadataDomainRepository();
		IMetadataDomainRepository repo = metadataRepository
				.getMetadataDomainRepository();
		SaikuPmdConnectionProvider pmdConnectionProvider = new SaikuPmdConnectionProvider(
				repo);

		String domainId = "";

		String mql = spec.getDataSource().getQueryString();
        mql = mql.replace("&gt;", ">");
        mql = mql.replace("&lt;", "<");
        mql = mql.replace("&gt;=", ">=");
        mql = mql.replace("&lt;=", "<=");
		Pattern p = Pattern.compile("<domain_id>(.*?)</domain_id>");
		Matcher m = p.matcher(mql);
		if (m.find()) {
			domainId = m.group(1);
		}

		pmd.setDomainId(domainId);
		pmd.setConnectionProvider(pmdConnectionProvider);
		pmd.setXmiFile("pentaho2://" + domainId); // ??? TODO: Standalone
		pmd.setQuery("MASTER_QUERY", mql);
		mReport.setDataFactory(pmd);
		mReport.setQuery("MASTER_QUERY");

	}

	protected ReportParameterValues getReportParameterValues(
			ReportSpecification model) {

		ReportParameterValues vals = new ReportParameterValues();

		// Map<String, Object> reportParameters =
		// ParamUtils.getReportParameters("", model);
		//
		// if (null != model) {
		// Collection<String> keyset = reportParameters.keySet();
		// for (Iterator<String> iterator = keyset.iterator(); iterator
		// .hasNext();) {
		// String key = (String) iterator.next();
		// vals.put(key, reportParameters.get(key));
		// }
		// }
		return vals;
	}

	public void setMetadataRepository(MetadataRepository metadataRepository) {
		this.metadataRepository = metadataRepository;
	}

    public void renderReportCsv(ReportSpecification spec, ByteArrayOutputStream stream) throws SaikuReportingException, ResourceException, MalformedURLException {
        MasterReport output = prepareReport(spec);

        Map<String, Object> reportParameters = null; // ParamUtils.getReportParameters("",
        // spec);

        try {
            // let the engine process the report
            generateCsvReport(output, stream, reportParameters);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SaikuReportingException("failed to generate xls");
        }
    }

/*    public MarshallableResultSet doQuery( Query query, Integer rowLimit ) {
        org.pentaho.metadata.query.model.Query fullQuery = convertQuery( query );
        QueryXmlHelper helper = new QueryXmlHelper();
        String xml = helper.toXML( fullQuery );
        return doXmlQuery( xml, rowLimit );
    }
public MarshallableResultSet doXmlQuery( String xml, Integer rowLimit ) {
        IPentahoResultSet resultSet = executeQuery( xml, rowLimit );
        if ( resultSet == null ) {
            return null;
        }
        MarshallableResultSet result = new MarshallableResultSet();
        result.setResultSet( resultSet );
        return result;
    }

    protected IPentahoResultSet executeQuery( String query, Integer rowLimit ) {
        // create a component to execute the query
        MetadataQueryComponent dataComponent = new MetadataQueryComponent();
        dataComponent.setQuery( query );
        dataComponent.setLive( false );
        dataComponent.setUseForwardOnlyResultSet( true );
        if ( rowLimit != null && rowLimit > -1 ) {
            // set the row limit
            dataComponent.setMaxRows( rowLimit );
        }
        if ( dataComponent.execute() ) {
            return dataComponent.getResultSet();
        }
        return null;
    }

    public org.pentaho.metadata.query.model.Query convertQuery( Query src ) {

        IMetadataDomainRepository domainRepository =
                PentahoSystem.get( IMetadataDomainRepository.class, PentahoSessionHolder.getSession() );

        Domain fullDomain = domainRepository.getDomain( src.getDomainName() );
        LogicalModel logicalModel = fullDomain.findLogicalModel( src.getModelId() );

        // create a new full query object
        org.pentaho.metadata.query.model.Query dest =
                new org.pentaho.metadata.query.model.Query( fullDomain, logicalModel );

        // now add the selections
        List<Selection> selections = dest.getSelections();
        for ( Column column : src.getColumns() ) {
            // get the objects needed for the selection
            LogicalColumn logicalColumn = logicalModel.findLogicalColumn( column.getId() );
            org.pentaho.metadata.model.Category category = getCategory( column.getId(), logicalModel );
            AggregationType aggregationType = AggregationType.valueOf( column.getSelectedAggType() );
            // create a selection and add it to the list
            Selection selection = new Selection( category, logicalColumn, aggregationType );
            selections.add( selection );
        }

        // now add the filters
        List<Constraint> constraints = dest.getConstraints();
        for ( Condition condition : src.getConditions() ) {
            org.pentaho.metadata.query.model.CombinationType combinationType =
                    CombinationType.valueOf(condition.getCombinationType());
            LogicalColumn logicalColumn = logicalModel.findLogicalColumn( condition.getColumn() );
            String paramName = condition.isParameterized() ? condition.getValue()[0] : null;
            String formula = condition.getCondition( logicalColumn.getDataType().name(), paramName );
            Constraint constraint = new Constraint( combinationType, formula );
            constraints.add( constraint );
        }

        // now set the disable distinct option
        if ( src.getDisableDistinct() != null ) {
            dest.setDisableDistinct( src.getDisableDistinct() );
        }

        // now add the sorting information
        List<org.pentaho.metadata.query.model.Order> orders = dest.getOrders();
        for ( Order order : src.getOrders() ) {
            // find the selection
            for ( Selection selection : selections ) {
                if ( selection.getLogicalColumn().getId().equals( order.getColumn() ) ) {
                    Order.Type type = Order.Type.valueOf(order.getOrderType());
                    org.pentaho.metadata.query.model.Order fullOrder =
                            new org.pentaho.metadata.query.model.Order( selection, type );
                    orders.add( fullOrder );
                }
            }
        }

        // now add the parameter information
        List<org.pentaho.metadata.query.model.Parameter> parameters = dest.getParameters();
        for ( Parameter parameter : src.getParameters() ) {
            // find the column for this parameter
            LogicalColumn logicalColumn = logicalModel.findLogicalColumn( parameter.getColumn() );
            DataType type = logicalColumn.getDataType();
            String[] value = parameter.getValue();
            final String name = parameter.getName() != null ? parameter.getName() : parameter.getColumn();
            org.pentaho.metadata.query.model.Parameter fullParam =
                    new org.pentaho.metadata.query.model.Parameter( name, type, value[0] );
            parameters.add( fullParam );
        }
        return dest;
    }
*/
}
