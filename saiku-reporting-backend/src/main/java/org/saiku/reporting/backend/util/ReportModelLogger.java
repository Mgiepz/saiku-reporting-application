package org.saiku.reporting.backend.util;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.saiku.reporting.core.model.Chart;
import org.saiku.reporting.core.model.DataSource;
import org.saiku.reporting.core.model.ElementFormat;
import org.saiku.reporting.core.model.FieldDefinition;
import org.saiku.reporting.core.model.GroupDefinition;
import org.saiku.reporting.core.model.Label;
import org.saiku.reporting.core.model.Length;
import org.saiku.reporting.core.model.LengthUnit;
import org.saiku.reporting.core.model.PageSetup;
import org.saiku.reporting.core.model.ReportSpecification;
import org.saiku.reporting.core.model.RootBandFormat;
import org.slf4j.Logger;

public class ReportModelLogger {

	public static void log(ReportSpecification spec, Logger logger) {
	
		LogOutputStream logOut = new LogOutputStream(logger);
		
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(
					Chart.class,
					DataSource.class,
					ElementFormat.class,
					FieldDefinition.class,
					GroupDefinition.class,
					Label.class,
					Length.class,
					LengthUnit.class,
					PageSetup.class,
					//Parameter.class,
					ReportSpecification.class,
					RootBandFormat.class
					//TemplateDefinition.class	
					);

			Marshaller m = jc.createMarshaller();
		
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(spec, logOut);
			
			logOut.close();
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
