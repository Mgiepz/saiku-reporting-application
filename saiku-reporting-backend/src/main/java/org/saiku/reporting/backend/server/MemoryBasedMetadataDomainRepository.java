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
package org.saiku.reporting.backend.server;

import java.io.InputStream;
import java.util.UUID;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.repository.InMemoryMetadataDomainRepository;
import org.pentaho.metadata.util.XmiParser;
import org.springframework.beans.factory.annotation.Autowired;


public class MemoryBasedMetadataDomainRepository {

    InMemoryMetadataDomainRepository immdr;
    
    private String metadataFile;
    
//	@Autowired
//	private IRepositoryAccess repositoryAccess;
	
//	@Autowired
	//private IContentAccessFactory contentAccessFactory;

    public MemoryBasedMetadataDomainRepository() {

    }
    
    public MemoryBasedMetadataDomainRepository(String metadataFile) {
        createDomain(metadataFile);
    }
 
    public InMemoryMetadataDomainRepository getImmdr() {
    	return immdr;
    }

    private void createDomain(String metadataFile) {
    	
    	this.metadataFile = metadataFile;
        immdr = new InMemoryMetadataDomainRepository();

        final XmiParser parser = new XmiParser();
        Domain domain = null;
        InputStream in = null;
        
        try {
     	
//        	contentAccessFactory.ge
//        	
//        	in = repositoryAccess.getResourceInputStream("metadata.xmi", FileAccess.READ);
//        	
//            domain = parser.parseXmi(in);
//            domain.setId(UUID.randomUUID().toString() + "/" + "metadata.xmi");
//            immdr.storeDomain(domain, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    	throw new RuntimeException("Does not work at the moment!");
    	
    }

	public String getMetadataFile() {
		return metadataFile;
	}

	public void setMetadataFile(String metadataFile) {
		createDomain(metadataFile);
	}
}
