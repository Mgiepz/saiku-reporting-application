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
package org.saiku.reporting.backend.pho;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.IMetadataQueryExec;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.query.model.Parameter;
import org.pentaho.metadata.query.model.Query;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.saiku.reporting.backend.messages.Messages;
//import org.saiku.reporting.backend.objects.dto.ReportTemplate;
import org.saiku.reporting.backend.objects.metadata.MetadataDtoMapper;
import org.saiku.reporting.backend.objects.metadata.impl.MetadataModel;
import org.saiku.reporting.backend.server.AbstractMetadataRepository;
import org.saiku.reporting.backend.server.MetadataRepository;

import java.util.Map;

public class PentahoMetadataRepositoryImpl extends AbstractMetadataRepository {

	private Log log = LogFactory.getLog(PentahoMetadataRepositoryImpl.class);

	protected final IMetadataDomainRepository repo;

	public PentahoMetadataRepositoryImpl() {
		repo = getMetadataDomainRepository();
	}

	public LogicalModel getLogicalModel(String domainId, String modelId) {
		return repo.getDomain(domainId).findLogicalModel(modelId);
	}

	public Domain getDomain(String domainId) {
		return repo.getDomain(domainId);
	}

	public MetadataModel loadModel(String domainId, String modelId) {

		if (domainId == null) {
			log.error(Messages.getErrorString("MetadataService.ERROR_0003_NULL_DOMAIN")); //$NON-NLS-1$
			return null;
		}

		if (modelId == null) {
			log.error(Messages.getErrorString("MetadataService.ERROR_0004_NULL_Model")); //$NON-NLS-1$
			return null;
		}

		Domain domain = repo.getDomain(domainId);

		if (domain == null) {
			log.error(Messages.getErrorString("MetadataService.ERROR_0005_DOMAIN_NOT_FOUND", domainId)); //$NON-NLS-1$
			return null;
		}

		LogicalModel model = domain.findLogicalModel(modelId);

		if (model == null) {
			// the model cannot be found or cannot be loaded
			log.error(Messages.getErrorString("MetadataService.ERROR_0006_MODEL_NOT_FOUND", modelId)); //$NON-NLS-1$
			return null;
		}

		MetadataDtoMapper util = new MetadataDtoMapper();
		util.setDomain(domain);
		MetadataModel thinModel = util.createThinModel(model, domainId);
		return thinModel;

	}


	/**
	 * Returns a instance of the IMetadataDomainRepository for the current
	 * session
	 * 
	 * @return
	 */
	public IMetadataDomainRepository getMetadataDomainRepository() {
		IMetadataDomainRepository mdr = PentahoSystem
				.get(IMetadataDomainRepository.class,
						PentahoSessionHolder.getSession());
		return mdr;
	}

    public Object getExecutor(Object queryObject, Map<String, Object> inputs, MetadataRepository metadataRepository){
       IPentahoSession session = null;

       String queryExecName = ((Query)queryObject).getLogicalModel().getPhysicalModel().getQueryExecName();
       String queryExecDefault = ((Query)queryObject).getLogicalModel().getPhysicalModel().getDefaultQueryClassname();
       // String modelType = (String) inputs.get("modeltype");
       IMetadataQueryExec executor = PentahoSystem.get( IMetadataQueryExec.class, queryExecName, session );

       if ( executor == null ) {
           // get the executor from a plugin possibly?
           Class clazz;
           try {
               clazz =
                       Class.forName( queryExecDefault, true, ((Query)queryObject).getLogicalModel().getPhysicalModel().getClass()
                               .getClassLoader() );
               executor = (IMetadataQueryExec) clazz.getConstructor( new Class[] {} ).newInstance( new Object[] {} );
           } catch ( Exception e ) {

           }
       }

       if ( executor == null ) {
           // the query exec class is not defined thru configuration, go with the default
           Class clazz;
           try {
               clazz = Class.forName( queryExecDefault );
               executor = (IMetadataQueryExec) clazz.getConstructor( new Class[] {} ).newInstance( new Object[] {} );
           } catch ( Exception e ) {

               return null;
           }
       }
       else{
           return executor;
       }

        if ( ((Query)queryObject).getParameters() != null ) {
            for ( Parameter param : ((Query)queryObject).getParameters() ) {

                Object value = null;
                if ( inputs != null ) {
                    value = inputs.get( param.getName() );
                }

                executor = (IMetadataQueryExec) metadataRepository.getExecutor(queryObject, inputs, metadataRepository);
                executor.setParameter( param, value );

            }
        }

        return executor;
   }

}
