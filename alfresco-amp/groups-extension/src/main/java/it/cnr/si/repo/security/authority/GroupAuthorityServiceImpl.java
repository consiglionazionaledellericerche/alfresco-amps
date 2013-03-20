package it.cnr.si.repo.security.authority;

import it.cnr.si.service.cmr.security.GroupAuthorityService;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

public class GroupAuthorityServiceImpl implements GroupAuthorityService{

	private NamespacePrefixResolver namespacePrefixResolver;

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
        this.namespacePrefixResolver = namespacePrefixResolver;
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
    	groupAuthorityDAO.addAuthority(Collections.singleton(groupAuthorityDAO.getAuthorityName(parentName)), 
    			groupAuthorityDAO.getAuthorityName(childName));
	}

    public void removeAuthority(NodeRef parentName, NodeRef childName) {
    	groupAuthorityDAO.removeAuthority(groupAuthorityDAO.getAuthorityName(parentName), 
    			groupAuthorityDAO.getAuthorityName(childName));
	}
    
    public NodeRef createAuthority(NodeRef authorityParentRef, String shortName, String authorityDisplayName){
    	return createAuthority(authorityParentRef, shortName, authorityDisplayName, null);
    }
    
    public NodeRef createAuthority(NodeRef authorityParentRef, String shortName, String authorityDisplayName, Set<String> authorityZones){
    	final String name = authorityService.getName(AuthorityType.GROUP, shortName);        
    	final HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_AUTHORITY_NAME, name);
        props.put(ContentModel.PROP_AUTHORITY_DISPLAY_NAME, authorityDisplayName);
        NodeRef childRef = null;
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<NodeRef>() {
        	@Override
			public NodeRef doWork() throws Exception {
				return nodeService.createNode(getAuthorityContainer(), ContentModel.ASSOC_CHILDREN, 
		        		QName.createQName("cm", name, namespacePrefixResolver),
		                ContentModel.TYPE_AUTHORITY_CONTAINER, props).getChildRef();
			}
        	
		});
        if (authorityZones != null)
        {
            Set<NodeRef> zoneRefs = new HashSet<NodeRef>(authorityZones.size() * 2);
            for (String authorityZone : authorityZones)
            {
                zoneRefs.add(authorityService.getOrCreateZone(authorityZone));
            }
            nodeService.addChild(zoneRefs, childRef, ContentModel.ASSOC_IN_ZONE, QName.createQName("cm", name, namespacePrefixResolver));
        }
        addAuthority(authorityParentRef, childRef);
    	return childRef;
    }
    
    public String getAuthorityNameOrNull(NodeRef nodeRef){
    	return groupAuthorityDAO.getAuthorityName(nodeRef);
    }

    public NodeRef getAuthorityNodeRefOrNull(String groupName){
    	return groupAuthorityDAO.getAuthorityNodeRefOrNull(groupName);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteAuthority(NodeRef authorityNoderRef)
    {
    	authorityService.deleteAuthority(groupAuthorityDAO.getAuthorityName(authorityNoderRef), false);
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteAuthority(NodeRef authorityNoderRef, boolean cascade)
    {
    	authorityService.deleteAuthority(groupAuthorityDAO.getAuthorityName(authorityNoderRef), cascade);
    }
    
}