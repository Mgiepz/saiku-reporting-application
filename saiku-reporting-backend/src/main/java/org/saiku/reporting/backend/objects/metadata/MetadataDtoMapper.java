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
package org.saiku.reporting.backend.objects.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.Concept;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.Alignment;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.FieldType;
import org.saiku.reporting.backend.objects.metadata.impl.Category;
import org.saiku.reporting.backend.objects.metadata.impl.Column;
import org.saiku.reporting.backend.objects.metadata.impl.MetadataModel;

public class MetadataDtoMapper {
	
	public org.pentaho.metadata.model.Domain getDomain() {
		return domain;
	}

	public void setDomain(org.pentaho.metadata.model.Domain domain) {
		this.domain = domain;
	}

	private Log log = LogFactory.getLog(MetadataDtoMapper.class);

	private org.pentaho.metadata.model.Domain domain;
	
	/**
	 * Works out what is the most appropriate locale to use given a domain and the user's
	 * current locale
	 * @return
	 */
	protected String getLocale() {
		String locale = Locale.getDefault().toString();
		//LocaleHelper.getClosestLocale(LocaleHelper.getLocale().toString(), domain.getLocaleCodes());
		return locale;
	}
	
	/**
	 * Creates a lightweight, serializable model object from a logical model
	 * @param m
	 * @param domainId
	 * @return
	 */
	public MetadataModel createThinModel(LogicalModel m, String domainId) {
		// create the model object
		MetadataModel model = new MetadataModel();
		model.setName(m.getName(getLocale()));
		model.setId(m.getId());
		model.setDomainId(domainId);
		model.setDescription(m.getDescription(getLocale()));
		// add the categories to the model
		List<ICategory> categories = new ArrayList<ICategory>();
		for (org.pentaho.metadata.model.Category cat : m.getCategories()) {

			//check visibility
			if(isVisible(cat)){
			categories.add(createCategory(m, cat));
			}
		}
		model.setCategories(categories.toArray(new Category[categories.size()]));

		return model;

	}
	
	/**
	 * Creates a lightweight, serializable category objects from a logical model category
	 * @param m
	 * @param c
	 * @return
	 */
	private Category createCategory(LogicalModel m, org.pentaho.metadata.model.Category c) {
		// create a thin category object
		Category cat = new Category();
		cat.setName(c.getName(getLocale()));
		cat.setId(c.getId());
		cat.setDescription(c.getDescription(getLocale()));
		if(cat.getId().equals(cat.getDescription())) {
			cat.setDescription(null);
		}
		List<IColumn> columns = new ArrayList<IColumn>();
		for (LogicalColumn col : c.getLogicalColumns()) {
			if(isVisible(col)){
				columns.add(createColumn(m, col, c));	
			}
		}
		cat.setColumns(columns.toArray(new Column[columns.size()]));

		return cat;
	}
	
	/**
	 * Creates a lightweight, serializable Column object from a logical model column
	 * @param m
	 * @param c
	 * @return
	 */
	private Column createColumn(LogicalModel m, LogicalColumn c, org.pentaho.metadata.model.Category category) {
		Column col = new Column();
		col.setName(c.getName(getLocale()));
		col.setId(c.getId());
		col.setDescription(c.getDescription(getLocale()));
		if(col.getId().equals(col.getDescription())) {
			col.setDescription(null);
		}
		if( c.getFieldType() != null ) {      
			col.setFieldType(c.getFieldType().name());
		} else {
			col.setFieldType( "UNKNOWN" ); //$NON-NLS-1$
		}

		col.setType(c.getDataType().getName().toUpperCase());
		col.setCategory(category.getId());
		// set the aggregation fields for the column
		List<AggregationType> possibleAggs = c.getAggregationList();
		List<String> aggTypes = new ArrayList<String>();
		if (possibleAggs != null) {
			for (AggregationType agg : possibleAggs) {
				aggTypes.add(agg.name());
			}
		}

		// There might be a default agg, but no agg list. If so, add it to the list.

		AggregationType defaultAggType = AggregationType.NONE;
		if (c.getAggregationType() != null) {
			defaultAggType = c.getAggregationType();
		}
		if (!aggTypes.contains(defaultAggType)) {
			aggTypes.add(defaultAggType.name());
		}
		col.setAggTypes(aggTypes);
		col.setDefaultAggType(defaultAggType.name());
		col.setSelectedAggType(defaultAggType.name());

		// set the alignment
		DataType dataType = c.getDataType();
		FieldType fieldType = c.getFieldType();
		Object obj = c.getProperty("alignment"); //$NON-NLS-1$
		if(obj instanceof Alignment) {
			if(obj == Alignment.LEFT) {
				col.setHorizontalAlignment(Alignment.LEFT.toString());
			}
			else if(obj == Alignment.RIGHT) {
				col.setHorizontalAlignment(Alignment.RIGHT.toString());
			}
			else if(obj == Alignment.CENTERED) {
				col.setHorizontalAlignment(Alignment.CENTERED.toString());
			}
		}
		else if(fieldType == FieldType.FACT) {
			col.setHorizontalAlignment(Alignment.RIGHT.toString());
		}
		else if(fieldType == FieldType.OTHER && dataType == DataType.NUMERIC) {
			col.setHorizontalAlignment(Alignment.RIGHT.toString());
		}
		else {
			col.setHorizontalAlignment(Alignment.LEFT.toString());
		}
		// set the format mask
		obj = c.getProperty("mask"); //$NON-NLS-1$
		if(obj != null) {
			col.setFormatMask((String)obj);
		}
		return col;
	}
	
	public static boolean isVisible(Concept concept) {

		String context = null;
		
		//check visible
		String vis = (String) concept.getProperty("visible");
		if (vis != null) {
			String[] visibleContexts = vis.split(",");
			for (String c : visibleContexts) {
				if (c.equals(context)) {
					return true;
				}
			}
			return false;
		}
		
		//also check hidden
		Boolean hidden = (Boolean) concept.getProperty("hidden");
		if (hidden != null && hidden.equals(Boolean.TRUE)) {
			return false;
		}		
		
		return true;
		
	}

}
