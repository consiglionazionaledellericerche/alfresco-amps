<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if events?exists && events?size &gt; 0>
<#assign prev = "">
<#-- We do the sort here as the sort in the JavaScript doesn't seem to work as expected! -->
<#list events?sort_by(["fromDate"]) as item>
<#assign event = item.event>
<#assign date = event.properties["ia:fromDate"]?string("M/d/yyyy")>
<#if date != prev>
<#assign counter = 0>
<#if item_index &gt; 0>],</#if>
"${date}" : [
</#if>
<#if counter &gt; 0>,</#if>
{
  "name" : "${event.properties["ia:whatEvent"]}",
  "from": "${event.properties["ia:fromDate"]?string("M/d/yyyy")}",
  "start" : "${event.properties["ia:fromDate"]?string("HH:mm")}",
  "to" : "${event.properties["ia:toDate"]?string("M/d/yyyy")}",
  "end" : "${event.properties["ia:toDate"]?string("HH:mm")}",
  "uri" : "calendar/event/${siteId}/${event.name}",
   <#assign tags><#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list></#assign>
  "tags": <#noescape>[${tags}]</#noescape>
}
<#assign counter = counter + 1>
<#if !item_has_next>]</#if>
<#assign prev = date>
</#list>
</#if>
}
</#escape>
