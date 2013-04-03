package it.cnr.si.service.cmr.security;

import java.util.Set;

import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;

public interface GroupAuthorityService{
	@Auditable	
	public NodeRef getAuthorityContainerRef();

    /**
     * @return Returns the authority container, <b>which must exist</b>
     */
	@Auditable	
	public NodeRef getAuthorityContainer();

    /**
     * @return Returns the zone container, <b>which must exist</b>
     */
	@Auditable	
	public NodeRef getZoneContainer();
	
    /**
     * Set an authority to include another authority. For example, adding a
     * group to a group or adding a user to a group.
     * 
     * @param parentName -
     *            the NodeRef identifier for the parent.
     * @param childName -
     *            the NodeRef identifier for the child.
     */
    @Auditable(parameters = {"parentName", "childName"})
    public void addAuthority(NodeRef parentName, NodeRef childName);
    
    @Auditable(parameters = {"parentName", "childName"})
    public void removeAuthority(NodeRef parentName, NodeRef childName);
	    
    /**
     * Create an authority.
     * 
     * @param type -
     *            the type of the authority
     * @param shortName -
     *            the short name of the authority to create
     *            this will also be set as the default display name for the authority 
     * 
     * @return the name of the authority (this will be the prefix, if any
     *         associated with the type appended with the short name)
     */
    @Auditable(parameters = {"authorityParentRef", "shortName", "authorityDisplayName"})
    public NodeRef createAuthority(NodeRef authorityParentRef, String shortName, String authorityDisplayName);
    
    @Auditable(parameters = {"authorityParentRef", "shortName", "authorityDisplayName", "authorityZones"})
    public NodeRef createAuthority(NodeRef authorityParentRef, String shortName, String authorityDisplayName, String... authorityZones);	    

    @Auditable(parameters = {"authorityNoderRef"})
	public void deleteAuthority(NodeRef authorityNoderRef);
	
	@Auditable(parameters = {"authorityNoderRef","cascade"})
	public void deleteAuthority(NodeRef authorityNoderRef, boolean cascade);
	
	@Auditable(parameters = {"nodeRef"})
	public String getAuthorityNameOrNull(NodeRef nodeRef);
	
	@Auditable(parameters = {"groupName"})
	public NodeRef getAuthorityNodeRefOrNull(String groupName);
	
	@Auditable(parameters = {"parent"})
	public Set<NodeRef> getAllUserAuthorities(NodeRef parent);

	@Auditable(parameters = {"parent"})
	public Set<NodeRef> getAllGroupAuthorities(NodeRef parent);
	
	@Auditable(parameters = {"parent"})
	public Set<NodeRef> getAuthorities(NodeRef parent, AuthorityType authorityType);
	
	@NotAuditable
    public Set<String> getAuthorityZones(String name);

	@NotAuditable
    public void addAuthorityToZones(String authorityName, String... zones);

	@NotAuditable
    public void removeAuthorityFromZones(String authorityName, String... zones);
	
}