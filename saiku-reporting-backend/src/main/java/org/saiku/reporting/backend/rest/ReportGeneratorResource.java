package org.saiku.reporting.backend.rest;

import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.codehaus.jackson.map.ObjectMapper;
import org.saiku.reporting.backend.exceptions.SaikuClientException;
import org.saiku.reporting.backend.objects.dto.HtmlReport;
import org.saiku.reporting.backend.service.CdaService;
import org.saiku.reporting.backend.service.ReportGeneratorService;
import org.saiku.reporting.backend.util.ReportModelLogger;
import org.saiku.reporting.core.model.ReportSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Path("saiku-reporting/generator")
@Scope("request")
@XmlAccessorType(XmlAccessType.NONE)
public class ReportGeneratorResource {

	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(ReportGeneratorResource.class);

	@Autowired
	private ReportGeneratorService reportGeneratorService;
	
	@Autowired
    private CdaService cda;

	@POST
	@Produces({"application/json" })
	@Consumes({"application/json"})
	@Path("/webreport/{page}")
	public HtmlReport generateReport(ReportSpecification spec,
			@PathParam("page") String page){
		
		log.info("Current Thread in ReporGenerator-Resource: " + Thread.currentThread().getId()); 
		
		try {

			HtmlReport report = new HtmlReport();
			Integer acceptedPage = Integer.parseInt(page) - 1;
			reportGeneratorService.renderReportHtml(spec, report, acceptedPage);
			return report;

		}catch (Exception e) {

			log.error("Cannot generate report",e);
			throw new SaikuClientException(e.getMessage());
		}

	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/resource")
	public Response saveReport(
			@FormParam("path") String path,
			@FormParam("model") String reportSpec
		){

		ReportSpecification spec = null;

		try {
			if (reportSpec != null) {
				ObjectMapper mapper = new ObjectMapper();
				spec = mapper.readValue(URLDecoder.decode(reportSpec, "UTF-8"),ReportSpecification.class);

			}
			reportGeneratorService.saveReport(path, spec);
			
			return Response.ok().build();
			
		}catch (Exception e) {
			log.error("Cannot save report",e);
			throw new SaikuClientException(e.getMessage());
		}

	}
	
	@GET
	@Produces({"application/json" })
	@Path("/resource")
	public Response loadResource (@QueryParam("file") String file)
	{
		
		ReportSpecification doc = reportGeneratorService.loadReport(file);
		
		return Response.ok(doc, MediaType.APPLICATION_JSON).build();

	}
	
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces({"application/json" })
	@Path("/filtervalues")
	public String getFilterValues(@FormParam("mql") String mqlQueryString) {
		return cda.doMqlQuery(mqlQueryString);
	}
	
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces({ "application/vnd.pdf" })
	@Path("/pdf")
	public Response exportPdf(@FormParam("json") String reportSpec) {
		
		ReportSpecification spec = null;

		try {
			if (reportSpec != null) {
				ObjectMapper mapper = new ObjectMapper();
				spec = mapper.readValue(URLDecoder.decode(reportSpec, "UTF-8"),ReportSpecification.class);

			}
			
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			
			reportGeneratorService.renderReportPdf(spec, output);
			String name = "export";

			byte[] doc = output.toByteArray();

			return Response.ok(doc, MediaType.APPLICATION_OCTET_STREAM).header(
					"content-disposition",
					"attachment; filename = " + name + ".pdf").header(
							"content-length",doc.length).build();

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces({ "application/vnd.ms-excel" })
	@Path("/xls")
	public Response exportXls(@FormParam("json") String reportSpec) {
		
		ReportSpecification spec = null;

		try {
			if (reportSpec != null) {
				ObjectMapper mapper = new ObjectMapper();
				spec = mapper.readValue(URLDecoder.decode(reportSpec, "UTF-8"),ReportSpecification.class);

			}
			
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			
			reportGeneratorService.renderReportPdf(spec, output);
			String name = "export";

			byte[] doc = output.toByteArray();

			return Response.ok(doc, MediaType.APPLICATION_OCTET_STREAM).header(
					"content-disposition",
					"attachment; filename = " + name + ".pdf").header(
							"content-length",doc.length).build();

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@POST
	@Produces({ "application/vnd.ms-excel" })
	@Path("/xls")
	public Response exportXls(ReportSpecification spec) {

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			//exportService.writeXls(queryName, output); <- has to be done with cda resource
			String name = "export";

			byte[] doc = output.toByteArray();

			return Response.ok(doc, MediaType.APPLICATION_OCTET_STREAM).header(
					"content-disposition",
					"attachment; filename = " + name + ".xls").header(
							"content-length",doc.length).build();

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@POST
	@Produces({ "text/csv" })
	@Path("/csv")
	public Response exportCsv(ReportSpecification spec) {

		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			//exportService.writeCsv(queryName, output);  <- has to be done with cda resource
			String name = "export";

			byte[] doc = output.toByteArray();

			return Response.ok(doc, MediaType.APPLICATION_OCTET_STREAM).header(
					"content-disposition",
					"attachment; filename = " + name + ".csv").header(
							"content-length",doc.length).build();

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

	}


}
