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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.db.GraphSession;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.def.Node.NodeType;
import org.jbpm.taskmgmt.def.Swimlane;
import org.springmodules.workflow.jbpm31.JbpmCallback;



/**
 * JBoss JBPM based implementation of:
 * 
 * Workflow Definition Component
 * Workflow Component
 * Task Component
 * 
 * @author davidc
 */
public class JBPMEngine extends org.alfresco.repo.workflow.jbpm.JBPMEngine implements it.cnr.alfresco.repo.workflow.WorkflowComponent

{
	private TenantService tenantServiceCNR;
	
	public void setTenantServiceCNR(TenantService tenantServiceCNR) {
		this.tenantServiceCNR = tenantServiceCNR;
	}

	/*EDGARDO: Aggiungo il seguente metodo*/
    @SuppressWarnings("unchecked")
    public List<String> getTaskDefinitionsFromTo(final String workflowDefinitionId,final String taskNodeName)
    {
    	//System.out.println("EDGARDOFromTo: Cerco i nodi incidenti sulla transizione: "+transitionId);
        try
        {
            return (List<String>)jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    // retrieve process
                    GraphSession graphSession = context.getGraphSession();
                    ProcessDefinition processDefinition = getProcessDefinition(graphSession, workflowDefinitionId);
                    
                    if (processDefinition == null)
                    {
                        return null;
                    }
                    else
                    {
                        String processName = processDefinition.getName();
                        if (tenantServiceCNR.isEnabled())
                        {
                            tenantServiceCNR.checkDomain(processName); // throws exception if domain mismatch
                        }
                                                
                        processDefinition.getTaskMgmtDefinition();
                        new ArrayList<WorkflowTaskDefinition>();

                        List<Node> nodes=processDefinition.getNodes();
                        List<String> arrivingTransitions= new ArrayList<String>();
                        
                        for (int i=0;i<nodes.size();i++){
                            /*CREARE UN METODO APPOSITO PER OTTENERE LO STATO DI UN TASK*/
                                                        	
                        	Iterator<Transition> iterTransition = (processDefinition.getNodes()).get(i).getArrivingTransitions().iterator();
                        	if ((nodes.get(i)).getName() == taskNodeName){ 
                        		System.out.println((nodes.get(i)).getName()+", ha in arrivo: "+(nodes.get(i)).getArrivingTransitions().size());
	                        	while(iterTransition.hasNext()){
	                        		Transition transitionArriving = iterTransition.next();
	                        		String arrivingTransition= transitionArriving.getName()==""? "blank":transitionArriving.getName();
	                        		//arrivingTransitions.add(arrivingTransition);
	                        		arrivingTransitions.add(transitionArriving.getFrom().getName()+","+arrivingTransition+","+transitionArriving.getTo().getName());
	                        	}
                        	}
                        }	
                        /*for(int i=0;i<arrivingTransitions.size();i++){
                        	System.out.println(arrivingTransitions.get(i));
                        }*/
                        
                        return arrivingTransitions;
                    }
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to retrieve workflow task definitions for workflow definition '" + workflowDefinitionId + "'", e);
        }
    }    
    
    /*EDGARDO: Aggiungo il seguente metodo*/
    @SuppressWarnings("unchecked")
    public List<String> getTaskDefinitionsToFrom(final String workflowDefinitionId,final String taskNodeName)
    {
    	//System.out.println("EDGARDOToFrom: Cerco le transizioni incidenti sul nodo: "+ taskNodeName);
        try
        {
            return (List<String>)jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    // retrieve process
                    GraphSession graphSession = context.getGraphSession();
                    ProcessDefinition processDefinition = getProcessDefinition(graphSession, workflowDefinitionId);
                    
                    if (processDefinition == null)
                    {
                        return null;
                    }
                    else
                    {
                        String processName = processDefinition.getName();
                        if (tenantServiceCNR.isEnabled())
                        {
                            tenantServiceCNR.checkDomain(processName); // throws exception if domain mismatch
                        }
                        
                        processDefinition.getTaskMgmtDefinition();
                        new ArrayList<WorkflowTaskDefinition>();
  
                        List<Node> nodes=processDefinition.getNodes();
                        List<String> leavingTransitions= new ArrayList<String>();
      
                        for (int i=0;i<nodes.size();i++){
                        	if ((nodes.get(i)).getName() == taskNodeName){ 
                        		System.out.println((nodes.get(i)).getName()+", ha in uscita: "+(nodes.get(i)).getLeavingTransitions().size());
                        		for (int j=0;j<(nodes.get(i)).getLeavingTransitions().size();j++){
                        			Transition transitionLeaving= (nodes.get(i)).getLeavingTransitions().get(j);
	                        		String leavingTransition= transitionLeaving.getName()==""? "blank":transitionLeaving.getName();
	                        		//leavingTransitions.add(leavingTransition);
	                        		leavingTransitions.add(transitionLeaving.getFrom().getName()+","+leavingTransition+","+transitionLeaving.getTo().getName());
	                        	}
                        	}
                        }
                        
                        /*
                        for(int i=0;i<leavingTransitions.size();i++){
                        	System.out.println(leavingTransitions.get(i));
                        }
                        */
                        return leavingTransitions;
                    }
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to retrieve workflow task definitions for workflow definition '" + workflowDefinitionId + "'", e);
        }
    }    
    
    @SuppressWarnings("unchecked")
    public List<Node> getAllTaskDefinitions(final String workflowDefinitionId)
    {
        try
        {
            return (List<Node>)jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    // retrieve process
                    GraphSession graphSession = context.getGraphSession();
                    ProcessDefinition processDefinition = getProcessDefinition(graphSession, workflowDefinitionId);
                    
                    if (processDefinition == null)
                    {
                        return null;
                    }
                    else
                    {
                        String processName = processDefinition.getName();
                        if (tenantServiceCNR.isEnabled())
                        {
                            tenantServiceCNR.checkDomain(processName); // throws exception if domain mismatch
                        }
                        List<Node> nodes=processDefinition.getNodes();
                        return (nodes.size() == 0) ? null : nodes;
                    }
                }
            });
        }
        catch(JbpmException e)
        {
        	throw new WorkflowException("Failed to retrieve workflow task definitions for workflow definition '" + workflowDefinitionId + "'", e);
        }
   }

    /*EDGARDO: Aggiungo il seguente metodo*/
    @SuppressWarnings("unchecked")
    public List<String> getSwimlaneDefinitions(final String workflowDefinitionId)
    {
        try
        {
            return (List<String>)jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    // retrieve process
                    GraphSession graphSession = context.getGraphSession();
                    ProcessDefinition processDefinition = getProcessDefinition(graphSession, workflowDefinitionId);
                    
                    if (processDefinition == null)
                    {
                        return null;
                    }
                    else
                    {
                        String processName = processDefinition.getName();
                        if (tenantServiceCNR.isEnabled())
                        {
                            tenantServiceCNR.checkDomain(processName); // throws exception if domain mismatch
                        }
                        
                        List<String> swimlanes = new ArrayList<String>();
                        for (Object o:processDefinition.getTaskMgmtDefinition().getSwimlanes().keySet()){
                        	swimlanes.add(((Swimlane)processDefinition.getTaskMgmtDefinition().getSwimlanes().get(o)).getPooledActorsExpression());
                        }
      
                        return swimlanes;
                    }
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to retrieve workflow task definitions for workflow definition '" + workflowDefinitionId + "'", e);
        }
    }    
    
    
}
