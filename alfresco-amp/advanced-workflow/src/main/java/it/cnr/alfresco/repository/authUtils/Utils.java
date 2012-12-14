package it.cnr.alfresco.repository.authUtils;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;

public class Utils {

	private ServiceRegistry serviceRegistry;
	private String userName;
	
	public ServiceRegistry getServiceRegistry() {
		return this.serviceRegistry;
	}
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public String getUserName() {
		return this.userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
 
	 
    public Set<String> getGroups() {
    	try{
    		Set<String> auth=this.serviceRegistry.getAuthorityService().getAuthoritiesForUser(getUserName());
    		return auth;
    	}catch(AccessDeniedException ade){
    		Set<String> auth=null;
    		return auth; 
    	}
    }	
	
}
