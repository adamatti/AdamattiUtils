package adamatti;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
//import org.apache.log4j.Logger;
public abstract class IOUtil {
	public static String read(URL url) throws Exception{
		URLConnection conn = url.openConnection();
		return read(conn);
	}
	public static String read(URL url,String user, String pass) throws Exception{
		URLConnection conn = url.openConnection();
		String encoding = new sun.misc.BASE64Encoder().encode((user + ":" + pass).getBytes());
		conn.setRequestProperty ("Authorization", "Basic " + encoding);
		return read(conn);
	}
	public static String read(URLConnection conn) throws Exception{
		InputStream is = conn.getInputStream();
		InputStreamReader reader = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(reader);
		String aux = read(br);
		br.close();
		reader.close();
		is.close();		
		return aux;
	}
	public static String read(String path) throws Exception{
		File file = new File(path);
		return read(file);
	}
	public static String read(File file) throws Exception{
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String aux = read(br);
		br.close();
		fr.close();
		return aux;
	}
	private static String read(BufferedReader br) throws Exception {
		StringBuffer sb = new StringBuffer();
		String line;
		while ( (line = br.readLine())!=null ){
			sb.append(line+"\n");
		}
		return sb.toString();
		
	}
	public static String read(String path,String regex) throws Exception{
		Pattern p = Pattern.compile(regex);
		
		StringBuffer sb = new StringBuffer();
		File file = new File(path);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line;
		while ( (line = br.readLine())!=null ){
			if (p.matcher(line).find())
				sb.append(line+"\n");
		}
		br.close();
		fr.close();
		return sb.toString();
	}
	public static String readTrim(String path) throws Exception{
		StringBuffer sb = new StringBuffer();
		File file = new File(path);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line;
		while ( (line = br.readLine())!=null ){
			sb.append(line.trim());
		}
		br.close();
		fr.close();
		return sb.toString();
	}
	public static void write(String folder, String file, String text) throws Exception{
		File f = new File(folder);		
		write(f,file,text);		
	}
	public static void write(File folder, String file, String text) throws Exception{		
		folder.mkdirs();
		File f = new File(folder, file);
		write(f,text);		
	}
	public static void write(String path, String text) throws Exception{
		File file = new File(path);
		write(file,text);		
	}
	public static void write(File file, String text) throws Exception{		
		FileWriter fw = new FileWriter(file);
		fw.write(text);
		fw.close();
	}
	/**
	 * Download a file
	 * @param file
	 * @param url
	 * @throws Throwable
	 */
	public static void write(File file, URL url) throws Throwable {
		URLConnection conn = url.openConnection();
		InputStream is = conn.getInputStream();
		
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
		
		byte data[] = new byte[1024];
		int count;
		while( (count = is.read(data,0,1024)) != -1)
			bout.write(data,0,count);
		is.close();
		bout.close();
		fos.close();
	}
	public static void unzip(File source) throws Throwable {
		final int BUFFER = 2048;
		FileInputStream fis = new FileInputStream(source);
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			int count;
			byte data[] = new byte[BUFFER];
			File file = new File(source.getParent(),entry.getName());
			if (entry.isDirectory()) {
				file.mkdirs();
			} else {
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = zis.read(data, 0, BUFFER))!= -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
			}
		}
		zis.close();
		fis.close();
	}
	public static void waitEnter(String msg) throws Exception{
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		System.out.println(msg);
		br.readLine();
		//br.close();
	}
	public static void waitEnter() throws Exception{
		waitEnter("Press Enter to exit");
	}
}
