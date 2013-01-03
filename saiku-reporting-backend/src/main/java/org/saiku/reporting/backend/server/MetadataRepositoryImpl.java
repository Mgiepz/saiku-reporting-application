package org.saiku.reporting.backend.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.saiku.reporting.backend.messages.Messages;
import org.saiku.reporting.backend.objects.dto.ReportTemplate;
import org.saiku.reporting.backend.objects.metadata.MetadataDtoMapper;
import org.saiku.reporting.backend.objects.metadata.impl.MetadataModel;
import org.springframework.beans.factory.annotation.Autowired;

public class MetadataRepositoryImpl extends AbstractMetadataRepository {

    private Log log = LogFactory.getLog(MetadataRepositoryImpl.class);
    
    @Autowired
    private MemoryBasedMetadataDomainRepository mbr;

    public MetadataRepositoryImpl() {
        super();
    }

    @Override
    public LogicalModel getLogicalModel(String domainId, String modelId) {
        return mbr.getImmdr().getDomain(domainId).findLogicalModel(modelId);
    }

    @Override
    public Domain getDomain(String domainId) {
        return mbr.getImmdr().getDomain(domainId);
    }

    @Override
    public MetadataModel loadModel(String domainId, String modelId) {

        if (domainId == null) {
            log.error(Messages.getErrorString("MetadataService.ERROR_0003_NULL_DOMAIN")); //$NON-NLS-1$
            return null;
        }

        if (modelId == null) {
            log.error(Messages.getErrorString("MetadataService.ERROR_0004_NULL_Model")); //$NON-NLS-1$
            return null;
        }

        Domain domain = mbr.getImmdr().getDomain(domainId);
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
     * Returns a instance of the IMetadataDomainRepository for the current session
     *
     * @return
     */
    public IMetadataDomainRepository getMetadataDomainRepository() {

        return mbr.getImmdr();

    }

    public ReportTemplate[] loadTemplates() {
    	//TODO:
    	return null;

    }

}
