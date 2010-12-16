package com.example.web;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.gateway.JadeGateway;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

/**
 * Servlet implementation class GateWayServlet
 */
public class GateWayServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String TMP_DIR_PATH = "c:\\tmp";
	private File tmpDir;
	private static final String DESTINATION_DIR_PATH ="/files/";
	private File destinationDir;
	private ArrayList<String> xmlnames = new ArrayList<String>();
	
	private Object[] args = new Object[3];
	private Object[] bargs = new Object[2];
	private String user = new String();
	private String ccrname = new String();
	private String termname = new String();
	private String host = "localhost";
	private String port = "1099";
	// Retrieve the singleton instance of the JADE Runtime
	private Runtime rt = Runtime.instance();
	private Profile p = new ProfileImpl();
	private ContainerController cc;
	
	
	
	 public AgentController startBuyerAgent(String host, // JADE  environment Main Container host
             String port, // JADE  environment Main Container port
             String name, //  agent name
             Object[] args ) {
		 

		
		 
		 if (cc != null) {
			 // Create the agent and start it
			 try {
				 AgentController ac = cc.createNewAgent(name, "com.example.agent.BuyerAgent", args);
				 ac.start();
				 return ac;
			 }
			 catch (Exception e) {
				 e.printStackTrace();
			 }
		 }
		 return null;
	 }
	 public AgentController startSellerAgent(String host, // JADE  environment Main Container host
             String port, // JADE  environment Main Container port
             String name, //  agent name
             Object[] args // arg for agent
            ) {
		 

		 
		 if (cc != null) {
			 // Create the agent and start it
			 try {
				 AgentController ac = cc.createNewAgent(name, "com.example.agent.SellerAgent", args);
				 ac.start();
				 return ac;
			 }
			 catch (Exception e) {
				 e.printStackTrace();
			 }
		 }
		 return null;
	 }
	/**
     * @see HttpServlet#HttpServlet()
     */
    public GateWayServlet() {
        super();
        // TODO Auto-generated constructor stub
        p.setParameter(Profile.MAIN_HOST, host);
		p.setParameter(Profile.MAIN_PORT, port);
		// Create a main container to host the agent 
		 
		 
		 cc = rt.createMainContainer(p);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		 PrintWriter out = response.getWriter();
		 response.setContentType("text/html");
		 
		 
		String actionName = request.getParameter("action");
		
		if (actionName == null)	{
			//response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
			//return;
			actionName = "seller";
		}
		
		if(actionName.equals("buyer")){
			String bname = request.getParameter("name");   
			out.println("<h3>Welcome " + bname + "</h3>");
			String condition = request.getParameter("condition");
			String license = request.getParameter("license");
			out.println("You have requested for " + condition + " with " + license +" usage license.<br />");
			out.println("The program will start creating the agents for you.....<br />");
			
			out.println("Creating a BuyerAgent " + bname + "<br />");
			//out.println("<a href="index.html" title="Main Page" rel="external">Go Back</a>");
			bargs[0] = condition;
			bargs[1] = license;
			//StartBuyerAgent bnew = new StartBuyerAgent();
			this.startBuyerAgent("localhost", "1099", bname, bargs);
		}
		else {
			DiskFileItemFactory  fileItemFactory = new DiskFileItemFactory ();
			/*
			 *Set the size threshold, above which content will be stored on disk.
			 */
			fileItemFactory.setSizeThreshold(1*1024*1024); //1 MB
			/*
			 * Set the temporary directory to store the uploaded files of size above threshold.
			 */
			fileItemFactory.setRepository(tmpDir);
	 
			ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
			try {
				/*
				 * Parse the request
				 */
				List items = uploadHandler.parseRequest(request);
				Iterator itr = items.iterator();
				while(itr.hasNext()) {
					FileItem item = (FileItem) itr.next();
					/*
					 * Handle Form Fields.
					 */
					if(item.isFormField()) {
						String sname = (item.getString());
						out.println("<h3>Welcome " + sname + "</h3>");
						
						
					} else {
						out.println("You have uploaded the following file <br />");
						//Handle Uploaded files.
						out.println(" File Name = "+item.getName()+
							", Content type = "+item.getContentType()+
							", File Size = "+item.getSize() +"<br />");
						//request.setAttribute("filedetaillist", filedetaillist);
						/*
						 * Write file to the ultimate location.
						 */
						String fileName = item.getName();
						if(fileName != null){
							fileName = FilenameUtils.getName(fileName);
						}
						xmlnames.add(fileName);
						File file = new File(destinationDir,fileName);
						//out.println(destinationDir.getPath());
						item.write(file);
					}
				}
				
			}catch(FileUploadException ex) {
				log("Error encountered while parsing the request",ex);
			} catch(Exception ex) {
				log("Error encountered while uploading file",ex);
			}
			//Get xml file name for forwarding it to agent as argument
			ccrname = xmlnames.get(0);
			//Get xml file name for the terms.xml file to extract the usage element and pass it to the agent
			termname = xmlnames.get(1);	
			
			//Get first four letters of the the xml file to represent the name of an agent
			user = ccrname.substring(0,4);
			
			
			args[0] = ccrname;
			
			args[1] = termname;
			//Pass directory information
			args[2] = destinationDir.getPath();
			
			/*TermsExtracter test = new TermsExtracter();
			test.setUvalue(destinationDir.getPath(), termname);
			out.println(test.getUvalue());*/
			//StartSellerAgent snew = new StartSellerAgent();
			out.println("Creating a Seller Agent - " + user + "<br />");
			
		    this.startSellerAgent("localhost", "1099", user, args);	
			/*BlackBoardBean board = new BlackBoardBean();
			board.setReceiver(user);
			board.setMessage("Hey sell my data and report back the result");*/
			
			//Now create an agent
			/*try
			{

				JadeGateway.execute(board);
			
			}
			catch(Exception e)
			{
			e.printStackTrace();
			System.out.println(e.getMessage());
			}*/

		}
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		tmpDir = new File(TMP_DIR_PATH);
		
		if(!tmpDir.isDirectory()) {
			throw new ServletException(TMP_DIR_PATH + " is not a directory");
		}
		String realPath = getServletContext().getRealPath(DESTINATION_DIR_PATH);
		destinationDir = new File(realPath);
		if(!destinationDir.isDirectory()) {
			throw new ServletException(DESTINATION_DIR_PATH+" is not a directory");
			
		}
		JadeGateway.init(null,null);
	}
	

}
