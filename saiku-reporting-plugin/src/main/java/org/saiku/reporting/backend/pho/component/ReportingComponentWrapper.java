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
package org.saiku.reporting.backend.pho.component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.platform.plugin.SimpleReportingComponent;
import org.saiku.reporting.backend.component.IReportingComponent;

public class ReportingComponentWrapper implements IReportingComponent {
	
	private SimpleReportingComponent reportingComponent;

	public int getAcceptedPage() {
		return reportingComponent.getAcceptedPage();
	}

	public void setDefaultOutputTarget(String defaultOutputTarget) {
		reportingComponent.setDefaultOutputTarget(defaultOutputTarget);
	}
	
	public void setReportDefinitionInputStream(
			InputStream reportDefinitionInputStream) {
		reportingComponent.setReportDefinitionInputStream(reportDefinitionInputStream);
	}

	public void setOutputTarget(String outputTarget) {
		reportingComponent.setOutputTarget(outputTarget);
	}

	public IActionSequenceResource getReportDefinition() {
		return reportingComponent.getReportDefinition();
	}

	public void setReportFileId(Serializable path) {
		
		RepositoryFile prptFile = PentahoSystem.get(IUnifiedRepository.class, null).getFile((String) path);
		
		reportingComponent.setReportFileId(prptFile.getId());
	}

	public void setPaginateOutput(boolean paginateOutput) {
		reportingComponent.setPaginateOutput(paginateOutput);
	}

	public void setAcceptedPage(int acceptedPage) {
		reportingComponent.setAcceptedPage(acceptedPage);
	}

	public void setDashboardMode(boolean dashboardMode) {
		reportingComponent.setDashboardMode(dashboardMode);
	}

	public void setOutputStream(OutputStream outputStream) {
		reportingComponent.setOutputStream(outputStream);
	}

	public void setInputs(Map<String, Object> inputs) {
		reportingComponent.setInputs(inputs);
	}

	public void setReport(MasterReport report) {
		reportingComponent.setReport(report);
	}

	public MasterReport getReport() throws ResourceException, IOException {
		return reportingComponent.getReport();
	}

	public int getPageCount() {
		return reportingComponent.getPageCount();
	}

	public boolean validate() throws Exception {
		return reportingComponent.validate();
	}

	public boolean execute() throws Exception {
		return reportingComponent.execute();
	}

	public int paginate() throws IOException, ResourceException {
		return reportingComponent.paginate();
	}

	public ReportingComponentWrapper() {
		reportingComponent = new SimpleReportingComponent();
	}



}
