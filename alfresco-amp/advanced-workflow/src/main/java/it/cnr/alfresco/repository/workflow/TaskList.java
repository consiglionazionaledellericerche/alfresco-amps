package it.cnr.alfresco.repository.workflow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import it.cnr.alfresco.repo.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class TaskList extends DeclarativeWebScript
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
		Map<String, Object> model = new HashMap<String, Object>();
		java.util.List<String[]> tasks=new ArrayList<String[]>();
		java.util.List<String> properties=new ArrayList<String>();
		
		//java.util.List<WorkflowPath> listpath=this.workflowService.getWorkflowPaths(req.getParameter("p"));
		//java.util.List<WorkflowTask> listtasks=this.workflowService.getTasksForWorkflowPath(listpath.get(0).id);
		if (req.getParameter("p")==null){
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
			System.out.println("**********************************************************");
	    	for (Object o:listtasks){
	    		Map m=((WorkflowTask)o).properties;
	    		String s1=new String();	
	    		for (Object h:m.keySet()){
	    			s1=s1+h+"==="+m.get(h)+",";
	    		}
	    		String initiator=(String)serviceRegistry.getNodeService().getProperty(((WorkflowTask)o).path.instance.initiator, ContentModel.PROP_USERNAME);
	    		String s[] ={((WorkflowTask)o).id, ((WorkflowTask)o).name, ((WorkflowTask)o).title, ((WorkflowTask)o).description, ((java.lang.Enum<WorkflowTaskState>)((WorkflowTask)o).state).toString(),s1,initiator};
	    		System.out.println(((WorkflowTask)o).id+" "+ ((WorkflowTask)o).name+" "+  ((WorkflowTask)o).title+" "+  ((WorkflowTask)o).description+" "+  ((java.lang.Enum<WorkflowTaskState>)((WorkflowTask)o).state).toString()+" "+s1.toString()+" "+initiator);
	    		tasks.add(s);
	    	}
	    	System.out.println("**********************************************************");
		}else{
			System.out.println("**********************************************************");
			java.util.List<WorkflowTask> listtasks=this.workflowService.getTasksForWorkflowPath(req.getParameter("p"));
	    	for (Object o:listtasks){
	    		Map m=((WorkflowTask)o).properties;
	    		String s1=new String();	
	    		for (Object h:m.keySet()){
	    			s1=s1+h+"==="+m.get(h)+",";
	    		}
	    		String initiator=(String)serviceRegistry.getNodeService().getProperty(((WorkflowTask)o).path.instance.initiator, ContentModel.PROP_USERNAME);
	    		String s[] ={((WorkflowTask)o).id, ((WorkflowTask)o).name, ((WorkflowTask)o).title, ((WorkflowTask)o).description, ((java.lang.Enum<WorkflowTaskState>)((WorkflowTask)o).state).toString(),s1,initiator};
	    		System.out.println(((WorkflowTask)o).id+" "+ ((WorkflowTask)o).name+" "+  ((WorkflowTask)o).title+" "+  ((WorkflowTask)o).description+" "+  ((java.lang.Enum<WorkflowTaskState>)((WorkflowTask)o).state).toString()+" "+s1.toString()+" "+initiator);
	    		tasks.add(s);
	    	}
	    	System.out.println("**********************************************************");
		}
		
		model.put("tasks", tasks);
		
		return model;
    }    
}
