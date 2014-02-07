package it.cnr.jada.firma.script;

import it.actalis.ellips.capi.CapiException;
import it.cnr.jada.firma.Verifica;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class VerificaScript extends BaseScopableProcessorExtension implements
		ApplicationContextAware {

	private final Logger LOGGER = Logger.getLogger(VerificaScript.class);

	private ApplicationContext applicationContext;
	private ContentService contentService;

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public boolean verificaBustaFirmata(String encryptedNodeRef,
			String clearNodeRef) throws FileNotFoundException, CapiException {

		ByteArrayOutputStream decryptedContentOutputStream = new ByteArrayOutputStream();

		InputStream encryptedInputStream = getInputStreamForNode(encryptedNodeRef);

		Verifica.verificaBustaFirmata(encryptedInputStream,
				decryptedContentOutputStream);

		InputStream clearInputStream = getInputStreamForNode(clearNodeRef);
		byte[] clearText = null;
		try {
			clearText = IOUtils.toByteArray(clearInputStream);
		} catch (IOException e) {
			LOGGER.error("Unable to get data for node " + clearNodeRef, e);
		}

		return Arrays.equals(decryptedContentOutputStream.toByteArray(),
				clearText);
		
		
	}

	private InputStream getInputStreamForNode(String nodeRef) {

		NodeRef node = new NodeRef(nodeRef);

		ContentReader reader = contentService.getReader(node,
				ContentModel.PROP_CONTENT);

		if (reader != null) {
			return reader.getContentInputStream();
		} else {
			LOGGER.error("Unable to get reader for node " + nodeRef);
		}

		return null;
	}

}
