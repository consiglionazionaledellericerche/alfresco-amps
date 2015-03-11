package it.cnr.si;

import com.github.stephenc.javaisotools.udflib.SabreUDFImageBuilder;
import com.github.stephenc.javaisotools.udflib.UDFImageBuilderFile;
import com.github.stephenc.javaisotools.udflib.UDFRevision;
import com.ibm.icu.text.Normalizer;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.LogFactory;
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


/**
 * Created by cirone on 19/02/2015.
 */
public class IsoContent extends AbstractWebScript {

    private static final int BUFFER_SIZE = 1024;
    private static final String MIMETYPE_ISO = "application/octet-stream";
    private static final String TEMP_FILE_PREFIX = "alf";
    private static final String ISO_EXTENSION = ".iso";
    private static final org.apache.commons.logging.Log LOGGER = LogFactory.getLog(IsoContent.class);
    private ContentService contentService;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private FileFolderService fileFolderService;
    private SearchService searchService;


    private String unAccent(String s) {
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
    public void execute(WebScriptRequest req, WebScriptResponse res)
            throws WebScriptException {
        String[] nodes = req.getParameterValues("nodes");
        List<NodeRef> nodesRef;
        String query = req.getParameter("query");

        if (nodes != null && query == null) {
            if (nodes.length != 0) {
                nodesRef = new ArrayList<NodeRef>();
                for (String node : nodes) {
                    nodesRef.add(new NodeRef(node));
                }
            } else {
                throw new WebScriptException(
                        HttpServletResponse.SC_BAD_REQUEST, "L'array nodes è vuoto");
            }
        } else if (query != null && nodes == null) {
            if (query.length() != 0) {
                StoreRef store = new StoreRef(StoreRef.PROTOCOL_WORKSPACE,
                                              StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
                                                      .getIdentifier());
                ResultSet rs = searchService.query(store, SearchService.LANGUAGE_CMIS_ALFRESCO, query);
                if (rs.getNodeRefs().size() != 0) {
                    nodesRef = rs.getNodeRefs();
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

        try {
            res.setContentType(MIMETYPE_ISO);
            res.setHeader("Content-Transfer-Encoding", "binary");
            res.addHeader("Content-Disposition", "attachment;filename=\""
                    + (Boolean.valueOf(noaccentStr) ? unAccent(filename) : filename) + ISO_EXTENSION + "\"");
            res.setHeader("Cache-Control",
                          "must-revalidate, post-check=0, pre-check=0");
            res.setHeader("Pragma", "public");
            res.setHeader("Expires", "0");

            createIsoFile(nodesRef, destinazione, filename,
                          res.getOutputStream(), Boolean.valueOf(noaccentStr), download);
        } catch (IOException ioExc) {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                         ioExc.getLocalizedMessage() + " - " + Arrays.toString(ioExc.getStackTrace()));
        }
    }

    /**
     * @param nodesRefs: Array dei nodeRef ilc ui contenuto va inserito nei file ISO
     * @param destinazione: noderef dello spazio di Alfresco in cui verrà salfavo il file ISO creato
     * @param filename: nome del file ISO
     * @param os: OutputStream della response in cui scrivere il file ISO creati
     * @param noaccent: boleano che indica se normalizzare il nome del file iso creato
     * @param download: boleano che indica se scrivere il file iso nel OutputStream della response
     */
    void createIsoFile(
            List<NodeRef> nodesRefs, String destinazione,
            String filename, OutputStream os, boolean noaccent, boolean download) {

        QName contentQName = QName
                .createQName("{http://www.alfresco.org/model/content/1.0}content");
        NodeRef destNodeRef = new NodeRef(destinazione);
        // creo il file ISO in dest
        FileInfo outfile = fileFolderService.create(destNodeRef, filename
                + ".iso", contentQName);
        SabreUDFImageBuilder root = new SabreUDFImageBuilder();
        UDFImageBuilderFile parent = new UDFImageBuilderFile(filename);
        try {
            root.addFileToRootDirectory(parent);
        } catch (Exception e) {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                         e.getLocalizedMessage() + " - " + Arrays.toString(e.getStackTrace()));
        }

        File isoAppo = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, ISO_EXTENSION);

        for (NodeRef node : nodesRefs) {
            addToIso(node, parent, noaccent);
        }

        try {
            root.setImageIdentifier(filename);
            root.writeImage(isoAppo.getAbsolutePath(), UDFRevision.Revision260);
        } catch (Exception e) {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                         e.getLocalizedMessage() + " - " + Arrays.toString(e.getStackTrace()));
        }

        // creo il file ISO in dest
        NodeRef isoNodeRef = outfile.getNodeRef();
        ContentWriter writer = contentService.getWriter(isoNodeRef,
                                                        ContentModel.PROP_CONTENT, true);
        writer.setLocale(Locale.getDefault());
        String DEFAULT_ENCODING = "UTF-8";
        writer.setEncoding(DEFAULT_ENCODING);
        writer.setMimetype(MIMETYPE_ISO);
        writer.putContent(isoAppo);
        try {
            if (download) {
                InputStream in = new FileInputStream(isoAppo);
                byte[] buffer = new byte[BUFFER_SIZE];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    os.write(buffer, 0, len);
                }
                in.close();
            } else {
                // Restituisco il noderef del file iso creato
                final JSONObject json = new JSONObject();
                json.put("nodeRef", outfile.getNodeRef().toString());
                os.write(json.toString(2).getBytes());
            }

        } catch (IOException ioExc) {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                         ioExc.getLocalizedMessage() + " - " + Arrays.toString(ioExc.getStackTrace()));
        } catch (JSONException jsonExc) {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                         jsonExc.getLocalizedMessage() + " - " + Arrays.toString(jsonExc.getStackTrace()));
        }

        isoAppo.delete();
    }

    /**
     * @param node:     nodeRef dello spazio da aggiungere al file iso
     * @param root:     UDFImageBuilderFile corrispondente alla cartella root il cui contenuto va aggiunto al file ISO
     * @param noaccent: boleano che indica se normalizzare il nome del file iso creato
     */
    void addToIso(
            NodeRef node, UDFImageBuilderFile root, boolean noaccent) {
        QName nodeQnameType = this.nodeService.getType(node);
        // link alle cartelle e link ai file (vengono risolti)
        if (this.dictionaryService.isSubClass(nodeQnameType, ApplicationModel.TYPE_FILELINK) ||
                this.dictionaryService.isSubClass(nodeQnameType, ApplicationModel.TYPE_FOLDERLINK)) {
            NodeRef linkDestinationNode = (NodeRef) nodeService.getProperty(
                    node, ContentModel.PROP_LINK_DESTINATION);
            if (linkDestinationNode == null) {
                return;
            }
            //entry Duplicata: controlla se il collegamento non è nello stesso spazio di destinazione del link (link ciclico)
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
        String nodeName = (String) nodeService.getProperty(node, ContentModel.PROP_NAME);
        nodeName = noaccent ? unAccent(nodeName) : nodeName;

        if (this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_CONTENT)) {
            ContentReader reader = contentService.getReader(node, ContentModel.PROP_CONTENT);
            if (reader != null) {
                File appo = new File(nodeName);
                //il reader legge il contenuto del file di alfresco e lo copia nel file passatogli
                reader.getContent(appo);
                try {
                    //file aggiunto all' UDFImageBuilderFile
                    root.addChild(appo);
                } catch (Exception e) {
                    throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                                 e.getLocalizedMessage() + " - " + Arrays.toString(e.getStackTrace()));
                }
            } else {
                LOGGER.warn("Could not read : " + nodeName + "content");
            }
        } else if (this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_FOLDER)
                && !this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_SYSTEM_FOLDER)) {
            List<ChildAssociationRef> children = nodeService.getChildAssocs(node);
            UDFImageBuilderFile child = new UDFImageBuilderFile(nodeName);
            try {
                root.addChild(child);
            } catch (Exception e) {
                throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                             e.getLocalizedMessage() + " - " + Arrays.toString(e.getStackTrace()));
            }
            // richiamo addIso sui children della cartella esplorata
            for (ChildAssociationRef childAssoc : children) {
                NodeRef childNodeRef = childAssoc.getChildRef();
                addToIso(childNodeRef, child, noaccent);
            }
        } else {
            LOGGER.error("Tipo di contenuto non gestibile: "
                                 + nodeQnameType.getPrefixedQName(this.namespaceService)
                                 + ", filename: " + nodeName);
        }
    }
}