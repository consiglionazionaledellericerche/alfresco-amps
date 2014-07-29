package it.cnr.si;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class EstablishLinkages extends AbstractWebScript {
	private static Log LOGGER = LogFactory.getLog(EstablishLinkages.class);
	private NodeService nodeService;
	private DictionaryService dd;
	private SearchService searchService;
	private NamespaceService namespaceService;

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dd = dictionaryService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws WebScriptException {
		NodeRef sourceNodeRef = new NodeRef(req.getParameter("sourceNodeRef"));
		NodeRef destNodeRef = new NodeRef(req.getParameter("destNodeRef"));

		establishLinkages(sourceNodeRef, destNodeRef);
	}

	/**
	 * genera un link in alfresco tra il sourceNodeRef e destNoderef
	 * 
	 * @param sourceNodeRef
	 *            il nodeRef che deve essere linkato
	 * @param destNodeRef
	 *            la destinazione del link
	 */

	private void establishLinkages(NodeRef sourceNodeRef, NodeRef destNodeRef) {
		boolean operationComplete = false;
		// TODO: Should we be using primary parent here?
		// We are assuming that the item exists in only a single parent and that
		// the source for
		// the clipboard operation (e.g. the source folder) is specifically that
		// parent node.
		// So does not allow for more than one possible parent node - or for
		// linked objects!
		// This code should be refactored to use a parent ID when appropriate.

		ChildAssociationRef assocRef = nodeService
				.getPrimaryParent(sourceNodeRef);
		String name = getName(sourceNodeRef);
		String linkTo = "Link to";
		name = linkTo + ' ' + name;

		LOGGER.debug("Attempting to link node ID: " + sourceNodeRef
				+ " into node: " + destNodeRef.toString());

		// we create a special Link Object node that has a property to reference
		// the original
		// create the node using the nodeService (can only use FileFolderService
		// for content)
		String LINK_NODE_EXTENSION = ".url";
		if (checkExists(name + LINK_NODE_EXTENSION, destNodeRef) == false) {
			Map<QName, Serializable> props = new HashMap<QName, Serializable>(
					2, 1.0f);
			String newName = name + LINK_NODE_EXTENSION;
			props.put(ContentModel.PROP_NAME, newName);
			props.put(ContentModel.PROP_LINK_DESTINATION, sourceNodeRef);

			if (dd.isSubClass(getType(sourceNodeRef), ContentModel.TYPE_CONTENT)) {
				// create File Link node
				ChildAssociationRef childRef = nodeService.createNode(
						destNodeRef, ContentModel.ASSOC_CONTAINS, QName
								.createQName(assocRef.getQName()
										.getNamespaceURI(), newName),
						ApplicationModel.TYPE_FILELINK, props);
				// apply the titled aspect - title and description
				Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(
						2, 1.0f);
				titledProps.put(ContentModel.PROP_TITLE, name);
				titledProps.put(ContentModel.PROP_DESCRIPTION, name);
				nodeService.addAspect(childRef.getChildRef(),
						ContentModel.ASPECT_TITLED, titledProps);
			} else {
				ChildAssociationRef childRef = nodeService.createNode(
						destNodeRef, ContentModel.ASSOC_CONTAINS,
						assocRef.getQName(), ApplicationModel.TYPE_FOLDERLINK,
						props);

				// mio (prova - Indifferente: non cambia nulla)
				nodeService.setProperty(childRef.getChildRef(),
						ContentModel.PROP_NAME, name);

				// apply the uifacets aspect - icon, title and description props
				Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(
						4, 1.0f);
				uiFacetsProps
						.put(ApplicationModel.PROP_ICON, "space-icon-link");
				uiFacetsProps.put(ContentModel.PROP_TITLE, name);
				uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, name);
				nodeService.addAspect(childRef.getChildRef(),
						ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
			}
			// if we get here without an exception, the clipboard link operation
			// was successful
			operationComplete = true;
		}
		LOGGER.debug("Is Linkage operation complete????" + operationComplete);
	}

	public String getName(NodeRef sourceNodeRef) {
		String name = (String) nodeService.getProperty(sourceNodeRef,
				ContentModel.PROP_NAME);
		return name;
	}

	protected boolean checkExists(String name, NodeRef parent) {
		/** Shallow search for nodes with a name pattern */
		String XPATH_QUERY_NODE_MATCH = "./*[like(@cm:name, $cm:name, false)]";
		QueryParameterDefinition[] params = new QueryParameterDefinition[1];
		params[0] = new QueryParameterDefImpl(ContentModel.PROP_NAME,
				dd.getDataType(DataTypeDefinition.TEXT), true, name);
		// execute the query
		List<NodeRef> nodeRefs = searchService.selectNodes(parent,
				XPATH_QUERY_NODE_MATCH, params, namespaceService, false);

		return (nodeRefs.size() != 0);
	}

	public QName getType(NodeRef nodeRef) {
		QName type = nodeService.getType(nodeRef);
		return type;
	}
}
