package it.cnr.rest.application;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class HelloWorldResource extends ServerResource {

	@Get
	public String getEngineInfo() {
		return "hello World";
	}

}
