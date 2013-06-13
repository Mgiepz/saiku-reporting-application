//package org.saiku.reporting.backend.rest;
//
//import javax.ws.rs.GET;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//
//import org.pentaho.platform.api.engine.IPentahoSession;
//import org.saiku.reporting.backend.pho.PentahoSessionManager;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//
//@Component
//@Path("saiku-reporting/echo")
//@Scope("request")
//public class EchoResource {
//
//    @GET
//    @Produces({"application/json" })
//    @Path("/echo")
//    public String getEcho(){
//    	
//    	String echo = "echoing session info:";
//    	
//    	IPentahoSession session = PentahoSessionManager.getSessionData();
//    	
//    	echo += session.getName();
//    	echo += session.toString();
//    	
//		return echo;
//    	
//    }
//
//}
