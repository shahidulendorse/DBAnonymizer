package eu.fiware.security.dbanonymizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.transport.http.HTTPConduit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import eu.fiware.security.dbanonymizer.mySQL.MySQLConnection;
//import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;




import eu.fiware.security.dbanonymizer.entities.policyproposal.PolicyProposalResult;
import eu.fiware.security.dbanonymizer.log.MyLogger;
import eu.fiware.security.dbanonymizer.mySQL.MySQLQueryExecutor_Util;
import eu.fiware.security.dbanonymizer.mySQL.MySQLQueryFactory;
import eu.fiware.security.dbanonymizer.ws.DbAnonymizerJAX_RS;

//import eu.fiware.security.dbanonymizer.entities.TransferredFile;

/**
 * JUNit Test for DbAnonymizer_JAX_RS implementation.
 *
 * @author Francesco Di Cerbo
 */
public class DbAnonymizerJAX_RS_Impl_DeepSearchTest_IT {


	
	private static String endpointUrl;
	private static boolean debug = true;
	
	MyLogger logger = MyLogger.getInstance();

	
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
	

	
	
	@Test
	public final void testComputeDeepSearchAnalysis() {
		
		// end point creation
		
		String address = endpointUrl + "/DBA/evaluateDeepSearch";

		WebClient client = WebClient.create(address);
		client.type("multipart/mixed").accept("text/xml");		
		
		 /**
		  * retrieval of resources needed to invoke requests
		  * this strange procedure is aiming at retrieving
		  * the full and exact path of "src/test/resources"
		  */
		 
        String path = TestUtilities.getTemporaryFilePath();
        
        
        String privacyFileName = path + "/src/test/resources/policy_deepsearch_unittest.xml";
        
//        String dbDumpFileName  = path + "/src/test/resources/Census_20090715_1025.zip";
        String dbDumpFileName  = path + "/src/test/resources/deepsearch_unittest.zip";
        
       
//        Map<String, Object> objects = new LinkedHashMap<String, Object>();
        try {
        	
//        	InputStream is1 = new FileInputStream(new File(privacyFileName));
//        	InputStream is2 = new FileInputStream(new File(dbDumpFileName));
        
//        	objects.put(MediaType.APPLICATION_OCTET_STREAM, is1);
//        	objects.put(MediaType.APPLICATION_OCTET_STREAM, is2);
        	
        	LinkedList<Attachment> attsList = new LinkedList<Attachment>();
    	
    		attsList.add(TestUtilities.createAttachmentFromFileURI(privacyFileName, "policyFile", MediaType.APPLICATION_OCTET_STREAM));
    		attsList.add(TestUtilities.createAttachmentFromFileURI(dbDumpFileName,"dbDump", MediaType.APPLICATION_OCTET_STREAM));
    		attsList.add(new Attachment("maxRisk", MediaType.TEXT_PLAIN, "0.3"));
    		
    		String returnString = client.postCollection(attsList, Attachment.class, String.class);

    		if (debug) 
    			System.err.println("result is : "+ returnString);
    		
    		long gid = -1;
    		
    		try {
    			gid = TestUtilities.getGIDfromResult(returnString);
    		} catch (Exception e) {
    			e.printStackTrace();
    			assertTrue(false);
    		}
    		
        	Thread.sleep(5000);
        	
        	String resultString = testInnergetDeepSearchResult(gid);
        	
        	File referenceFile = new File(TestUtilities.getTemporaryFilePath()
					+ File.separator + "src" + File.separator + "test"
					+ File.separator + "resources" + File.separator
					+ "deepsearch_unittest_result.xml");
        	
        	boolean resultComp = TestUtilities.checkIsEqual(resultString, referenceFile);

        	System.out.println("Request GID is: "+gid);
        	
			if (debug)
//        	if(true)
				System.err.println("Test result: "+resultString + " " + "length: "+resultString.length());
        	
			assertTrue(resultComp);
			

        	
			System.out.println("Received deepSearch output is correct.");
			
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.writeLog(Level.INFO, "problem");
        	logger.writeLog(Level.INFO, e.getMessage());
        	assertEquals(1, 0);
		}
     
	}
	

	/**
	 * @param gid
	 * @return
	 */
	public String testInnergetDeepSearchResult(long gid) {
		
		
		DbAnonymizerJAX_RS dbImpl = (DbAnonymizerJAX_RS) 
				TestUtilities.setupRSClient(endpointUrl, DbAnonymizerJAX_RS.class);

					
		Response response = dbImpl.getDeepSearchkResult(String.valueOf(gid), -1, -1);
				
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


	
	
		
}
