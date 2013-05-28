package org.saiku.reporting.backend.component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterContext;
import org.pentaho.reporting.engine.classic.core.parameters.ValidationResult;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;

public interface IReportingComponent {

	public abstract String getDefaultOutputTarget();

	public abstract void setDefaultOutputTarget(String defaultOutputTarget);

	public abstract void setForceDefaultOutputTarget(
			boolean forceDefaultOutputTarget);

	public abstract boolean isForceDefaultOutputTarget();

	public abstract String getOutputTarget();

	public abstract void setOutputTarget(String outputTarget);

	public abstract void setOutputType(String outputType);

	public abstract String getOutputType();

	/**
	 * This method will be called if an input is called reportDefinitionInputStream, or any variant of that with dashes report-definition-inputstream for example.
	 * The primary purpose of this method is to facilitate unit testing.
	 *
	 * @param reportDefinitionInputStream any kind of InputStream which contains a valid report-definition
	 */
	public abstract void setReportDefinitionInputStream(
			InputStream reportDefinitionInputStream);

	/**
	 * Returns the path to the report definition (for platform use this is a path in the solution repository)
	 *
	 * @return reportdefinitionPath
	 */
	public abstract Serializable getReportFileId();

	/**
	 * Sets the path to the report definition (platform path)
	 *
	 * @param fileId the path to the report definition.
	 */
	public abstract void setReportFileId(Serializable fileId);

	/**
	 * Returns true if the report engine will be asked to use a paginated (HTML) output processor
	 *
	 * @return paginated
	 */
	public abstract boolean isPaginateOutput();

	/**
	 * Set the paging mode used by the reporting engine. This will also be set if an input
	 *
	 * @param paginateOutput page mode
	 */
	public abstract void setPaginateOutput(boolean paginateOutput);

	public abstract int getAcceptedPage();

	public abstract void setAcceptedPage(int acceptedPage);
	
	public abstract void setDashboardMode(boolean dashboardMode);
	/**
	 * This method returns the mime-type for the streaming output based on the effective output target.
	 *
	 * @return the mime-type for the streaming output
	 * @see SimpleReportingComponent#computeEffectiveOutputTarget()
	 */
	public abstract String getMimeType();

	/**
	 * This method sets the OutputStream to write streaming content on.
	 *
	 * @param outputStream an OutputStream to write to
	 */
	public abstract void setOutputStream(OutputStream outputStream);

	/**
	 * This method checks if the output is targeting a printer
	 *
	 * @return true if the output is supposed to go to a printer
	 */
	public abstract boolean isPrint();

	/**
	 * Set whether or not to send the report to a printer
	 *
	 * @param print a flag indicating whether the report should be printed.
	 */
	public abstract void setPrint(boolean print);

	/**
	 * This method gets the name of the printer the report will be sent to
	 *
	 * @return the name of the printer that the report will be sent to
	 */
	public abstract String getPrinter();

	/**
	 * Set the name of the printer to send the report to
	 *
	 * @param printer the name of the printer that the report will be sent to, a null value will be interpreted as the default printer
	 */
	public abstract void setPrinter(String printer);

	/**
	 * Get the inputs, needed by subclasses, such as with interactive adhoc
	 *
	 * @return immutable input map
	 */
	public abstract Map<String, Object> getInputs();

	/**
	 * This method sets the map of *all* the inputs which are available to this component. This allows us to use action-sequence inputs as parameters for our
	 * reports.
	 *
	 * @param inputs a Map containing inputs
	 */
	public abstract void setInputs(Map<String, Object> inputs);

	/**
	 * Sets the MasterReport for the report-definition, needed by subclasses, such as with interactive adhoc
	 *
	 * @return nothing
	 */
	public abstract void setReport(MasterReport report);

	/**
	 * Get the MasterReport for the report-definition, the MasterReport object will be cached as needed, using the PentahoResourceLoader.
	 *
	 * @return a parsed MasterReport object
	 * @throws ResourceException
	 * @throws IOException
	 */
	public abstract MasterReport getReport() throws ResourceException,
			IOException;

	public abstract String getComputedOutputTarget() throws IOException,
			ResourceException;

	/**
	 * Apply inputs (if any) to corresponding report parameters, care is taken when checking parameter types to perform any necessary casting and conversion.
	 *
	 * @param report  a MasterReport object to apply parameters to
	 * @param context a ParameterContext for which the parameters will be under
	 * @deprecated use the single parameter version instead. This method will now fail with an error if the
	 *             report passed in is not the same as the report this component has. This method will be removed in
	 *             version 4.0.
	 */
	public abstract void applyInputsToReportParameters(MasterReport report,
			ParameterContext context);

	/**
	 * Apply inputs (if any) to corresponding report parameters, care is taken when
	 * checking parameter types to perform any necessary casting and conversion.
	 *
	 * @param context          a ParameterContext for which the parameters will be under
	 * @param validationResult the validation result that will hold the warnings. If null, a new one will be created.
	 * @return the validation result containing any parameter validation errors.
	 * @throws java.io.IOException if the report of this component could not be parsed.
	 * @throws ResourceException   if the report of this component could not be parsed.
	 * @deprecated As of release 4.5, replaced by {@link ReportContentUtil#applyInputsToReportParameters(MasterReport, ParameterContext, Map, ValidationResult)}
	 */
	@Deprecated
	public abstract ValidationResult applyInputsToReportParameters(
			ParameterContext context, ValidationResult validationResult)
			throws IOException, ResourceException;

	/**
	 * This method returns the number of logical pages which make up the report. This results of this method are available only after validate/execute have been
	 * successfully called. This field has no setter, as it should never be set by users.
	 *
	 * @return the number of logical pages in the report
	 */
	public abstract int getPageCount();

	/**
	 * Determines if the output type supports pagination or not.
	 *
	 * @return True if the output type supports pagination.
	 */
	public abstract boolean outputSupportsPagination();

	/**
	 * This method will determine if the component instance 'is valid.' The validate() is called after all of the bean 'setters' have been called, so we may
	 * validate on the actual values, not just the presence of inputs as we were historically accustomed to.
	 * <p/>
	 * Since we should have a list of all action-sequence inputs, we can determine if we have sufficient inputs to meet the parameter requirements of the
	 * report-definition. This would include validation of values and ranges of values.
	 *
	 * @return true if valid
	 * @throws Exception
	 */
	public abstract boolean validate() throws Exception;

	/**
	 * Perform the primary function of this component, this is, to execute. This method will be invoked immediately following a successful validate().
	 *
	 * @return true if successful execution
	 * @throws Exception
	 */
	public abstract boolean execute() throws Exception;

	/**
	 * Perform a pagination run.
	 *
	 * @return the number of pages or streams generated.
	 * @throws IOException       if an IO error occurred while loading the report.
	 * @throws ResourceException if a resource loading error occurred.
	 */
	public abstract int paginate() throws IOException, ResourceException;

}