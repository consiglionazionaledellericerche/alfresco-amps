<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">

script:
{
    // locate node
    var object = getObjectFromUrl();
    if (object.node == null)
    {
        break script;
    }
    var node = object.node;
    
    // locate parent
    if (node.id == cmis.defaultRootFolder.id)
    {
        status.code = 404;
        status.message = "Object " + object.ref + " parent not found";
        status.redirect = true;
        break script;
    }
    model.node = node.parent;
 
    // property filter 
    model.filter = args[cmis.ARG_FILTER];
    if (model.filter === null)
    {
        model.filter = "*";
    }
   
    // include allowable actions
    var includeAllowableActions = args[cmis.ARG_INCLUDE_ALLOWABLE_ACTIONS];
    model.includeAllowableActions = (includeAllowableActions == "true" ? true : false);
}
