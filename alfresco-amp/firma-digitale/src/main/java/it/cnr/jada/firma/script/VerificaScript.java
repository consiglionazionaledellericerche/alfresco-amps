package it.cnr.jada.firma.script;

import it.actalis.ellips.capi.CapiException;
import it.cnr.jada.firma.Verifica;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
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

	public void verificaBustaFirmata(String nodeRef)
			throws FileNotFoundException,
			CapiException {

		NodeRef node = new NodeRef(nodeRef);
		ContentReader reader = contentService.getReader(node,
				ContentModel.PROP_CONTENT);
		if (reader != null) {
			InputStream is = reader.getContentInputStream();
			Verifica.verificaBustaFirmata(is, System.out);
		} else {
			LOGGER.error("node not found " + nodeRef);
		}
	}

}
