package eu.fiware.security.dbanonymizer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.logging.Level;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.transport.http.HTTPConduit;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TestUtilities {

	
	
	public static boolean checkIsEqual(String resultString, File referenceFile) {

		boolean resultComp = false ;
		
		try {

//			File tmpFileResult = File.createTempFile("tmpComp", "");

//			System.err.println("Temporary file name is: "+tmpFileResult.getAbsolutePath());

//			FileUtils.writeStringToFile(tmpFileResult, resultString);

			StringReader tmpfileResultS = new StringReader(
					resultString);
//			
//			tmpfileResultS.
			
			FileReader referenceResult = new FileReader(referenceFile);
			
			
//			FileInputStream referenceResult = new FileInputStream(
//					referenceFile);
//			FileInputStream tmpfileResultS = new FileInputStream(
//					tmpFileResult);

//			InputStream tmpfileResultS = IOUtils.toInputStream(resultString);
			
			resultComp = IOUtils
					.contentEqualsIgnoreEOL(tmpfileResultS, referenceResult);

			IOUtils.closeQuietly(tmpfileResultS);
			IOUtils.closeQuietly(referenceResult);
			
//			tmpFileResult.delete();

		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(1, 0);
		} 

		return resultComp;
	}
	
	public static String getTemporaryFilePath() {
		
		String testFileName = "test.txt";
		
		File flag = new File(testFileName);
		
        String path = flag.getAbsolutePath().substring(0, flag.getAbsolutePath().indexOf(testFileName));

        flag.delete();
		return path;
	}
	
	public static long getGIDfromResult(String result)
			throws ParserConfigurationException, SAXException, IOException, NumberFormatException  {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder
				.parse(new InputSource(new StringReader(result)));
		doc.getDocumentElement().normalize();

		NodeList list = doc.getElementsByTagName("RequestID");
		
//		if (debug) 
//			System.err.println(list.item(0).getTextContent());
		
		if (list.getLength() == 1) {
			String tmp = list.item(0).getTextContent();
			return Long.parseLong(tmp);
		} else {
			return -111;
		}

	}
	
	/**
	 * 
	 * @param uri
	 * @param name
	 * @param contentType can be null
	 * @return
	 * @throws Exception
	 */
	public static Attachment createAttachmentFromFileURI(String uri, String name, String contentType) throws Exception {
        		
        File aFile = new File(uri);
		
		InputStream is1 = new FileInputStream(aFile);
		
		ContentDisposition cd = new ContentDisposition("form-data;name=\""+name+";filename=\""+aFile.getName()+"\"");
        
        MultivaluedMap<String, String> headers = new MetadataMap<String, String>();
//        headers.putSingle("Content-ID", "image");
        headers.putSingle("Content-Disposition", cd.toString());
        headers.putSingle("Content-Location", "http://host/bar");
//        headers.putSingle("custom-header", "custom");
        
        if (contentType != null) {
        	headers.putSingle("Content-Type", contentType);
        }
        
        Attachment att = new Attachment(is1, headers);
        
        return att;
        
    }

	/**
	 * Method used by the RS tests for setup the right RS client (DbAnonymizerJAX_RS_Simple or DbAnonymizerJAX_RS)
	 * @param whichClass
	 * @return
	 */
	public static Object setupRSClient(String endpointUrl, Class whichClass) {
		
		String address = endpointUrl + "/DBA";

		JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
		bean.setAddress(address);
		bean.setProperties(Collections.singletonMap(org.apache.cxf.message.Message.MTOM_ENABLED, 
				(Object)"true"));

		try {
			Class.forName("org.apache.cxf.jaxrs.provider.JAXBElementProvider");
		} catch (Exception e) {
			e.printStackTrace();
			assert(false);
		}

		WebClient client = bean.createWebClient();

		HTTPConduit conduit = WebClient.getConfig(client).getHttpConduit();
		conduit.getClient().setReceiveTimeout(1000000);
		conduit.getClient().setConnectionTimeout(1000000);

		//client.accept("text/plain");
		client.type("multipart/form-data").accept("text/plain");
	
		Object dbImpl = JAXRSClientFactory.fromClient(client, whichClass);
		return dbImpl;
	}
	
}
