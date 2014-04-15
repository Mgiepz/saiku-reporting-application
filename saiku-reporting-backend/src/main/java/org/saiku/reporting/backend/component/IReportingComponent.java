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
package org.saiku.reporting.backend.component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;

public interface IReportingComponent {

	public abstract int getAcceptedPage();
	
	public abstract void setDefaultOutputTarget(String defaultOutputTarget);

	public abstract void setOutputTarget(String outputTarget);

	public abstract void setReportFileId(Serializable fileId);

	public abstract void setPaginateOutput(boolean paginateOutput);

	public abstract void setAcceptedPage(int acceptedPage);

	public abstract void setDashboardMode(boolean dashboardMode);

	public abstract void setOutputStream(OutputStream outputStream);

	public abstract void setInputs(Map<String, Object> inputs);

	public abstract void setReport(MasterReport report);
	
	public abstract void setReportDefinitionInputStream(final InputStream reportDefinitionInputStream);

	public abstract MasterReport getReport() throws ResourceException,
			IOException;

	public abstract int getPageCount();

	public abstract boolean validate() throws Exception;

	public abstract boolean execute() throws Exception;

	public abstract int paginate() throws IOException, ResourceException;

}
