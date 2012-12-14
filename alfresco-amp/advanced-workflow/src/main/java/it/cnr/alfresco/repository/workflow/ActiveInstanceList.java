package it.cnr.alfresco.repository.workflow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import it.cnr.alfresco.repo.workflow.WorkflowService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ActiveInstanceList extends DeclarativeWebScript
{
    // service instances
    private WorkflowService workflowService;
    @SuppressWarnings("unused")
	private ServiceRegistry serviceRegistry;
    
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
		Map<String, Object> model = new HashMap<String, Object>();
		String ticket="alf_ticket="+req.getParameter("alf_ticket");
		
		java.util.List<String[]> activeInstances=new ArrayList<String[]>();
		if (req.getParameter("p")!=null){
			List<WorkflowInstance> workflowInstances = this.workflowService.getActiveWorkflows(req.getParameter("p"));
			for (Object o:workflowInstances){
	    		String s[] ={((WorkflowInstance)o).id, ((WorkflowInstance)o).description};
				activeInstances.add(s);
			}	
		}else{
			List<WorkflowDefinition> workflowDefintions=this.workflowService.getAllDefinitions();
			for (Object k:workflowDefintions){
				List<WorkflowInstance> workflowInstances = this.workflowService.getActiveWorkflows(((WorkflowDefinition)k).getId());
				for (Object o:workflowInstances){
		    		String s[] ={((WorkflowInstance)o).id, ((WorkflowInstance)o).description};
					activeInstances.add(s);
				}	
			}
		}	
		model.put("activeInstances", activeInstances);
		return model;
    }    
}
