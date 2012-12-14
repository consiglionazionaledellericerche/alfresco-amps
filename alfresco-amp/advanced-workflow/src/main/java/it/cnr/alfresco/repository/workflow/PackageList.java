package it.cnr.alfresco.repository.workflow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import it.cnr.alfresco.repo.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class PackageList extends DeclarativeWebScript
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
	
		if(req.getParameter("p")==null){
			java.util.List<String> contents=new ArrayList<String>();
			WorkflowTask workflowTask;
			for (int i=0;i<100;i++){
				try{
					workflowTask = this.workflowService.getTaskById("jbpm$"+i);
					java.util.List<String> contentsx;
					contentsx=getBundle(workflowTask.id);
					for(Object o:contentsx){
						contents.add((String)o);
					}
				}catch(Exception e){
					continue;
				}
			}
			model.put("packages", contents);
		}else{
			model.put("packages", getBundle(req.getParameter("p")));
		}	
		return model;
    }    
	
	private java.util.List<String> getBundle(String task){

//		List<NodeRef> bundle=this.workflowService.getPackageContents(req.getParameter("p"));
//		for (Object o:bundle){
//			System.out.println(o);
//		}
		String user=AuthenticationUtil.getFullyAuthenticatedUser();
		if ("admin".equals(user)){
			WorkflowTask workflowTask = this.workflowService.getTaskById(task);
			//nodeRef root del package associato al workflow
			NodeRef nr=(NodeRef)workflowTask.properties.get(WorkflowModel.ASSOC_PACKAGE);
			NodeService ns=this.serviceRegistry.getNodeService();
			for (Object o:ns.getChildAssocs(nr)){
				System.out.println(nr);
				System.out.println(o);
			}	
			//System.out.println(nr.getId());
			//System.out.println(ns.getProperties(nr));
			java.util.List<String> contents=new ArrayList<String>();
	   		//contents.add(nr.getStoreRef()+nr.getId());
			for (Object o:ns.getChildAssocs(nr)){
				ChildAssociationRef car=((ChildAssociationRef)o);
				StringTokenizer s=new StringTokenizer(car.toString(),"|");
				//s.nextToken();s.nextToken();s.nextToken(); //salto 4 per avere il nome del documento associato
				s.nextToken(); //salto 1 per avere il nodRef del documento associato
				contents.add(s.nextToken()+"|"+task);
			}	
			return contents;
		}else{
			WorkflowTask workflowTask = this.workflowService.getTaskById(task);
			String owner=(String)workflowTask.properties.get(ContentModel.PROP_OWNER);
			if (user.equals(owner)){
				//nodeRef root del package associato al workflow
				NodeRef nr=(NodeRef)workflowTask.properties.get(WorkflowModel.ASSOC_PACKAGE);
				NodeService ns=this.serviceRegistry.getNodeService();
				for (Object o:ns.getChildAssocs(nr)){
					System.out.println(nr);
					System.out.println(o);
				}	
				//System.out.println(nr.getId());
				//System.out.println(ns.getProperties(nr));
				java.util.List<String> contents=new ArrayList<String>();
		   		//contents.add(nr.getStoreRef()+nr.getId());
				for (Object o:ns.getChildAssocs(nr)){
					ChildAssociationRef car=((ChildAssociationRef)o);
					StringTokenizer s=new StringTokenizer(car.toString(),"|");
					//s.nextToken();s.nextToken();s.nextToken(); //salto 4 per avere il nome del documento associato
					s.nextToken(); //salto 1 per avere il nodRef del documento associato
					contents.add(s.nextToken()+"|"+task);
				}	
				return contents;
			}else{
				java.util.List<String> contents=new ArrayList<String>();
				return contents;
			}
			
		}
		
	}
	
}
