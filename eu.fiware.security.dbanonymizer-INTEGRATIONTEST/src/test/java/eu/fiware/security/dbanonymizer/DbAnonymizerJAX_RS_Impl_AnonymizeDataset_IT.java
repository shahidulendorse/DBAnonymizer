package eu.fiware.security.dbanonymizer;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.logging.Level;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.fiware.security.dbanonymizer.log.MyLogger;
import eu.fiware.security.dbanonymizer.ws.DbAnonymizerJAX_RS;

public class DbAnonymizerJAX_RS_Impl_AnonymizeDataset_IT {


	
	private static String endpointUrl;
	private static boolean debug = true;
	
	MyLogger logger = MyLogger.getInstance();

	
	@BeforeClass
	public static void beforeClass() {
		endpointUrl = System.getProperty("service.url");
		if (endpointUrl == null) {
			endpointUrl = "http://localhost:8080/eu.fiware.security.dbanonymizer";
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
	public final void testAnonymizeDatasetMultipartBody() {
			
			// end point creation
			
			String address = endpointUrl + "/DBA/anonymizeDataset";

			WebClient client = WebClient.create(address);
			client.type("multipart/mixed").accept("text/xml");
			
			 /**
			  * retrieval of resources needed to invoke requests
			  * this strange procedure is aiming at retrieving
			  * the full and exact path of "src/test/resources"
			  */
			 
	        String path = TestUtilities.getTemporaryFilePath();
	        
	        
	        String privacyFileName = path + "/src/test/resources/policy_anonymization_unittest.xml";
	        
	        String dbDumpFileName  = path + "/src/test/resources/deepsearch_unittest.zip";
	        
	       
//	        Map<String, Object> objects = new LinkedHashMap<String, Object>();
	        try {
	        	
//	        	InputStream is1 = new FileInputStream(new File(privacyFileName));
//	        	InputStream is2 = new FileInputStream(new File(dbDumpFileName));
	        
//	        	objects.put(MediaType.APPLICATION_OCTET_STREAM, is1);
//	        	objects.put(MediaType.APPLICATION_OCTET_STREAM, is2);
	        	
	        	LinkedList<Attachment> attsList = new LinkedList<Attachment>();
	    	
	    		attsList.add(TestUtilities.createAttachmentFromFileURI(privacyFileName, "policyFile", MediaType.APPLICATION_OCTET_STREAM));
	    		attsList.add(TestUtilities.createAttachmentFromFileURI(dbDumpFileName,"dbDump", MediaType.APPLICATION_OCTET_STREAM));
	    		
	    		String returnString = client.postCollection(attsList, Attachment.class, String.class);

	    		if (debug) 
	    			System.err.println("result is : "+ returnString);
	    		
	    		assertNotNull(returnString);
	    		
	    		long gid = -1;
	    		
	    		try {
	    			gid = TestUtilities.getGIDfromResult(returnString);
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			assertTrue(false);
	    		}
	    		
	        	Thread.sleep(5000);
	        	
	        	File referenceFile = new File(TestUtilities.getTemporaryFilePath()
						+ File.separator + "src" + File.separator + "test"
						+ File.separator + "resources" + File.separator
						+ "anonymization_export_unittest.txt");
	        	
	        	System.out.println("Request GID is: "+gid);
	        	
	        	boolean resultComparison = 
	        			testInnergetAnonymizeDatasetResult(gid, referenceFile);
	        	
	        	
	        	
				if (debug) {
					System.err.println("Test result: "+resultComparison /*+ " " + "length: "+resultComparison.length()*/);
				}
				
				assertTrue(resultComparison);
	        	
				System.out.println("Received anonymizeDataset output is correct.");
				
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
	public boolean testInnergetAnonymizeDatasetResult(long gid, File reference) {
		
		
		DbAnonymizerJAX_RS dbImpl = (DbAnonymizerJAX_RS) 
				TestUtilities.setupRSClient(endpointUrl, DbAnonymizerJAX_RS.class);

		Response response = dbImpl.getAnonymizeDataset(String.valueOf(gid));
		
		if (response.getStatus() == 400) {
			System.err.println("returned HTTP Status 400 :(");
			return false;
		}
		
		String result = "";

		try {


			String resultString = IOUtils.toString((InputStream) response
					.getEntity());
			
			
			return TestUtilities.checkIsEqual(resultString, reference);
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		
	}

}
