package it.cnr.jada.firma.arss;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Test;

public class ArubaSignServiceClientTest {


	private static final Logger LOGGER = Logger.getLogger(ArubaSignServiceClientTest.class);

	private static final String USERNAME = "test_collaudo3";
	private static final String PASSWORD = "password11";
	private static final String OTP = "dspin";
	private static final String CONTENT = "Prova CNR";

	@Test
	public void testPkcs7SignV2() throws IOException, ArubaSignServiceException {
		String content = new ArubaSignServiceClient().pkcs7SignV2(USERNAME,
				PASSWORD, OTP, CONTENT.getBytes());
		assertTrue(content != null && content.length() > 0);
		LOGGER.info(content);
	}

}
