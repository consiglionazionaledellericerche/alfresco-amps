package it.cnr.si.repo.jscript;

import it.cnr.si.service.cmr.security.GroupAuthorityService;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.springframework.extensions.surf.util.ParameterCheck;

public class GroupAuthority extends BaseScopableProcessorExtension {
	private GroupAuthorityService groupAuthorityService;
	private ServiceRegistry services;
	
	public void setServices(ServiceRegistry services) {
		this.services = services;
	}

	public void setGroupAuthorityService(GroupAuthorityService groupAuthorityService) {
		this.groupAuthorityService = groupAuthorityService;
	}

	private AuthorityService getAuthorityService(){
		return services.getAuthorityService();
	}
    /**
     * AddAuthority as a child of group parent
     * @param fullAuthorityName the full name of the authority to add to this group.
     */
    public void addAuthority(String parentName, String childName)
    {
    	ParameterCheck.mandatoryString("parentName", parentName);
    	ParameterCheck.mandatoryString("childName", childName);
    	NodeRef nodeRefParent = groupAuthorityService.getAuthorityNodeRefOrNull(parentName);
    	NodeRef nodeRefChild = groupAuthorityService.getAuthorityNodeRefOrNull(childName);
    	groupAuthorityService.addAuthority(nodeRefParent, nodeRefChild);
    }

    /**
     * RemoveAuthority as a child of group parent
     * @param fullAuthorityName the full name of the authority to add to this group.
     */
    public void removeAuthority(String parentName, String childName)
    {
    	ParameterCheck.mandatoryString("parentName", parentName);
    	ParameterCheck.mandatoryString("childName", childName);
    	NodeRef nodeRefParent = groupAuthorityService.getAuthorityNodeRefOrNull(parentName);
    	NodeRef nodeRefChild = groupAuthorityService.getAuthorityNodeRefOrNull(childName);
    	groupAuthorityService.removeAuthority(nodeRefParent, nodeRefChild);
    }
	
    /**
     * Create a new root level group with the specified unique name
     * 
     * @param groupName     The unique group name to create - NOTE: do not prefix with "GROUP_"
     * 
     * @return the group reference if successful or null if failed
     */
    public ScriptNode createGroup(String groupName, String groupDisplayName)
    {
        return createGroup(null, groupName, groupDisplayName);
    }
    
    /**
     * Create a new group with the specified unique name
     * 
     * @param parentGroup   The parent group node - can be null for a root level group
     * @param groupName     The unique group name to create - NOTE: do not prefix with "GROUP_"
     * 
     * @return the group reference if successful or null if failed
     */
    public ScriptNode createGroup(ScriptNode parentGroup, String groupName, String groupDisplayName)
    {
        ParameterCheck.mandatoryString("GroupName", groupName);
        
        ScriptNode group = null;
        
        String actualName = getAuthorityService().getName(AuthorityType.GROUP, groupName);
        if (getAuthorityService().authorityExists(actualName) == false)
        {
        	NodeRef result = groupAuthorityService.createAuthority(
        			parentGroup==null ? groupAuthorityService.getAuthorityContainer():parentGroup.getNodeRef(), 
        					groupName, groupDisplayName);

        	group = new ScriptNode(result, services, getScope());
        }
        
        return group;
    }
    
    /**
     * Gets the Group given the group name
     * 
     * @param groupName  name of group to get
     * @return  the group node (type usr:authorityContainer) or null if no such group exists
     */
    public ScriptNode getGroup(String groupName)
    {
        ParameterCheck.mandatoryString("GroupName", groupName);
        ScriptNode group = null;
        NodeRef groupRef = groupAuthorityService.getAuthorityNodeRefOrNull(groupName);
        if (groupRef != null)
        {
            group = new ScriptNode(groupRef, services, getScope());
        }
        return group;
    }	

}
