package com.example.model;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TermsExtracter {
	private String uvalue = new String();
	//Now Read the Usage Terms and pass it as an argument to the agent
	  public void setUvalue(String destinationDir, String xmlname){
		  try {
	  
		  File file = new File(destinationDir,xmlname);
		  
		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  DocumentBuilder db = dbf.newDocumentBuilder();
		  Document doc = db.parse(file);
		  doc.getDocumentElement().normalize();
		  
		  NodeList nodeLst = doc.getElementsByTagName("usage");
		  

		  for (int s = 0; s < nodeLst.getLength(); s++) {

		    Node fstNode = nodeLst.item(s);
		    
		    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
		  
		      Element fstElmnt = (Element) fstNode;
		      NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("type");
		      Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
		      NodeList fstNm = fstNmElmnt.getChildNodes();
		      
		      this.uvalue = ((Node) fstNm.item(0)).getNodeValue();     
		      
		      
		    }

		  }
		  } catch (Exception e) {
		    e.printStackTrace();
		  }
	  }
	  public String getUvalue(){
		  return uvalue;
	  }
}
