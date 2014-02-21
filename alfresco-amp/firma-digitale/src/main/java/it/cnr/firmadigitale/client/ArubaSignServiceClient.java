package it.cnr.firmadigitale.client;

import it.cnr.firmadigitale.stub.ArubaSignService;
import it.cnr.firmadigitale.stub.ArubaSignServiceService;
import it.cnr.firmadigitale.stub.Auth;
import it.cnr.firmadigitale.stub.SignRequestV2;
import it.cnr.firmadigitale.stub.SignReturnV2;
import it.cnr.firmadigitale.stub.TypeOfTransportNotImplemented_Exception;
import it.cnr.firmadigitale.stub.TypeTransport;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class ArubaSignServiceClient {

	private static final String TYPE_OTP_AUTH = "collaudo";
	private static final String CERT_ID = "AS0";

	private static final String UTF_8 = "UTF-8";
	private static final String STATUS_OK = "OK";

	private static final Logger LOGGER = Logger.getLogger(ArubaSignServiceClient.class);

	public String pkcs7SignV2(String username, String password, String otp,
			String content) throws ArubaSignServiceException {
		LOGGER.debug(username);
		LOGGER.debug(otp);
		Auth identity = getIdentity(username, password, otp);
		return pkcs7SignV2(identity, content);
	}

	public String pkcs7SignV2(Auth identity, String content)
			throws ArubaSignServiceException {

		LOGGER.debug(identity.getUser());

		ArubaSignService service = new ArubaSignServiceService()
				.getArubaSignServicePort();

		LOGGER.debug("version " + service.getVersion());

		try {
			SignReturnV2 response = service.pkcs7SignV2(
					getRequest(identity, content),
					false,
					false);

			LOGGER.debug(response.getReturnCode() + " " + response.getStatus());

			if (response.getStatus().equals(STATUS_OK)) {
				try {
					return IOUtils.toString(response.getBinaryoutput(), UTF_8);
				} catch (IOException e) {
					throw new ArubaSignServiceException("Unable to get content", e);
				}
			} else {
				throw new ArubaSignServiceException("Server side error code "
						+ response.getReturnCode() + ", "
						+ response.getStatus());
			}

		} catch (TypeOfTransportNotImplemented_Exception e) {
			throw new ArubaSignServiceException("error while invoking pkcs7SignV2", e);
		}

	}

	private SignRequestV2 getRequest(Auth identity, String content) {
		SignRequestV2 request = new SignRequestV2();
		request.setIdentity(identity);
		request.setCertID(CERT_ID);
		request.setTransport(TypeTransport.BYNARYNET);
		request.setBinaryinput(content.getBytes());
		return request;
	}

	private Auth getIdentity(String username, String password, String otp) {
		Auth identity = new Auth();
		identity.setUser(username);
		identity.setUserPWD(password);
		identity.setOtpPwd(otp);
		identity.setTypeOtpAuth(TYPE_OTP_AUTH);
		return identity;
	}

}
