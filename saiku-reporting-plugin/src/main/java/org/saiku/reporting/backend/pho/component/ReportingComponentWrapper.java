package org.saiku.reporting.backend.pho.component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterContext;
import org.pentaho.reporting.engine.classic.core.parameters.ValidationResult;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.platform.plugin.SimpleReportingComponent;
import org.saiku.reporting.backend.component.IReportingComponent;

public class ReportingComponentWrapper implements IReportingComponent{

	private SimpleReportingComponent reportingComponent;
	
	public ReportingComponentWrapper() {
		reportingComponent = new SimpleReportingComponent();
	}

	@SuppressWarnings("deprecation")
	public void applyInputsToReportParameters(MasterReport arg0,
			ParameterContext arg1) {
		reportingComponent.applyInputsToReportParameters(arg0, arg1);
	}

	@SuppressWarnings("deprecation")
	public ValidationResult applyInputsToReportParameters(
			ParameterContext context, ValidationResult validationResult)
			throws IOException, ResourceException {
		return reportingComponent.applyInputsToReportParameters(context,
				validationResult);
	}

	public boolean equals(Object obj) {
		return reportingComponent.equals(obj);
	}

	public boolean execute() throws Exception {
		return reportingComponent.execute();
	}

	public int getAcceptedPage() {
		return reportingComponent.getAcceptedPage();
	}

	public String getComputedOutputTarget() throws IOException,
			ResourceException {
		return reportingComponent.getComputedOutputTarget();
	}

	public String getDefaultOutputTarget() {
		return reportingComponent.getDefaultOutputTarget();
	}

	public Map<String, Object> getInputs() {
		return reportingComponent.getInputs();
	}

	public String getMimeType() {
		return reportingComponent.getMimeType();
	}

	public String getOutputTarget() {
		return reportingComponent.getOutputTarget();
	}

	public String getOutputType() {
		return reportingComponent.getOutputType();
	}

	public int getPageCount() {
		return reportingComponent.getPageCount();
	}

	public String getPrinter() {
		return reportingComponent.getPrinter();
	}

	public MasterReport getReport() throws ResourceException, IOException {
		return reportingComponent.getReport();
	}

	public IActionSequenceResource getReportDefinition() {
		return reportingComponent.getReportDefinition();
	}

	public String getReportDefinitionPath() {
		return reportingComponent.getReportDefinitionPath();
	}

	public boolean getUseContentRepository() {
		return reportingComponent.getUseContentRepository();
	}

	public int hashCode() {
		return reportingComponent.hashCode();
	}

	public boolean isDashboardMode() {
		return reportingComponent.isDashboardMode();
	}

	public boolean isForceDefaultOutputTarget() {
		return reportingComponent.isForceDefaultOutputTarget();
	}

	public boolean isPaginateOutput() {
		return reportingComponent.isPaginateOutput();
	}

	public boolean isPrint() {
		return reportingComponent.isPrint();
	}

	public boolean outputSupportsPagination() {
		return reportingComponent.outputSupportsPagination();
	}

	public int paginate() throws IOException, ResourceException {
		return reportingComponent.paginate();
	}

	public void setAcceptedPage(int acceptedPage) {
		reportingComponent.setAcceptedPage(acceptedPage);
	}

	public void setDashboardMode(boolean dashboardMode) {
		reportingComponent.setDashboardMode(dashboardMode);
	}

	public void setDefaultOutputTarget(String defaultOutputTarget) {
		reportingComponent.setDefaultOutputTarget(defaultOutputTarget);
	}

	public void setForceDefaultOutputTarget(boolean forceDefaultOutputTarget) {
		reportingComponent
				.setForceDefaultOutputTarget(forceDefaultOutputTarget);
	}

	public void setInputs(Map<String, Object> inputs) {
		reportingComponent.setInputs(inputs);
	}

	public void setOutputStream(OutputStream outputStream) {
		reportingComponent.setOutputStream(outputStream);
	}

	public void setOutputTarget(String outputTarget) {
		reportingComponent.setOutputTarget(outputTarget);
	}

	public void setOutputType(String outputType) {
		reportingComponent.setOutputType(outputType);
	}

	public void setPaginateOutput(boolean paginateOutput) {
		reportingComponent.setPaginateOutput(paginateOutput);
	}

	public void setPrint(boolean print) {
		reportingComponent.setPrint(print);
	}

	public void setPrinter(String printer) {
		reportingComponent.setPrinter(printer);
	}

	public void setReport(MasterReport report) {
		reportingComponent.setReport(report);
	}

	public void setReportDefinitionInputStream(
			InputStream reportDefinitionInputStream) {
		reportingComponent
				.setReportDefinitionInputStream(reportDefinitionInputStream);
	}

	public void setReportDefinitionPath(String reportDefinitionPath) {
		reportingComponent.setReportDefinitionPath(reportDefinitionPath);
	}


	public String toString() {
		return reportingComponent.toString();
	}

	public boolean validate() throws Exception {
		return reportingComponent.validate();
	}

	public Serializable getReportFileId() {
		return getReportDefinitionPath();
	}

	public void setReportFileId(Serializable fileId) {
		setReportDefinitionPath((String) fileId);
	}
		

}
