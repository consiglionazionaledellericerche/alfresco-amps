package it.cnr.si.repo.security.authority;

import it.cnr.si.service.cmr.security.GroupAuthorityService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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

    private AuthorityDAO groupAuthorityDAO;

    private TenantService tenantService;

    private StoreRef storeRef;

    private NodeService nodeService;

    private AuthorityService authorityService;
    
    private PermissionService permissionService;

    private OwnableService ownableService;
    
    public void setOwnableService(OwnableService ownableService) {
		this.ownableService = ownableService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setGroupAuthorityDAO(AuthorityDAO groupAuthorityDAO) {
		this.groupAuthorityDAO = groupAuthorityDAO;
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
    
    public void addAuthority(NodeRef parentName, NodeRef childName) {
		try {
			AuthenticationUtil.setRunAsUserSystem();
			groupAuthorityDAO.addAuthority(Collections.singleton(groupAuthorityDAO.getAuthorityName(parentName)),
					groupAuthorityDAO.getAuthorityName(childName));
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}
	}

    public void removeAuthority(NodeRef parentName, NodeRef childName) {
		try {
			AuthenticationUtil.setRunAsUserSystem();
    		groupAuthorityDAO.removeAuthority(groupAuthorityDAO.getAuthorityName(parentName),
    			groupAuthorityDAO.getAuthorityName(childName));
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}
	}
    
    public NodeRef createAuthority(NodeRef authorityParentRef, String shortName, String authorityDisplayName){
		try {
			AuthenticationUtil.setRunAsUserSystem();
    		return createAuthority(authorityParentRef, shortName, authorityDisplayName, null, null);
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}
    }
    
    public NodeRef createAuthority(final NodeRef authorityParentRef, String shortName, final String authorityDisplayName, final String... authorityZones){
		try {
			AuthenticationUtil.setRunAsUserSystem();
			final String name = authorityService.getName(AuthorityType.GROUP, shortName);
			NodeRef childRef = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<NodeRef>() {
				@Override
				public NodeRef doWork() throws Exception {
					Set<String> zones = new HashSet<String>();
					if (authorityZones == null)
						zones.add(AuthorityService.ZONE_APP_DEFAULT);
					else
						zones.addAll(Arrays.asList(authorityZones));
					groupAuthorityDAO.createAuthority(name, authorityDisplayName, zones);
					NodeRef newGroup = groupAuthorityDAO.getAuthorityNodeRefOrNull(name);
					if (!authorityParentRef.equals(getAuthorityContainer()))
						addAuthority(authorityParentRef, newGroup);
					return newGroup;
				}
			});
			ownableService.takeOwnership(childRef);
			permissionService.setPermission(childRef, AuthenticationUtil.getFullyAuthenticatedUser(),
					PermissionService.FULL_CONTROL, true);
			return childRef;
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}
    }
    
    public String getAuthorityNameOrNull(NodeRef nodeRef){
		try {
			AuthenticationUtil.setRunAsUserSystem();
    		return groupAuthorityDAO.getAuthorityName(nodeRef);
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}
    }

    public String getAuthorityDisplayNameOrNull(NodeRef nodeRef){
		try {
			AuthenticationUtil.setRunAsUserSystem();
    		return groupAuthorityDAO.getAuthorityDisplayName( getAuthorityNameOrNull(nodeRef));
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}

    }
    
    public NodeRef getAuthorityNodeRefOrNull(String groupName){
		try {
			AuthenticationUtil.setRunAsUserSystem();
    		return groupAuthorityDAO.getAuthorityNodeRefOrNull(groupName);
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}
    }

    /**
     * {@inheritDoc}
     */
    public void deleteAuthority(NodeRef authorityNoderRef) {
		try {
			AuthenticationUtil.setRunAsUserSystem();
    		authorityService.deleteAuthority(groupAuthorityDAO.getAuthorityName(authorityNoderRef), false);
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteAuthority(NodeRef authorityNoderRef, boolean cascade) {
		try {
			AuthenticationUtil.setRunAsUserSystem();
    		authorityService.deleteAuthority(groupAuthorityDAO.getAuthorityName(authorityNoderRef), cascade);
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}
    }
    
    public Set<NodeRef> getAllUserAuthorities(NodeRef parent, List<String> zones){
		try {
			AuthenticationUtil.setRunAsUserSystem();
    		return getAuthorities(parent, AuthorityType.USER, zones);
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}
    }

    public Set<NodeRef> getAllGroupAuthorities(NodeRef parent, List<String> zones){
		try {
			AuthenticationUtil.setRunAsUserSystem();
    		return getAuthorities(parent, AuthorityType.GROUP, zones);
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}
    }
    
    public Set<NodeRef> getAuthorities(NodeRef parent, AuthorityType authorityType, List<String> zones){
		try {
			AuthenticationUtil.setRunAsUserSystem();
			Set<String> authorities = null;
			if (parent == null) {
				if (zones != null && zones.size() > 0) {
					authorities = new HashSet<String>();
					for (String zone : zones) {
						authorities.addAll(authorityService.getAllRootAuthoritiesInZone(zone, authorityType));
					}
				}else
					authorities = authorityService.getAllRootAuthorities(authorityType);
			} else{
				if (zones != null) {
					Set<String> authoritiesInZones = new HashSet<String>();
					authorities = authorityService.getContainedAuthorities(authorityType, getAuthorityNameOrNull(parent), true);
					for (String zoneName : zones) {
						authoritiesInZones.addAll(authorityService.getAllAuthoritiesInZone(zoneName, authorityType));
					}
					for (Iterator<String> iterator = authorities.iterator(); iterator.hasNext();) {
						String authority = iterator.next();
						if (!authoritiesInZones.contains(authority)){
							iterator.remove();
						}
					}
				}else
					authorities = authorityService.getContainedAuthorities(authorityType, getAuthorityNameOrNull(parent), true);
			}
			Set<NodeRef> result = new HashSet<NodeRef>(authorities.size());
			for (String authority : authorities) {
				result.add(getAuthorityNodeRefOrNull(authority));
			}
			return result;
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}
    }

	@Override
	public Set<String> getAuthorityZones(String name) {
		try {
			AuthenticationUtil.setRunAsUserSystem();
			return authorityService.getAuthorityZones(name);
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}
	}

	@Override
	public void addAuthorityToZones(String authorityName, String... zones) {
		try {
			AuthenticationUtil.setRunAsUserSystem();
			authorityService.addAuthorityToZones(authorityName, new HashSet<String>(Arrays.asList(zones)));
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}
	}

	@Override
	public void removeAuthorityFromZones(String authorityName, String... zones) {
		try {
			AuthenticationUtil.setRunAsUserSystem();
			authorityService.removeAuthorityFromZones(authorityName, new HashSet<String>(Arrays.asList(zones)));
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
		}
	}
}
