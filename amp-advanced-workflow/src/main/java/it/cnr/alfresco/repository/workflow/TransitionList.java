package it.cnr.alfresco.repository.workflow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import it.cnr.alfresco.repo.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTransition;

import org.alfresco.service.cmr.workflow.WorkflowNode;

import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class TransitionList extends DeclarativeWebScript
{
    // service instances
    private WorkflowService workflowService;
    private ServiceRegistry serviceRegistry;
    private SiteService siteService;
    private InvitationService invitationService;
    
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,
            Status status) 
    {

		/*
		Map<String, Object> model = new HashMap<String, Object>();
		
		java.util.List<String[]> transitions=null;
		java.util.List<WorkflowTaskDefinition> wtd=this.workflowService.getTaskDefinitions(req.getParameter("p"));
		for (Object o:wtd){
			WorkflowNode wn=((WorkflowTaskDefinition)o).node;
			WorkflowTransition[] listtransition=wn.transitions;
			transitions=new ArrayList<String[]>();
			for (Object k:listtransition){
				String s[] ={((WorkflowTransition)k).id, ((WorkflowTransition)k).title, ((WorkflowTransition)k).description};
				System.out.println(((WorkflowTransition)k).id +"  "+ ((WorkflowTransition)k).title +"  "+ ((WorkflowTransition)k).description);
				transitions.add(s);
			}	
		}	
		
		model.put("transitions",transitions);
		return model;
		
		*/
		
		
		Map<String, Object> model = new HashMap<String, Object>();
		java.util.List<String[]> transitions=new ArrayList<String[]>();

		java.util.List<WorkflowTask> listtasks=new ArrayList<WorkflowTask>();
		List<WorkflowDefinition> workflowDefintions=this.workflowService.getAllDefinitions();
		for (Object k:workflowDefintions){
			List<WorkflowInstance> workflowInstances = this.workflowService.getActiveWorkflows(((WorkflowDefinition)k).getId());
			for (Object o:workflowInstances){
				List<WorkflowPath> workflowPaths = this.workflowService.getWorkflowPaths(((WorkflowInstance)o).id); //il metodo dice di usare il wfId per ottenere i path, ma non funziona! Invece funziona usando l'id della istanza del workflow
				for(Object h:workflowPaths){ 
					for (Object l:this.workflowService.getTasksForWorkflowPath(((WorkflowPath)h).id)){
						listtasks.add((WorkflowTask)l);
					} 	
				}	
			}	
		}	
    	for (Object o:listtasks){
			if (((WorkflowTask)o).id.equals(req.getParameter("p"))) {
				WorkflowNode wn=((WorkflowTask)o).definition.node;
				WorkflowTransition[] listtransition=wn.transitions;
				transitions=new ArrayList<String[]>();
				for (Object k:listtransition){
					String s[] ={((WorkflowTransition)k).id, ((WorkflowTransition)k).title, ((WorkflowTransition)k).description};
					System.out.println(((WorkflowTransition)k).id +"  "+ ((WorkflowTransition)k).title +"  "+ ((WorkflowTransition)k).description);
					transitions.add(s);
				}	
				
			}
		}

		model.put("transitions", transitions);
		return model;

		
		
		
    }    
}
