package it.cnr.alfresco.repository.workflow;

import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import it.cnr.alfresco.repo.workflow.WorkflowService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class EndInstance extends DeclarativeWebScript
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
	   
	   String instanceId=req.getParameter("p");
//	   JBPMEngine jbpmEngine=new JBPMEngine();
	   WorkflowInstance wi=null;
	   try{
		   wi=this.workflowService.deleteWorkflow(instanceId);
		   System.out.println("EDGARDO: "+wi.description +" instance cancelled!");
//		   wi = jbpmEngine.deleteWorkflow(instanceId);
	   }catch(Exception e){
		   e.printStackTrace();
	   }
//	   if (wi==null){
//		   try{
//			   wi = jbpmEngine.cancelWorkflow(instanceId);
//		   }catch(NullPointerException npe){
//			   System.out.println(npe.getMessage());
//			   Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
//		       model.put("instanceId",instanceId);
//		       return model;
//		   }
//	   }
	   
	   Map<String, Object> model = new HashMap<String, Object>();
       model.put("id",wi.id);
       model.put("endDate",wi.endDate.toString());
       model.put("active",new Boolean(wi.active).toString());
       return model;
   }
}