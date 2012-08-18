package adamatti;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public abstract class XMLUtils {
	public static NodeList getList(Node node,String xpathString) throws Exception{
		XPathFactory factory = new org.apache.xpath.jaxp.XPathFactoryImpl();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile(xpathString);
		NodeList result = (NodeList) expr.evaluate(node, XPathConstants.NODESET);
		return result;
	}
	
	public static boolean getBool(Node node,String xpathString) throws Exception{
		String aux =getString(node,xpathString);
		return aux.toUpperCase().equals("TRUE");
	}
	public static long getLong(Node node,String xpathString) throws Exception{
		String aux = getString(node,xpathString);
		return Long.parseLong(aux + "0");
	}
	public void print(Node node) throws Exception{
		System.out.println(toString(node));
	}
	public static String getString(Node node,String xpathString) throws Exception{		
		String aux = "";
		try {
			NodeList result = getList(node,xpathString);
			aux = result.item(0).getNodeValue();
		} catch (NullPointerException e){
			//log("NullPointer");			
		}
		//log(xpathString + " = " + aux);
		return aux;
	}
	public static Document getDocument(String file) throws Throwable{
		return getDocument(new File(file));
	}
	public static Document getDocument(File file) throws Throwable{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();		
		DocumentBuilder builder = factory.newDocumentBuilder();			
		Document doc = builder.parse(file);
		return doc;
	}
	public static Node copy(Node doc,String source, String target) throws Throwable{
		Node t = getList(doc,target).item(0);
		NodeList srcList = getList(doc,source);
		
		for (int i=0;i<srcList.getLength();i++){
			Node node = srcList.item(i);
			t.appendChild(node);
		}
		return getRoot(t);
	}
	public static Node getRoot(Node node){
		while (node.getParentNode()!=null)
			node = node.getParentNode();
		return node;
	}
	public static Node remove(Node root, String xpathString) throws Throwable{
		Node node = getList(root,xpathString).item(0);
		
		Node p = node.getParentNode();
		p.removeChild(node);
		return getRoot(p);
	}
	public static Node toXML(String text) throws Exception{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();		
		DocumentBuilder builder = factory.newDocumentBuilder();
		StringReader reader = new StringReader( text );
		InputSource is = new InputSource( reader );
		Node doc = builder.parse(is);
		reader.close();
		return doc;
	}
	public static String toString(Node doc) throws Exception{
		DOMSource domSource = new DOMSource(doc);
		StreamResult result = new StreamResult(new StringWriter());
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer serializer = tf.newTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
		//serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"users.dtd");
		serializer.setOutputProperty(OutputKeys.INDENT,"yes");
		serializer.transform(domSource, result);
		return result.getWriter().toString();
	}
}
