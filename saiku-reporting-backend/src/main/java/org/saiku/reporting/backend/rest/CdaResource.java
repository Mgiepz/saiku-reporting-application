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

import org.saiku.reporting.backend.service.CdaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Path("/saiku-adhoc/rest/cda")
@Scope("request")
public class CdaResource {
	
    @Context
    private HttpServletResponse anotherServletResponse;
  
	@Autowired
    private CdaService cda;
	
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
