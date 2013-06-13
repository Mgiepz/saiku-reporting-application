//package org.saiku.reporting.backend.pho;
//
//import org.pentaho.platform.api.engine.IPentahoSession;
//
//public class PentahoSessionManager {
//
//	private static final ThreadLocal<IPentahoSession> threadLocal;
//
//	static {
//
//		threadLocal = new ThreadLocal<IPentahoSession>();
//
//	}
//
//	public static synchronized IPentahoSession getSessionData() {
//
//		return threadLocal.get();
//
//	}
//
//	public static synchronized void setSessionData(IPentahoSession session) {
//
//		threadLocal.set(session);
//
//	}
//	
//}
