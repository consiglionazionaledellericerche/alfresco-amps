package it.cnr.alfresco.repository.workflow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import it.cnr.alfresco.repo.workflow.WorkflowService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class PathList extends DeclarativeWebScript
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

			
		List<WorkflowDefinition> wdList=this.workflowService.getAllDefinitions();
		List<WorkflowInstance> allWiList=new Vector<WorkflowInstance>();
		for (Object o:wdList){
			String s=((WorkflowDefinition)o).getId();
			List<WorkflowInstance> wiList= this.workflowService.getActiveWorkflows(s);
			for(Object k:wiList){
				allWiList.add((WorkflowInstance)k);	
			}
		}
		String wfId=new String (); 
		WorkflowInstance wi = null;
		for (Object o:allWiList){
			if ( (((WorkflowInstance)o).id).equals(req.getParameter("p")) ){
				wfId=((WorkflowDefinition)((WorkflowInstance)o).definition).getId();
				wi=((WorkflowInstance)o);
				System.out.println("EDGARDO "+wfId+" -- "+wi.id);
			}
		}
		if( wfId==null || wi==null ) {
				java.util.List<String[]> paths=new ArrayList<String[]>();
	    		String[] s={"none","none","none"};
	    		paths.add(s);
				model.put("paths", paths);
				return model;
		}
		
		
		List<WorkflowPath> workflowPaths = this.workflowService.getWorkflowPaths(wi.id); //il metodo dice di usare il wfId per ottenere i path, ma non funziona! Invece funziona usando l'id della istanza del workflow
		java.util.List<String[]> paths=new ArrayList<String[]>();
		for (Object o:workflowPaths){
			Boolean b=new Boolean(((WorkflowPath)o).active);
    		String[] s={ ((WorkflowPath)o).id, b.toString(),wfId};
    		paths.add(s);
		}	
		
		model.put("paths", paths);
		return model;
    }    
}
