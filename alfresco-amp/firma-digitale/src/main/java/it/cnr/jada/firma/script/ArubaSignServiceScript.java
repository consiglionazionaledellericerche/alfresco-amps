package it.cnr.jada.firma.script;

import it.cnr.jada.firma.arss.ArubaSignServiceClient;
import it.cnr.jada.firma.arss.ArubaSignServiceException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
                otp, bytes);

        ContentWriter w = contentService.getWriter(new NodeRef(nodeRef),
                ContentModel.PROP_CONTENT, true);
        ByteArrayInputStream is = new ByteArrayInputStream(responseBytes);
        w.putContent(is);

    }



}
