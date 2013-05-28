package org.saiku.reporting.backend.pho;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.saiku.reporting.backend.messages.Messages;
import org.saiku.reporting.backend.objects.dto.ReportTemplate;
import org.saiku.reporting.backend.objects.metadata.MetadataDtoMapper;
import org.saiku.reporting.backend.objects.metadata.impl.MetadataModel;
import org.saiku.reporting.backend.server.AbstractMetadataRepository;

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

	public ReportTemplate[] loadTemplates() {
		// TODO Auto-generated method stub
		return null;
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

}
