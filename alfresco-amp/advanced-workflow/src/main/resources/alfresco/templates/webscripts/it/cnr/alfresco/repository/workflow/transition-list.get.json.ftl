{
"data":[
#list transitions as transition>
{	
"transitionId":"${jsonUtils.encodeJSONString("${transition[0]}")}" ,
"title":"${jsonUtils.encodeJSONString("${transition[1]}")}" ,
"description":"${jsonUtils.encodeJSONString("${transition[2]}")}"
}
<#if transition_has_next>,</#if>
</#list>  
],
"taskId": "${taskId}"
}
