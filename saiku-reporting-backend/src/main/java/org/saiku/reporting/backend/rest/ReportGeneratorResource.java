package org.saiku.reporting.backend.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.saiku.reporting.backend.exceptions.SaikuClientException;
import org.saiku.reporting.backend.objects.dto.HtmlReport;
import org.saiku.reporting.backend.service.ReportGeneratorService;
import org.saiku.reporting.core.model.ReportSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Path("/saiku-adhoc/rest/generator")
@Scope("request")
@XmlAccessorType(XmlAccessType.NONE)
public class ReportGeneratorResource {
	
	private static final long serialVersionUID = 1L;

	private Log log = LogFactory.getLog(ReportGeneratorResource.class);

	@Autowired
	private ReportGeneratorService reportGeneratorService;

	
	@POST
	@Produces({"application/json" })
	@Consumes({"application/json"})
	@Path("/report/{page}")
	public HtmlReport generateReport(ReportSpecification spec,
			@PathParam("page") String page){

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

}
