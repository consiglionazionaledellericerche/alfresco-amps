{
   "data": 
   [
<#if defarraylength != 1>
<#list defarray as i>	
	{
   	    "id" : "${i.id}",
	    "name" : "${i.name}",
	    "title" : "${i.title}",
  	    "description" : "${i.description}"
	}
            <#if i_has_next>,</#if>
</#list>  
</#if>
<#if defarraylength == 1>
<#list defarray as i>	
	{
   	    "id" : "${i.id}",
	    "name" : "${i.name}",
	    "title" : "${i.title}",
  	    "description" : "${i.description}"
	}
            <#if i_has_next>,</#if>
</#list>  
</#if>
   ]
}