package it.cnr.si.repo.jscript;


import it.cnr.si.service.cmr.security.GroupAuthorityService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authority.script.Authority;
import org.alfresco.repo.security.authority.script.ScriptGroup;
import org.alfresco.repo.security.authority.script.ScriptUser;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.ScriptPagingDetails;
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

	private PermissionService getPermissionService(){
		return services.getPermissionService();
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
     * Gets the Authority given the Authority name
     * 
     * @param authorityName  name of group to get
     * @return  the group node (type usr:authorityContainer) or null if no such group exists
     */
    public ScriptNode getAuthority(String authorityName)
    {
        ParameterCheck.mandatoryString("AuthorityName", authorityName);
        ScriptNode authority = null;
        NodeRef authorityRef;
        if (NodeRef.isNodeRef(authorityName)){
        	authorityRef = new NodeRef(authorityName);
        }else	
        	authorityRef = groupAuthorityService.getAuthorityNodeRefOrNull(authorityName);
        if (authorityRef != null)
        {
            authority = new ScriptNode(authorityRef, services, getScope());
        }
        return authority;
    }	

    public AuthorityPermission getAuthorityPermission(String authorityName) 
    {
        ParameterCheck.mandatoryString("AuthorityName", authorityName);
        AuthorityPermission authorityPermission = null;
        NodeRef authorityRef;
        if (NodeRef.isNodeRef(authorityName)){
        	authorityRef = new NodeRef(authorityName);
        }else	
        	authorityRef = groupAuthorityService.getAuthorityNodeRefOrNull(authorityName);
        if (authorityRef != null)
        {
        	authorityPermission = new AuthorityPermission(authorityRef,
				new ScriptGroup(groupAuthorityService.getAuthorityNameOrNull(authorityRef), 
				services, this.getScope()));
        	addPermission(authorityPermission, authorityRef);
        }
		return authorityPermission;
    }

    public AuthorityPermission getRootAuthorityPermission() 
    {
        AuthorityPermission authorityPermission = null;
        NodeRef authorityRef = groupAuthorityService.getAuthorityContainer();
        if (authorityRef != null)
        {
        	authorityPermission = new AuthorityPermission(authorityRef, null);
        	addPermission(authorityPermission, authorityRef);
        }
		return authorityPermission;
    }
    
    public void deleteGroup(String groupName, boolean cascade){
    	groupAuthorityService.deleteAuthority(groupAuthorityService.getAuthorityNodeRefOrNull(groupName), cascade);
    }
    
    public void deleteGroup(String groupName){
    	deleteGroup(groupName, false);
    }
    
    public AuthorityPermission[] getChildAuthorities(){
    	return getChildAuthorities(new ScriptPagingDetails(), null);
    }
    
    public AuthorityPermission[] getChildAuthorities(ScriptPagingDetails paging, String sortBy){
    	return getChildAuthorities(null, paging, sortBy);
    }
    
    public AuthorityPermission[] getChildAuthorities(String groupName){
    	return getChildAuthorities(groupName, new ScriptPagingDetails(), null);
    }
    
    public AuthorityPermission[] getChildAuthorities(String groupName, ScriptPagingDetails paging, String sortBy){
    	return getChildAuthorities(groupName, null, paging, sortBy);
    }

    public AuthorityPermission[] getChildAuthorities(String groupName, String authorityType){
    	return getChildAuthorities(groupName, authorityType, new ScriptPagingDetails(), null);
    }
    
    public class AuthorityPermission implements Authority{
    	private final Authority authority;
    	private final NodeRef nodeRef;
    	private List<String> allowableActions;
    	
		public AuthorityPermission(NodeRef nodeRef, Authority authority) {
			super();
			this.authority = authority;
			this.nodeRef = nodeRef; 
			this.allowableActions = new ArrayList<String>();
		}

		public NodeRef getNodeRef() {
			return nodeRef;
		}

		public Authority getAuthority() {
			return authority;
		}

		@Override
		public ScriptAuthorityType getAuthorityType() {
			if (authority == null)
				return null;
			return authority.getAuthorityType();
		}

		@Override
		public String getShortName() {
			if (authority == null)
				return null;
			return authority.getShortName();
		}

		@Override
		public String getFullName() {
			if (authority == null)
				return null;
			return authority.getFullName();
		}

		@Override
		public String getDisplayName() {
			if (authority == null)
				return null;
			return authority.getDisplayName();
		}

		public List<String> getAllowableActions() {
			return allowableActions;
		}
    	
		public void addAllowableActions(String key){
			allowableActions.add(key);
		}
    }
    
    private void addPermission(AuthorityPermission authorityPermission, NodeRef child){
    	if (getPermissionService().hasPermission(child, 
    			PermissionService.CREATE_CHILDREN).equals(AccessStatus.ALLOWED))
    		authorityPermission.addAllowableActions("CAN_CREATE_CHILDREN");
    	if (getPermissionService().hasPermission(child, 
    			PermissionService.CHANGE_PERMISSIONS).equals(AccessStatus.ALLOWED))
    		authorityPermission.addAllowableActions("CAN_APPLY_ACL");
    	if (getPermissionService().hasPermission(child, 
    			PermissionService.DELETE).equals(AccessStatus.ALLOWED))
    		authorityPermission.addAllowableActions("CAN_DELETE_OBJECT");
    	if (getPermissionService().hasPermission(child, 
    			PermissionService.DELETE_ASSOCIATIONS).equals(AccessStatus.ALLOWED))
    		authorityPermission.addAllowableActions("CAN_DELETE_ASSOCIATIONS");
    	if (getPermissionService().hasPermission(child, 
    			PermissionService.CREATE_ASSOCIATIONS).equals(AccessStatus.ALLOWED))
    		authorityPermission.addAllowableActions("CAN_CREATE_ASSOCIATIONS");
    	if (getPermissionService().hasPermission(child, 
    			PermissionService.WRITE_PROPERTIES).equals(AccessStatus.ALLOWED))
    		authorityPermission.addAllowableActions("CAN_UPDATE_PROPERTIES");
    }
    /**
     * Get all the children of this group, regardless of type
     */
    public AuthorityPermission[] getChildAuthorities(String groupName, String authorityType, ScriptPagingDetails paging, String sortBy)
    {
    	Set<AuthorityPermission> result = new HashSet<AuthorityPermission>();
        NodeRef groupRef = null;
        if (groupName != null){
            if (NodeRef.isNodeRef(groupName)){
            	groupRef = new NodeRef(groupName);
            }else	
            	groupRef = groupAuthorityService.getAuthorityNodeRefOrNull(groupName);
        }    
        if (authorityType == null || authorityType.equals(AuthorityType.GROUP.name())){
        	Set<NodeRef> childs = groupAuthorityService.getAllGroupAuthorities(groupRef);
        	for (NodeRef child : childs) {
        		AuthorityPermission authorityPermission = new AuthorityPermission(child,
        				new ScriptGroup(groupAuthorityService.getAuthorityNameOrNull(child), 
        				services, this.getScope()));
        		addPermission(authorityPermission, child);
        		result.add(authorityPermission);
    		}
        }
        if (authorityType == null || authorityType.equals(AuthorityType.USER.name())){
        	Set<NodeRef> childs = groupAuthorityService.getAllUserAuthorities(groupRef);
        	for (NodeRef child : childs) {
        		AuthorityPermission authorityPermission = new AuthorityPermission(child,
        				new ScriptUser(groupAuthorityService.getAuthorityNameOrNull(child), 
                				child, services, this.getScope()));
        		addPermission(authorityPermission, child);
        		result.add(authorityPermission);
    		}
        }
        return makePagedAuthority(paging, sortBy, result.toArray(new AuthorityPermission[result.size()]));
    }

    private <T extends AuthorityPermission> T[] makePagedAuthority(ScriptPagingDetails paging, String sortBy, T[] groups)
    {
        // Sort the groups
    	Arrays.sort(groups, new GroupAuthorityComparator(sortBy));

        // Now page
        int maxItems = paging.getMaxItems(); 
        int skipCount = paging.getSkipCount();
        paging.setTotalItems(groups.length);
        return ModelUtil.page(groups, maxItems, skipCount);
    }   
 
    public static class GroupAuthorityComparator implements Comparator<Authority>
    {
        private Map<Authority,String> nameCache;
        private String sortBy;
        
        public GroupAuthorityComparator(String sortBy)
        {
            this.sortBy = sortBy;
            this.nameCache = new HashMap<Authority, String>();
        }

        @Override
        public int compare(Authority g1, Authority g2)
        {
        	if (g1.getAuthorityType().equals(g2.getAuthorityType()))
        		return get(g1).compareTo( get(g2) );
        	else
        		return g1.getAuthorityType().compareTo(g2.getAuthorityType());
        }
        
        private String get(Authority g)
        {
            String v = nameCache.get(g);
            if(v == null)
            {
                // Get the value from the group
                if("displayName".equals(sortBy))
                {
                    v = g.getDisplayName(); 
                }
                else if("shortName".equals(sortBy))
                {
                    v = g.getShortName();
                }
                else
                {
                    v = g.getFullName(); 
                }
                // Lower case it for case insensitive search
                v = v.toLowerCase();
                // Cache it
                nameCache.put(g, v);
            }
            return v;
        }
    }
    
}
