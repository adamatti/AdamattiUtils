package adamatti;
import java.io.File;
import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import org.apache.log4j.Logger;

import weblogic.jms.common.BytesMessageImpl;
public abstract class JMSUtils {	 
	private static Logger log = Logger.getLogger(JMSUtils.class);
	public static void send(Context ctx,String factoryName,String destName,Serializable msg) throws Exception{		
		ConnectionFactory factory = (ConnectionFactory) ctx.lookup(factoryName);
		Destination destination = (Destination) ctx.lookup(destName);
		if (destination instanceof Topic){
			send((TopicConnectionFactory) factory,(Topic)destination,msg);
		} else if (destination instanceof Queue){
			send((QueueConnectionFactory) factory,(Queue)destination,msg);	    
		}		
	}
	private static void send(TopicConnectionFactory tcf,Topic topic,Serializable o) throws Exception{
		OpenStack stack = new OpenStack();
		try {
			TopicConnection conn = tcf.createTopicConnection();
			stack.add(conn);
			TopicSession session = conn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			stack.add(session);
			conn.start();
			TopicPublisher publisher = session.createPublisher(topic);
			stack.add(publisher);			
			publisher.publish(createMessage(session, o));
			log.debug("Msg sent to topic");
		} catch (InvalidDestinationException e){
			//WRONG SELECTION
			send((QueueConnectionFactory) tcf,(Queue)topic,o);
		} catch (Exception e){
			log.error("Error: " + e.getMessage(),e);
			throw e;
		} finally {
			stack.closeAll();
		}
	}
	/**
	 * Centralize creation of messages
	 */
	private static Message createMessage(Session session, Serializable o) throws Exception{
		Message msg = null;
		if (o instanceof File) {
			log.debug("Read file " + ((File)o).getName());
			o = IOUtil.read(o.toString());
			msg = session.createTextMessage(o.toString());
		} else if (o instanceof String) {
			msg = session.createTextMessage(o.toString());
		} else {
			//msg = session.createBytesMessage();msg.writeObject(o);
			ObjectMessage om = session.createObjectMessage();
			om.setObject(o);
			msg = om;
		}	    
	    return msg;
	}
	private static void send(QueueConnectionFactory qcf,Queue queue,Serializable o) throws Exception{
		OpenStack stack = new OpenStack();
		try {	    	
			QueueConnection conn = qcf.createQueueConnection();
			stack.add(conn);
		    QueueSession session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		    stack.add(session);
		    conn.start();
		    QueueSender sender = session.createSender(queue);
		    stack.add(sender);		    
		    sender.send(createMessage(session,o));
		    log.debug("Msg sent to queue");
	    } catch (InvalidDestinationException e){
	    	//WRONG SELECTION
	    	send((TopicConnectionFactory) qcf,(Topic)queue,o);
	    } catch (JMSException jms){
	    	Exception e = jms.getLinkedException()!=null?jms.getLinkedException():jms;	    	
	    	log.error("Error: " + e.getMessage(),e);
	    	throw e;
	    } catch (Exception e){
	    	log.error("Error: " + e.getMessage(),e);
	    	throw e;
	    } finally {
	    	stack.closeAll();
	    }
	}
	public static OpenStack addListener(Context ctx,String factoryName,String destinationName,MessageListener listener) throws Exception{
		OpenStack o = addListener(ctx,factoryName,new String[]{destinationName},listener);
		return o;
	}
	public static OpenStack addListener(Context ctx,String factoryName,String [] destinationNames,MessageListener listener) throws Exception{
		Object o = ctx.lookup(factoryName);
		if (o instanceof TopicConnectionFactory){
			TopicConnectionFactory tcf = (TopicConnectionFactory)o;
			return addListener(ctx,tcf,destinationNames,listener);
		}
		QueueConnectionFactory qcf = (QueueConnectionFactory) o;
		return addListener(ctx,qcf,destinationNames,listener);
	}
	private static OpenStack addListener(Context ctx,QueueConnectionFactory qcf,String [] queueNames,MessageListener listener) throws Exception{
		OpenStack stack = new OpenStack();		
		QueueConnection conn = qcf.createQueueConnection();
		stack.add(conn);
		if (listener instanceof ExceptionListener)
			conn.setExceptionListener((ExceptionListener)listener);
		QueueSession session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		stack.add(session);
		for (int i=0;i<queueNames.length;i++){
			try {
				Queue queue = (Queue) ctx.lookup(queueNames[i]);
				QueueReceiver receiver = session.createReceiver(queue);
				receiver.setMessageListener(listener);
				stack.add(receiver);
			}catch (javax.naming.NameNotFoundException e){
				log.error("Unable to connect to " + queueNames[i]);
			}
		}
		conn.start();		
		return stack;
	}
	private static OpenStack addListener(Context ctx,TopicConnectionFactory tcf,String [] topicNames,MessageListener listener) throws Exception{
		OpenStack stack = new OpenStack();
		TopicConnection conn = tcf.createTopicConnection();
		stack.add(conn);
		if (listener instanceof ExceptionListener)
			conn.setExceptionListener((ExceptionListener)listener);
		TopicSession session = conn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		stack.add(session);
		for (int i=0;i<topicNames.length;i++){
			try {
				Topic topic = (Topic) ctx.lookup(topicNames[i]);
				TopicSubscriber subscriber = session.createSubscriber(topic);
				subscriber.setMessageListener(listener);
				stack.add(subscriber);
			}catch (javax.naming.NameNotFoundException e){
				log.error("Unable to connect to " + topicNames[i]);
				e.printStackTrace();
			}
		}
		conn.start();
		return stack;
	}
	public static String getString(Message msg){
		try {
			if (msg instanceof TextMessage) {
				return ((TextMessage)msg).getText();
			} else if (msg instanceof BytesMessage){
				BytesMessageImpl bm = new BytesMessageImpl((BytesMessage)msg );
				int length = (int)bm.getBodyLength();
				byte [] bytes = new byte[length];
				for ( int i = 0; i < length; i++ )
					bytes[i] = bm.readByte();
				String aux = new String(bytes);
				//SOLVE "Content is not allowed in prolog" ISSUE
				aux = aux.trim().replaceFirst("^(.*)<","<");
				return aux;
			}
		} catch (Throwable t){
			log.error("Error: " + t.getMessage(),t);
		}
		return "";
	}
}
