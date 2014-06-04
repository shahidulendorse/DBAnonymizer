package eu.fiware.security.dbanonymizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.transport.http.HTTPConduit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.fiware.security.dbanonymizer.ws.DbAnonymizerJAX_RS;

//import eu.fiware.security.dbanonymizer.log.MyLogger;

//import eu.fiware.security.dbanonymizer.entities.TransferredFile;

/**
 * JUNit Test for DbAnonymizer_JAX_RS implementation.
 *
 * @author Francesco Di Cerbo
 */
public class DbAnonymizerJAX_RS_Impl_RiskColumnTestIT {

//	/**
//	 * size of the transmission buffer to be used for throughput-limited tests  
//	 */
//	private static int BUFFER = 100000;
	
	private static String endpointUrl;
	private static boolean debug = false;
	
//	MyLogger logger = MyLogger.getInstance();

	
	@BeforeClass
	public static void beforeClass() {
		endpointUrl = System.getProperty("service.url");
		if (endpointUrl == null) {
			endpointUrl = "http://localhost:8080/eu.fiware.security.dbanonymizer/";
		}
		
		endpointUrl += "/services";
		
		boolean confDebug = true; 
		try { 
			confDebug = Boolean.valueOf(System.getProperty("integrationtest.debug"));
		} catch (Exception e) {
			// nothing to do, we leave it set to "false"
		}
		
		debug = confDebug;
		
	}
	

	/**
	 * 
	 * @param uri
	 * @param name
	 * @param contentType can be null
	 * @return
	 * @throws Exception
	 */
	private Attachment createAttachmentFromFileURI(String uri, String name, String contentType) throws Exception {
        
//		InputStream is1 = 
//            getClass().getResourceAsStream(uri);
		
        File aFile = new File(uri);
		
		InputStream is1 = new FileInputStream(aFile);
		
        

        
        /**
         * TODO check whether the ContentDisposition is OK or it is better:
         * new ContentDisposition("form-data;filename="+aFile.getName());
         * 
         * here or in the following request
         */
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
	private Object setupRSClient(Class whichClass) {
		
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



	/**
	 * @param gid
	 * @return
	 */
	private String testInnergetComputationRiskColumnResult(long gid) {
		
		
		DbAnonymizerJAX_RS dbImpl = (DbAnonymizerJAX_RS) 
				setupRSClient(DbAnonymizerJAX_RS.class);

					
		Response response = dbImpl.getColumnRiskResult(String.valueOf(gid));
		
		String result = "";
		
		BufferedReader reader = null;
		
		reader = new BufferedReader(new InputStreamReader((InputStream)response.getEntity()));
		
		StringBuilder sb = new StringBuilder();
		
		String line ;
		
		
		try {
			while ((line = reader.readLine()) != null)	{
				sb.append(line);          
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		result = sb.toString();
		
		if (debug) 
			System.err.println(result);
		
		return result;
		
	}

	
	
	@Test
	public final void testComputeRiskColumns() {
		
		// end point creation
		
		String address = endpointUrl + "/DBA/evaluateColumnRisk";

		WebClient client = WebClient.create(address);
		client.type("multipart/mixed").accept("text/xml");		
		
		 /**
		  * retrieval of resources needed to invoke requests
		  * this strange procedure is aiming at retrieving
		  * the full and exact path of "src/test/resources"
		  */
		 
		 		
		String testFileName = "test.txt";
		
		File flag = new File(testFileName);
		
        String path = flag.getAbsolutePath().substring(0, flag.getAbsolutePath().indexOf(testFileName));

        flag.delete();        
        
//        String dbDumpFileName  = path + "/src/test/resources/Census_20090715_1025.zip";
        String dbDumpFileName  = path + "/src/test/resources/census_small-dump.zip";
        
       
        Map<String, Object> objects = new LinkedHashMap<String, Object>();
        try {
        
        	InputStream is2 = new FileInputStream(new File(dbDumpFileName));
        
        	objects.put(MediaType.APPLICATION_OCTET_STREAM, is2);
        	
        	LinkedList<Attachment> attsList = new LinkedList<Attachment>();
    	
    		attsList.add(createAttachmentFromFileURI(dbDumpFileName,"dbDump", MediaType.APPLICATION_OCTET_STREAM));
    		
    		String returnString = client.postCollection(attsList, Attachment.class, String.class);

    		if (debug) 
    			System.err.println("result is : "+ returnString);
    		
    		long gid = -1;
    		
    		try {
    			gid = getGIDfromResult(returnString);
    		} catch (Exception e) {
    			e.printStackTrace();
    			assertTrue(false);
    		}
    		
        	Thread.sleep(10000);
        	
        	String resultString = testInnergetComputationRiskColumnResult(gid);

			File referenceFile = new File(getTemporaryFilePath()
					+ File.separator + "src" + File.separator + "test"
					+ File.separator + "resources" + File.separator
					+ "3239730322580660745.xml");
			
			boolean resultComp = TestUtilities.checkIsEqual(resultString, referenceFile);
			
			if (debug) {
				System.err.println(resultString + " " + "length: "+resultString.length());
				System.err.println(resultString + " " + "hash: "+resultString.hashCode());
			}
			
			assertTrue(resultComp);
			// 2141 is the full size of the expected result
			assertTrue(resultString.length() == 2141);
			
			System.out.println("Received riskColumnRisk output is correct.");
        	
        } catch (Exception e) {
        	e.printStackTrace();
//        	logger.writeLog(Level.INFO, "problem");
//        	logger.writeLog(Level.INFO, e.getMessage());
        	assertEquals(1, 0);
		}
     
	}
	
	/**
	 * it is necessary to create a result with standard unit tests (mvn test) ,
	 * that can be used later for mvn integration-test
	 * at that point, it is possible to enable the @ Test
	 */
//	@Test
	public final void testgetRiskColumnResult() {

		long gid = 124L;
		
		assertTrue(testInnergetComputationRiskColumnResult(gid).length() > 0);
		
	}
	
	public long getGIDfromResult(String result)
			throws ParserConfigurationException, SAXException, IOException, NumberFormatException  {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder
				.parse(new InputSource(new StringReader(result)));
		doc.getDocumentElement().normalize();

		NodeList list = doc.getElementsByTagName("RequestID");
		
		if (debug) 
			System.err.println(list.item(0).getTextContent());
		
		if (list.getLength() == 1) {
			String tmp = list.item(0).getTextContent();
			return Long.parseLong(tmp);
		} else {
			return -111;
		}

	}
	

	
	/**
	 * @param testFileName
	 * @return
	 */
	private String getTemporaryFilePath() {
		
		String testFileName = "test.txt";
		
		File flag = new File(testFileName);
		
        String path = flag.getAbsolutePath().substring(0, flag.getAbsolutePath().indexOf(testFileName));

        flag.delete();
		return path;
	}

}
