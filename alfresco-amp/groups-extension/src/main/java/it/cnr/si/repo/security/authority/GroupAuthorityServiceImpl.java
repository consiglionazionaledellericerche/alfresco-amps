package it.cnr.si.repo.security.authority;

import it.cnr.si.service.cmr.security.GroupAuthorityService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

public class GroupAuthorityServiceImpl implements GroupAuthorityService{

    private QName qnameAssocSystem;

    private QName qnameAssocAuthorities;

    private QName qnameAssocZones;

    /** System Container ref cache (Tennant aware) */
    private Map<String, NodeRef> systemContainerRefs = new ConcurrentHashMap<String, NodeRef>(4);

    private TenantService tenantService;

    private StoreRef storeRef;

    private NodeService nodeService;

    private AuthorityService authorityService;
    
    private PermissionService permissionService;

    private OwnableService ownableService;
    
    private AuthorityDAO authorityDAO;
    
    public void setAuthorityDAO(AuthorityDAO authorityDAO) {
		this.authorityDAO = authorityDAO;
	}

	public void setOwnableService(OwnableService ownableService) {
		this.ownableService = ownableService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
     * Return the system container for the specified assoc name.
     * The containers are cached in a thread safe Tenant aware cache.
     *
     * @param assocQName
     *
     * @return System container, <b>which must exist</b>
     */
    private NodeRef getSystemContainer(QName assocQName)
    {
        final String cacheKey = tenantService.getCurrentUserDomain() + assocQName.toString();
        NodeRef systemContainerRef = systemContainerRefs.get(cacheKey);
        if (systemContainerRef == null)
        {
            NodeRef rootNodeRef = nodeService.getRootNode(this.storeRef);
            List<ChildAssociationRef> results = nodeService.getChildAssocs(rootNodeRef, RegexQNamePattern.MATCH_ALL, qnameAssocSystem, false);
            if (results.size() == 0)
            {
                throw new AlfrescoRuntimeException("Required system path not found: " + qnameAssocSystem);
            }
            NodeRef sysNodeRef = results.get(0).getChildRef();
            results = nodeService.getChildAssocs(sysNodeRef, RegexQNamePattern.MATCH_ALL, assocQName, false);
            if (results.size() == 0)
            {
                throw new AlfrescoRuntimeException("Required path not found: " + assocQName);
            }
            systemContainerRef = results.get(0).getChildRef();
            systemContainerRefs.put(cacheKey, systemContainerRef);
        }
        return systemContainerRef;
    }        

    public void setStoreUrl(String storeUrl)
    {
        this.storeRef = new StoreRef(storeUrl);
    }
    
    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        qnameAssocSystem = QName.createQName("sys", "system", namespacePrefixResolver);
        qnameAssocAuthorities = QName.createQName("sys", "authorities", namespacePrefixResolver);
        qnameAssocZones = QName.createQName("sys", "zones", namespacePrefixResolver);
    }
    

    public NodeRef getAuthorityContainerRef()
    {
    	return getAuthorityContainer();
    }

    /**
     * @return Returns the authority container, <b>which must exist</b>
     */
    public NodeRef getAuthorityContainer()
    {
        return getSystemContainer(qnameAssocAuthorities);
    }

    /**
     * @return Returns the zone container, <b>which must exist</b>
     */
    public NodeRef getZoneContainer()
    {
        return getSystemContainer(qnameAssocZones);
    }
    
    public void addAuthority(final NodeRef parentName, final NodeRef childName) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
        	@Override
			public Void doWork() throws Exception {
            	authorityService.addAuthority(authorityDAO.getAuthorityName(parentName), 
            			authorityDAO.getAuthorityName(childName));
            	return null;
			}
		});
	}

    public void removeAuthority(final NodeRef parentName, final NodeRef childName) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
        	@Override
			public Void doWork() throws Exception {
            	authorityService.removeAuthority(authorityDAO.getAuthorityName(parentName), 
            			authorityDAO.getAuthorityName(childName));
            	return null;
			}
		});
	}
    
    public NodeRef createAuthority(NodeRef authorityParentRef, String shortName, String authorityDisplayName){
    	return createAuthority(authorityParentRef, shortName, authorityDisplayName, null);
    }
    
    public NodeRef createAuthority(final NodeRef authorityParentRef, String shortName, final String authorityDisplayName, final Set<String> authorityZones){
    	final String name = authorityService.getName(AuthorityType.GROUP, shortName);        
        NodeRef childRef = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<NodeRef>() {
        	@Override
			public NodeRef doWork() throws Exception {
        		authorityService.createAuthority(AuthorityType.GROUP, name, authorityDisplayName, authorityZones);
        		NodeRef newGroup = authorityDAO.getAuthorityNodeRefOrNull(name);
	        	if (!authorityParentRef.equals(getAuthorityContainer()))
	        		addAuthority(authorityParentRef, newGroup);
	        	return newGroup;
			}
		});
    	ownableService.takeOwnership(childRef);
    	permissionService.setPermission(childRef, AuthenticationUtil.getFullyAuthenticatedUser(), 
    			PermissionService.COORDINATOR, true);
    	return childRef;
    }
    
    public String getAuthorityNameOrNull(NodeRef nodeRef){
    	return authorityDAO.getAuthorityName(nodeRef);
    }

    public String getAuthorityDisplayNameOrNull(NodeRef nodeRef){
    	return authorityDAO.getAuthorityDisplayName( getAuthorityNameOrNull(nodeRef));
    }
    
    public NodeRef getAuthorityNodeRefOrNull(String groupName){
    	return authorityDAO.getAuthorityNodeRefOrNull(groupName);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteAuthority(NodeRef authorityNoderRef)
    {
    	authorityService.deleteAuthority(authorityDAO.getAuthorityName(authorityNoderRef), false);
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteAuthority(NodeRef authorityNoderRef, boolean cascade)
    {
    	authorityService.deleteAuthority(authorityDAO.getAuthorityName(authorityNoderRef), cascade);
    }
    
    public Set<NodeRef> getAllUserAuthorities(NodeRef parent){
    	return getAuthorities(parent, AuthorityType.USER);
    }

    public Set<NodeRef> getAllGroupAuthorities(NodeRef parent){
    	return getAuthorities(parent, AuthorityType.GROUP);
    }
    
    public Set<NodeRef> getAuthorities(NodeRef parent, AuthorityType authorityType){
    	Set<String> authorities = null;
    	if (parent == null)
    		authorities = authorityService.getAllRootAuthorities(authorityType);
    	else{
    		authorities = authorityService.getContainedAuthorities(authorityType, getAuthorityNameOrNull(parent), true);
    	}
    	Set<NodeRef> result = new HashSet<NodeRef>(authorities.size());
    	for (String authority : authorities) {
    		result.add(getAuthorityNodeRefOrNull(authority));
		}
    	return result;
    }
    
}