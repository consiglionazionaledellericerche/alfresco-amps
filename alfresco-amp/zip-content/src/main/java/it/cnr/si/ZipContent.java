package it.cnr.si;

import com.ibm.icu.text.Normalizer;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.security.crypto.codec.Utf8;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.*;

/**
 * https://github.com/atolcd/alfresco-zip-and-download
 */
public class ZipContent extends AbstractWebScript {

	private static final int BUFFER_SIZE = 1024;
	private static final String MIMETYPE_ZIP = "application/zip";
	private static final String TEMP_FILE_PREFIX = "alf";
	private static final String ZIP_EXTENSION = ".zip";
	private static Log LOGGER = LogFactory.getLog(ZipContent.class);
	private static String DEFAULT_ENCODING = "UTF-8";
	private ContentService contentService;
	private NodeService nodeService;
	private NamespaceService namespaceService;
	private DictionaryService dictionaryService;
	private FileFolderService fileFolderService;
	private SearchService searchService;

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

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

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

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException {
		List<NodeRef> nodesRef = new ArrayList<>();;
//		in caso di parametro non impostato nella request getParent avrà valore false
		boolean getParent = Boolean.parseBoolean(req.getParameter("getParent"));
		List<ChildAssociationRef> parentAssociation;

		try {
//			se il content della request NON è vuoto contiene il JSONArray dei nodes da zippare
			if (! req.getContent().getContent().isEmpty()) {
				org.json.JSONArray nodes = (JSONArray) ((JSONObject) req.parseContent()).get("nodes");
				if (nodes.length() != 0) {
					for (int i = 0; i < nodes.length(); i++) {
						NodeRef nodeRefToAdd = null;
						nodeRefToAdd = new NodeRef((String) nodes.get(i));
						if(getParent){
							parentAssociation = nodeService.getParentAssocs(nodeRefToAdd);
							NodeRef parent = parentAssociation.get(0).getParentRef();
							nodesRef.add(parent);
						} else{
							nodesRef.add(nodeRefToAdd);
						}
					}
				} else {
					throw new WebScriptException(
							HttpServletResponse.SC_BAD_REQUEST, "L'array nodes è vuoto");
				}

			} else if (req.getContent().getContent().isEmpty()) {
				//se il content della request è vuoto la request contiene il parametro query
				String query = req.getParameter("query");
				if (query.length() != 0) {
					StoreRef store = new StoreRef(StoreRef.PROTOCOL_WORKSPACE,
												  StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
														  .getIdentifier());
					ResultSet rs = searchService.query(store,
													   SearchService.LANGUAGE_CMIS_ALFRESCO, query);
					if(rs.getNodeRefs().size() != 0) {
						if(getParent){
							for (ResultSetRow child : rs) {
								parentAssociation = nodeService.getParentAssocs(child.getNodeRef());
								nodesRef.add(parentAssociation.get(0).getParentRef());
							}
						} else {
							nodesRef = rs.getNodeRefs();
						}
					} else {
						throw new WebScriptException(
								HttpServletResponse.SC_BAD_REQUEST, "La query ha result set vuoto");
					}
				} else {
					throw new WebScriptException(
							HttpServletResponse.SC_BAD_REQUEST, "Il campo query è valorizzato con una stringa vuota");
				}
			} else {
				throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
											 "Occorre specificare il parametro query oppure il parametro nodes");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
										 "Errore nel parsing del json nel body della request contenente i nodes");
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Errore nella lettura del content della request");
		}

		String filename = Utf8.decode(req.getParameter("filename").getBytes());
		if (filename == null || filename.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
										 "Il parametro filename non è valorizzato");
		}

		String noaccentStr = req.getParameter("noaccent");
		if (noaccentStr == null || noaccentStr.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
										 "Il parametro noaccent non è valorizzato");
		}

		String destinazione = req.getParameter("destination");
		if (destinazione == null || destinazione.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
										 "Il parametro destination non è valorizzato");
		}

		Boolean download = Boolean.valueOf(req.getParameter("download"));
		if (download == null)
			download = false;

		Boolean compress = Boolean.valueOf(req.getParameter("compress"));
		if (compress == null)
			compress = true;

		try {
			res.setContentType(MIMETYPE_ZIP);
			res.setHeader("Content-Transfer-Encoding", "binary");
			res.addHeader("Content-Disposition", "attachment;filename=\""
					+ unAccent(filename) + ZIP_EXTENSION + "\"");

			res.setHeader("Cache-Control",
						  "must-revalidate, post-check=0, pre-check=0");
			res.setHeader("Pragma", "public");
			res.setHeader("Expires", "0");
			createZipFile(nodesRef, destinazione, filename,
						  res.getOutputStream(), new Boolean(noaccentStr), download, compress);
		} catch (Exception e) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
										 e.getLocalizedMessage() + "--" + e.getMessage());
		}
	}


	public void createZipFile(List<NodeRef> nodesRef, String destinazione,
							  String filename, OutputStream os, boolean noaccent, boolean download, boolean compress)
			throws Exception {
		File zipAppo = null;
		ZipOutputStream out = null;
		FileInfo zipInfo = null;

		if (nodesRef != null && nodesRef.size() > 0) {
			zipAppo = TempFileProvider.createTempFile(TEMP_FILE_PREFIX,
													  ZIP_EXTENSION);
			FileOutputStream stream = new FileOutputStream(zipAppo);
			CheckedOutputStream checksum = new CheckedOutputStream(stream,
																   new Adler32());
			BufferedOutputStream buff = new BufferedOutputStream(checksum);
			out = new ZipOutputStream(buff);
			out.setMethod(ZipOutputStream.DEFLATED);

			if(compress)
				out.setLevel(Deflater.BEST_COMPRESSION);
			else
				out.setLevel(Deflater.NO_COMPRESSION);

			try {
				for (int i = 0; i < nodesRef.size(); i++) {
					addToZip(nodesRef.get(i), out, noaccent, "");
				}
			} catch (Exception e) {
				LOGGER.debug(e);
			} finally {
				// Scrivo su alfresco il file zip
				out.close();
				buff.close();
				checksum.close();
				stream.close();

				NodeRef destNodeRef = new NodeRef(destinazione);

				NodeRef oldZip = nodeService.getChildByName(destNodeRef,
															ContentModel.ASSOC_CONTAINS, filename + ZIP_EXTENSION);

				if (oldZip != null) {
					// cancello il vecchio file zip prima di sostituirlo con
					// quello che sto creando
					nodeService.deleteNode(oldZip);
				}

				QName contentQName = QName
						.createQName("{http://www.alfresco.org/model/content/1.0}content");

				// creo il file zip in dest
				zipInfo = fileFolderService.create(destNodeRef, filename
						+ ".zip", contentQName);
				NodeRef zipNodeRef = zipInfo.getNodeRef();
				ContentWriter writer = contentService.getWriter(zipNodeRef,
																ContentModel.PROP_CONTENT, true);
				writer.setLocale(Locale.getDefault());
				writer.setEncoding(DEFAULT_ENCODING);
				writer.setMimetype(MIMETYPE_ZIP);
				writer.putContent(zipAppo);
				// riempio l'output stream
				if (download) {
					InputStream in = new FileInputStream(zipAppo);
					byte[] buffer = new byte[BUFFER_SIZE];
					int len;
					while ((len = in.read(buffer)) > 0) {
						os.write(buffer, 0, len);
					}
					in.close();
				} else {
					// Restituisco il noderef del file zip creato
					final JSONObject json = new JSONObject();
					json.put("nodeRef", zipInfo.getNodeRef().toString());
					os.write(json.toString(2).getBytes());
				}
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
		// customizzazione: link alle cartelle
		if (this.dictionaryService.isSubClass(nodeQnameType,
											  ApplicationModel.TYPE_FOLDERLINK)) {
			NodeRef linkDestinationNode = (NodeRef) nodeService.getProperty(
					node, ContentModel.PROP_LINK_DESTINATION);
			if (linkDestinationNode != null) {
				addToZip(linkDestinationNode, out, noaccent, path);
			}
		}
		String nodeName = (String) nodeService.getProperty(node,
														   ContentModel.PROP_NAME);
		nodeName = noaccent ? unAccent(nodeName) : nodeName;

		if (this.dictionaryService.isSubClass(nodeQnameType,
											  ContentModel.TYPE_CONTENT)) {
			ContentReader reader = contentService.getReader(node,
															ContentModel.PROP_CONTENT);
			if (reader != null) {
				InputStream is = reader.getContentInputStream();
				String filename = path.isEmpty() ? nodeName : path + '/'
						+ nodeName;

				ZipEntry entry = new ZipEntry(filename);
				entry.setTime(((Date) nodeService.getProperty(node,
															  ContentModel.PROP_MODIFIED)).getTime());

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
				LOGGER.warn("Could not read : " + nodeName + "content");
			}
		} else if (this.dictionaryService.isSubClass(nodeQnameType,
													 ContentModel.TYPE_FOLDER)
				&& !this.dictionaryService.isSubClass(nodeQnameType,
													  ContentModel.TYPE_SYSTEM_FOLDER)) {
			List<ChildAssociationRef> children = nodeService
					.getChildAssocs(node);
			if (children.isEmpty()) {
				String folderPath = path.isEmpty() ? nodeName + '/' : path
						+ '/' + nodeName + '/';
				out.putNextEntry(new ZipEntry(folderPath));
			} else {
				for (ChildAssociationRef childAssoc : children) {
					NodeRef childNodeRef = childAssoc.getChildRef();
					addToZip(childNodeRef, out, noaccent,
							 path.isEmpty() ? nodeName : path + '/' + nodeName);
				}
			}
		} else {
			LOGGER.info("Unmanaged type: "
								+ nodeQnameType.getPrefixedQName(this.namespaceService)
								+ ", filename: " + nodeName);
		}
	}
}
