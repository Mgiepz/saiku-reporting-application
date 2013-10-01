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
package org.saiku.reporting.backend.server;

import java.util.Locale;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.saiku.reporting.backend.exceptions.MetadataException;
import org.saiku.reporting.backend.objects.metadata.impl.MetadataModel;
import org.saiku.reporting.backend.objects.metadata.impl.MetadataModelInfo;

public interface MetadataRepository {

	/**
	 * Get a thin representation of all available metadata models
	 * 
	 * @param domainName
	 * @param locale 
	 * @return
	 * @throws QuerybuilderServiceException
	 */
	public abstract MetadataModelInfo[] getBusinessModels(String domainName, Locale locale)
			throws MetadataException;

	/**
	 * Get the real logical model for a given ID
	 * 
	 * @param modelId
	 * @param domainId 
	 * @return
	 */
	public abstract LogicalModel getLogicalModel(String domainId, String modelId);

	/**
	 * Get the real domain object for a given id
	 * @param domain
	 * @return
	 */
	public abstract Domain getDomain(String domainId);

	/**
	 * Returns a Model object for the requested model. The model will include
	 * the basic metadata - categories and columns.
	 * 
	 * @param domainId
	 * @param modelId
	 * @return
	 */
	public abstract MetadataModel loadModel(String domainId, String modelId);

	//public abstract ReportTemplate[] loadTemplates();
	
	public abstract IMetadataDomainRepository getMetadataDomainRepository();

}
