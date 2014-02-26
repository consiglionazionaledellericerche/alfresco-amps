package it.cnr.jada.firma.arss;

import it.cnr.jada.firma.arss.stub.ArubaSignService;
import it.cnr.jada.firma.arss.stub.ArubaSignServiceService;
import it.cnr.jada.firma.arss.stub.Auth;
import it.cnr.jada.firma.arss.stub.DocumentType;
import it.cnr.jada.firma.arss.stub.SignRequestV2;
import it.cnr.jada.firma.arss.stub.SignReturnV2;
import it.cnr.jada.firma.arss.stub.TypeOfTransportNotImplemented_Exception;
import it.cnr.jada.firma.arss.stub.TypeTransport;
import it.cnr.jada.firma.arss.stub.VerifyRequest;
import it.cnr.jada.firma.arss.stub.VerifyReturn;

import java.util.Properties;

import org.apache.log4j.Logger;

public class ArubaSignServiceClient {

	private static final String CERT_ID = "arubaRemoteSignService.certId";
	private static final String TYPE_OTP_AUTH = "arubaRemoteSignService.typeOtpAuth";

	private static final String UTF_8 = "UTF-8";
	private static final String STATUS_OK = "OK";

	private static final Logger LOGGER = Logger.getLogger(ArubaSignServiceClient.class);

	private Properties props;

	public byte[] pkcs7SignV2(String username, String password, String otp,
			byte[] bytes) throws ArubaSignServiceException {
		LOGGER.debug(username);
		LOGGER.debug(otp);
		Auth identity = getIdentity(username, password, otp);
		return pkcs7SignV2(identity, bytes);
	}

	public byte[] verify(byte[] bytes) {
		ArubaSignService service = new ArubaSignServiceService()
				.getArubaSignServicePort();
		VerifyRequest request = new VerifyRequest();
		request.setBinaryinput(bytes);
		request.setTransport(TypeTransport.BYNARYNET);
		request.setType(DocumentType.PKCS_7);

		VerifyReturn out = service.verify(request);
		LOGGER.info(out.getStatus());
		LOGGER.info(out.getDescription());
		return out.getBinaryoutput();
	}

	public byte[] pkcs7SignV2(Auth identity, byte[] bytes)
			throws ArubaSignServiceException {

		LOGGER.debug(identity.getUser());

		ArubaSignService service = new ArubaSignServiceService()
				.getArubaSignServicePort();

		LOGGER.debug("version " + service.getVersion());

		try {
			SignReturnV2 response = service.pkcs7SignV2(
					getRequest(identity, bytes),
					false,
					false);

			LOGGER.debug(response.getReturnCode() + " " + response.getStatus());

			if (response.getStatus().equals(STATUS_OK)) {
				return response.getBinaryoutput();
			} else {
				throw new ArubaSignServiceException("Server side error code "
						+ response.getReturnCode() + ", "
						+ response.getStatus());
			}

		} catch (TypeOfTransportNotImplemented_Exception e) {
			throw new ArubaSignServiceException("error while invoking pkcs7SignV2", e);
		}

	}

	private SignRequestV2 getRequest(Auth identity, byte[] bytes) {
		SignRequestV2 request = new SignRequestV2();
		request.setIdentity(identity);
		request.setCertID(props.getProperty(CERT_ID));
		request.setTransport(TypeTransport.BYNARYNET);
		request.setBinaryinput(bytes);
		return request;
	}

	private Auth getIdentity(String username, String password, String otp) {
		Auth identity = new Auth();
		identity.setUser(username);
		identity.setUserPWD(password);
		identity.setOtpPwd(otp);
		identity.setTypeOtpAuth(props.getProperty(TYPE_OTP_AUTH));
		return identity;
	}


	public void setProps(Properties props) {
		this.props = props;
	}

}
