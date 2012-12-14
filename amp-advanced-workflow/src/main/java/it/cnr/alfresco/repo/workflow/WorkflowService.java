package it.cnr.alfresco.repo.workflow;

import java.util.List;

import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.jbpm.graph.def.Node;


@PublicService
public interface WorkflowService extends org.alfresco.service.cmr.workflow.WorkflowService
{

    /*EDGARDO*/
    @Auditable(parameters = {"workflowDefinitionId","transitionId"})
    public List<String> getTaskDefinitionsFromTo(final String workflowDefinitionId,final String taskNodeName);
    /*EDGARDO*/
    @Auditable(parameters = {"workflowDefinitionId","transitionId"})
    public List<String> getTaskDefinitionsToFrom(final String workflowDefinitionId,final String taskNodeName);
  
    /*EDGARDO:*/
    public List<Node> getAllTaskDefinitions(final String workflowDefinitionId);
    
    /*EDGARDO:*/
    public List<String> getSwimlaneDefinitions(final String workflowDefinitionId);
}
