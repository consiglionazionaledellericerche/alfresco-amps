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
package it.cnr.alfresco.repo.workflow;

import java.util.List;
import org.alfresco.service.Auditable;
import org.jbpm.graph.def.Node;


/**
 * SPI to be implemented by a BPM Engine that provides Workflow instance management.
 * 
 * @author davidc
 */
public interface WorkflowComponent extends org.alfresco.repo.workflow.WorkflowComponent
{

    /*EDGARDO:Aggiungo il seguente metodo*/
    @Auditable(parameters = {"workflowDefinitionId", "taskNodeName"})
    public List<String> getTaskDefinitionsFromTo(final String workflowDefinitionId,final String taskNodeName);    

    /*EDGARDO:Aggiungo il seguente metodo*/
    @Auditable(parameters = {"workflowDefinitionId", "taskNodeName"})
    public List<String> getTaskDefinitionsToFrom(final String workflowDefinitionId,final String taskNodeName);    
    
    /*EDGARDO:Aggiungo il seguente metodo*/
    public List<Node> getAllTaskDefinitions(final String workflowDefinitionId);

    /*EDGARDO:Aggiungo il seguente metodo*/
    public List<String> getSwimlaneDefinitions(final String workflowDefinitionId);

}

