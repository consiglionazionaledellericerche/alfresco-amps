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
package it.cnr.alfresco.repository.workflow;

import java.util.Map;

import it.cnr.alfresco.repository.workflow.WorkflowModelBuilder;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import it.cnr.alfresco.repo.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Smith
 *
 */
public abstract class AbstractWorkflowWebscript extends DeclarativeWebScript
{

    private NamespaceService namespaceService;
    
    /*EDGARDO:SOSTITUISCO IL SEGUENTE*/
    //private NodeService nodeService;
    protected NodeService nodeService;
    
    protected ServiceRegistry serviceRegistry;
    private PersonService personService;
    protected WorkflowService workflowService;
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        WorkflowModelBuilder modelBuilder = new WorkflowModelBuilder(namespaceService, nodeService, personService);
        return buildModel(modelBuilder, req, status, cache);
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    	this.serviceRegistry = serviceRegistry;
    }

    
    /**
     * This method uses a {@link WorkflowModelBuilder} to build up the model to return.
     * @param modelBuilder A {@link WorkflowModelBuilder}.
     * @param req the {@link WebScriptRequest}
     * @param status the {@link Status}
     * @param cache the {@link Cache}
     * @return the data model.
     */
    protected abstract Map<String, Object> buildModel(
            WorkflowModelBuilder modelBuilder,
            WebScriptRequest req,
            Status status, Cache cache);

}
