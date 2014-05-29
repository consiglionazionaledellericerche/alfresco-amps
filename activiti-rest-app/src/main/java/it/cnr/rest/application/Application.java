package it.cnr.rest.application;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.SecretVerifier;
import org.restlet.security.Verifier;


public class Application extends org.restlet.Application {

	private ChallengeAuthenticator authenticator;

	@Override
	public synchronized Restlet createInboundRoot() {
		Verifier verifier = new SecretVerifier() {

			@Override
			public boolean verify(String username, char[] password)
					throws IllegalArgumentException {
				System.out.println("CHECK AUTHORIZATION FOR USER " + username
						+ " " + password);
				return true;
			}
		};
		authenticator = new ChallengeAuthenticator(null, true,
				ChallengeScheme.HTTP_BASIC, "Activiti Realm") {

			@Override
			protected boolean authenticate(Request request, Response response) {
				if (request.getChallengeResponse() == null) {
					return false;
				} else {
					return super.authenticate(request, response);
				}
			}
		};
		authenticator.setVerifier(verifier);

		Router router = new Router(getContext());

		router.attach("/hello", HelloWorldResource.class);

		authenticator.setNext(router);

		return authenticator;
	}

	public String authenticate(Request request, Response response) {
		if (!request.getClientInfo().isAuthenticated()) {
			authenticator.challenge(response, false);
			return null;
    }
		return request.getClientInfo().getUser().getIdentifier();
	}
}

