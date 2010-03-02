script:
{
    // process query statement
    // <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    // <cmis:query xmlns:cmis="http://docs.oasis-open.org/ns/cmis/core/200908/">
    //   <cmis:statement>SELECT * FROM Document</cmis:statement>
    //   <cmis:searchAllVersions>true</cmis:searchAllVersions>
    //   <cmis:maxItems>50</cmis:maxItems>
    //   <cmis:skipCount>0</cmis:skipCount>
    // </cmis:query>
    
    
    // TODO: XML parsing need to be moved to Java

    function ltrim(str){
        return str.replace(/^\s+/, '');
    }

    default xml namespace = 'http://docs.oasis-open.org/ns/cmis/core/200908/';
    
    // regex to match an XML declaration
    var xmlDeclaration = /^<\?xml version[^>]+?>/; 
    
    // remove xml declaration and leading whitespace
    query = ltrim(query.replace(xmlDeclaration, ''));

    // need to move the XML declaration if it exists
    var cmisQuery = new XML(query);

    // extract query statement
    model.statement = cmisQuery.statement.toString();
    if (model.statement == null || model.statement.length == 0)
    {
        status.setCode(status.STATUS_BAD_REQUEST, "Query statement must be provided");
        break script;
    }
    
    // process search all versions (NOTE: not supported)
    var searchAllVersions = cmisQuery.searchAllVersions;
    if (searchAllVersions != null && searchAllVersions === "true")
    {
        status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Search all versions not supported");
        break script;
    }
    
    // include allowable actions
    var includeAllowableActions = cmisQuery.includeAllowableActions.toString();
    model.includeAllowableActions = (includeAllowableActions == null || includeAllowableActions == "true" ? true : false);
    
    // process paging
    var skipCount = parseInt(cmisQuery.skipCount);
    var maxItems = parseInt(cmisQuery.maxItems);
    var page = paging.createPageOrWindow(null, null, isNaN(skipCount) ? null : skipCount, isNaN(maxItems) ? null : maxItems);
    
    // perform query
    var paged = cmis.query(model.statement, page);
    model.resultset = paged.result;
    model.cursor = paged.cursor;
    
    // check includeFlags are valid for query
    var multiNodeResultSet = false;  // todo: calculate from result set (for now, don't support joins)
    if (multiNodeResultSet && (model.includeAllowableActions))
    {
        status.setCode(status.STATUS_BAD_REQUEST, "Can't includeAllowableActions for multi-selector column result sets");
        break script;
    }

    // construct query uri
    model.queryUri = "/cmis/query";
    model.queryArgs = cmis.ARG_QUERY_STATEMENT + "=" + model.statement;
    if (model.includeAllowableActions) model.queryArgs += "&" + cmis.ARG_INCLUDE_ALLOWABLE_ACTIONS + "=true";
    model.queryArgs += "&" + cmis.ARG_SKIP_COUNT + "=" + page.number;
    model.queryArgs += "&" + cmis.ARG_MAX_ITEMS + "=" + page.size;
    
    // TODO: set Content-Location
    status.code = 201;
    status.location = url.server + url.serviceContext + model.queryUri + "?" + model.queryArgs;
}
