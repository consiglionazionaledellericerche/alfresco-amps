package it.cnr.si;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.cmis.CMISServices;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.exporter.ACPExportPackageHandler;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterException;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.cmr.view.ReferenceType;
import org.alfresco.service.cmr.view.RepositoryExporterService.FileExportHandle;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.FileField;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

@Component
@WebScript(baseUri = "/dynamic-extensions")
public class SynchronizingAlfrescoWebScript {
	public static final String NAME = "export";
	public static final String PARAM_STORE = "store";
	public static final String PARAM_PACKAGE_NAME = "package-name";
	public static final String PARAM_DESTINATION_FOLDER = "destination";
	public static final String PARAM_INCLUDE_CHILDREN = "include-children";
	public static final String PARAM_INCLUDE_SELF = "include-self";
	public static final String PARAM_ENCODING = "encoding";
	private static final int BUFFER_SIZE = 1024;
	private static final String MIMETYPE_ACP = "application/acp";
	public final static String DEFAULT_ENCODING = "UTF-8";
	@Autowired
	private ServiceRegistry svcRegistry;

	@Autowired
	private ContentService contentService;
	@Autowired
	private ExporterService exporter;
	@Autowired
	private ImporterService importer;
	@Autowired
	private MimetypeService mimetypeService;
	@Autowired
	private NodeService nodeService;
	@Autowired
	private CMISServices cmisService;

	/**
	 * @param destDirName
	 *            : nome della folder nel file system in cui copiare il file acp
	 *            generato
	 * @param packageName
	 *            : nome del file acp
	 * @param storeRef
	 *            : nodeRef spazio da esportare nel file acp
	 * @param self
	 *            : boolean che indica se si vuole esportare anche lo spazio
	 *            indicato in storeRef oppure solo i suoi figli
	 * @param children
	 *            : boolean che indica se si vogliono esportare gli spazi figli
	 * @param content
	 *            : boolean che indica se si vuole esportare il contenuto dei
	 *            nodi
	 * @param association
	 *            : boolean che indica se si vuole esportare le associazioni
	 * @param nullProperties
	 *            : boolean che indica se si vuole esportare le proprietà
	 *            settate a null
	 * @param download
	 *            : boolean che indica se si vuole effettuare il download del
	 *            file acp generato
	 * @throws IOException
	 */
	@Uri("/export/acp")
	public void exportAcp(final WebScriptResponse response,
			@RequestParam String destDirName, @RequestParam String packageName,
			@RequestParam String storeRef, @RequestParam boolean self,
			@RequestParam boolean children, @RequestParam boolean content,
			@RequestParam boolean association,
			@RequestParam boolean nullProperties, @RequestParam boolean download)
			throws IOException {

		File file = export(destDirName, packageName, storeRef, self, children,
				content, association, nullProperties);
		if (download) {
			FileExportHandle handle = new FileExportHandle();
			handle.storeRef = new StoreRef(storeRef);
			handle.packageName = packageName;
			handle.mimeType = MimetypeMap.MIMETYPE_ACP;
			handle.exportFile = file;

			response.setContentType(MIMETYPE_ACP);
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.addHeader("Content-Disposition", "attachment;filename=\""
					+ packageName + "." + ACPExportPackageHandler.ACP_EXTENSION
					+ "\"");
			response.setHeader("Cache-Control",
					"must-revalidate, post-check=0, pre-check=0");
			response.setHeader("Pragma", "public");
			response.setHeader("Expires", "0");
			OutputStream os = response.getOutputStream();
			InputStream in = new FileInputStream(file);
			byte[] buffer = new byte[BUFFER_SIZE];
			int len;
			while ((len = in.read(buffer)) > 0) {
				os.write(buffer, 0, len);
			}
			in.close();
		}
	}

	/**
	 * @param nodeRef
	 *            : nodeRef dello spazio in cui copiare il contenuto
	 *            dell'acpFile
	 * @param acpFile
	 *            : file acp da importare
	 */
	@Uri(method = HttpMethod.POST, value = "/import/acp", multipartProcessing = true)
	public void importAcp(final WebScriptResponse response,
			@RequestParam String nodeRef, @FileField final FormField acpFile) {
		if (acpFile != null) {
			File tempFile = null;
			try {
				// copio il file in un file temporaneo
				tempFile = File.createTempFile("dynamic-extensions-bundle",
						ACPExportPackageHandler.ACP_EXTENSION);
				tempFile.deleteOnExit();
				IOUtils.copy(acpFile.getInputStream(), new FileOutputStream(
						tempFile));
			} catch (IOException e) {
				throw new ImporterException("Failed to Import "
						+ acpFile.getFilename() + " to nodeRef" + nodeRef, e);
			}
			ACPImportPackageHandler acpHandler = new ACPImportPackageHandler(
					tempFile, DEFAULT_ENCODING);
			NodeRef space = new NodeRef(nodeRef);
			Location importLocation = new Location(space);
			importer.importView(acpHandler, importLocation, null, null);
		}
	}

	private File export(String destDirName, String packageName,
			String storeRef, boolean self, boolean children, boolean content,
			boolean association, boolean nullProperties) {

		File directoryDestination = new File(destDirName);
		if (!directoryDestination.exists()) {
			// Se non esiste la directory di destinazione la creo
			directoryDestination.mkdirs();
		}
		// acp file
		File file = new File(directoryDestination, packageName + "."
				+ ACPExportPackageHandler.ACP_EXTENSION);

		ExporterCrawlerParameters exportParameters = new ExporterCrawlerParameters();
		exportParameters.setCrawlSelf(self);
		exportParameters.setCrawlChildNodes(children);
		exportParameters.setCrawlContent(content);
		exportParameters.setCrawlAssociations(association);
		exportParameters.setCrawlNullProperties(nullProperties);
		exportParameters.setReferenceType(ReferenceType.NODEREF);

		Location location = new Location(new NodeRef(storeRef));
		exportParameters.setExportFrom(location);
		try {
			OutputStream outputStream = new FileOutputStream(file);
			// file e cartelle temporanei
			File dataFile = new File(packageName);
			File contentDir = new File(packageName);
			ACPExportPackageHandler acpHandler = new ACPExportPackageHandler(
					outputStream, dataFile, contentDir, mimetypeService);
			// export the store
			exporter.exportView(acpHandler, exportParameters, null);
		} catch (FileNotFoundException e) {
			file.delete();
			throw new ExporterException("Failed to create file "
					+ file.getAbsolutePath()
					+ " for holding the export of store "
					+ exportParameters.getExportFrom().getStoreRef());
		}
		return file;
	}
}