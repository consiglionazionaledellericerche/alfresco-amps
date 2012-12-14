package it.cnr.alfresco.repository.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import it.cnr.alfresco.repo.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.QName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class TaskParameters extends DeclarativeWebScript
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
	   
	   //System.out.println("EDGARDO: ********************** "+workflowService.getTaskById(req.getParameter("p")).properties.get(ContentModel.PROP_DESCRIPTION));
	   Map<QName, Serializable> updateProperties = new HashMap<QName, Serializable>();
	   
	   String taskId=req.getParameter("p");
	   //System.out.println("EDGARDO: "+taskId);
	   //System.out.println("EDGARDO: "+req.getParameter("m"));
	   JSONObject json=null;
		try {
			json = new JSONObject(req.getParameter("m"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
       Iterator itr=json.keys();
       while(itr.hasNext()){
    	   String property=(String)itr.next();
    	   String propVal=new String();
    	   String[] propArr=null;
			try {//provo se è una stringa
				propVal = (String)json.get(property);
			} catch(java.lang.ClassCastException cce) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				try {//provo se è un array di stringhe
					propVal=null;
					
					final JSONArray keyArray =(JSONArray)json.get(property);
					propArr = new String[keyArray.length()];
					for(int j = 0; j < keyArray.length();++j) {
						propArr[j] = keyArray.getString(j);
						System.out.println("********************"+keyArray.getString(j));
					}
					
					
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (JSONException e) {
				e.printStackTrace();	
			}

			
			boolean non_skip=true;
			StringTokenizer st=new StringTokenizer(property,"_");
			if (st.countTokens()>1){
				String ns=st.nextToken();
				String nm=st.nextToken();
				try{
					property="{"+serviceRegistry.getNamespaceService().getNamespaceURI(ns)+"}"+nm;
					System.out.println("EDGARDO: LA PROP "+st+" CON SUFFISSO "+ns+" VIENE TRASFORMATA IN "+property);
					if(serviceRegistry.getNamespaceService().getNamespaceURI(ns)==null){
						System.out.println("Property sintax is wrong! Suffix matchs null namespace" );
						non_skip=false;
					}
				}catch(NamespaceException nse){
					System.out.println("EDGARDO: LA PROP "+st+" NON PUO ESSERE INSERITA A CAUSA DEL SUFFISSO SBAGLIATO.");
					System.out.println(nse.getMessage());
					non_skip=false;
				}
			}	
			
			if(non_skip){
				Map<QName, Serializable> existingProperties = new HashMap<QName, Serializable>();
				existingProperties=workflowService.getTaskById(taskId).properties;
				for(QName qname:existingProperties.keySet()){
					if (qname.toString().contains(property)){
						System.out.println("La prop da cambiare "+property+" trova corrispondenza con la prop: "+qname.toString());
						updateProperties.put(QName.createQName(property),propVal!=null?propVal:propArr);
						non_skip=false;
					}
					//System.out.println("La prop da cambiare "+property + " non trova corrispondenza con la prop: "+qname.toString() );
				}
				if(non_skip){
					System.out.println("La prop da cambiare "+property+" non esiste tra le props e quindi viene inserita come nuova con valore: "+propVal!=null?propVal:propArr);
					updateProperties.put(QName.createQName(property),propVal!=null?propVal:propArr);
				}
			}	
       }

       workflowService.updateTask(taskId, updateProperties, null, null);

       Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
       model.put("success", Boolean.TRUE);
       model.put("alf_ticket", req.getParameter("alf_ticket"));
       return model;
   }
}
