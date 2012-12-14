{"data":[
<#list packages as package>
<#list package?split("|") as x>
	<#if x_index==0><#assign pack=x></#if>
	<#if x_index==1><#assign task=x></#if> 
</#list>
{ "nodeRef": "${jsonUtils.encodeJSONString("${pack}")}", "taskId": "${jsonUtils.encodeJSONString("${task}")}"}
<#if package_has_next>,</#if>
</#list>
]  
}