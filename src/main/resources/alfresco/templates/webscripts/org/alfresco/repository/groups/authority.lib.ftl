<#-- renders an authority object which can be either a GROUP or USER (and possibly ROLE in future)-->
<#macro authorityJSON authority>
    <#escape x as jsonUtils.encodeJSONString(x)>
{
        "authorityType" : "${authority.authorityType}",
        "shortName" : "${authority.shortName}",
        "fullName" : "${authority.fullName}",
        "displayName" : "${authority.displayName}",
        
        <#-- Group specific properties -->
        <#if authority.rootGroup??>"isRootGroup": ${authority.rootGroup?string("true", "false")}, </#if>
        <#if authority.adminGroup??>"isAdminGroup": ${authority.adminGroup?string("true", "false")}, </#if>
        <#if authority.groupCount??>"groupCount": ${authority.groupCount}, </#if>
        <#if authority.userCount??>"userCount": ${authority.userCount}, </#if>        
        <#-- end of group specific properties -->
        
        <#if authority.authorityType = "GROUP" >     
        "url" : "/api/groups/${authority.shortName}"
        </#if> 
        
        <#if authority.authorityType = "USER" >     
        "url" : "/api/people/${authority.shortName}"
        </#if> 
}
	</#escape>
</#macro>