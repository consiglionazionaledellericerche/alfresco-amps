package it.cnr.si;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import com.ibm.icu.text.Normalizer;

/**
 * https://github.com/atolcd/alfresco-zip-and-download
 */
public class ZipContent extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(ZipContent.class);
	private static final int BUFFER_SIZE = 1024;
	private static final String MIMETYPE_ZIP = "application/zip";
	private static final String TEMP_FILE_PREFIX = "alf";
	private static final String ZIP_EXTENSION = ".zip";

	private ContentService contentService;
	private NodeService nodeService;
	private NamespaceService namespaceService;
	private DictionaryService dictionaryService;
	private FileFolderService fileFolderService;
	private SearchService searchService;

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	private static String DEFAULT_ENCODING = "UTF-8";

	public FileFolderService getFileFolderService() {
		return fileFolderService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException {
		
		String[] nodes = req.getParameterValues("nodes");
		
		List<NodeRef> nodesRef;
		
//		if ((nodes == null || nodes.length == 0 )) {
//			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,"nodes");
//		} else{
//			nodesRef = new ArrayList<NodeRef>();
//			for (int i = 0; i < nodes.length; i++) {
//				nodesRef.add(new NodeRef(nodes[i]));
//			}
//		}
//		
//		
		String query = req.getParameter("query");
//		if(query == null || query.length() == 0){
//			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
//					"query");
//		}else{
//			StoreRef store = new StoreRef(StoreRef.PROTOCOL_WORKSPACE,StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier());	
//			ResultSet rs = searchService.query(store, SearchService.LANGUAGE_CMIS_ALFRESCO, query);
//			nodesRef = rs.getNodeRefs();
////			List<NodeRef> appo = rs.getNodeRefs();
////			NodeRef[] appo2 = new NodeRef[appo.size()];
////			appo.toArray(appo2);
////			for (int i = 0; i < appo2.length; i++) {
////				nodes[i] = appo2[i].getId();
////			}
//		}

		if (nodes != null && query == null){
			if(nodes.length != 0){
				nodesRef = new ArrayList<NodeRef>();
				for (int i = 0; i < nodes.length; i++) {
					nodesRef.add(new NodeRef(nodes[i]));
				}
			} else {
				throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "nodes");
			}
		} else if (query != null && nodes == null ) {
			if (query.length() != 0){
				StoreRef store = new StoreRef(StoreRef.PROTOCOL_WORKSPACE,StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier());	
				ResultSet rs = searchService.query(store, SearchService.LANGUAGE_CMIS_ALFRESCO, query);
				nodesRef = rs.getNodeRefs();
			} else{
				throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "query");
			}
		} else{
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "query or nodes");
		}


		String filename = req.getParameter("filename");
		if (filename == null || filename.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "filename");
		}

		String noaccentStr = req.getParameter("noaccent");
		if (noaccentStr == null || noaccentStr.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "noaccent");
		}

		String destinazione = req.getParameter("destination");
		if (destinazione == null || destinazione.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "destination");
		}

		try {
			res.setContentType(MIMETYPE_ZIP);
			res.setHeader("Content-Transfer-Encoding", "binary");
			res.addHeader("Content-Disposition", "attachment;filename=\"" + unAccent(filename) + ZIP_EXTENSION + "\"");

			res.setHeader("Cache-Control","must-revalidate, post-check=0, pre-check=0");
			res.setHeader("Pragma", "public");
			res.setHeader("Expires", "0");

//			if (query != null){
//				StoreRef store = new StoreRef(StoreRef.PROTOCOL_WORKSPACE,StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier());	
//				ResultSet rs = searchService.query(store, SearchService.LANGUAGE_CMIS_ALFRESCO, query);	
//				List<NodeRef> appo = rs.getNodeRefs();
//				NodeRef[] appo2 = new NodeRef[appo.size()];
//				appo.toArray(appo2);
//				for (int i = 0; i < appo2.length; i++) {
//					nodes[i] = appo2[i].getId();
//				}
//			}
			createZipFile(nodesRef, destinazione, filename, new Boolean(noaccentStr));
			
			res.getOutputStream();
			
		} catch (Exception e) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
					e.getLocalizedMessage() + "--" + e.getMessage());
		}
	}

	public void createZipFile(List<NodeRef> nodesRef, String destinazione, String filename, boolean noaccent) throws Exception {
		File zipAppo = null;
		ZipOutputStream out = null;

		if (nodesRef != null && nodesRef.size() > 0) {
			zipAppo = TempFileProvider.createTempFile(TEMP_FILE_PREFIX,	ZIP_EXTENSION);
			FileOutputStream stream = new FileOutputStream(zipAppo);
			CheckedOutputStream checksum = new CheckedOutputStream(stream, new Adler32());
			BufferedOutputStream buff = new BufferedOutputStream(checksum);
			out = new ZipOutputStream(buff);
			out.setMethod(ZipOutputStream.DEFLATED);
			out.setLevel(Deflater.BEST_COMPRESSION);

			// NodeRef node;
//			NodeRef[] nodesRef = new NodeRef[nodeIds.length];
			try {
			

//				StoreRef store = new StoreRef(StoreRef.PROTOCOL_WORKSPACE,StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier());	
//				ResultSet rs = searchService.query(store, SearchService.LANGUAGE_CMIS_ALFRESCO, "select cmis:name from cmis:document");	
//				List<NodeRef> appo = rs.getNodeRefs();			
				
				for (int i = 0; i < nodesRef.size(); i++) {
//					nodesRef[i] = new NodeRef(nodeIds[i]);
					addToZip(nodesRef.get(i), out, noaccent, "");
				}
				
//				for (int i = 0; i < nodeIds.length; i++) {
//					nodesRef[i] = new NodeRef(nodeIds[i]);
//					addToZip(nodesRef[i], out, noaccent, "");
//				}
				// }
				
			} catch (Exception e) {
				logger.debug(e);
			} finally {
				// Scrivo su alfresco il file zip
				out.close();
				buff.close();
				checksum.close();
				stream.close();

				InputStream in = new FileInputStream(zipAppo);
				byte[] buffer = new byte[BUFFER_SIZE];
				in.read(buffer);

				String fileName = zipAppo.getName();
				NodeRef dest = new NodeRef(destinazione);

				Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(1, 1.0f);
				titledProps.put(ContentModel.PROP_TITLE, fileName);
				this.nodeService.addAspect(dest, ContentModel.ASPECT_TITLED, titledProps);

				QName contentQName = QName.createQName("{http://www.alfresco.org/model/content/1.0}content");

				NodeRef appo = fileFolderService.searchSimple(dest, filename + ".zip");
				if (appo != null) {
					fileFolderService.delete(appo);
				}
				// creo il file zip in dest
				FileInfo zipInfo = fileFolderService.create(dest, filename + ".zip", contentQName);
				NodeRef zipNodeRef = zipInfo.getNodeRef();
				ContentWriter writer = contentService.getWriter(zipNodeRef,	ContentModel.PROP_CONTENT, true);
				writer.setLocale(Locale.getDefault());
				writer.setEncoding(DEFAULT_ENCODING);
				writer.setMimetype(MIMETYPE_ZIP);
				writer.putContent(zipAppo);
			}
		}

		if (zipAppo != null) {
			zipAppo.delete();
		}
	}

	/**
	 * Non modificato
	 * 
	 * @param node
	 * @param out
	 * @param noaccent
	 * @param path
	 * @throws IOException
	 */
	public void addToZip(NodeRef node, ZipOutputStream out, boolean noaccent,
			String path) throws IOException {
		QName nodeQnameType = this.nodeService.getType(node);
		// Special case : links
		if (this.dictionaryService.isSubClass(nodeQnameType,
				ApplicationModel.TYPE_FILELINK)) {
			NodeRef linkDestinationNode = (NodeRef) nodeService.getProperty(
					node, ContentModel.PROP_LINK_DESTINATION);
			if (linkDestinationNode == null) {
				return;
			}
			// Duplicate entry: check if link is not in the same space of the
			// link destination
			if (nodeService
					.getPrimaryParent(node)
					.getParentRef()
					.equals(nodeService.getPrimaryParent(linkDestinationNode)
							.getParentRef())) {
				return;
			}
			nodeQnameType = this.nodeService.getType(linkDestinationNode);
			node = linkDestinationNode;
		}
		String nodeName = (String) nodeService.getProperty(node,
				ContentModel.PROP_NAME);
		nodeName = noaccent ? unAccent(nodeName) : nodeName;

		if (this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_CONTENT)) {
			ContentReader reader = contentService.getReader(node, ContentModel.PROP_CONTENT);
			if (reader != null) {
				InputStream is = reader.getContentInputStream();
				String filename = path.isEmpty() ? nodeName : path + '/' + nodeName;

				ZipEntry entry = new ZipEntry(filename);
				entry.setTime(((Date) nodeService.getProperty(node,	ContentModel.PROP_MODIFIED)).getTime());

				entry.setSize(reader.getSize());
				out.putNextEntry(entry);

				byte buffer[] = new byte[BUFFER_SIZE];
				while (true) {
					int nRead = is.read(buffer, 0, buffer.length);
					if (nRead <= 0) {
						break;
					}
					out.write(buffer, 0, nRead);
				}
				is.close();
				out.closeEntry();
			} else {
				logger.warn("Could not read : " + nodeName + "content");
			}
		} else if (this.dictionaryService.isSubClass(nodeQnameType,	ContentModel.TYPE_FOLDER) && !this.dictionaryService.isSubClass(nodeQnameType,ContentModel.TYPE_SYSTEM_FOLDER)) {
			List<ChildAssociationRef> children = nodeService
					.getChildAssocs(node);
			if (children.isEmpty()) {
				String folderPath = path.isEmpty() ? nodeName + '/' : path + '/' + nodeName + '/';
				out.putNextEntry(new ZipEntry(folderPath));
			} else {
				for (ChildAssociationRef childAssoc : children) {
					NodeRef childNodeRef = childAssoc.getChildRef();
					addToZip(childNodeRef, out, noaccent, path.isEmpty() ? nodeName : path + '/' + nodeName);
				}
			}
		} else {
			logger.info("Unmanaged type: "
					+ nodeQnameType.getPrefixedQName(this.namespaceService)
					+ ", filename: " + nodeName);
		}
	}

	/**
	 * ZipEntry() does not convert filenames from Unicode to platform (waiting
	 * Java 7) http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4244499
	 * 
	 * @param s
	 * @return
	 */
	public static String unAccent(String s) {
		String temp = Normalizer.normalize(s, Normalizer.NFD, 0);
		return temp.replaceAll("[^\\p{ASCII}]", "");
	}
}
