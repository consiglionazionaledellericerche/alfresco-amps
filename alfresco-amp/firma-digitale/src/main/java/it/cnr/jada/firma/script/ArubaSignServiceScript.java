package it.cnr.jada.firma.script;

import it.cnr.jada.firma.arss.ArubaSignServiceClient;
import it.cnr.jada.firma.arss.ArubaSignServiceException;
import it.cnr.jada.firma.arss.stub.PdfSignApparence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ArubaSignServiceScript extends BaseScopableProcessorExtension implements
ApplicationContextAware {

	private static final Logger LOGGER = Logger
			.getLogger(ArubaSignServiceScript.class);

	private ContentService contentService;

	private ArubaSignServiceClient arubaSignServiceClient;

	public void setArubaSignServiceClient(
			ArubaSignServiceClient arubaSignServiceClient) {
		this.arubaSignServiceClient = arubaSignServiceClient;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		// do nothing
	}

	public void pkcs7SignV2(String username, String password, String otp,
			String source, String target) throws ArubaSignServiceException,
			IOException {
		byte[] bytes = pkcs7SignV2(username, password, otp, source);
		ContentWriter w = contentService.getWriter(new NodeRef(target),
				ContentModel.PROP_CONTENT, true);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		w.putContent(is);

	}

	public byte[] pkcs7SignV2(String username, String password, String otp,
			String nodeRef) throws ArubaSignServiceException,
			IOException {

		NodeRef node = new NodeRef(nodeRef);

		LOGGER.debug("pkcs7Sign " + nodeRef);

		ContentReader reader = contentService.getReader(node,
				ContentModel.PROP_CONTENT);

		byte[] bytes;

		if (reader != null) {
			bytes = IOUtils.toByteArray(reader.getContentInputStream());
		} else {
			bytes = new byte[0];
		}

		LOGGER.debug(bytes.length + " bytes");

		return arubaSignServiceClient.pkcs7SignV2(username, password,
				otp, bytes);
	}

	public void pdfsignatureV2(String username, String password, String otp,
			String nodeRef) throws ArubaSignServiceException,
			IOException {
		pdfsignatureV2(username, password, otp, nodeRef, null);		
	}
	/**
	 * 
	 * @param username
	 * @param password
	 * @param otp
	 * @param nodeRef
	 * Parametri per specificare l'immagine il testo la posizione e la pagina 
	 * @param image
	 * @param leftx
	 * @param lefty
	 * @param location
	 * @param page
	 * @param reason
	 * @param rightx
	 * @param righty
	 * @param testo
	 * @throws ArubaSignServiceException
	 * @throws IOException
	 */
	public void pdfsignatureV2(String username, String password, String otp,
			String nodeRef, String image, int leftx, int lefty, String location, int page, String reason, int rightx, int righty, String testo) throws ArubaSignServiceException,
			IOException {
		PdfSignApparence apparence = new PdfSignApparence();
		apparence.setImage(image);
		apparence.setLeftx(leftx);
		apparence.setLefty(lefty);
		apparence.setLocation(location);
		apparence.setPage(page);
		apparence.setReason(reason);
		apparence.setRightx(rightx);
		apparence.setRighty(righty);
		apparence.setTesto(testo);
		LOGGER.debug("PdfSignApparence: " + apparence);	
		
		pdfsignatureV2(username, password, otp, nodeRef, apparence);		
	}	
	private void pdfsignatureV2(String username, String password, String otp,
			String nodeRef, PdfSignApparence apparence) throws ArubaSignServiceException,
			IOException {

		NodeRef node = new NodeRef(nodeRef);

		LOGGER.debug("pdfsignatureV2 " + nodeRef);

		ContentReader reader = contentService.getReader(node,
				ContentModel.PROP_CONTENT);

		byte[] bytes;

		if (reader != null) {
			bytes = IOUtils.toByteArray(reader.getContentInputStream());
		} else {
			bytes = new byte[0];
		}

		LOGGER.debug(bytes.length + " bytes");

		byte[] responseBytes = arubaSignServiceClient.pdfsignatureV2(username, password,
				otp, bytes, apparence);

		ContentWriter w = contentService.getWriter(new NodeRef(nodeRef),
				ContentModel.PROP_CONTENT, true);
		ByteArrayInputStream is = new ByteArrayInputStream(responseBytes);
		w.putContent(is);

	}
	
	public void pdfsignatureV2Multiple(String username, String password, String otp,
			List<String> nodeRefs) throws ArubaSignServiceException,
			IOException {
		pdfsignatureV2Multiple(username, password, otp, nodeRefs, null);
	}

	public void pdfsignatureV2Multiple(String username, String password, String otp,
			List<String> nodeRefs, String image, int leftx, int lefty, String location, 
			int page, String reason, int rightx, int righty, String testo) throws ArubaSignServiceException,
			IOException {
		PdfSignApparence apparence = new PdfSignApparence();
		apparence.setImage(image);
		apparence.setLeftx(leftx);
		apparence.setLefty(lefty);
		apparence.setLocation(location);
		apparence.setPage(page);
		apparence.setReason(reason);
		apparence.setRightx(rightx);
		apparence.setRighty(righty);
		apparence.setTesto(testo);
		LOGGER.debug("PdfSignApparence: " + apparence);		
		pdfsignatureV2Multiple(username, password, otp, nodeRefs, apparence);
	}
	
	private void pdfsignatureV2Multiple(String username, String password, String otp,
			List<String> nodeRefs, PdfSignApparence apparence) throws ArubaSignServiceException,
			IOException {
		List<byte[]> bytesArray = new ArrayList<byte[]>();
		
		for (String nodeRef : nodeRefs) {			
			NodeRef node = new NodeRef(nodeRef);

			LOGGER.debug("pdfsignatureV2 " + nodeRef);

			ContentReader reader = contentService.getReader(node,
					ContentModel.PROP_CONTENT);

			byte[] bytes;

			if (reader != null) {
				bytes = IOUtils.toByteArray(reader.getContentInputStream());
			} else {
				bytes = new byte[0];
			}

			LOGGER.debug(bytes.length + " bytes");
			bytesArray.add(bytes);
		}

		List<byte[]> responseBytes = arubaSignServiceClient.pdfsignatureV2Multiple(username, password,
				otp, bytesArray);
		for (int i = 0; i < responseBytes.size(); i++) {
			ContentWriter w = contentService.getWriter(new NodeRef(nodeRefs.get(i)),
					ContentModel.PROP_CONTENT, true);
			ByteArrayInputStream is = new ByteArrayInputStream(responseBytes.get(i));
			w.putContent(is);			
		}
	}	
}