package it.cnr.alfresco.repository.workflow;

import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.ServiceRegistry;
import it.cnr.alfresco.repo.workflow.WorkflowService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ProcessUndef extends DeclarativeWebScript
{
   
   // service instances
   private WorkflowService workflowService;
   public void setWorkflowService(WorkflowService workflowService)
   {
       this.workflowService = workflowService;
   }

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
	}

   @Override
   protected Map<String, Object> executeImpl(WebScriptRequest req, Status status) { 
	   
	   String defId=req.getParameter("p");
	   Map<String, Object> model = new HashMap<String, Object>();
	   try{
		   this.workflowService.undeployDefinition(defId);
		   System.out.println("EDGARDO: process cancelled!");
		   model.put("success","true");
	   }catch(Exception e){
		   e.printStackTrace();
		   model.put("success","false");
		   return model;
	   }
       return model;
   }
}