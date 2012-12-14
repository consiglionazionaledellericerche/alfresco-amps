<#compress >
{
"data": [
<#list tasks as task>	
{
"id":"${jsonUtils.encodeJSONString("${task[0]}")}",
"name":"${jsonUtils.encodeJSONString("${task[1]}")}", 
"title":"${jsonUtils.encodeJSONString("${task[2]}")}", 
"description":"${jsonUtils.encodeJSONString("${task[3]}")}", 
"state":<#if task[4]== "COMPLETED" > "${jsonUtils.encodeJSONString("COMPLETED")}"<#else>"${jsonUtils.encodeJSONString("IN_PROGRESS")}"</#if>,
"typeDefinitionTitle": "",
"isPooled": "",
"owner":{
<#list "${task[5]}"?split(",") as x>
<#if "${x}"?contains("owner")>
<#list "${x}"?split("===") as y>
      <#if y_index==1>"userName": "${y}"</#if>
</#list>  		
</#if>
</#list>  
},
"properties":
{
<#list "${task[5]}"?split(",") as x>
	<#if "${x}"!="">
		<#assign key="">
		<#assign val="">				
		<#list "${x}"?split("===") as keyVal>
			<#if keyVal_index==0><#assign key="${keyVal}"></#if>
			<#if keyVal_index==1><#assign val="${keyVal}"></#if>						
		</#list>
		<#if "${key}"?contains("Date")>
			<#assign val="${val}"?replace(" ",'T')>
		<#elseif "${key}"?contains("cm_created")>
			<#assign val="${val}"?replace(" ",'T')>
		</#if>		
		<#if "${val}"?contains("null")>
			"${key}":null
		<#else>
			"${key}":"${val}"
		</#if>		
	<#elseif "${x}"=="">"null":"null"</#if>
	<#if x_has_next>,</#if>	
</#list>  
}
}
<#if task_has_next>,</#if>
</#list> 
] 
}
<#--"AdvandedProp":${task[5]}-->
</#compress>