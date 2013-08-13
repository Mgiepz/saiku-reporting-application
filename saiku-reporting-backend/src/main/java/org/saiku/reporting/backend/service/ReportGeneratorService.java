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
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfPageableModule;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlTableModule;
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
import org.saiku.reporting.backend.util.ReportModelLogger;
import org.saiku.reporting.core.SaikuReportPreProcessorUtil;
import org.saiku.reporting.core.SaikuReportProcessor;
import org.saiku.reporting.core.model.ReportSpecification;
import org.saiku.reporting.core.model.types.DatasourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import pt.webdetails.cpf.repository.IRepositoryAccess;

public class ReportGeneratorService {

	private static final Logger log = LoggerFactory
			.getLogger(ReportGeneratorService.class);

	@Autowired
	private MetadataRepository metadataRepository;

	@Autowired
	private CdaService cdaService;

	@Autowired
	private IRepositoryAccess repositoryAccess;

	private IReportingComponent reportingComponent;

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
			cdaService.generateCdaDatasource(mReport, spec);
		} else if (DatasourceType.METADATA.equals(dsType)) {
			generatePmdDatasource(mReport, spec);
		}

		try {
			SaikuReportPreProcessorUtil.saveReportSpecification(mReport, spec);
		} catch (BundleWriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

		try {
			MasterReport output = prepareReport(spec);

			if (repositoryAccess.canWrite(path)) {
				try {
					final ByteArrayOutputStream prptContent = new ByteArrayOutputStream();

					BundleWriter.writeReportToZipStream(output, prptContent);

					repositoryAccess.publishFile(path,
							prptContent.toByteArray(), true);
				} catch (Exception e) {
					e.printStackTrace();
					throw new SaikuReportingException("Error saving srpt", e);
				}
			} else {
				throw new SaikuReportingException("Error saving srpt");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new SaikuReportingException(e);
		}

	}
	
	public ReportSpecification loadReport(String file) {

		reportingComponent.setReportFileId(file);

	    try {
			MasterReport reportBundle = reportingComponent.getReport();
			ReportSpecification reportSpecification = SaikuReportPreProcessorUtil.loadReportSpecification(reportBundle, reportBundle.getResourceManager());
			return reportSpecification;
	    } catch (Exception e) {
			e.printStackTrace();
		}
		return null;
			 
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

	private MasterReport getPrptTemplate(ReportSpecification spec)
			throws SaikuReportingException {

		// this should be stored in the ReportSpecification.
		String fileId = "templates/cobalt_4_left_aligned_grid.prpt";

		reportingComponent.setReportFileId(fileId);

		try {
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
		reportingComponent
				.setDefaultOutputTarget(PdfPageableModule.PDF_EXPORT_TYPE);
		reportingComponent.setOutputTarget(PdfPageableModule.PDF_EXPORT_TYPE);
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



}
