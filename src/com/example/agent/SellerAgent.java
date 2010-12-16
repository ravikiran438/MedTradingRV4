
package com.example.agent;
import com.example.model.*;

import java.io.File;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;

import java.util.*;


public class SellerAgent extends Agent {
  // The catalogue of medical conditions available for sale
  private Map catalogue = new HashMap();
  private String ccrname = new String();
  private String termname = new String();
  private String path = new String();
  private String termvalue = new String();
  
  private File doc;
  private UnmarshallingDemo6 h;
  private ACLMessage startrequest, replyfinal;
  
  

     

  /* Agent initializations*/
  protected void setup() {
    // Printout a welcome message
    System.out.println("Seller-agent "+getAID().getName()+" is ready.");
    try{
	//Get the xml document to work with
	Object[] args = getArguments();
	if(args != null && args.length > 0) {
	  for (int i =0; i <args.length; i++) {
		  
	    ccrname = (String) args[i];
	    System.out.println(ccrname);
	    i++;
	    termname = (String) args[i];
	    System.out.println(termname);
	    i++;
	    path = (String) args[i];
	    System.out.println(path);
	  }
	  
	  
	  
	}
	//a = new String("Ravi Kiran Kadaboina.xml");
	//Get xml document as input
	
	doc = new File(path + '\\',ccrname);
	h = new UnmarshallingDemo6();
    h.extract(doc);
    TermsExtracter test = new TermsExtracter();
	test.setUvalue(path, termname);
	termvalue = test.getUvalue();
	
	}
	catch(Exception e) {
	e.printStackTrace();
	}

    /*h.setMedCondition("Asthma");
    h.setStatus("Intermittent:Comes and Goes");
    h.setMedicine("Betaloc");
    h.setDosageInMg(200);
    h.setUsageTimesDaily(3);*/
    //Add the behaviour for replying back to the servlet
    //addBehaviour(new CommServer());
    putForSale();
    // Add the behaviour serving calls for price from buyer agents
    addBehaviour(new CallForOfferServer());

    // Add the behaviour serving purchase requests from buyer agents
    addBehaviour(new PurchaseOrderServer());

    
    // Register the Data-selling service in the yellow pages
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    ServiceDescription sd = new ServiceDescription();
    sd.setType("Data-selling");
    sd.setName(getLocalName()+"-Data-selling");
    dfd.addServices(sd);
    try {
      DFService.register(this, dfd);
    }
    catch (FIPAException fe) {
      fe.printStackTrace();
    }
  }

  /*Agent clean-up*/
  protected void takeDown() {
    doDelete();

    // Printout a dismissal message
    System.out.println("Seller-agent "+getAID().getName()+"terminating.");

    
    // Deregister from the yellow pages
    try {
      DFService.deregister(this);
    }
    catch (FIPAException fe) {
      fe.printStackTrace();
    }
  }

  /*private class CommServer extends CyclicBehaviour {
     private MessageTemplate mtstart = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
	public void action()
	{
	  ACLMessage startrequest = myAgent.receive(mtstart);
	  

	  if (startrequest!=null) {
		 
				   
		  ACLMessage replyfinal = startrequest.createReply();
		  replyfinal.setPerformative( ACLMessage.INFORM );
          }
	  else {
		  block();
	  }
	}
 }*/

  
  public void putForSale(){
    String medCondition = h.getMedCondition();
    int initPrice = 80;
    int minPrice = 20;
    long deadline = System.currentTimeMillis() + 500000;
    addBehaviour(new PriceManager(this, medCondition, initPrice, minPrice, deadline));
  }



  private class PriceManager extends TickerBehaviour {
    private String medCondition;
    private int minPrice, currentPrice, initPrice, deltaP;
    private long initTime, deadline, deltaT;

    private PriceManager(Agent a, String t, int ip, int mp, long d) {
      super(a, 30000); // tick every 30 seconds
      medCondition = t;
      initPrice = ip;
      currentPrice = initPrice;
      deltaP = initPrice - mp;
      deadline = d;
      initTime = System.currentTimeMillis();
      deltaT = ((deadline - initTime) > 0 ? (deadline - initTime) : 60000);
    }

    public void onStart() {
      // Insert the condition in the catalogue available for sale
      catalogue.put(medCondition, this);
      super.onStart();
    }

    public void onTick() {
      long currentTime = System.currentTimeMillis();
      if (currentTime > deadline) {
        // Deadline expired
        System.out.println("Cannot sell data for "+medCondition);
        catalogue.remove(medCondition);
        stop();
      }
      else {
        // Compute the current price
        long elapsedTime = currentTime - initTime;
        currentPrice = (int)Math.round(initPrice - 1.0 * deltaP * (1.0 * elapsedTime / deltaT));
      }
    }

    public int getCurrentPrice() {
      return currentPrice;
    }
  }

  /*
   * Inner class CallForOfferServer.
   * This is the behaviour used by Seller agents to serve
   * incoming call for offer from buyer agents.
   * If the indicated medical condition is in the local catalogue, the seller agent
   * replies with a PROPOSE message specifying the price. Otherwise
   * a REFUSE message is sent back.
  **/
  private class CallForOfferServer extends CyclicBehaviour {
    private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);

    public void action() {
      ACLMessage msg = myAgent.receive(mt);
      if (msg != null) {
        // CFP Message received. Process it
        String content = msg.getContent();
        //String term = new String("commercial-use");
        String[] x = new String[2];
        int i = 0;
        StringTokenizer st = new StringTokenizer(content);
        while(st.hasMoreTokens()) {
	x[i] = st.nextToken();
        i++;
        }
        String medCondition = x[0];
        String conditionOfUse = x[1];
        
        System.out.println("Received Proposal to buy "+medCondition);
        ACLMessage reply = msg.createReply();
        PriceManager pm = (PriceManager) catalogue.get(medCondition);
        if (pm != null) {
          // The requested data is available for sale. Reply with the price
          //Now check if the terms are acceptable
          if(conditionOfUse.equals(termvalue)){
          reply.setPerformative(ACLMessage.PROPOSE);
          reply.setContent(String.valueOf(pm.getCurrentPrice()));
          }
          else{
          reply.setPerformative(ACLMessage.REFUSE);
	  }
        }
        else {
          // The requested data is NOT available for sale.
          reply.setPerformative(ACLMessage.REFUSE);
        }
        myAgent.send(reply);
        System.out.println(pm != null ? "Sent Proposal to sell at "+reply.getContent() : "Refused Proposal as the data is not for sale");
      }
      else {
        block();
      }
    }
  } // End of inner class CallForOfferServer

  private class PurchaseOrderServer extends CyclicBehaviour {
    private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);

    public void action() {
      ACLMessage msg = myAgent.receive(mt);
      String content = "";
      if (msg != null) {
        // ACCEPT_PROPOSAL Message received. Process it
        String medCondition = msg.getContent();
        System.out.println("Received ACCEPT_PROPOSAL to buy "+medCondition);
        ACLMessage reply = msg.createReply();
        PriceManager pm = (PriceManager) catalogue.get(medCondition);
        if (pm != null) {
          // Reply with the data you have
	  reply.setPerformative( ACLMessage.INFORM );
	  reply.setContent("Status:" + h.getStatus() + "  Medicine: " + h.getMedicine());
	  send(reply);
	   /*content = "<br/> - " + myAgent.getLocalName() + " received: " + startrequest.getContent() + "<br/> - " + "report:" + "Status:" + h.getStatus() + "  Medicine: " + h.getMedicine();
	  replyfinal.setContent(content);
	  send(replyfinal);*/
			
	}
				
	else{
	  reply.setPerformative( ACLMessage.FAILURE );
	  send(reply);
          System.out.println("Failure");
        }
      }
      else {
        block();
      }
    }
  } // End of inner class PurchaseOrderServer

	


}
