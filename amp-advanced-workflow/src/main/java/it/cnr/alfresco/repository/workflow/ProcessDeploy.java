package it.cnr.alfresco.repository.workflow;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import it.cnr.alfresco.repo.workflow.WorkflowService;

import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ProcessDeploy extends DeclarativeWebScript
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
	   
       InputStream workflowDef = (InputStream) req.getContent().getInputStream();
       WorkflowDeployment deployment = workflowService.deployDefinition("jbpm", workflowDef, MimetypeMap.MIMETYPE_XML);
       WorkflowDefinition def = deployment.definition;
//       for (String problem : deployment.problems)
//       {
//           out.println(problem);
//       }
       System.out.println("deployed definition id: " + def.id + " , name: " + def.name + " , title: " + def.title + " , version: " + def.version);
       Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
       model.put("success", "true");
       return model;
   }

}