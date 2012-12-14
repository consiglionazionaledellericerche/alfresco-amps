{
   "data":
   <#if (activeInstances?size-1)<0 >
		<#else>
			<#list activeInstances as i>
			   {
					"instancesId":"${i[0]}"
					"instanceDescription":"${i[1]!"Start-Task Not Yet Initialized!"}"
   			   }
			</#list>
   </#if>
}