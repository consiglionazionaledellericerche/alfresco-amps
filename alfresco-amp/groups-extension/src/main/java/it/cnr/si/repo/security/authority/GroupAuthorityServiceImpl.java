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
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service("GroupAuthorityService")
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

    @Override
    @PreAuthorize("hasPermission(#parentName, 'sys:base.CreateChildren') and hasPermission(#childName, 'sys:base.ReadProperties')")
    public void addAuthority(NodeRef parentName, NodeRef childName) {
		AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {
				groupAuthorityDAO.addAuthority(Collections.singleton(groupAuthorityDAO.getAuthorityName(parentName)),
						groupAuthorityDAO.getAuthorityName(childName));
				return null;
			}
		});
	}
    @Override
    @PreAuthorize("hasPermission(#parentName, 'sys:base.DeleteChildren') and hasPermission(#childName, 'sys:base.ReadProperties')")
    public void removeAuthority(NodeRef parentName, NodeRef childName) {
		AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {
    		groupAuthorityDAO.removeAuthority(groupAuthorityDAO.getAuthorityName(parentName),
    			groupAuthorityDAO.getAuthorityName(childName));
				return null;
			}
		});
	}
    @Override
    @PreAuthorize("hasPermission(#authorityParentRef, 'sys:base.CreateChildren')")
    public NodeRef createAuthority(NodeRef authorityParentRef, String shortName, String authorityDisplayName){
		return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<NodeRef>() {
			@Override
			public NodeRef doWork() throws Exception {
    			return createAuthority(authorityParentRef, shortName, authorityDisplayName, null, null);
			}
		});
    }
    @Override
    @PreAuthorize("hasPermission(#authorityParentRef, 'sys:base.CreateChildren')")
    public NodeRef createAuthority(final NodeRef authorityParentRef, String shortName, final String authorityDisplayName, final String... authorityZones){
		return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<NodeRef>() {
			@Override
			public NodeRef doWork() throws Exception {

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
			}
		});
    }
    @Override
    @PreAuthorize("hasPermission(#nodeRef, 'sys:base.ReadProperties')")
    public String getAuthorityNameOrNull(NodeRef nodeRef){
		return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<String>() {
			@Override
			public String doWork() throws Exception {
    			return groupAuthorityDAO.getAuthorityName(nodeRef);
			}
		});
    }

    @PreAuthorize("hasPermission(#nodeRef, 'sys:base.ReadProperties')")
    public String getAuthorityDisplayNameOrNull(NodeRef nodeRef){
		return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<String>() {
			@Override
			public String doWork() throws Exception {
    			return groupAuthorityDAO.getAuthorityDisplayName( getAuthorityNameOrNull(nodeRef));
			}
		});
    }
    @Override
    @PreAuthorize("hasPermission(#nodeRef, 'sys:base.ReadProperties')")
    public NodeRef getAuthorityNodeRefOrNull(String groupName){
		return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<NodeRef>() {
		  	@Override
			public NodeRef doWork() throws Exception {
				return groupAuthorityDAO.getAuthorityNodeRefOrNull(groupName);
			}
		});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasPermission(#authorityNodeRef, 'sys:base.DeleteChildren')")
    public void deleteAuthority(NodeRef authorityNodeRef) {
		AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {
				authorityService.deleteAuthority(groupAuthorityDAO.getAuthorityName(authorityNodeRef), false);
				return null;
			}
		});
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasPermission(#authorityNodeRef, 'sys:base.DeleteChildren')")
    public void deleteAuthority(NodeRef authorityNodeRef, boolean cascade) {
		AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {
				authorityService.deleteAuthority(groupAuthorityDAO.getAuthorityName(authorityNodeRef), cascade);
				return null;
			}
		});
    }
    @Override
    @PreAuthorize("hasPermission(#parent, 'sys:base.ReadChildren')")
    public Set<NodeRef> getAllUserAuthorities(NodeRef parent, List<String> zones){
		return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Set<NodeRef>>() {
			@Override
			public Set<NodeRef> doWork() throws Exception {
    			return getAuthorities(parent, AuthorityType.USER, zones);
			}
		});
    }
    @Override
    @PreAuthorize("hasPermission(#parent, 'sys:base.ReadChildren')")
    public Set<NodeRef> getAllGroupAuthorities(NodeRef parent, List<String> zones){
		return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Set<NodeRef>>() {
			@Override
			public Set<NodeRef> doWork() throws Exception {
    			return getAuthorities(parent, AuthorityType.GROUP, zones);
			}
		});
    }

    @Override
    @PreAuthorize("hasPermission(#parent, 'sys:base.ReadChildren')")
    public Set<NodeRef> getAuthorities(NodeRef parent, AuthorityType authorityType, List<String> zones){
		return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Set<NodeRef>>() {
			@Override
			public Set<NodeRef> doWork() throws Exception {
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
			}
		});
    }

	@Override
	public Set<String> getAuthorityZones(String name) {
		return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Set<String>>() {
			@Override
			public Set<String> doWork() throws Exception {
				return authorityService.getAuthorityZones(name);
			}
		});

	}

	@Override
	public void addAuthorityToZones(String authorityName, String... zones) {
		AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {
				authorityService.addAuthorityToZones(authorityName, new HashSet<String>(Arrays.asList(zones)));
				return null;
			}
		});

	}

	@Override
	public void removeAuthorityFromZones(String authorityName, String... zones) {
		AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {
				authorityService.removeAuthorityFromZones(authorityName, new HashSet<String>(Arrays.asList(zones)));
				return null;
			}
		});
	}
}
