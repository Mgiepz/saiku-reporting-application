//package org.saiku.reporting.backend.server;
//import java.util.Hashtable;
//
//import javax.naming.Context;
//import javax.naming.InitialContext;
//import javax.naming.NameClassPair;
//import javax.naming.NamingEnumeration;
//import javax.naming.NamingException;
//
//public class JNDITree {
//	private Context context = null;
//
//	public static void main(String[] args) throws Exception {
//		new JNDITree().printJNDITree("");
//		System.out.println("DONE");
//	}
//
//	public JNDITree() throws NamingException {
//		setEnv();
//	}
//
//	/* Please modify this method or comment and use jndi.properties
//	 */
//	public void setEnv() throws NamingException {
//		Hashtable env = new Hashtable();
//		//OC4J
//		//  env.put(Context.INITIAL_CONTEXT_FACTORY, "com.evermind.server.rmi.RMIInitialContextFactory");
//		//  env.put(Context.PROVIDER_URL, "ormi://172.16.x.x:12404");
//		//  env.put(Context.SECURITY_PRINCIPAL, "admin");
//		//  env.put(Context.SECURITY_CREDENTIALS, "welcome");
//		//JBOSS
//		//  env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
//		//  env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
//		//  env.put(Context.PROVIDER_URL, "jnp://172.16.x.x:1099");
//		//WEBLOGIC
//		//  env.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
//		//  env.put(Context.PROVIDER_URL, "t3://172.16.x.x:7001");
//
//
//		context = new InitialContext(env);
//	}
//
//	public void printJNDITree(String ct)   {
//		try {
//			printNE(context.list(ct), ct);
//		} catch (NamingException e) {
//			//ignore leaf node exception
//		}
//	}
//
//	private void printNE(NamingEnumeration ne, String parentctx) throws NamingException {
//		while (ne.hasMoreElements()) {
//			NameClassPair next = (NameClassPair) ne.nextElement();
//			printEntry(next);
//			increaseIndent();
//			printJNDITree((parentctx.length() == 0) ? next.getName() : parentctx + "/" + next.getName());
//			decreaseIndent();
//		}
//	}
//
//	private void printEntry(NameClassPair next) {
//		System.out.println(printIndent() + "-->" + next);
//	}
//
//
//	private int indentLevel = 0;
//
//	private void increaseIndent() {
//		indentLevel += 4;
//	}
//
//	private void decreaseIndent() {
//		indentLevel -= 4;
//	}
//
//	private String printIndent() {
//		StringBuffer buf = new StringBuffer(indentLevel);
//		for (int i = 0; i < indentLevel; i++) {
//			buf.append(" ");
//		}
//		return buf.toString();
//	}