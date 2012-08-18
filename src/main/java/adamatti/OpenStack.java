package adamatti;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.QueueReceiver;
import javax.jms.Session;
import javax.jms.TopicSubscriber;
import javax.naming.Context;

import org.apache.log4j.Logger;
public class OpenStack {
	private static boolean flagLog = true;
	private Logger log = Logger.getLogger(this.getClass());
	private List objects = new ArrayList();
	public OpenStack(){	}
	public void add(Object [] o){
		objects.addAll(Arrays.asList(o));
		//Collections.addAll(objects,o);
	}
	public Object add(Object o){		
		objects.add(o);
		if (flagLog)
			log.debug("Added to stack: " + o.getClass());		
		return o;
	}
	public void closeAll(){
		if (flagLog)
			log.debug("Closing objects");
		ListIterator i = objects.listIterator(objects.size());		
		while(i.hasPrevious()){
			Object o = i.previous();
			try {
				if (flagLog)
					log.debug("Closing " + o.getClass());
				if (o instanceof TopicSubscriber){
					close((TopicSubscriber)o);
				} else if (o instanceof Connection){
					close ((Connection)o);
				} else if (o instanceof Session){
					close ((Session)o);
				} else if (o instanceof Context){
					close ((Context)o);
				} else if (o instanceof QueueReceiver){
					close ((QueueReceiver)o);
				} else if (o instanceof MessageProducer){
					close ((MessageProducer)o);
				} else if (o instanceof OpenStack){
					close ((OpenStack)o);
				} else {
					log.warn("Close not implemented: " + o.getClass());
				}				
			}catch (Throwable t){
				log.error("Error closing " + o.getClass() + ": " + t.getMessage(),t);
			}
		}
		objects.clear();
	}
	private void close(OpenStack stack) {
		stack.closeAll();
	}
	private void close (MessageProducer producer) throws Exception{
		producer.close();
	}
	private void close(Context ctx) throws Exception{
		ctx.close();
	}
	private void close(TopicSubscriber subscriber) throws Exception{
		subscriber.close();
	}
	private void close(QueueReceiver receiver) throws Exception{
		receiver.close();
	}
	private void close(Connection conn) throws Exception{
		conn.close();
	}
	private void close(Session session) throws Exception{
		session.close();
	}
	public static void setFlagLog(boolean flag) {
		flagLog = flag;
	}	
}
