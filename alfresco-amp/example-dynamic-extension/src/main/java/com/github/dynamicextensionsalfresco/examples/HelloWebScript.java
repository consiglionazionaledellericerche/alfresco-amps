package com.github.dynamicextensionsalfresco.examples;

import java.io.IOException;

import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

@Component
@WebScript
public class HelloWebScript {

  @Uri("/dynamic-extensions/examples/hello")
  public void handleHello(@RequestParam final String name, final WebScriptResponse response) throws IOException {
    final String message = String.format("Hello, %s", name);
    response.getWriter().write(message);
  }

}
