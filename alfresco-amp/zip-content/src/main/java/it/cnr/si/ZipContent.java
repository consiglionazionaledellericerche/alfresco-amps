package it.cnr.si;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
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

		String nodes = req.getParameter("nodes");
		if (nodes == null || nodes.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "nodes");
		}

		String filename = req.getParameter("filename");
		if (filename == null || filename.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "filename");
		}

		String noaccentStr = req.getParameter("noaccent");
		if (noaccentStr == null || noaccentStr.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "noaccent");
		}

		String destinazione = req.getParameter("destinazione");
		if (destinazione == null || destinazione.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "destinazione");
		}

		try {
			res.setContentType(MIMETYPE_ZIP);
			res.setHeader("Content-Transfer-Encoding", "binary");
			res.addHeader("Content-Disposition", "attachment;filename=\"" + unAccent(filename) + ZIP_EXTENSION + "\"");

			res.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
			res.setHeader("Pragma", "public");
			res.setHeader("Expires", "0");

			createZipFile(nodes, destinazione, filename, new Boolean(noaccentStr));

		} catch (Exception e) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, e.getLocalizedMessage() + "--" + e.getMessage());
		}
	}


	public void createZipFile(String nodeIds, String destinazione, String filename, boolean noaccent) throws Exception  {
		File zipAppo = null;

		if (nodeIds != null && !nodeIds.isEmpty()) {
			zipAppo = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, ZIP_EXTENSION);
			FileOutputStream stream = new FileOutputStream(zipAppo);
			CheckedOutputStream checksum = new CheckedOutputStream(stream, new Adler32());
			BufferedOutputStream buff = new BufferedOutputStream(checksum);
			ZipOutputStream out = new ZipOutputStream(buff);
			out.setMethod(ZipOutputStream.DEFLATED);
			out.setLevel(Deflater.BEST_COMPRESSION);

			NodeRef node;
			try {
				node = new NodeRef(nodeIds);
				addToZip(node, out, noaccent, "");
			} catch (Exception e) {
				logger.debug(e);
			} finally {
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
				if(appo != null){
					fileFolderService.delete(appo);
				}
				//	                    creo il file zip in dest
				FileInfo zipInfo = fileFolderService.create(dest, filename + ".zip" , contentQName);
				NodeRef zipNodeRef = zipInfo.getNodeRef();
				ContentWriter writer = contentService.getWriter(zipNodeRef, ContentModel.PROP_CONTENT, true);
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

	public void addToZip(NodeRef node, ZipOutputStream out, boolean noaccent, String path) throws IOException {
		QName nodeQnameType = this.nodeService.getType(node);
		// Special case : links
		if (this.dictionaryService.isSubClass(nodeQnameType, ApplicationModel.TYPE_FILELINK)) {
			NodeRef linkDestinationNode = (NodeRef) nodeService.getProperty(node, ContentModel.PROP_LINK_DESTINATION);
			if (linkDestinationNode == null) {
				return;
			}
			// Duplicate entry: check if link is not in the same space of the link destination
			if (nodeService.getPrimaryParent(node).getParentRef().equals(nodeService.getPrimaryParent(linkDestinationNode).getParentRef())) {
				return;
			}
			nodeQnameType = this.nodeService.getType(linkDestinationNode);
			node = linkDestinationNode;
		}
		String nodeName = (String) nodeService.getProperty(node, ContentModel.PROP_NAME);
		nodeName = noaccent ? unAccent(nodeName) : nodeName;

		if (this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_CONTENT)) {
			ContentReader reader = contentService.getReader(node, ContentModel.PROP_CONTENT);
			if (reader != null) {
				InputStream is = reader.getContentInputStream();
				String filename = path.isEmpty() ? nodeName : path + '/' + nodeName;

				ZipEntry entry = new ZipEntry(filename);
				entry.setTime(((Date) nodeService.getProperty(node, ContentModel.PROP_MODIFIED)).getTime());

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
			}
			else {
				logger.warn("Could not read : "	+ nodeName + "content");
			}
		}
		else if(this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_FOLDER) 
				&& !this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_SYSTEM_FOLDER)) {
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
			logger.info("Unmanaged type: " + nodeQnameType.getPrefixedQName(this.namespaceService) + ", filename: " + nodeName);
		}
	}
	
	public static String unAccent(String s) {
		String temp = Normalizer.normalize(s, Normalizer.NFD, 0);
		return temp.replaceAll("[^\\p{ASCII}]", "");
	}
}
