package adamatti;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
public abstract class WLUtils {
	public static final String INITIAL_CONTEXT_FACTORY = "weblogic.jndi.WLInitialContextFactory";
	private static Map mapCtx = new HashMap();
	public static Context getEnv(String url,String user, String pass) throws Exception{		
		if (!mapCtx.containsKey(url)){
			Hashtable map = new Hashtable();
			map.put(Context.PROVIDER_URL, url);
			map.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
			map.put(Context.SECURITY_PRINCIPAL,user);
			map.put(Context.SECURITY_CREDENTIALS, pass);
			Context ctx = new InitialContext(map);
			mapCtx.put(url, ctx);
		}
	    return (Context)mapCtx.get(url);
	}
	public static Context getEnv(String host, int port,String user, String pass) throws Exception{
		String url = "t3://" + host + ":" + port;
		return getEnv(url,user,pass);
	}
	public static void close() throws Exception{
		Iterator i = mapCtx.values().iterator();
		while(i.hasNext()){
			Context ctx = (Context) i.next();
			ctx.close();
		}
		mapCtx = new HashMap();
	}
}
