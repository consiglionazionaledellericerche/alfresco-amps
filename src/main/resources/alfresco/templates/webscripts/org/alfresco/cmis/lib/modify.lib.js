//
// Create Alfresco Node from Atom Entry
//
// @param parent  parent to create node within
// @param entry  atom entry
// @param slug (optional)  node name
// @return  created node (or null, in case of error)
//
function createNode(parent, entry, slug)
{
    var object = entry.getExtension(atom.names.cmisra_object);
    var typeId = (object !== null) ? object.objectTypeId.nativeValue : null;

    // locate type definition
    // TODO: check this against spec - default to Document, if not specified
    var type = cmis.queryType(typeId === null ? DOCUMENT_TYPE_ID.id : typeId);
    if (type === null)
    {
        status.code = 400;
        status.message = "CMIS object type " + typeId + " not understood";
        status.redirect = true;
        return null;
    }

    // construct node of folder or file
    var node = null;
    var name = (slug !== null) ? slug : entry.title;
    var baseType = type.typeId.baseTypeId;
    if (baseType == DOCUMENT_TYPE_ID)
    {
        node = parent.createFile(name);
        // TODO: versioningState argument (CheckedOut/CheckedInMinor/CheckedInMajor)
    }
    else if (baseType == FOLDER_TYPE_ID)
    {
        node = parent.createFolder(name);
    }
    else
    {
        status.code = 400;
        status.message = "Cannot create object of type " + typeId;
        status.redirect = true;
        return null;
    }

    // specialize to required custom type
    if (type.typeId != DOCUMENT_TYPE_ID && type.typeId != FOLDER_TYPE_ID)
    {
        if (!node.specializeType(type.typeId.QName))
        {
            status.code = 400;
            status.message = "Cannot create object of type " + typeId;
            status.redirect = true;
            return null;
        }
    }
    
    // update node properties (excluding object type & name)
    var exclude = [ PROP_OBJECT_TYPE_ID, PROP_NAME ];
    var updated = updateNode(node, entry, exclude, function(propDef) {return patchValidator(propDef, true);});

    // only return node if updated successfully
    return (updated == null) ? null : node;
}


//
// Update Alfresco Node with Atom Entry
//
// @param node  Alfresco node to update
// @param entry  Atom entry to update from
// @param exclude  property names to exclude
// @param validator  function callback for validating property update
// @return  true => node has been updated (or null, in case of error)
//
function updateNode(node, entry, exclude, validator)
{
    // check update is allowed
    if (!node.hasPermission("WriteProperties") || !node.hasPermission("WriteContent"))
    {
        status.code = 403;
        status.message = "Permission to update is denied";
        status.redirect = true;
        return null;
    }
    
    var updated = false;
    var object = entry.getExtension(atom.names.cmisra_object);
    var props = (object == null) ? null : object.properties;
    var vals = new Object();

    // calculate list of properties to update
    // TODO: consider array form of properties.names
    var updateProps = (props == null) ? new Array() : props.ids.toArray().filter(function(element, index, array) {return true;});
    updateProps.push(PROP_NAME);   // mapped to entry.title
    var exclude = (exclude == null) ? new Array() : exclude;
    exclude.push(PROP_BASE_TYPE_ID);
    updateProps = updateProps.filter(includeProperty, exclude);
    
    // build values to update
    if (updateProps.length > 0)
    {
        var typeDef = cmis.queryType(node);
        var propDefs = typeDef.propertyDefinitions;
        for each (propName in updateProps)
        {
            // is this a valid property?
            var propDef = propDefs[propName];
            if (propDef == null)
            {
                status.code = 400;
                status.message = "Property " + propName + " is not a known property for type " + typeDef.typeId;
                status.redirect = true;
                return null;
            }

            // validate property update
            var valid = validator(propDef);
            if (valid == null)
            {
                // error, abort update
                return null;
            }
            if (valid == false)
            {
                // ignore property
                continue;
            }

            // extract value
            var val = null;
            var prop = (props == null) ? null : props.find(propName);
            if (prop != null && !prop.isNull())
            {
                if (prop.isMultiValued())
                {
                    if (propDef.updatability === CMISCardinalityEnum.MULTI_VALUED)
                    {
                        status.code = 500;
                        status.message = "Property " + propName + " is single valued."
                        status.redirect = true;
                        return null;
                    }
                    val = prop.nativeValues;
                }
                else
                {
                    val = prop.nativeValue;
                }
            }
            
            // NOTE: special case name: entry.title overrides cmis:name
            if (propName === PROP_NAME)
            {
                val = entry.title;
            }
            
            vals[propDef.propertyAccessor.mappedProperty.toString()] = val;
        }
    }

    // NOTE: special case cm_description property (this is defined on an aspect, so not part of
    //       formal CMIS type model
    if (entry.summary != null) vals["cm:description"] = entry.summary;
    
    // update node values
    for (val in vals)
    {
        node.properties[val] = vals[val];
        updated = true;
    }

    // handle content
    // NOTE: cmisra:content overrides atom:content
    var cmiscontent = entry.getExtension(atom.names.cmisra_content);
    if (cmiscontent != null)
    {
        if (!node.isDocument)
        {
            status.code = 400;
            status.message = "Cannot update content on folder " + node.displayPath;
            status.redirect = true;
            return null;
        }
    
        var mediatype = cmiscontent.mediaType;
        if (mediatype == null)
        {
            status.code = 400;
            status.message = "cmisra:content mediatype is missing";
            status.redirect = true;
            return null;
        }
        var contentStream = cmiscontent.contentStream;
        if (contentStream == null)
        {
            status.code = 400;
            status.message = "cmisra:content base64 content is missing";
            status.redirect = true;
            return null;
        }
        
        // update content
        node.properties.content.write(contentStream);
        node.properties.content.encoding = "UTF-8";
        node.properties.content.mimetype = mediatype;
    }
    
    if (cmiscontent == null && entry.content != null && entry.contentSrc == null)
    {
        if (!node.isDocument)
        {
            status.code = 400;
            status.message = "Cannot update content on folder " + node.displayPath;
            status.redirect = true;
            return null;
        }
        
        if (entry.contentType != null && entry.contentType == "MEDIA")
        {
            node.properties.content.write(entry.contentStream);
        }
        else
        {
            node.content = entry.content;
        }
        node.properties.content.encoding = "UTF-8";
        node.properties.content.mimetype = atom.toMimeType(entry);
        updated = true;
    }
    
    return updated;
}


//
// Create Alfresco Association from Atom Entry
//
// @param source  source node
// @param entry  atom entry
// @return  created association (or null, in case of error)
//
function createAssociation(source, entry)
{
    var object = entry.getExtension(atom.names.cmisra_object);
    var typeId = (object !== null) ? object.objectTypeId.nativeValue : null;

    // locate relationship type definition
    // TODO: check this against spec - default to Relationship, if not specified
    var type = cmis.queryType(typeId === null ? RELATIONSHIP_TYPE_ID.id : typeId);
    if (type === null)
    {
        status.setCode(400, "CMIS object type " + typeId + " not understood");
        return null;
    }
    if (type.typeId.baseTypeId != RELATIONSHIP_TYPE_ID)
    {
        status.setCode(400, "CMIS object type " + typeId + " is not a relationship type");
        return null;
    }
    if (!type.creatable)
    {
        status.setCode(400, "Relationship type " + typeId + " is not creatable");
        return null;
    }

    // locate target
    var targetId = (object !== null) ? object.targetId.nativeValue : null;
    if (targetId === null)
    {
        status.setCode(400, "Target Id has not been specified");
        return null;
    }
    var target = search.findNode(targetId);
    if (target === null)
    {
        status.setCode(400, "Target Id " + targetId + " does not refer to known item");
        return null;
    }
    
    // create association
    var assoc = source.createAssociation(target, type.typeId.QName.toString());
    return assoc;
}


// callback for validating property update for patch
// return null => update not allowed, abort update
//        true => update allowed
//        false => update not allowed, ignore property
function patchValidator(propDef, pwc)
{
    // is the property write-able?
    if (propDef.updatability === CMISUpdatabilityEnum.READ_ONLY)
    {
        status.code = 500;
        status.message = "Property " + propName + " cannot be updated. It is read only."
        status.redirect = true;
        return null;
    }
    if (!pwc && propDef.updatability === CMISUpdatabilityEnum.READ_AND_WRITE_WHEN_CHECKED_OUT)
    {
        status.code = 500;
        status.message = "Property " + propName + " can only be updated on a private working copy.";
        status.redirect = true;
        return null;
    }
    var mappedProperty = propDef.propertyAccessor.mappedProperty;
    if (mappedProperty == null)
    {
        status.code = 500;
        status.message = "Internal error: Property " + propName + " does not map to a write-able Alfresco property";
        status.redirect = true;
        return null;
    }
    return true;
}

//callback for validating property update for put
//return null => update not allowed, abort update
//     true => update allowed
//     false => update not allowed, ignore property
function putValidator(propDef, pwc)
{
    // is the property write-able?
    if (propDef.updatability === CMISUpdatabilityEnum.READ_ONLY)
    {
        return false;
    }
    if (!pwc && propDef.updatability === CMISUpdatabilityEnum.READ_AND_WRITE_WHEN_CHECKED_OUT)
    {
        return false;
    }
    var mappedProperty = propDef.propertyAccessor.mappedProperty;
    if (mappedProperty == null)
    {
        return false;
    }
    return true;
}

// callback function for determining if property name should be excluded
// note: this refers to array of property names to exclude
function includeProperty(element, index, array)
{
    for each (exclude in this)
    {
        if (element == exclude)
        {
            return false;
        }
    }
    return true;
}
