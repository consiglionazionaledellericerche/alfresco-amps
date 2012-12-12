[#ftl]
[#import "/org/alfresco/cmis/lib/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/lib/links.lib.atom.ftl" as linksLib/]
[#import "/org/alfresco/cmis/lib/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/lib/atomentry.lib.atom.ftl" as entryLib/]
[#import "/org/alfresco/paging.lib.atom.ftl" as pagingLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[#-- TODO: uuid --]
[@feedLib.generic "urn:uuid:resultset" "Result set for ${statement}" "${person.properties.userName}"]
  [@linksLib.linkservice/]
  [@linksLib.linkself/]
  [@pagingLib.links cursor/]
[/@feedLib.generic]
[@pagingLib.opensearch cursor/]
[@pagingLib.cmis cursor/]

[#assign rs = cmisresultset(resultset, cursor)]
[#list rs.rows as row]
[@entryLib.row row=row includeallowableactions=includeAllowableActions includerelationships=includeRelationships/]
[/#list]
<solr:facets>
[#list resultset.fieldFacets as facetName]
  <solr:facet name="${facetName}">       
        [#list resultset.getFieldFacet(facetName) as facet]
              [#if facet.first?? && facet.first?length &gt; 0]
              <solr:facetvalue key="${facet.first?xml}">${facet.second?string('0')}</solr:facetvalue>
              [/#if]
        [/#list]
  </solr:facet>      
[/#list]
</solr:facets>

</feed>

[/#compress]
