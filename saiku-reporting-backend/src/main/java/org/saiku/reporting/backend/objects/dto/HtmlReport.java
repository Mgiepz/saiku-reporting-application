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
package org.saiku.reporting.backend.objects.dto;

import org.saiku.reporting.core.model.ReportSpecification;

public class HtmlReport {
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getPageCount() {
		return pageCount;
	}

	public ReportSpecification getReportModel() {
		return reportModel;
	}

	public void setReportModel(ReportSpecification reportModel) {
		this.reportModel = reportModel;
	}

	private String data;
	
	private ReportSpecification reportModel;

	private int currentPage;
	
	private int pageCount;

}
