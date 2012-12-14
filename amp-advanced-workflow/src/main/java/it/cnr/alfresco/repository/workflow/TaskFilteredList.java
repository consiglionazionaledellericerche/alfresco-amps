package it.cnr.alfresco.repository.workflow;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.acegisecurity.GrantedAuthority;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.web.scripts.bean.Authentication;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import it.cnr.alfresco.repo.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import it.cnr.alfresco.repository.authUtils.*;

public class TaskFilteredList extends DeclarativeWebScript
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
		
		
		Utils authUtils =new Utils();
		authUtils.setServiceRegistry(this.serviceRegistry);
		authUtils.setUserName(AuthenticationUtil.getFullyAuthenticatedUser());
		boolean isAdmin=false;
		if (authUtils.getGroups()!=null){
			isAdmin=authUtils.getGroups().contains("GROUP_ALFRESCO_ADMINISTRATORS");
			System.out.println(isAdmin);
		}else{
			System.out.println(isAdmin);
		}
		
		
		if ( !isAdmin && req.getParameter("initiator")==null ){
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
			boolean includeTask=false;
	    	for (Object o:listtasks){
	    		Map<QName,Serializable> m=((WorkflowTask)o).properties;
	    		String s1=new String();	
	    		for (QName h:m.keySet()){
	    			if(h.toString().contains("content")) s1=s1+"cm_"+h.getLocalName()+"==="+m.get(h)+","; // System.out.println("cm_"+h.getLocalName()+"==="+m.get(h));			
	    			if(h.toString().contains("bpm")) s1=s1+"bpm_"+h.getLocalName()+"==="+m.get(h)+","; //System.out.println("bpm_"+h.getLocalName()+"==="+m.get(h));
	    			//s1=s1+h+"==="+m.get(h)+",";
	    				//Filtro per i my-started-task
//	    				if (h.toString().equals("{http://www.alfresco.org/model/content/1.0}userName")){
//	    						 if ( ((String)m.get(h)).equals(AuthenticationUtil.getFullyAuthenticatedUser()) ){
//	    							 includeTask=true;
//	    						 }
//	    				}(String)nodeService.getProperty(task.path.instance.initiator, ContentModel.PROP_USERNAME)
	    				if ( AuthenticationUtil.getFullyAuthenticatedUser().equals((String)serviceRegistry.getNodeService().getProperty( ((WorkflowTask)o).path.instance.initiator, ContentModel.PROP_USERNAME ) ) ){
	    						includeTask=true;
	    				}

	    				//Filtro per i my-started-task
	    		}
	    		if(includeTask){
	    			String s[] ={((WorkflowTask)o).id, ((WorkflowTask)o).name, ((WorkflowTask)o).title, ((WorkflowTask)o).description, ((java.lang.Enum<WorkflowTaskState>)((WorkflowTask)o).state).toString(),s1};
	    			System.out.println(((WorkflowTask)o).id+" "+ ((WorkflowTask)o).name+" "+  ((WorkflowTask)o).title+" "+  ((WorkflowTask)o).description+" "+  ((java.lang.Enum<WorkflowTaskState>)((WorkflowTask)o).state).toString()+" "+s1.toString());
	    			tasks.add(s);
	    			includeTask=false;
	    		}	
	    		
	    	}
	    	System.out.println("**********************************************************");
		}
		
		if ( !isAdmin && req.getParameter("initiator")!=null ){
					String initiator=null;
					if (!req.getParameter("initiator").equals(AuthenticationUtil.getFullyAuthenticatedUser())){
						initiator=AuthenticationUtil.getFullyAuthenticatedUser();
					}else{
						initiator=AuthenticationUtil.getFullyAuthenticatedUser();
					}

					
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
					boolean includeTask=false;
			    	for (Object o:listtasks){
			    		Map<QName,Serializable> m=((WorkflowTask)o).properties;
			    		String s1=new String();	
			    		for (QName h:m.keySet()){
			    			if(h.toString().contains("content")) s1=s1+"cm_"+h.getLocalName()+"==="+m.get(h)+","; // System.out.println("cm_"+h.getLocalName()+"==="+m.get(h));			
			    			if(h.toString().contains("bpm")) s1=s1+"bpm_"+h.getLocalName()+"==="+m.get(h)+","; //System.out.println("bpm_"+h.getLocalName()+"==="+m.get(h));
			    			//s1=s1+h+"==="+m.get(h)+",";
		    				//Filtro per i my-started-task
//		    				if (h.toString().equals("{http://www.alfresco.org/model/content/1.0}userName")){
//		    						 if ( ((String)m.get(h)).equals(AuthenticationUtil.getFullyAuthenticatedUser()) ){
//		    							 includeTask=true;
//		    						 }
//		    				}(String)nodeService.getProperty(task.path.instance.initiator, ContentModel.PROP_USERNAME)
		    				if ( AuthenticationUtil.getFullyAuthenticatedUser().equals((String)serviceRegistry.getNodeService().getProperty( ((WorkflowTask)o).path.instance.initiator, ContentModel.PROP_USERNAME ) ) ){
		    						includeTask=true;
		    				}

		    				//Filtro per i my-started-task

			    		}
			    		if(includeTask){
			    			String s[] ={((WorkflowTask)o).id, ((WorkflowTask)o).name, ((WorkflowTask)o).title, ((WorkflowTask)o).description, ((java.lang.Enum<WorkflowTaskState>)((WorkflowTask)o).state).toString(),s1};
			    			System.out.println(((WorkflowTask)o).id+" "+ ((WorkflowTask)o).name+" "+  ((WorkflowTask)o).title+" "+  ((WorkflowTask)o).description+" "+  ((java.lang.Enum<WorkflowTaskState>)((WorkflowTask)o).state).toString()+" "+s1.toString());
			    			tasks.add(s);
							includeTask=false;
			    		}	
			    		
			    	}
			    	System.out.println("**********************************************************");
	
		}

		if ( isAdmin && req.getParameter("initiator")==null ){
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
	    		Map<QName,Serializable> m=((WorkflowTask)o).properties;
	    		String s1=new String();	
	    		for (QName h:m.keySet()){
	    			if(h.toString().contains("content")) s1=s1+"cm_"+h.getLocalName()+"==="+m.get(h)+","; // System.out.println("cm_"+h.getLocalName()+"==="+m.get(h));			
	    			if(h.toString().contains("bpm")) s1=s1+"bpm_"+h.getLocalName()+"==="+m.get(h)+","; //System.out.println("bpm_"+h.getLocalName()+"==="+m.get(h));
	    			//s1=s1+h+"==="+m.get(h)+",";
	    		}
    			String s[] ={((WorkflowTask)o).id, ((WorkflowTask)o).name, ((WorkflowTask)o).title, ((WorkflowTask)o).description, ((java.lang.Enum<WorkflowTaskState>)((WorkflowTask)o).state).toString(),s1};
    			System.out.println(((WorkflowTask)o).id+" "+ ((WorkflowTask)o).name+" "+  ((WorkflowTask)o).title+" "+  ((WorkflowTask)o).description+" "+  ((java.lang.Enum<WorkflowTaskState>)((WorkflowTask)o).state).toString()+" "+s1.toString());
    			tasks.add(s);
	    		
	    	}
	    	System.out.println("**********************************************************");
		}

		if ( isAdmin && req.getParameter("initiator")!=null ){
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
			boolean includeTask=false;
	    	for (Object o:listtasks){
	    		Map<QName,Serializable> m=((WorkflowTask)o).properties;
	    		String s1=new String();	
	    		for (QName h:m.keySet()){
	    			if(h.toString().contains("content")) s1=s1+"cm_"+h.getLocalName()+"==="+m.get(h)+","; // System.out.println("cm_"+h.getLocalName()+"==="+m.get(h));			
	    			if(h.toString().contains("bpm")) s1=s1+"bpm_"+h.getLocalName()+"==="+m.get(h)+","; //System.out.println("bpm_"+h.getLocalName()+"==="+m.get(h));
	    			//s1=s1+h+"==="+m.get(h)+",";
    				//Filtro per i my-started-task
//    				if (h.toString().equals("{http://www.alfresco.org/model/content/1.0}userName")){
//    						 if ( ((String)m.get(h)).equals(AuthenticationUtil.getFullyAuthenticatedUser()) ){
//    							 includeTask=true;
//    						 }
//    				}(String)nodeService.getProperty(task.path.instance.initiator, ContentModel.PROP_USERNAME)
    				if ( req.getParameter("initiator").equals((String)serviceRegistry.getNodeService().getProperty( ((WorkflowTask)o).path.instance.initiator, ContentModel.PROP_USERNAME ) ) ){
    						includeTask=true;
    				}

    				//Filtro per i my-started-task

	    		}
	    		if(includeTask){
	    			String s[] ={((WorkflowTask)o).id, ((WorkflowTask)o).name, ((WorkflowTask)o).title, ((WorkflowTask)o).description, ((java.lang.Enum<WorkflowTaskState>)((WorkflowTask)o).state).toString(),s1};
	    			System.out.println(((WorkflowTask)o).id+" "+ ((WorkflowTask)o).name+" "+  ((WorkflowTask)o).title+" "+  ((WorkflowTask)o).description+" "+  ((java.lang.Enum<WorkflowTaskState>)((WorkflowTask)o).state).toString()+" "+s1.toString());
	    			tasks.add(s);
					includeTask=false;
	    		}	
	    		
	    	}
	    	System.out.println("**********************************************************");
		}
		
		
		
		
		model.put("tasks", tasks);
		
		return model;
    }    	
	
}
