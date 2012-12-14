/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.cmis.rest;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.alfresco.cmis.CMISResultSet;
import org.alfresco.repo.cmis.ws.CmisQueryType;
import org.alfresco.repo.web.util.paging.Page;
import org.alfresco.repo.web.util.paging.PagedResults;
import org.alfresco.repo.web.util.paging.Paging;
import org.alfresco.service.cmr.search.SearchParameters;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.connector.HttpMethod;


/**
 * Base Web Script Implementation that ensures query result sets are closed
 */
public class CMISQueryWebScript extends DeclarativeWebScript
{
	private String method;
	private Paging paging;
	private CMISScript cmisScript;
	
    public static final JAXBContext CONTEXT;
    static {
        JAXBContext jc = null;
        try {
            jc = JAXBContext.newInstance(org.alfresco.repo.cmis.ws.ObjectFactory.class, ObjectFactory.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        CONTEXT = jc;
    }
	
   
	public void setPaging(Paging paging) {
		this.paging = paging;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setCmisScript(CMISScript cmisScript) {
		this.cmisScript = cmisScript;
	}

	private String getMethod(){
		return method;
	}

	protected boolean isGET(){
		return getMethod().equals(HttpMethod.GET.name());
	}

	protected boolean isPOST(){
		return getMethod().equals(HttpMethod.POST.name());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,
			Status status, Cache cache) {
		Map<String, Object> model = new HashMap<String, Object>();
		String statement;
		String searchAllVersions;
		String includeAllowableActions;
		String includeRelationships;
		String renditionFilter;
		String skipCount;
		String maxItems; 
		List<Object> facets = null;
		if (isPOST()){
			InputStream is = null;
			try{
				is = req.getContent().getInputStream();
				try {
					JAXBElement<CmisQueryType> element = (JAXBElement<CmisQueryType>) CONTEXT.createUnmarshaller().unmarshal(is);
					CmisQueryType cmisQueryType = element.getValue();
					if (cmisQueryType.getAny() != null){
						facets = new ArrayList<Object>();
						for (Object object : cmisQueryType.getAny()) {
							FieldFacet fieldFacet = ((JAXBElement<FieldFacet>)object).getValue();
							SearchParameters.FieldFacet facet = new SearchParameters.FieldFacet(fieldFacet.getField()); 
							facet.setCountDocsMissingFacetField(fieldFacet.isCountDocsMissingFacetField());
							facet.setEnumMethodCacheMinDF(fieldFacet.getEnumMethodCacheMinDF());
							facet.setLimit(fieldFacet.getLimit());
							if (fieldFacet.getMethod() != null)
								facet.setMethod(SearchParameters.FieldFacetMethod.valueOf(fieldFacet.getMethod().name()));
							facet.setMinCount(fieldFacet.getMinCount());
							facet.setOffset(fieldFacet.getOffset());
							facet.setPrefix(fieldFacet.getPrefix());
							if (fieldFacet.getSort() != null)
								facet.setSort(SearchParameters.FieldFacetSort.valueOf(fieldFacet.getSort().name()));
							facets.add(facet);
						}
					}
					statement = cmisQueryType.getStatement();
					searchAllVersions = String.valueOf(cmisQueryType.isSearchAllVersions());
					includeAllowableActions = String.valueOf(cmisQueryType.isIncludeAllowableActions());
					includeRelationships = cmisQueryType.getIncludeRelationships().value();
					renditionFilter = cmisQueryType.getRenditionFilter();
					skipCount = String.valueOf(cmisQueryType.getSkipCount());
					maxItems = String.valueOf(cmisQueryType.getMaxItems());	    
				} catch (JAXBException e) {
					throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Query statement must be provided");
				}
			}finally{
				if (is != null)
					try {
						is.close();
					} catch (IOException e) {
					}
			}
		}else {
			statement = req.getParameter("q");
			searchAllVersions = req.getParameter("searchAllVersions");
			includeAllowableActions = req.getParameter(CMISScript.ARG_INCLUDE_ALLOWABLE_ACTIONS);
			includeRelationships = req.getParameter(CMISScript.ARG_INCLUDE_RELATIONSHIPS);
			renditionFilter = req.getParameter(CMISScript.ARG_RENDITION_FILTER);
			skipCount = req.getParameter(CMISScript.ARG_SKIP_COUNT);
			maxItems = req.getParameter(CMISScript.ARG_MAX_ITEMS);
		}
	    if (statement == null || statement.length() == 0)
	    {
	    	throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Query statement must be provided");
	    }
//	    if (searchAllVersions != null && searchAllVersions.equalsIgnoreCase("true"))
//	    {
//	    	throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Search all versions not supported");
//	    }	    
		model.put("statement", statement);
		model.put("searchAllVersions", searchAllVersions);		
		model.put("includeAllowableActions", includeAllowableActions == null || includeAllowableActions == "true" ? true : false);
		model.put("includeRelationships", includeRelationships == null || includeRelationships.length() == 0? "none" : includeRelationships);
		model.put("renditionFilter", renditionFilter == null || renditionFilter.length() == 0? "cmis:none" : renditionFilter);
		
		Page page = paging.createPageOrWindow(null, null, skipCount==null?null:Integer.valueOf(skipCount), maxItems==null?null:Integer.parseInt(maxItems));
		PagedResults pagedResults = cmisScript.query(statement, page, facets);
	    model.put("resultset", pagedResults.getResult());
	    model.put("cursor", pagedResults.getCursor());

	    String queryArgs = CMISScript.ARG_QUERY_STATEMENT + "=" + statement;
	    if (Boolean.valueOf(includeAllowableActions)) queryArgs += "&" + CMISScript.ARG_INCLUDE_ALLOWABLE_ACTIONS + "=" + includeAllowableActions;
	    if (includeRelationships != "none") queryArgs += "&" + CMISScript.ARG_INCLUDE_RELATIONSHIPS + "=" + includeRelationships;
	    if (renditionFilter != "cmis:none") queryArgs += "&" + CMISScript.ARG_RENDITION_FILTER + "=" + renditionFilter;
	    queryArgs += "&" + CMISScript.ARG_SKIP_COUNT + "=" + page.getNumber();
	    queryArgs += "&" + CMISScript.ARG_MAX_ITEMS + "=" + page.getSize();
	    model.put("queryArgs", queryArgs);
	    String queryUri = "/cmis/query";
	    model.put("queryUri", queryUri);
		
	    status.setCode(201);
	    status.setLocation(req.getURL() + queryUri + "?" + queryArgs);
	    
		return model;
	}
	
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeFinallyImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache, java.util.Map)
     */
    @Override
    protected void executeFinallyImpl(WebScriptRequest req, Status status, Cache cache, Map<String, Object> model)
    {
        Object object = model.get("resultset");
        if (object != null && object instanceof CMISResultSet)
        {
            CMISResultSet resultSet = (CMISResultSet)object;
            resultSet.close();
        }
    }
    
    @Override
    protected Map<String, Object> createScriptParameters(WebScriptRequest req,
    		WebScriptResponse res, ScriptDetails script,
    		Map<String, Object> customParams) {
        Map<String, Object> params = new HashMap<String, Object>(32, 1.0f);
        return params;
    }
}
