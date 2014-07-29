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
	private DictionaryService dictionaryService;
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
		this.dictionaryService = dictionaryService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws WebScriptException {
		NodeRef sourceNodeRef = new NodeRef(req.getParameter("sourceNodeRef"));
		NodeRef destNodeRef = new NodeRef(req.getParameter("destNodeRef"));

		// establishLinkages(sourceNodeRef, destNodeRef);

		ChildAssociationRef assocRef = nodeService
				.getPrimaryParent(sourceNodeRef);
		String name = (String) nodeService.getProperty(sourceNodeRef,
				ContentModel.PROP_NAME);
		String linkTo = "Link to";
		name = linkTo + ' ' + name;

		LOGGER.debug("Attempting to link node ID: " + sourceNodeRef
				+ " into node: " + destNodeRef.toString());

		// creo un particolare nodo Object Link, che ha una proprietà con il
		// link
		// TODO: si potrebbe usare direttamente FileFolderService per i
		// contenuti)
		String LINK_NODE_EXTENSION = ".url";
		List<NodeRef> oldLink = checkExists(name + LINK_NODE_EXTENSION,
				destNodeRef);
		if (oldLink.size() == 0) {
			createLinkage(sourceNodeRef, destNodeRef, assocRef, name,
					LINK_NODE_EXTENSION);
		} else {
			nodeService.deleteNode(oldLink.get(0));
			createLinkage(sourceNodeRef, destNodeRef, assocRef, name,
					LINK_NODE_EXTENSION);
		}
	}

	/**
	 * genera un link in alfresco tra il sourceNodeRef e destNoderef
	 * 
	 * @param sourceNodeRef
	 *            il nodeRef che deve essere linkato
	 * @param destNodeRef
	 *            la destinazione del link
	 */

	private void createLinkage(NodeRef sourceNodeRef, NodeRef destNodeRef,
			ChildAssociationRef assocRef, String name,
			String LINK_NODE_EXTENSION) {
		Map<QName, Serializable> props = new HashMap<QName, Serializable>(2,
				1.0f);
		String newName = name + LINK_NODE_EXTENSION;
		props.put(ContentModel.PROP_NAME, newName);
		props.put(ContentModel.PROP_LINK_DESTINATION, sourceNodeRef);

		if (dictionaryService.isSubClass(nodeService.getType(sourceNodeRef),
				ContentModel.TYPE_CONTENT)) {
			// creo il File Link node
			ChildAssociationRef childRef = nodeService.createNode(destNodeRef,
					ContentModel.ASSOC_CONTAINS, QName.createQName(assocRef
							.getQName().getNamespaceURI(), newName),
					ApplicationModel.TYPE_FILELINK, props);
			// aspect: title (nome link) e description
			Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(
					2, 1.0f);
			titledProps.put(ContentModel.PROP_TITLE, name);
			titledProps.put(ContentModel.PROP_DESCRIPTION, name);
			nodeService.addAspect(childRef.getChildRef(),
					ContentModel.ASPECT_TITLED, titledProps);
		} else {
			ChildAssociationRef childRef = nodeService.createNode(destNodeRef,
					ContentModel.ASSOC_CONTAINS, assocRef.getQName(),
					ApplicationModel.TYPE_FOLDERLINK, props);

			// uifacets aspect: proprietà icona, title e description
			Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(
					4, 1.0f);
			uiFacetsProps.put(ApplicationModel.PROP_ICON, "space-icon-link");
			uiFacetsProps.put(ContentModel.PROP_TITLE, name);
			uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, name);
			nodeService.addAspect(childRef.getChildRef(),
					ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
		}
	}

	protected List<NodeRef> checkExists(String name, NodeRef parent) {
		/** ricerca per pattern e nome del file */
		String XPATH_QUERY_NODE_MATCH = "./*[like(@cm:name, $cm:name, false)]";
		QueryParameterDefinition[] params = new QueryParameterDefinition[1];
		params[0] = new QueryParameterDefImpl(ContentModel.PROP_NAME,
				dictionaryService.getDataType(DataTypeDefinition.TEXT), true,
				name);
		// query
		List<NodeRef> nodeRefs = searchService.selectNodes(parent,
				XPATH_QUERY_NODE_MATCH, params, namespaceService, false);

		return nodeRefs;
	}
}
