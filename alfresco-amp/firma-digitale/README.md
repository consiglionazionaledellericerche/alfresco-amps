# Sample usage #

## Javascript ##

	var encrypted = "workspace://SpacesStore/7eb10a6e-f0d5-43e1-bcdf-79a6eb0acb50";
	var clear = "workspace://SpacesStore/544c0572-a7e9-4ba4-acd2-a7cadc84a63c";
	try {
		model.outcome = verifica.verificaBustaFirmata(encrypted, clear);
	} catch(error) {
		model.error = error.toString();
	}

## Freemarker Template ##

	<#if error??>
		${error}
	<#else>
		${outcome?string}
	</#if>


## Firma PDF

	var nodeRef = 'workspace://SpacesStore/e7712707-66fe-4bb6-84ea-59ba35d7667e';	

	model.a = arubaSign.pdfsignatureV2("utentefr", "utentefr123", "6629961578", nodeRef);
