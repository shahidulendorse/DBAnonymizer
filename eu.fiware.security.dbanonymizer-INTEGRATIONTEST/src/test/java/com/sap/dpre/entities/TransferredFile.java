package com.sap.dpre.entities;

import javax.activation.DataHandler;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.sap.dpre.entities.jaxb.riskcolumnresult.RiskColumnResult;

@XmlRootElement
public class TransferredFile {
	
	/**
	 * Tricky: annotation @XmlMimeType("application/octet-stream") must be carefully placed!
	 * 
	 * Best option seems to put it in the getter or in the setter, in this case setter is better 
	 * 
	 * see 
	 * http://technology.amis.nl/blog/2046/using-the-javaxxmlbind-annotations-to-convert-java-objects-to-xml-and-xsd
	 */
	
	private DataHandler fileData;

	private String fileName;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public DataHandler getFileData() {
		return this.fileData;
	}
	
	public void setFileData(DataHandler fileDataInput) {
		this.fileData = fileDataInput;
	}

	
}
