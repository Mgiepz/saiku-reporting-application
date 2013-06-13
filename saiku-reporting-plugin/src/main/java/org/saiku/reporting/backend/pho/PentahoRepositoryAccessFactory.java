//package org.saiku.reporting.backend.pho;
//
//import org.springframework.beans.factory.FactoryBean;
//
//import pt.webdetails.cpf.repository.PentahoRepositoryAccess;
//import pt.webdetails.cpf.session.IUserSession;
//import pt.webdetails.cpf.session.PentahoSession;
//
//public class PentahoRepositoryAccessFactory  implements FactoryBean {
//
//
//	public Object getObject() throws Exception {
//
//		PentahoRepositoryAccess repo = new PentahoRepositoryAccess();
//		
//		IUserSession userSession = new PentahoSession(PentahoSessionManager.getSessionData());
//		repo.setUserSession(userSession);
//		
//		return repo;
//	}
//
//	public Class<PentahoRepositoryAccess> getObjectType() {
//		return PentahoRepositoryAccess.class;
//	}
//
//	public boolean isSingleton() {
//		return false;
//	}
//
//}
