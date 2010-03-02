<#--
   Renders paged results information.
   The passed in data object should contain following attributes:
   pageSize: the number of elements requested to be returned (forms part of the request)
   startIndex: the index of the first element to be returned (forms part of the request)
   total: the total number of elements in the result set
   itemCount: the actual number of elements returned
   items: the result elements
   
Usage:
   <#import "generic-paged-results.lib.ftl" as gen/>
   {
   <@gen.pagedResults data=data ; item>
      output of the individual item, for example by calling another macro:
      <@yourLib.itemJSON item=item />
   </@gen.pagedResults>
   }
-->
<#macro pagedResults data>
   "total": ${data.total?c},
   "pageSize": ${data.pageSize?c},
   "startIndex": ${data.startIndex?c},
   "itemCount": ${data.itemCount?c},
   "items":
   [
   <#list data.items as item>
      <#nested item>
      <#if item_has_next>,</#if>
   </#list>
   ]
</#macro>
