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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.util.PageFormatFactory;
import org.saiku.reporting.backend.exceptions.MetadataException;
import org.saiku.reporting.backend.objects.dto.ReportTemplate;
import org.saiku.reporting.backend.objects.metadata.impl.MetadataModel;
import org.saiku.reporting.backend.objects.metadata.impl.MetadataModelInfo;
import org.saiku.reporting.backend.server.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Path("/saiku-adhoc/rest/discover")
@Scope("request")
public class MetadataDiscoverResource {

	private static final Log log = LogFactory.getLog(MetadataDiscoverResource.class);
    
    @Autowired
    private MetadataRepository metadataRepository;

    /**
     * Returns the datasources available.
     */
    @GET
    @Produces({"application/json"})
    @Path("/{locale}")
    public MetadataModelInfo[] getModelInfos(
    		@Context HttpServletRequest request) {

        try {

            if (log.isDebugEnabled()) {
                log.debug("REST:GET " + " getModelInfos");
            }

            return metadataRepository.getBusinessModels("", request.getLocale());

        } catch (MetadataException e) {
            log.error(this.getClass().getName(), e);
            return new MetadataModelInfo[]{};
        }
    }

    @GET
    @Produces({"application/json"})
    @Path("/{domainId}/{modelId}/model")
    public MetadataModel getModel(
            @PathParam("domainId") String domainId,
            @PathParam("modelId") String modelId) {
        try {

            if (log.isDebugEnabled()) {
                log.debug("REST:GET " + " getModel domainId=" + domainId + " modelId=" + modelId);
            }

            return metadataRepository.loadModel(URLDecoder.decode(domainId, "UTF-8"), modelId);
        } catch (UnsupportedEncodingException e) {
            log.error(e);
        }
        return null;
    }

    /**
     * Returns the available templates.
     */
    @GET
    @Produces({"application/json"})
    @Path("/templates")
    public ReportTemplate[] getReportTemplates() {
    	return null;
    }

    /**
     * Returns the available page formats.
     */
    @GET
    @Produces({"application/json"})
    @Path("/pageformats")
    public String[] getPageFormats() {

        return PageFormatFactory.getInstance().getPageFormats();

    }
}
