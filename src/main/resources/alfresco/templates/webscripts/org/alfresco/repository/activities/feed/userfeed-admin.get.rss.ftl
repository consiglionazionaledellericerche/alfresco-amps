<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0">
   <channel>
      <title>Alfresco Activities User Feed for ${feedUserId}</title>
      <copyright>Copyright (c) 2008-2009 Alfresco Software, Inc. All rights reserved.</copyright>
      <link></link>
      <description>Alfresco Activities User Feed for ${feedUserId}</description>
      <language>en-us</language>
      <lastBuildDate></lastBuildDate>
      <pubDate></pubDate>
      <ttl>120</ttl>
      <generator>Alfresco (0.1)</generator>
      <#if feedEntries??>
      <#list feedEntries as feedEntry>
      ${feedEntry.activitySummary}
      </#list>
      <#else>
      </#if>
   </channel>
</rss>