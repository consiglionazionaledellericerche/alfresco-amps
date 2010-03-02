<#-- Renders a person object. -->
<#macro renderPerson person fieldName>
<#escape x as jsonUtils.encodeJSONString(x)>
   "${fieldName}":
   {
      <#if person.assocs["cm:avatar"]??>
      "avatarRef": "${person.assocs["cm:avatar"][0].nodeRef?string}",
      </#if>
      "username": "${person.properties["cm:userName"]}",
      "firstName": "${person.properties["cm:firstName"]}",
      "lastName": "${person.properties["cm:lastName"]}"
   },
</#escape>
</#macro>

<#--
   This template renders a comment.
-->
<#macro commentJSON item>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "url": "api/comment/node/${item.node.nodeRef?replace('://','/')}",
   "nodeRef": "${item.node.nodeRef}",
   "name": "${item.node.properties.name!''}",
    "title": "${item.node.properties.title!''}",
   "content": "${stringUtils.stripUnsafeHTML(item.node.content)}",
   <#if item.author??>
   <@renderPerson person=item.author fieldName="author" />
   <#else>
   "author":
   {
      "username": "${item.node.properties.creator}"
   },
   </#if>
   "createdOn": "${item.node.properties.created?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
   "modifiedOn": "${item.node.properties.modified?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
   "isUpdated": ${item.isUpdated?string},
   "permissions":
   {
      "edit": ${item.node.hasPermission("Write")?string},
      "delete": ${item.node.hasPermission("Delete")?string}
   }
}
</#escape>
</#macro>