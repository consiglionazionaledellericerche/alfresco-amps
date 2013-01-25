{
"data":
[
<#list tasks as task>
{
  "Task ID":"${jsonUtils.encodeJSONString("${task[0]}")}",
  "Nome":"${jsonUtils.encodeJSONString("${task[1]}")}",
  "Titolo":"${jsonUtils.encodeJSONString("${task[2]}")}",
  "Descrizione":"${jsonUtils.encodeJSONString("${task[3]}")}",
  "Stato":
  <#if task[4]== "COMPLETED" >
    "${jsonUtils.encodeJSONString("COMPLETED")}",
  <#else>
    "${jsonUtils.encodeJSONString("IN_PROGRESS")}",
  </#if>
  "AdvandedProp": "${task[5]}",
  "Initiator":"${task[6]}",
  "InstanceId":"${task[7]}"
}
  <#if task_has_next>,</#if>

</#list>
]
}
