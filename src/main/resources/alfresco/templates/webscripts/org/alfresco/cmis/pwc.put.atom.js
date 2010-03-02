<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/constants.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/modify.lib.js">

script:
{
    // locate node
    var object = getObjectFromUrl();
    if (object.node === null || !object.node.hasAspect("cm:workingcopy"))
    {
        break script;
    }
    model.node = object.node;
    
    // check permissions
    model.checkin = args[cmis.ARG_CHECKIN] == "true" ? true : false;
    if (model.checkin && !model.node.hasPermission("CheckIn"))
    {
        status.code = 403;
        status.message = "Permission to checkin is denied";
        status.redirect = true;
        break script;
    }
    
    if (entry !== null)
    {
        // update properties
        var updated = updateNode(model.node, entry, null, function(propDef) {return putValidator(propDef, true);});
        if (updated === null)
        {
            break script;
        }
        if (updated)
        {
            model.node.save();
        }
    }
    
    // checkin
    if (model.checkin)
    {
        var comment = args[cmis.ARG_CHECKIN_COMMENT];
        var major = args[cmis.ARG_MAJOR];
        major = (major === null || major == "true") ? true : false;
        model.node = model.node.checkin(comment === null ? "" : comment, major);
    }
}
