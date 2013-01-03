package org.saiku.reporting.backend.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import org.pentaho.reporting.engine.classic.extensions.datasources.cda.CdaQueryBackend;
import org.pentaho.reporting.engine.classic.extensions.datasources.cda.CdaResponseParser;
import org.pentaho.reporting.libraries.base.util.CSVTokenizer;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import pt.webdetails.cda.CdaEngine;
import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;

public class SaikuCdaQueryBackend extends CdaQueryBackend{

	private static final String MASTER_QUERY = "master";

	private static final Logger log = LoggerFactory.getLogger(SaikuCdaQueryBackend.class);

	final static int DEFAULT_PAGE_SIZE = 20;
	final static int DEFAULT_START_PAGE = 0;

	@Override
	public TypedTableModel fetchData(final DataRow dataRow, final String method,
			final Map<String, String> inputs)
					throws ReportDataFactoryException
					{

		CdaSettings cdaSettings = null;

		final String baseURL = getBaseUrl();
		if (StringUtils.isEmpty(baseURL, true))
		{
			throw new ReportDataFactoryException("Base URL is null");
		}

		try {

			FileSystemManager fileSystemManager = VFS.getManager();
			FileObject fileObject = fileSystemManager.resolveFile(baseURL);
			InputSource inputSource = new InputSource(fileObject.getContent().getInputStream());
			SAXReader reader = new SAXReader(); 
			Document cda = reader.read(inputSource);

			cdaSettings = new CdaSettings(cda, MASTER_QUERY, null);

		} catch (Exception e) {

		}

		final CdaEngine engine = CdaEngine.getInstance();

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		if (METHOD_LIST_PARAMETERS.equals(method))
		{
			final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
			discoveryOptions.setDataAccessId(MASTER_QUERY);
			discoveryOptions.setOutputType("xml");
			log.info("Doing discovery, return xml");
			try {
				engine.listParameters(out, cdaSettings, discoveryOptions);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				throw new ReportDataFactoryException(e.getMessage());
			}
		}
		else{

			QueryOptions queryOptions = new QueryOptions();
			queryOptions.setDataAccessId(MASTER_QUERY);
			queryOptions.setOutputType("xml");



			final String CDA_PARAMS = "cdaParameterString";
			final String CDA_PARAM_SEPARATOR = ";";

			// page info

			final long pageSize = inputsGetLong(inputs, "pageSize", 0);
			final long pageStart = inputsGetLong(inputs, "pageStart", 0);
			final boolean paginate = "true".equals(inputsGetString(inputs, "paginateQuery", "false"));
			if (pageSize > 0 || pageStart > 0 || paginate) {
				if (pageSize > Integer.MAX_VALUE || pageStart > Integer.MAX_VALUE) {
					throw new ArithmeticException("Paging values too large");
				}
				queryOptions.setPaginate(true);
				queryOptions.setPageSize(pageSize > 0 ? (int) pageSize : paginate ? DEFAULT_PAGE_SIZE : 0);
				queryOptions.setPageStart(pageStart > 0 ? (int) pageStart : paginate ? DEFAULT_START_PAGE : 0);
			}

			// query info 

			queryOptions.setOutputType(inputsGetString(inputs, "outputType", "xml"));
			queryOptions.setDataAccessId(inputsGetString(inputs, "dataAccessId", "<blank>"));
			queryOptions.setOutputIndexId(inputsGetInteger(inputs, "outputIndexId", 1));

			// params and settings

			//process parameter string "name1=value1;name2=value2"
			String cdaParamString = inputsGetString(inputs, CDA_PARAMS, null);
			if (cdaParamString != null && cdaParamString.trim().length() > 0) {

				List<String> cdaParams = new ArrayList<String>();
				//split to 'name=val' tokens
				CSVTokenizer tokenizer = new CSVTokenizer(cdaParamString, CDA_PARAM_SEPARATOR);
				while(tokenizer.hasMoreTokens()){
					cdaParams.add(tokenizer.nextToken());
				}

				//split '='
				for(String nameValue : cdaParams){
					int i = 0;
					CSVTokenizer nameValSeparator = new CSVTokenizer(nameValue, "=");
					String name=null, value=null;
					while(nameValSeparator.hasMoreTokens()){
						if(i++ == 0){
							name = nameValSeparator.nextToken();
						}
						else {
							value = nameValSeparator.nextToken();
							break;
						}
					}
					if(name != null) queryOptions.addParameter(name, value);
				}
			}

			for (String param : inputs.keySet()) {
				if (param.startsWith("param")) {
					queryOptions.addParameter(param.substring(5), inputsGetString(inputs, param, ""));
				} else if (param.startsWith("setting")) {
					queryOptions.addSetting(param.substring(7), inputsGetString(inputs, param, ""));
				}
			}

			try {
				engine.doQuery(out, cdaSettings, queryOptions);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				throw new ReportDataFactoryException(e.getMessage());
			}

		}




		final InputStream responseBodyIs = new ByteArrayInputStream(out.toByteArray());

		try {
			return CdaResponseParser.performParse(responseBodyIs);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			throw new ReportDataFactoryException(e.getMessage());
		}		

					}

	private static long inputsGetLong(Map<String,String>inputs,String name, long defaultVal) {
		String obj = inputs.get(name);
		// pojo component forces all strings to upper case :-(
		if (obj == null) {
			obj = inputs.get(name.toUpperCase());
		}
		if (obj == null) {
			return defaultVal;
		}
		return new Long(obj);
	}

	private static String inputsGetString(Map<String,String>inputs,String name, String defaultVal) {
		String obj = inputs.get(name);
		// pojo component forces all strings to upper case :-(
		if (obj == null) {
			obj = inputs.get(name.toUpperCase());
		}
		if (obj == null) {
			return defaultVal;
		}
		return obj;
	}

	private static int inputsGetInteger(Map<String,String>inputs, String name, int defaultVal)
	{
		String obj = inputs.get(name);

		// pojo component forces all strings to upper case :-(
		if (obj == null)
		{
			obj = inputs.get(name.toUpperCase());
		}

		if (obj == null)
		{
			return defaultVal;
		}

		return new Integer(obj);
	}

}
