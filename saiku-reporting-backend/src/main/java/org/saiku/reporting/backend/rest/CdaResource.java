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
package org.saiku.reporting.backend.rest;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.saiku.reporting.backend.service.ICdaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Path("saiku-reporting/api/cda")
@Scope("request")
public class CdaResource {
	
    @Context
    private HttpServletResponse anotherServletResponse;
  
	@Autowired
    private ICdaService cda;
	
    @GET
    @Produces({"application/json" })
    @Path("/doQuery")
    public String doQuery(
    		@QueryParam("solution") String solution, 
    		@QueryParam("path") String path, 
    		@QueryParam("file") String file, 
    		@QueryParam("dataAccessId") String dataAccessId, 
    		@QueryParam("outputType") String outputType){
   
        final OutputStream out = new ByteArrayOutputStream();
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("outputType", outputType);
        params.put("path", path+"/"+file);
        params.put("solution", "");
        params.put("dataAccessId", dataAccessId);
        
        return out.toString();
    	
    }
    
//	@POST
//	@Produces({"application/json" })
//	@Consumes({"application/json"})
//	@Path("/report/{page}")
//	public String doQuery(ThinCda cda){
//
//		try {
//
//		}catch (Exception e) {
//			throw new SaikuClientException(e.getMessage());
//		}
//
//	}
    

}
