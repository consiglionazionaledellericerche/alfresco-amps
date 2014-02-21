package it.cnr.jada.firma.script;

import it.cnr.jada.firma.arss.ArubaSignServiceClient;
import it.cnr.jada.firma.arss.ArubaSignServiceException;

import java.io.IOException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ArubaSignServiceScript extends BaseScopableProcessorExtension implements
		ApplicationContextAware {

	private final Logger LOGGER = Logger.getLogger(ArubaSignServiceScript.class);

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


	public String pkcs7SignV2(String username, String password, String otp,
			String nodeRef) throws ArubaSignServiceException,
			ContentIOException, IOException {

		NodeRef node = new NodeRef(nodeRef);

		ContentReader reader = contentService.getReader(node,
				ContentModel.PROP_CONTENT);

		byte[] bytes = IOUtils.toByteArray(reader.getContentInputStream());

		return new ArubaSignServiceClient().pkcs7SignV2(username, password,
				otp, bytes);
	}

}
