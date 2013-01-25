package it.cnr.alfresco.repository.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import it.cnr.alfresco.repo.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class StartTask extends DeclarativeWebScript
{
   
   // service instances
   private WorkflowService workflowService;
   private ServiceRegistry serviceRegistry;
   

   public void setWorkflowService(WorkflowService workflowService)
   {
       this.workflowService = workflowService;
   }

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

   @Override
   protected Map<String, Object> executeImpl(WebScriptRequest req, Status status) { 
	   
	   try{
	    
	    String workflowname_val =req.getParameter("workflowName");
	    String description_val=req.getParameter("description");
	    String priority_val=req.getParameter("priority");
	    String assignee_val=req.getParameter("assignee");
		String duedate_val=req.getParameter("duedate");
		String alf_ticket=req.getParameter("alf_ticket");
		String noderef_val=req.getParameter("documentnoderef");
		System.out.println(workflowname_val);
		System.out.println(description_val);
		System.out.println(priority_val);
		System.out.println(assignee_val);
		System.out.println(duedate_val);
		System.out.println(alf_ticket);
		System.out.println(noderef_val);

//		Map<String, Serializable> updateProperties = new HashMap<String, Serializable>();
//		updateProperties.put("{http://www.alfresco.org/model/bpm/1.0}workflowDefinitionName",workflowname_val );
//		updateProperties.put("{http://www.alfresco.org/model/bpm/1.0}workflowDescription",description_val );
//		updateProperties.put("{http://www.alfresco.org/model/bpm/1.0}priority", priority_val);
//		updateProperties.put("{http://www.alfresco.org/model/content/1.0}owner", assignee_val);
//		updateProperties.put("{http://www.alfresco.org/model/bpm/1.0}assignee", assignee_val);		
//		updateProperties.put("{http://www.alfresco.org/model/bpm/1.0}workflowDueDate", duedate_val);
//		ActionService actionService=this.serviceRegistry.getActionService();
//		Action action=actionService.createAction("start-workflow");
//		System.out.println(actionService);
//		System.out.println(action);
//		NodeRef nr=new NodeRef(noderef_val);
//		System.out.println("NodeRef: "+nr);
//		actionService.executeAction(action, nr);
		
		NodeRef workflowPackage = workflowService.createPackage(null); 
		boolean multiPackage=false;

		if(noderef_val.contains(";")) multiPackage=true;
		if(multiPackage) {
			String[] multi_noderef_val=noderef_val.split(";");
			for (String s:multi_noderef_val) {
				   NodeRef nr=new NodeRef(s);
				   System.out.println("NODEREF: "+nr);
			       System.out.println("NODEREFPACKAGE: "+workflowPackage);
			       String localQname=QName.createValidLocalName((String)this.serviceRegistry.getNodeService().getProperty(nr, ContentModel.PROP_NAME));
			       System.out.println("LOCALQNAME: "+localQname);
			       QName qname=QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,localQname);
			       System.out.println("QNAME: "+qname);
			       this.serviceRegistry.getNodeService().addChild(workflowPackage, nr, ContentModel.ASSOC_CONTAINS,qname);
			}
		}else {
		   NodeRef nr=new NodeRef(noderef_val);
		   System.out.println("NODEREF: "+nr);
	       System.out.println("NODEREFPACKAGE: "+workflowPackage);
	       String localQname=QName.createValidLocalName((String)this.serviceRegistry.getNodeService().getProperty(nr, ContentModel.PROP_NAME));
	       System.out.println("LOCALQNAME: "+localQname);
	       QName qname=QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,localQname);
	       System.out.println("QNAME: "+qname);
	       this.serviceRegistry.getNodeService().addChild(workflowPackage, nr, ContentModel.ASSOC_CONTAINS,qname);
       }
       

		   Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>(16);
	       workflowProps.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
		   //workflowProps.put(WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME,workflowname_val );
	       workflowProps.put(QName.createQName("{http://www.alfresco.org/model/bpm/1.0}workflowDescription"),description_val );
	       workflowProps.put(QName.createQName("{http://www.alfresco.org/model/bpm/1.0}workflowDueDate"), duedate_val);
	       workflowProps.put(QName.createQName("{http://www.alfresco.org/model/bpm/1.0}dueDate"), duedate_val);
	       workflowProps.put(QName.createQName("{http://www.alfresco.org/model/bpm/1.0}workflowPriority"), priority_val);
	       workflowProps.put(QName.createQName("{http://www.alfresco.org/model/bpm/1.0}priority"), priority_val);
	       workflowProps.put(QName.createQName("{http://www.alfresco.org/model/bpm/1.0}status"), "Not Yet Started");
	       workflowProps.put(QName.createQName("{http://www.alfresco.org/model/content/1.0}owner"), assignee_val);
	       workflowProps.put(QName.createQName("{http://www.alfresco.org/model/bpm/1.0}assignee"), assignee_val);		

	       
	       WorkflowDefinition workflowDefinition = workflowService.getDefinitionByName(workflowname_val);
	       WorkflowPath wp=this.workflowService.startWorkflow(workflowDefinition.getId(), workflowProps);
	       /*
	        * SE QUESTA PORZIONE DI CODICE NON VIENE INSERITA, IL WORKFLOW VIENE SOLO INIZIALIZZATO MA NON AVVIATO
	        *BISOGNA CAPIRE QUALI SONO LE IMPLICAZIONI E COSA SIGNIFICA 
	       */
	       List<WorkflowTask> tasks = this.workflowService.getTasksForWorkflowPath(wp.id);
	       for (WorkflowTask wt:tasks){
	    	   this.workflowService.endTask(wt.id, null);
	       }

	       /*QUESTE PROPERTIES VENGONO INSERITE IN SEGUITO ALLO START ATTRAVERSO IL MECCANISMO DI UPDATE
	        * ALTRIMENTI NON VENGONO VALORIZZATE
	        */
	       Map<QName, Serializable> updateProperties = new HashMap<QName, Serializable>();
	       updateProperties.put(QName.createQName("{http://www.alfresco.org/model/bpm/1.0}description"),"GENERAL DESCRIPTION TO BE INSERTED" );
	       updateProperties.put(QName.createQName("{http://www.alfresco.org/model/content/1.0}description"),"BROAD DESCRIPTION TO BE INSERTED" );
	       updateProperties.put(QName.createQName("{http://www.alfresco.org/model/bpm/1.0}comment"),"GENERAL COMMENT TO BE INSERTED" );
	       updateProperties.put(QName.createQName("{http://www.alfresco.org/model/bpm/1.0}outcome"),"GENERAL OUTCOME TO BE INSERTED" );
	       
	       for (WorkflowTask wt:tasks){
	    	   this.workflowService.updateTask(wt.id, updateProperties, null, null);
	       }
		
       Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
       
	   model.put("wkfname",workflowname_val);
       model.put("wkfdesc",description_val);
       model.put("wkfass",assignee_val);
       model.put("wkfnoderef",noderef_val);
       model.put("wkfduedate",duedate_val);
       model.put("wkfpriority",priority_val);
       model.put("instanceId", wp.instance.id);
       model.put("pathId", wp.id);
       
       
       return model;
	   }catch(Exception e){
	       
		   Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
	       model.put("wkfname","");
	       model.put("wkfdesc","");
	       model.put("wkfass","");
	       model.put("wkfnoderef","");
	       model.put("wkfduedate","");
	       model.put("wkfpriority","");
	       model.put("instanceId", "");
	       model.put("pathId", "");
	       e.printStackTrace();
	       return model;
	       
	   }
   }
}
