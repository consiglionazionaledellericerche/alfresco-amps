/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.alfresco.repository.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;

import it.cnr.alfresco.repository.authUtils.Utils;
import  it.cnr.alfresco.repository.workflow.WorkflowModelBuilder;

//blocco di codice valido dalla versione 3.4.6 di alfresco
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
//blocco di codice valido dalla versione 3.4.6 di alfresco

import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Smith
 *
 */
public class TaskInstancesGet extends AbstractWorkflowWebscript
{
    public static final String PARAM_AUTHORITY = "authority";
    public static final String PARAM_STATUS= "status";
    public static final String PARAM_PROPERTIES= "properties";
    
    
    //blocco di codice valido dalla versione 3.4.6 di alfresco
    protected DictionaryService dictionaryService;
    protected AuthenticationService authenticationService;
    protected AuthorityService authorityService;
    public AuthorityService getAuthorityService() {
		return authorityService;
	}
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}
	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
		System.out.println("EDGARDOOOOOOOOOOOOO  Authentication "+this.authorityService);
	}
	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	public ServiceRegistry getServiceRegistry() {
		return this.serviceRegistry;
	}
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
		System.out.println("EDGARDOOOOOOOOOOOOO  service "+this.serviceRegistry);
	}
	//blocco di codice valido dalla versione 3.4.6 di alfresco
	
	
	
	
	@Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status,
            Cache cache)
    {
    	String authority = getAuthority(req);
        WorkflowTaskState state = getState(req);
        List<String> properties = getProperties(req);

        //TODO Handle possible thrown exceptions here?
        List<WorkflowTask> tasks = workflowService.getAssignedTasks(authority, state);
        List<WorkflowTask> pooledTasks= workflowService.getPooledTasks(authority);
        ArrayList<WorkflowTask> allTasks = new ArrayList<WorkflowTask>(tasks.size() + pooledTasks.size());
        allTasks.addAll(tasks);
        allTasks.addAll(pooledTasks);
        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        String initiator=new String();
        String instanceId=new String();
        for (WorkflowTask task : allTasks) 
        {

        	/*EDGARDO:AGGIUNGO IL SEGUENTE*/
         	 Map<QName, Serializable> updateProperties = new HashMap<QName, Serializable>();

         	 initiator=(String)nodeService.getProperty(task.path.instance.initiator, ContentModel.PROP_USERNAME);
   		     updateProperties.put(QName.createQName(ContentModel.PROP_USERNAME.toString()),initiator);
			 instanceId=task.getPath().getInstance().getId();
			 updateProperties.put(QName.createQName(WorkflowModel.PROP_WORKFLOW_DEFINITION_ID.toString()),instanceId);

			 workflowService.updateTask(task.id, updateProperties, null, null);

			 
			 /*EDGARDO:*/

			 results.add(modelBuilder.buildSimple(task, properties));
			 
        }
        
        
        System.out.println("*************LISTA PROPRIETA' TASK INSTANCES********************");
        ArrayList<Map<String, Object>> resultsExtended = new ArrayList<Map<String, Object>>();
        for(Map<String,Object> m:results){
        	Map<String, Object> mapExtended=new HashMap<String,Object>();
        	for (String s:m.keySet()){
        		System.out.println(s+"  "+m.get(s));
       			mapExtended.put(s, m.get(s));
        	}
        	mapExtended.put("initiator", initiator);
        	mapExtended.put("instanceId", instanceId);
        	resultsExtended.add(mapExtended);
        }
        System.out.println("*************LISTA PROPRIETA' TASK INSTANCES********************");

        Map<String, Object> model = new HashMap<String, Object>();

        if(results.isEmpty()){	
        	model.put("taskInstances", results);
        }else{	
        	model.put("taskInstances", resultsExtended);
        }	
    
        return model;
    }

    private List<String> getProperties(WebScriptRequest req)
    {
        String propertiesStr = req.getParameter(PARAM_PROPERTIES);
        if(propertiesStr != null)
        {
            return Arrays.asList(propertiesStr.split(","));
        }
        return null;
    }

    /**
     * Gets the specified {@link WorkflowTaskState}, defaults to IN_PROGRESS.
     * @param req
     * @return
     */
    private WorkflowTaskState getState(WebScriptRequest req)
    {
        String stateName= req.getParameter(PARAM_STATUS);
        if(stateName != null)
        {
            try
            {
                return WorkflowTaskState.valueOf(stateName.toUpperCase());
            }
            catch(IllegalArgumentException e)
            {
                String msg = "Unrecognised State parameter:  "+stateName;
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
            }
        }
        // Defaults to IN_PROGRESS.
        return WorkflowTaskState.IN_PROGRESS;
    }

    /**
     * Returns the specified authority. If no authority is specified then returns the current Fully Authenticated user.
     * @param req
     * @return
     */
    /*EDGARDO: Sostituisco il seguente*/
/*    private String getAuthority(WebScriptRequest req)
    {
    	    	
    	String authority = req.getParameter(PARAM_AUTHORITY);
        if(authority == null)
        {
            authority = AuthenticationUtil.getFullyAuthenticatedUser();
        }
        return authority;
    }
*/
    private String getAuthority(WebScriptRequest req)
    {
    	
		Utils authUtils =new Utils();
		authUtils.setServiceRegistry(this.serviceRegistry);
		authUtils.setUserName(AuthenticationUtil.getFullyAuthenticatedUser());
		boolean isAdmin=false;
		if (authUtils.getGroups()!=null){
			//fino alla 3.3.5 il seguente vale GROUP_ALFRESCO_ADMINISTRATORS
			if (authUtils.getGroups().contains("GROUP_ALFRESCO_ADMINISTRATORS")) {
				isAdmin=true;
			}else if(authUtils.getGroups().contains("ALFRESCO_ADMINISTRATORS")) {
				isAdmin=true;
			}
			System.out.println(isAdmin);
		}else{
			System.out.println(isAdmin);
		}
		
    	
    	String authority = req.getParameter(PARAM_AUTHORITY);
    	if(authority == null)
        {
    		System.out.println("********************************1");
            authority = AuthenticationUtil.getFullyAuthenticatedUser();
            return authority;
        }
    	if(authority.equals(AuthenticationUtil.getFullyAuthenticatedUser()))
        {
    		System.out.println("********************************2");
            authority = AuthenticationUtil.getFullyAuthenticatedUser();
            return authority;
        }
    	if(!isAdmin && !authority.equals(AuthenticationUtil.getFullyAuthenticatedUser()))
        {
    		System.out.println("********************************3");
    		authority = null;
            return authority;
        }
    	if(isAdmin)
        {
    		System.out.println("********************************4");
            return authority;
        }
    	System.out.println(AuthenticationUtil.getFullyAuthenticatedUser()+" ====== "+authority);
        return null;
    }


    
    
}
