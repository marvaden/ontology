package update;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class UpdaterAgent extends Agent
{
  private UpdaterAgentGUI gui;
  static final String inputFileName = "test.rdf";
  private Model base;
  private InputStream inputStream;
  private String agentName;
  private AID[] agentList;
  
  protected void setup()
  {
    //this is the base model for the ontology
    base = ModelFactory.createDefaultModel();
    
    // use the FileManager to find the saved ontology
    inputStream = FileManager.get().open(inputFileName);
    if (inputStream == null) 
    {
      throw new IllegalArgumentException
                  ( "File: " + inputFileName + " not found");
    }
    
    // load the ontology
    base.read( inputStream, "" );
    
    // Get the name for this agent
    agentName = "";
    Object[] args = getArguments();
    if (args != null && args.length > 0) 
    {
      agentName = (String)args[0];
      System.out.println("Agent Name is  "+agentName);
    }
    
    
    //Register the agent service with the yellow pages
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    ServiceDescription sd = new ServiceDescription();
    sd.setType("modular-ontology");
    sd.setName(getLocalName() + "-modular-ontology");
    dfd.addServices(sd);
    try 
    {
      DFService.register(this, dfd);
    }
    catch (FIPAException fe) 
    {
      fe.printStackTrace();
    }
    
    addBehaviour(new RemoteOntologyUpdater());
    
    //start up the GUI
    gui = new UpdaterAgentGUI(this);
    gui.showGui();
  }
  
  protected void takeDown()
  {
    System.out.println("Updater agent going down.");
    gui.dispose();
    
    try
    {
      DFService.deregister(this);
    } catch (FIPAException e)
    {
      e.printStackTrace();
    }
  }
  
  protected void addResource(String resource) 
  {
    System.out.println("add resource:" + resource + ":");
    base.createResource(resource);
  }
  
  protected void addProperty(String property)
  {
    System.out.println("add property:" + property + ":");
    base.createProperty(property);
  }
  
  protected void addTriple(String subjectIn, String propertyIn, String objectIn)
  {
    System.out.println("&" +subjectIn+ "&" +propertyIn+ "&" +objectIn);
    
    Resource subject = base.getResource(subjectIn);
    System.out.println("resource: " + subject.toString());
    
    Property property = base.getProperty(propertyIn);
    System.out.println("property: " + property.toString());
    if(!base.contains(subject, property, objectIn))
    {
      base.add(subject, property, objectIn);
      String triple = subjectIn + "," + propertyIn + "," + objectIn;
      informAgents(triple);
    }
  }
  
  protected String queryProperty(String propertyIn)
  {
    Property property = base.getProperty(propertyIn);
    ResIterator iter = 
        base.listResourcesWithProperty(property);
    StringBuilder queryResult = new StringBuilder();
    if (iter.hasNext())
    {
      while (iter.hasNext())
      {
        Resource tempResource = 
                iter.nextResource();
        queryResult.append(tempResource.getURI() 
                                  + "  " 
                                  + tempResource
                                      .getRequiredProperty(property)
                                      .getString());
      }
    } 
    else 
    {
      queryResult.append("This new proprty was not found");
    }
    return queryResult.toString();
  }
  
  protected String getOntology()
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    base.write(baos);
    String ontology = new String(baos.toByteArray());
    return ontology;
  }
  
  protected void informAgents(String triple)
  {
    StringBuilder diff = new StringBuilder();
    ACLMessage inform = new ACLMessage(ACLMessage.PROPOSE);
    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();
    sd.setType("modular-ontology");
    template.addServices(sd);
    try 
    {
      DFAgentDescription[] agentsFound = DFService.search(this, template);
      for (int i = 0; i < agentsFound.length; i++)
      {
        AID tempAgent = agentsFound[i].getName();
        if(!tempAgent.equals(this.getAID()))
        { 
          inform.addReceiver(tempAgent);
        }
      }
      inform.setContent(triple);
      inform.setConversationId("ont-update");
      inform.setReplyWith("inform"+System.currentTimeMillis()); // Unique value
      this.send(inform);
    }
    catch (FIPAException fe) 
    {
      fe.printStackTrace();
    }
  }
  
  protected void mergeOntologies(String triple)
  {
    String[] parts = triple.split(",");
    addTriple(parts[0], parts[1], parts[2]);
  }
  
  private class RemoteOntologyUpdater extends CyclicBehaviour 
  {
    public void action() 
    {
      MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
      ACLMessage msg = myAgent.receive(mt);
      if (msg != null) 
      {
        String content = msg.getContent();
        mergeOntologies(content);
      }
      else 
      {
        block();
      }
    }
  } 
}
