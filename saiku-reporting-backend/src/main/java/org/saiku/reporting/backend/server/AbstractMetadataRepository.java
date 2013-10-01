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
package org.saiku.reporting.backend.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.saiku.reporting.backend.objects.metadata.impl.MetadataModelInfo;
import org.saiku.reporting.backend.objects.metadata.impl.ModelInfoComparator;

/**
 * @author mg
 *
 */
public abstract class AbstractMetadataRepository implements MetadataRepository{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Log log = LogFactory.getLog(AbstractMetadataRepository.class);

	/**
	 * Returns a list of ModelInfo objects for the specified domain.
	 * @param domainId
	 * @param context
	 *            Area to check for model visibility
	 * @param models
	 * @throws UnsupportedEncodingException 
	 */
	protected void getModelInfos(final String domainId, final String context, List<MetadataModelInfo> models,
			Locale locale) throws UnsupportedEncodingException {

		Domain domainObject = getDomain(domainId);
		if (domainObject == null) {
			return;
		}

		//Some guessing here
		String loc = getClosestLocale(locale.toString(), domainObject.getLocaleCodes());

		// iterate over all of the models in this domain
		for (LogicalModel model : domainObject.getLogicalModels()) {
			String vis = (String) model.getProperty("visible");
			if (vis != null) {
				String[] visibleContexts = vis.split(",");
				boolean visibleToContext = false;
				for (String c : visibleContexts) {
					if (c.equals(context)) {
						visibleToContext = true;
						break;
					}
				}
				if (!visibleToContext) {
					continue;
				}
			}
			// create a new ModelInfo object and give it the envelope
			// information about the model
			MetadataModelInfo modelInfo = new MetadataModelInfo();
			modelInfo.setDomainId(URLEncoder.encode(domainId,"UTF-8"));
			modelInfo.setModelId(model.getId());
			modelInfo.setModelName(model.getName(loc));
			if (model.getDescription() != null) {
				String modelDescription = model.getDescription(loc);
				modelInfo.setModelDescription(modelDescription);
			}
			models.add(modelInfo);
		}
		return;
	}

	@Override
	@SuppressWarnings("unchecked")
	public MetadataModelInfo[] getBusinessModels(String domainName, Locale locale) {

		List<MetadataModelInfo> models = new ArrayList<MetadataModelInfo>();

		IMetadataDomainRepository repo = getMetadataDomainRepository();

		if (repo == null) {
			log.error("MetadataService.ERROR_0001_BAD_REPO"); //$NON-NLS-1$
			return null;
		}

		// TODO: what context is appropriate here?
		String context = null;

		try {
			if (StringUtils.isEmpty(domainName)) {
				for (String domain : getMetadataDomainRepository().getDomainIds()) {
					getModelInfos(domain, context, models, locale);
				}
			} else {
				getModelInfos(domainName, context, models, locale);
			}
		} catch (Throwable t) {
			log.error("MetadataService.ERROR_0002_BAD_MODEL_LIST", t); //$NON-NLS-1$
		}

		Collections.sort(models, new ModelInfoComparator());
		return models.toArray(new MetadataModelInfo[models.size()]);
	}

	private static String getClosestLocale( String locale, String locales[] ) {
		// see if this locale is supported
		if( locales == null || locales.length == 0 ) {
			return locale;
		}
		if( locale == null || locale.length() == 0 ) {
			return locales[ 0 ];
		}
		String localeLanguage = locale.substring(0, 2);
		String localeCountry = (locale.length() > 4) ? locale.substring(0, 5) : localeLanguage;
		int looseMatch = -1;
		int closeMatch = -1;
		int exactMatch = -1;
		for( int idx=0; idx<locales.length; idx++ ) {
			if( locales[idx].equals( locale ) ) {
				exactMatch = idx;
				break;
			}
			else if( locales[idx].length() > 1 && locales[idx].substring(0, 2).equals( localeLanguage ) ) {
				looseMatch = idx;
			}
			else if( locales[idx].length() > 4 && locales[idx].substring(0, 5).equals( localeCountry ) ) {
				closeMatch = idx;
			}
		}
		if( exactMatch != -1 ) {
			// do nothing we have an exact match
		}
		else if( closeMatch != - 1) {
			locale = locales[ closeMatch ];
		}
		else if( looseMatch != - 1) {
			locale = locales[ looseMatch ];
		}
		else {
			// no locale is close , just go with the first?
			locale = locales[ 0 ];
		}
		return locale;
	}



}
