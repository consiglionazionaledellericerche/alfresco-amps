package it.cnr.alfresco.repository.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.apache.axis.components.uuid.UUIDGen;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.cnr.alfresco.repo.workflow.WorkflowService;
import it.cnr.alfresco.repository.authUtils.Utils;

import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ProcessList extends DeclarativeWebScript
{

	private PreferenceService preferenceService;

	public PreferenceService getPreferenceService() {
		return preferenceService;
	}

	public void setPreferenceService(PreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}

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
	protected Map<String, Object> executeImpl(WebScriptRequest req,
            Status status) 
    {
		Map<String, Object> model = new HashMap<String, Object>();
		String ticket="alf_ticket="+req.getParameter("alf_ticket");

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
	
		String defFilter=req.getParameter("p");
		String authority=req.getParameter("authority");
		Map<String,List<String>> swimlanes=new HashMap<String, List<String>>();
		List<WorkflowDefinition> defs=new ArrayList<WorkflowDefinition>();
		
		//mi aspetto una mappa che contenga un unico elemento, con chiave processDefinitions e valore array di stringhe String[]
		Map<String, Serializable> prefsx=new HashMap<String, Serializable>();
		ArrayList<String> preferences=new ArrayList<String>();
		
		if(defFilter==null){
			if(authority==null && isAdmin){
				for (WorkflowDefinition wd:this.workflowService.getDefinitions()){
					defs.add(wd);
					List<String> swimlane=this.workflowService.getSwimlaneDefinitions(wd.getId());
					swimlanes.put(wd.getId(),swimlane);
					System.out.println("**********************SWIMLANES for PROCESS: "+wd.getId()+" ***********************");
					for (String s:this.workflowService.getSwimlaneDefinitions(wd.getId())){
						System.out.println(s);
					}
					System.out.println("*********************************************");
				}
				model.put("swimlanes", swimlanes);
				model.put("defarraylength", defs.size());
				model.put("defarray", defs);
			}else if(authority!=null && isAdmin) {
				if(this.serviceRegistry.getPersonService().personExists(authority)) {
//					prefsx.put("jbpm$12", UUID.randomUUID().toString());
//					prefsx.put("jbpm$28", UUID.randomUUID().toString());
//					this.preferenceService.setPreferences(authority, prefsx);
					for(String s:this.preferenceService.getPreferences(authority).keySet()) {
						preferences.add(s);
					}
				}

				for (WorkflowDefinition wd:this.workflowService.getDefinitions()){
					if (preferences.contains(wd.getId())) {
						defs.add(wd);
						List<String> swimlane=this.workflowService.getSwimlaneDefinitions(wd.getId());
						swimlanes.put(wd.getId(),swimlane);
					}
					System.out.println("**********************SWIMLANES for PROCESS: "+wd.getId()+" ***********************");
					for (String s:this.workflowService.getSwimlaneDefinitions(wd.getId())){
						System.out.println(s);
					}
					System.out.println("*********************************************");
				}
				model.put("swimlanes", swimlanes);				
				model.put("defarraylength", defs.size());
				model.put("defarray", defs);
			}else if((authority==null || !authority.equals(authUtils.getUserName()) ) && !isAdmin) {
				model.put("swimlanes", swimlanes);				
				model.put("defarraylength", defs.size());
				model.put("defarray", defs);
			}else if(authority.equals(authUtils.getUserName()) && !isAdmin) {
				if(this.serviceRegistry.getPersonService().personExists(authority)) {
//					prefsx.put("jbpm$12", UUID.randomUUID().toString());
//					prefsx.put("jbpm$28", UUID.randomUUID().toString());
//					this.preferenceService.setPreferences(authority, prefsx);
					for(String s:this.preferenceService.getPreferences(authority).keySet()) {
						preferences.add(s);
					}
				}
				for (WorkflowDefinition wd:this.workflowService.getDefinitions()){
					if (preferences.contains(wd.getId())) {
						defs.add(wd);
						List<String> swimlane=this.workflowService.getSwimlaneDefinitions(wd.getId());
						swimlanes.put(wd.getId(),swimlane);
					}
					System.out.println("**********************SWIMLANES for PROCESS: "+wd.getId()+" ***********************");
					for (String s:this.workflowService.getSwimlaneDefinitions(wd.getId())){
						System.out.println(s);
					}
					System.out.println("*********************************************");
				}
				model.put("swimlanes", swimlanes);				
				model.put("defarraylength", defs.size());
				model.put("defarray", defs);
			}
		}else{
			if(authority==null && isAdmin){
				WorkflowDefinition def= this.workflowService.getDefinitionById(defFilter);
				defs.add(def);
				List<String> swimlane=this.workflowService.getSwimlaneDefinitions(def.getId());
				swimlanes.put(def.getId(),swimlane);
				model.put("swimlanes",swimlanes);
				model.put("defarraylength", 1);
				model.put("defarray", defs);
			}else if(authority!=null && isAdmin) {
				if(this.serviceRegistry.getPersonService().personExists(authority)) {
//					prefsx.put("jbpm$12", UUID.randomUUID().toString());
//					prefsx.put("jbpm$28", UUID.randomUUID().toString());
//					this.preferenceService.setPreferences(authority, prefsx);
					for(String s:this.preferenceService.getPreferences(authority).keySet()) {
						preferences.add(s);
					}
				}
				WorkflowDefinition def;
				List<String> swimlane;
				WorkflowDefinition wd=this.workflowService.getDefinitionById(defFilter);
				if (preferences.contains(wd.getId())){
					def=wd;
					defs.add(def);
					swimlane=this.workflowService.getSwimlaneDefinitions(def.getId());
					swimlanes.put(def.getId(),swimlane);
				}else{
					def=null;
					defs.add(def);
					swimlanes=null;
				}
				System.out.println("**********************SWIMLANES for PROCESS: "+wd.getId()+" ***********************");
				for (String s:this.workflowService.getSwimlaneDefinitions(wd.getId())){
					System.out.println(s);
				}
				System.out.println("*********************************************");

				model.put("swimlanes",swimlanes);
				model.put("defarraylength", 1);
				model.put("defarray", defs);
			}else if((authority==null || !authority.equals(authUtils.getUserName()) ) && !isAdmin) {
				model.put("swimlanes", swimlanes);				
				model.put("defarraylength", defs.size());
				model.put("defarray", defs);
			}else if(authority.equals(authUtils.getUserName()) && !isAdmin) {
				if(this.serviceRegistry.getPersonService().personExists(authority)) {
//					prefsx.put("jbpm$12", UUID.randomUUID().toString());
//					prefsx.put("jbpm$28", UUID.randomUUID().toString());
//					this.preferenceService.setPreferences(authority, prefsx);
					for(String s:this.preferenceService.getPreferences(authority).keySet()) {
						preferences.add(s);
					}
				}
				for (WorkflowDefinition wd:this.workflowService.getDefinitions()){
					if (preferences.contains(wd.getId())) {
						defs.add(wd);
						List<String> swimlane=this.workflowService.getSwimlaneDefinitions(wd.getId());
						swimlanes.put(wd.getId(),swimlane);
					}
					System.out.println("**********************SWIMLANES for PROCESS: "+wd.getId()+" ***********************");
					for (String s:this.workflowService.getSwimlaneDefinitions(wd.getId())){
						System.out.println(s);
					}
					System.out.println("*********************************************");
				}
				model.put("swimlanes", swimlanes);				
				model.put("defarraylength", defs.size());
				model.put("defarray", defs);
			}	
		}
		return model;
    }    
}
