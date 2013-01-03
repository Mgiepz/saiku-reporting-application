///*
// * Copyright (C) 2011 Marius Giepz
// *
// * This program is free software; you can redistribute it and/or modify it 
// * under the terms of the GNU General Public License as published by the Free 
// * Software Foundation; either version 2 of the License, or (at your option) 
// * any later version.
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * 
// * See the GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License along 
// * with this program; if not, write to the Free Software Foundation, Inc., 
// * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// *
// */
//package org.saiku.reporting.backend.rest;
//
//import java.io.ByteArrayOutputStream;
//import java.io.OutputStream;
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.servlet.http.HttpServletResponse;
//import javax.ws.rs.GET;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.QueryParam;
//import javax.ws.rs.core.Context;
//
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//
//import pt.webdetails.cda.CdaContentGenerator;
//import pt.webdetails.cda.CdaEngine;
//import pt.webdetails.cda.discovery.DiscoveryOptions;
//import pt.webdetails.cda.exporter.ExporterEngine;
//import pt.webdetails.cda.settings.CdaSettings;
//import pt.webdetails.cda.settings.SettingsManager;
//
//
//@Component
//@Path("/saiku-adhoc/rest/cda")
//@Scope("request")
//public class CdaResources {
//
//    @Context
//    private HttpServletResponse anotherServletResponse;
//
//    
//    CdaContentGenerator ccg;
//    
//    public CdaResources(){
//        ccg = new CdaContentGenerator();
//        
//    }
//    
//    @GET
//    @Produces({"application/json" })
//    @Path("/doQuery")
//    public String doQuery(@QueryParam("solution") String solution, @QueryParam("path") String path, @QueryParam("file") String file, @QueryParam("dataAccessId") String dataAccessId, @QueryParam("outputType") String outputType){
//        
//        final IParameterProvider pathParams = null;
//        final OutputStream out = new ByteArrayOutputStream();
//        
//        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("outputType", outputType);
//        params.put("path", path+"/"+file);
//        params.put("solution", "");
//        params.put("dataAccessId", dataAccessId);
////        IParameterProvider requestParams = new SimpleParameterProvider(params);
//        try {
//            ccg.doQuery(requestParams, out);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        
//        return out.toString();
//
//    }
//    
//    @GET
//    @Produces({"application/xml" })
//    @Path("/listParameters")
//    public String listParameters(@QueryParam("outputType") String outputType, @QueryParam("solution") String solution, @QueryParam("path") String path, @QueryParam("file") String file, @QueryParam("dataAccessId") String dataAccessId){
//        
//        final IParameterProvider pathParams = null;
//        final OutputStream out = new ByteArrayOutputStream();
//        
//        Map<String, Object> params = new HashMap<String, Object>();
//        
//        params.put("path", path);
//        params.put("solution", "");
//        params.put("dataAccessId", dataAccessId);
//        params.put("outputType", outputType);
//        params.put("file", file);
//        IParameterProvider requestParams = new SimpleParameterProvider(params);
//
//        try {
//            listParameters(requestParams, out);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        
//        return out.toString();
//
//    }
//    //Have to remove the pentaho specific call
//    public void listParameters(final IParameterProvider pathParams, final OutputStream out) throws Exception
//    {
//      final CdaEngine engine = CdaEngine.getInstance();
//
//      final String path = pathParams.getStringParameter("path", ".");
//      final String file = pathParams.getStringParameter("file", "na");
//      final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path+"/"+file);
//
//      // Handle the query itself and its output format...
//      final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
//      discoveryOptions.setOutputType(pathParams.getStringParameter("outputType", "json"));
//      discoveryOptions.setDataAccessId(pathParams.getStringParameter("dataAccessId", "<blank>"));
//
//      String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
//      setResponseHeaders(mimeType, null);
//
//      engine.listParameters(out, cdaSettings, discoveryOptions);
//    }
//    
//
//    private void setResponseHeaders(final String mimeType, final String attachmentName)
//    {
//      // Make sure we have the correct mime type
//      final HttpServletResponse response = anotherServletResponse;
//      if (response == null)
//      {
//          return;
//        }
//
//      response.setHeader("Content-Type", mimeType);
//
//      if (attachmentName != null)
//      {
//        response.setHeader("content-disposition", "attachment; filename=" + attachmentName);
//      }
//
//      // We can't cache this request
//      response.setHeader("Cache-Control", "max-age=0, no-store");
//    }
//    
//}
