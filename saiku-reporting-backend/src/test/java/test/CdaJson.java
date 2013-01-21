//package test;
//
//import java.util.ArrayList;
//
//import pt.webdetails.cda.connections.Connection;
//import pt.webdetails.cda.connections.metadata.MetadataConnection;
//import pt.webdetails.cda.dataaccess.MqlDataAccess;
//import pt.webdetails.cda.dataaccess.Parameter;
//import pt.webdetails.cda.settings.CdaSettings;
//
//public class CdaJson {
//
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		
//		CdaSettings cda = new CdaSettings("1", null);
//
//		Connection connection = new MetadataConnection("1", "a" , "b");
//		MqlDataAccess dataAccess = new MqlDataAccess("1", "a" , "b", "x");
//		dataAccess.setParameters(new ArrayList<Parameter>());
//		dataAccess.getColumnDefinitions().clear();
//		cda.addConnection(connection);
//		cda.addDataAccess(dataAccess);
//		
//		XmlMapper xmlMapper = new XmlMapper();
//		String xml = xmlMapper.writeValueAsString(user);
//		
//	}
//
//}
