<#assign doc_actions="${url.serviceContext}/office/docActions">
<#assign path=args.p!"">
<#assign extn=args.e!"doc"><#assign extnx=extn+"x">
<#assign nav=args.n!"">
<#-- resolve the path (from Company Home) into a node -->
<#if companyhome.childByNamePath[path]??>
   <#assign d=companyhome.childByNamePath[path]>
<#else>
   <#assign d=companyhome>
</#if>
<#assign defaultQuery="?p=" + path?url + "&e=" + extn + "&n=" + nav>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
   <title>${message("office.title.document_details")}</title>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office.css" />
<!--[if IE 6]>
   <link rel="stylesheet" type="text/css" href="${url.context}/css/office_ie6.css" />
<![endif]-->
   <script type="text/javascript" src="${url.context}/scripts/ajax/mootools.v1.11.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/office_addin.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/external_component.js"></script>
   <script type="text/javascript" src="${url.context}/scripts/office/doc_details.js"></script>
   <script type="text/javascript">//<![CDATA[
      OfficeAddin.defaultQuery = "${defaultQuery}";
      ExternalComponent.init(
      {
         fullUrl: "${url.full}",
         folderPath: "${url.serviceContext}/office/",
         ticket: "${session.ticket}"
      });
   //]]></script>
</head>
<body>

<div class="tabBar">
   <ul>
      <li><a title="${message("office.title.my_alfresco")}" href="${url.serviceContext}/office/myAlfresco${defaultQuery?html}"><span><img src="${url.context}/images/office/my_alfresco.gif" alt="${message("office.title.my_alfresco")}" /></span></a></li>
      <li><a title="${message("office.title.navigation")}" href="${url.serviceContext}/office/navigation${defaultQuery?html}"><span><img src="${url.context}/images/office/navigator.gif" alt="${message("office.title.navigation")}" /></span></a></li>
      <li><a title="${message("office.title.search")}" href="${url.serviceContext}/office/search${defaultQuery?html}"><span><img src="${url.context}/images/office/search.gif" alt="${message("office.title.search")}" /></span></a></li>
      <li id="current"><a title="${message("office.title.document_details")}" href="${url.serviceContext}/office/documentDetails${defaultQuery?html}"><span><img src="${url.context}/images/office/document_details.gif" alt="${message("office.title.document_details")}" /></span></a></li>
      <li><a title="${message("office.title.my_tasks")}" href="${url.serviceContext}/office/myTasks${defaultQuery?html}"><span><img src="${url.context}/images/office/my_tasks.gif" alt="${message("office.title.my_tasks")}" /></span></a></li>
      <li><a title="${message("office.title.document_tags")}" href="${url.serviceContext}/office/tags${defaultQuery?html}"><span><img src="${url.context}/images/office/tag.gif" alt="${message("office.title.document_tags")}" /></span></a></li>
   </ul>
   <span class="help">
      <a title="${message("office.help.title")}" href="${message("office.help.url")}" target="alfrescoHelp"><img src="${url.context}/images/office/help.gif" alt="${message("office.help.title")}" /></a>
   </span>
</div>

<div class="headerRow">
   <div class="headerWrapper"><div class="header">${message("office.header.details")}</div></div>
</div>

<div class="containerMedium">
   <div id="nonStatusText">
      <table width="265">
         <tbody>
            <tr>
               <td valign="top">
<#if d.isDocument>
                  <img src="${url.context}${d.icon32}" alt="${d.name?html}" />
               </td>
               <td style="padding-top: 4px;">
                  <span style="font-weight:bold; vertical-align: top;">
   <#if d.isLocked >
                     <img src="${url.context}/images/office/lock.gif" alt="${message("office.status.locked")}" title="${message("office.status.locked")}" style="margin: -2px 0px;" />
   </#if>
                     ${d.name?html}
                  </span>
                  <br />
                  <table style="margin-top: 4px;">
                     <tr><td valign="top">${message("office.property.title")}:</td><td>${(d.properties.title!"")?html}</td></tr>
                     <tr><td valign="top">${message("office.property.description")}:</td><td>${(d.properties. description!"")?html}</td></tr>
                     <tr><td>${message("office.property.creator")}:</td><td>${d.properties.creator}</td></tr>
                     <tr><td>${message("office.property.created")}:</td><td>${d.properties.created?datetime}</td></tr>
                     <tr><td>${message("office.property.modifier")}:</td><td>${d.properties.modifier}</td></tr>
                     <tr><td>${message("office.property.modified")}:</td><td>${d.properties.modified?datetime}</td></tr>
                     <tr><td>${message("office.property.size")}:</td><td>${d.size / 1024} ${message("office.unit.kb")}</td></tr>
                     <tr><td valign="top">${message("office.property.categories")}:</td>
                        <td>
   <#if d.properties.categories??>
      <#list d.properties.categories as category>
                           ${companyhome.nodeByReference[category.nodeRef].name?html}; 
      </#list>
   <#else>
                           ${message("office.message.none")}.
   </#if>
                        </td>
                     </tr>
                  </table>
<#else>
                  ${message("office.message.unmanaged")}
</#if>
               </td>
            </tr>
         </tbody>
      </table>
   </div>
   <div id="statusText"></div>
</div>

<div class="tabBarInline">
   <ul>
      <li class="current"><a id="tabLinkTags" title="${message("office.header.document_tags")}" href="#"><span><img src="${url.context}/images/office/document_tag.gif" alt="${message("office.header.document_tags")}" /></span></a></li>
      <li><a id="tabLinkVersion" title="${message("office.header.version_history")}" href="#"><span><img src="${url.context}/images/office/version.gif" alt="${message("office.header.version_history")}" /></span></a></li>
   </ul>
</div>

<div id="panelTags" class="tabPanel">
   <div class="tabHeader"><#if d.isDocument>${message("office.header.document_tags.for", d.name?html)}<#else>${message("office.header.document_tags")}</#if></div>
   <div id="tagList" class="containerTabMedium">
   <#if d.isDocument >
      <div class="addTagIcon"></div>
      <div id="addTagLinkContainer">
         <a href="#" onclick="OfficeDocDetails.showAddTagForm(); return false;">${message("office.action.add_tag")}</a>
      </div>
      <div id="addTagFormContainer">
         <form id="addTagForm" action="#" onsubmit="return OfficeDocDetails.addTag('${d.id}', this.tag.value);">
            <fieldset class="addTagFieldset">
               <input id="addTagBox" name="tag" type="text" />
               <input class="addTagImage" type="image" src="${url.context}/images/office/action_successful.gif" onclick="return ($('addTagBox').value.length > 0);" />
               <input class="addTagImage" type="image" src="${url.context}/images/office/action_failed.gif" onclick="return OfficeDocDetails.hideAddTagForm();" />
            </fieldset>
         </form>
      </div>
      <#if d.hasAspect("cm:taggable")>
         <#if (d.properties["cm:taggable"]?size > 0)>
            <#list d.properties["cm:taggable"]?sort_by("name") as tag>
               <#if tag??>
      <div class="tagListEntry">
         <a class="tagListDelete" href="#" title="${message("office.action.remove_tag", tag.name)?html}" onclick="OfficeDocDetails.removeTag('${d.id}', '${tag.name?js_string}');">[x]</a>
         <a class="tagListName" href="${url.serviceContext}/office/tags${defaultQuery?html}&amp;tag=${tag.name?url}">${tag.name?html}</a>
      </div>
               </#if>
            </#list>
         </#if>
      </#if>
   <#else>
      <table width="265">
         <tr>
            <td valign="top">
               ${message("office.message.unmanaged")}
            </td>
         </tr>
      </table>
   </#if>
   </div>
</div>

<div id="panelVersion" class="tabPanel tabPanelHidden">
   <div class="tabHeader"><#if d.isDocument>${message("office.header.version_history.for", d.name?html)}<#else>${message("office.header.version_history")}</#if></div>
   <div id="versionList" class="containerTabMedium">
      <table width="265">
   <#if d.isDocument >
      <#if d.hasAspect("cm:versionable")>
         <#assign versionRow=0>
         <#list d.versionHistory as record>
            <#assign versionRow=versionRow+1>
         <tr class="${(versionRow % 2 = 0)?string("odd", "even")}">
            <td valign="top">
               <a title="${message("office.action.download", record.versionLabel)}" href="${url.context}${record.downloadUrl}" target="_blank"><img src="${url.context}/images/office/document.gif" alt="${message("office.action.download", record.versionLabel)}" /></a>
            </td>
            <td>
               <a title="${message("office.action.download", record.versionLabel)}" href="${url.context}${record.downloadUrl}" target="_blank"><span style="font-weight:bold;">${record.versionLabel}</span></a><br />
               ${message("office.property.author")}: ${record.creator}<br />
               ${message("office.property.date")}: ${record.createdDate?datetime}<br />
            <#if record.description??>
               ${message("office.version.notes")}: ${record.description?html}<br />
            </#if>
            <#-- Only Word supports document compare -->
            <#if extn == "doc" || extn == "docx" || extn == "odt" || extn == "sxw" >
               <a class="bold" href="#" onclick="ExternalComponent.compareDocument('${record.url?js_string}')" title="${message("office.action.compare_current")}">${message("office.action.compare_current")}</a><br />
            </#if>
            </td>
         </tr>
         </#list>
      <#else>
         <tr>
            <td valign="top">
               ${message("office.message.unversioned")}<br />
               <br />
               <ul>
                  <li><a title="${message("office.action.make_versionable")}" href="#" onclick="OfficeAddin.getAction('${doc_actions}','makeversion','${d.id}',null,null,'version=1');">
                     <img src="${url.context}/images/office/make_versionable.gif" alt="${message("office.action.make_versionable")}" /> ${message("office.action.make_versionable")}
                  </a></li>
               </ul>
            </td>
         </tr>
      </#if>
   <#else>
         <tr>
            <td valign="top">
               ${message("office.message.unmanaged")}
            </td>
         </tr>
   </#if>
      </table>
   </div>
</div>

<div class="headerRow">
   <div class="headerWrapper"><div class="header">${message("office.header.document_actions")}</div></div>
</div>

<div id="documentActions" class="actionsPanel">
   <ul>
<#if d.isDocument>
   <#if d.isLocked >
   <#elseif d.hasAspect("cm:workingcopy")>
      <li>
         <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','checkin','${d.id}');">
            <img src="${url.context}/images/office/checkin.gif" alt="${message("office.action.checkin")}">
            ${message("office.action.checkin")}
         </a>
         <br />${message("office.action.checkin.description")}
      </li>
   <#else>
      <li>
         <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','checkout','${d.id}');">
            <img src="${url.context}/images/office/checkout.gif" alt="${message("office.action.checkout")}" />
            ${message("office.action.checkout")}
         </a>
         <br />${message("office.action.checkout.description")}
      </li>
   </#if>
      <li>
         <a href="${url.serviceContext}/office/myTasks${defaultQuery?html}&amp;w=new">
            <img src="${url.context}/images/office/new_workflow.gif" alt="${message("office.action.start_workflow")}" />
            ${message("office.action.start_workflow")}
         </a>
         <br />${message("office.action.start_workflow.description")}
      </li>
   <#if d.name?ends_with(extn) || d.name?ends_with(extnx)>
      <li>
         <a href="#" onclick="OfficeAddin.getAction('${doc_actions}','makepdf','${d.id}');">
            <img src="${url.context}/images/office/makepdf.gif" alt="${message("office.action.transform_pdf")}" />
            ${message("office.action.transform_pdf")}
         </a>
         <br />${message("office.action.transform_pdf.description")}
      </li>
    </#if>
      <li>
         <a href="${url.context}/navigate/showDocDetails/workspace/SpacesStore/${d.id}" rel="_blank">
            <img src="${url.context}/images/office/document_details.gif" alt="${message("office.action.open_details")}" />
            ${message("office.action.open_details")}
         </a>
         <br />${message("office.action.open_details.description")}
      </li>
<#else>
      <li>
         <a title="${message("office.action.save_to_alfresco")}" href="${url.serviceContext}/office/navigation${defaultQuery?html}">
            <img src="${url.context}/images/office/save_to_alfresco.gif" alt="${message("office.action.save_to_alfresco")}" />
            ${message("office.action.save_to_alfresco")}
         </a>
         <br />${message("office.action.save_to_alfresco.description")}
</#if>
   </ul>
</div>

<#if args.version??>
<script>
   window.addEvent("domready", function(){$('tabLinkVersion').fireEvent('click');});
</script>
</#if>

<div style="position: absolute; top: 0px; left: 0px; z-index: 100; display: none">
   <iframe id="if_externalComponenetMethodCall" name="if_externalComponenetMethodCall" src="" style="visibility: hidden;" width="0" height="0"></iframe>
</div>

</body>
</html>
