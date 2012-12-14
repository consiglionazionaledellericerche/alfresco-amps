package it.cnr.alfresco.repository.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import it.cnr.alfresco.repo.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class TaskDocuments extends DeclarativeWebScript
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
	   
	   Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
	   Map<QName, List<NodeRef>> updateBundles = new HashMap<QName, List<NodeRef>>();
	   Map<QName, Serializable> updateProperties = new HashMap<QName, Serializable>();
	   
	   System.out.println("***************START REMOVE/ADD ACTIVITY*******************");
	   
	   if(req.getParameter("o")==null || req.getParameter("o").equals("remove")){ //di default assumo che il metodo sia remove
		   System.out.println("***************REMOVE ACTIVITY*******************");
		   String taskId=req.getParameter("p");
		   JSONObject json=null;
			try {
				json = new JSONObject(req.getParameter("m"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   
		   List<NodeRef> listNodeRef=new ArrayList<NodeRef>();	
	       Iterator itr=json.keys();
	       NodeRef nodeRef=null;
	       while(itr.hasNext()){
	    	   String property=(String)itr.next();
				try {
					System.out.println("EDGARDO:************** Tentativo di rimuovere il doc: "+ (String)json.get(property));
					nodeRef = new NodeRef((String)json.get(property));
					listNodeRef.add(nodeRef);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//IL METODO UPDATETASK sembra non funzionare per la rimozione dei doc da un task
				//updateProperties.put(WorkflowModel.ASSOC_PACKAGE,new NodeRef("workspace://SpacesStore/aa5a747b-1eb6-442e-978a-390b27e15660"));
	    	    //updateBundles.put(WorkflowModel.ASSOC_PACKAGE_CONTAINS,listNodeRef);

	       }
	       //workflowService.updateTask(taskId, updateProperties, null, updateBundles);
	       
	       removeFromBundle(taskId, nodeRef);

	       
	       System.out.println("***************END REMOVE/ADD ACTIVITY*******************");
		   model.put("success", Boolean.TRUE);
	       model.put("alf_ticket", req.getParameter("alf_ticket"));
	       return model;
   
    }
	   	  System.out.println("***************END REMOVE/ADD ACTIVITY*******************");
       	  model.put("success", Boolean.FALSE);
	   	  return model;
   }

	private boolean removeFromBundle(String task, NodeRef docNodeRefToRemove){
		boolean remove=false;
		WorkflowTask workflowTask = this.workflowService.getTaskById(task);
		//nodeRef root del package associato al workflow
		NodeRef nr=(NodeRef)workflowTask.properties.get(WorkflowModel.ASSOC_PACKAGE); //node ref del bundle
		NodeService ns=this.serviceRegistry.getNodeService();
		
		for (Object o:ns.getChildAssocs(nr)){
			System.out.println(nr);
			System.out.println(o);
			ChildAssociationRef car=((ChildAssociationRef)o);
			StringTokenizer s=new StringTokenizer(car.toString(),"|");
			s.nextToken(); //salto 1 per avere il nodRef del documento associato
			String docNodeRef = s.nextToken(); 
			System.out.println("EDGARDO: Il doc da rimuovere è "+ docNodeRefToRemove);
			System.out.println("EDGARDO: Il doc trovato è "+ docNodeRef);
			if(docNodeRefToRemove.toString().equals(docNodeRef)){
				ns.removeChildAssociation(car);
				System.out.println("EDGARDO: Rimozione Riuscita");
				remove=true;
				break;
			}
		}	
		
		return remove==true?true:false;
	}

}
