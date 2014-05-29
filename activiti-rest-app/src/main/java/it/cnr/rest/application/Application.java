package it.cnr.rest.application;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;


public class Application extends org.restlet.Application {

	private ChallengeAuthenticator authenticator;

	@Override
	public synchronized Restlet createInboundRoot() {

		authenticator = getAuthenticator();

		Router router = new Router(getContext());

		router.attach("/hello", HelloWorldResource.class);

		authenticator.setNext(router);

		return authenticator;
	}

	private static ChallengeAuthenticator getAuthenticator() {

		ChallengeAuthenticator a = new ChallengeAuthenticator(null, true,
				ChallengeScheme.HTTP_BASIC, "CNR Realm") {

			@Override
			protected boolean authenticate(Request request, Response response) {
				return true;
			}
		};

		return a;
	}

}

