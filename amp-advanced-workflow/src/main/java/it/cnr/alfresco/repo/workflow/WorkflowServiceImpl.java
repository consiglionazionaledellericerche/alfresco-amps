
package it.cnr.alfresco.repo.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowAdminService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import it.cnr.alfresco.repo.workflow.WorkflowComponent;
import org.jbpm.graph.def.Node;

public class WorkflowServiceImpl extends org.alfresco.repo.workflow.WorkflowServiceImpl implements it.cnr.alfresco.repo.workflow.WorkflowService 
{
    /*EDGARDO: Il seguente blocco serve per la release 4.0 di alfresco enterprise */
//	private ServiceRegistry services;
//	private WorkflowAdminService workflowAdminService;
//   public ServiceRegistry getServices() {
//	   return services;
//   }
//   public void setServices(ServiceRegistry services) {
//	   this.services = services;
//   }
//   public WorkflowAdminService getWorkflowAdminService() {
//	   return workflowAdminService;
//   }
//   public void setWorkflowAdminService(WorkflowAdminService workflowAdminService) {
//	   this.workflowAdminService = workflowAdminService;
//   }
   /*EDGARDO*/
   
   private BPMEngineRegistry BPMEngineRegistryCNR;
   public void setBPMEngineRegistryCNR(BPMEngineRegistry bPMEngineRegistryCNR) {
	  this.BPMEngineRegistryCNR = bPMEngineRegistryCNR;
   }
   
/*EDGARDO:*/
   public List<String> getTaskDefinitionsFromTo(final String workflowDefinitionId,final String taskNodeName)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getTaskDefinitionsFromTo(workflowDefinitionId, taskNodeName);
    }
   /*EDGARDO:*/
   public List<String> getTaskDefinitionsToFrom(final String workflowDefinitionId,final String taskNodeName)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getTaskDefinitionsToFrom(workflowDefinitionId, taskNodeName);
    }

   /*EDGARDO:*/
   public List<Node> getAllTaskDefinitions(final String workflowDefinitionId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getAllTaskDefinitions(workflowDefinitionId);
    }

   /*EDGARDO:*/
   public List<String> getSwimlaneDefinitions(final String workflowDefinitionId)
    {
        String engineId = BPMEngineRegistry.getEngineId(workflowDefinitionId);
        WorkflowComponent component = getWorkflowComponent(engineId);
        return component.getSwimlaneDefinitions(workflowDefinitionId);
    }
   
   
   private WorkflowComponent getWorkflowComponent(String engineId)
   {
       WorkflowComponent component = (WorkflowComponent)BPMEngineRegistryCNR.getWorkflowComponent(engineId);
       if (component == null) { 
       	throw new WorkflowException("Workflow Component for engine id '" + engineId + "' is not registered"); 
       }
       return component;
   }

   //EDGARDO: blocco necessario per la versione 4 di Alfresco, metodo importato da ........
   public List<WorkflowDefinition> getDefinitions()
   {
       List<WorkflowDefinition> definitions = new ArrayList<WorkflowDefinition>(10);
       String[] ids = BPMEngineRegistryCNR.getWorkflowComponents();
      for (String id : ids)
       {
//           if(workflowAdminService.isEngineVisible(id))
//           {
               WorkflowComponent component = (WorkflowComponent) BPMEngineRegistryCNR.getWorkflowComponent(id);
               definitions.addAll(component.getDefinitions());
//           }
       }
       return Collections.unmodifiableList(definitions);
   }

   
}
