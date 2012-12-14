package it.cnr.alfresco.repository.workflow;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import it.cnr.alfresco.repo.workflow.WorkflowService;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowNode;

import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;


import org.jbpm.graph.def.Node;

public class DefTaskList extends DeclarativeWebScript
{
    // service instances
    private WorkflowService workflowService;
    private String DOT;
    /**
     * Set the workflow service property
     * 
     * @param workflowService
     *            the workflow service to set
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
	}
	
	public String getDOT(){
		return this.DOT;
	}
	
	public void setDOT(String DOT){
		this.DOT=DOT;
	}
	
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,
            Status status) 
    {
        
	
   		java.util.List<Node> listNodeTask=this.workflowService.getAllTaskDefinitions(req.getParameter("p"));
   		System.out.println("Lista dei Node Task in esame:");
   		for (Node node:listNodeTask) { 
   			System.out.println(node.getFullyQualifiedName()+" "+node.getName()+" "+node.getNameExt()+" "+node.getNodeType().name());
   		}
   		System.out.println("************************************");
   		
   		//EDGARDO: Fino alla release 3.4.x il seguente serviva, dalla 4.x deve essere commentato
   		//new WorkflowNode();
   		
   		Map<String, Object> model = new HashMap<String, Object>();
        java.util.List<String> transitions=new ArrayList<String>();
        java.util.Map<String,String> taskInformation=new HashMap<String,String>();
        for (Node node:listNodeTask){
        		System.out.println("EDGARDO-1: Task name: "+node.getName());
        		System.out.println("Transizioni Destinazione-Sorgente");
            	java.util.List listLeavingTransitions=this.workflowService.getTaskDefinitionsToFrom(req.getParameter("p"),node.getName());
           		//Dovrebbero bastare le leaving transitions
                for(int j=0;j<listLeavingTransitions.size();j++){
                	System.out.println(listLeavingTransitions.get(j));
                	if(!transitions.contains((String)listLeavingTransitions.get(j)))  transitions.add((String)listLeavingTransitions.get(j));
                }
                System.out.println("Transizioni Sorgente-Destinazioni");
                java.util.List listArrivingTransitions=this.workflowService.getTaskDefinitionsFromTo(req.getParameter("p"),node.getName());
                for(int j=0;j<listArrivingTransitions.size();j++){
                	System.out.println(listArrivingTransitions.get(j));
                	if(!transitions.contains((String)listArrivingTransitions.get(j))) transitions.add((String)listArrivingTransitions.get(j));
                } 		
        }	
        for (Node node:listNodeTask){
        	taskInformation.put(node.getName(), node.getNodeType().name());
        }	

        
        model.put("ntask",listNodeTask.size());
        model.put("TASKINFORMATION", taskInformation);


        
       //Codice per calcolare le coordinate dei nodi con GraphViz 
        GraphViz gv = new GraphViz(this.DOT);
        gv.addln(gv.start_graph());
        gv.addln("start [shape=circle,style=filled,color=yellow];");
        
        for (int i=0;i< transitions.toArray().length;i++){
        	String str = (String)transitions.toArray()[i];
        	String[] temp;
        	String delimiter = ",";
        	temp = str.split(delimiter);
        	String label=(temp[1].equals("") || temp[1].equals(" "))?"blank":temp[1];
        	String str1=temp[0]+" -> "+temp[2]+" [label="+label+"]"+";";
        	System.out.println(str1);
        	gv.addln(str1);
        	gv.addln(getShapeTaskType(temp[0], taskInformation));
        	gv.addln(getShapeTaskType(temp[2], taskInformation));
        }
        gv.addln("end [shape=doublecircle,style=filled,color=orange];");
        gv.addln(gv.end_graph());
       
        System.out.println(gv.getDotSource());
       
        java.util.List<String> nodeCoordinate=new ArrayList<String>();
        String s=new String(gv.getGraphPlain(gv.getDotSource()));
        java.io.StringReader sr=new java.io.StringReader(s);
        java.io.BufferedReader br=new java.io.BufferedReader(sr);
        
        String sbr=new String();
    	System.out.println("*********************GRAPHVIZ PROVIDES COORDINATE AND SVG**********************");
        try {
			while ((sbr=br.readLine())!=null){
			       	String[] temp;
			    	String delimiter = " ";
			    	System.out.println(sbr);
			    	temp = sbr.split(delimiter);
			    	if (temp[0].equals("node")){
					//			    		Controllare sempre la stringa sbr, potrebbe esserci qualche variazione nel formato. 
					//			    		 La stringa si presenta sotto la forma di record: "node nodename x y ..."
					//			    		 Ad esempio dalla versione 2.8 alla 2.26, hanno introdotto un ulteriore spazio tra 
					//			    		 il nome del nodo e la prima coordinata.
					//			    		 La stringa è diventata: "node nodename  y y ..."
			    		
			    		String coordinate= temp[1]+","+temp[3]+","+temp[4]; //nomeNodo,x,y
			    		nodeCoordinate.add(coordinate);
			    		System.out.println(coordinate);
			    	}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        String s1=new String(gv.getGraphSvg(gv.getDotSource()));

        System.out.println("*********************GRAPHVIZ**********************");

    	
 /*   	
        //Codice per calcolare le coordinate dei nodi con JUNG
        JungViz jungViz=new JungViz();
        for (int i=0;i< transitions.toArray().length;i++){
        	String str = (String)transitions.toArray()[i];
        	String[] temp;
        	String delimiter = ",";
        	temp = str.split(delimiter);
        	String label=temp[1];
        	String str1=temp[0]+" -> "+temp[2]+" [label="+label+"]"+";";
        	jungViz.addVertex(temp[0]);
        	jungViz.addVertex(temp[2]);
        	jungViz.addEdge("label"+i, temp[0], temp[2]);
        }
        System.out.println("**************JUNG*****************");
        String[] str=jungViz.getGraph();
        java.util.List<String> nodeCoordinate=new ArrayList<String>();
        for (int i=0;i<str.length;i++){
        	System.out.println(str[i]);
        	String coordinate= str[i]; //nomeNodo,x,y
        	nodeCoordinate.add(coordinate);
        }
        System.out.println("**************JUNG*****************");        
 */       

		
		
        model.put("NODECOORDINATE", nodeCoordinate.toArray());
        model.put("SVGGRAPH", s1);
        return model;
    	
    	
    	
}    

	
	private String getShapeTaskType(String taskName, Map taskInformation){
    	if (taskInformation.get(taskName).equals("Task")){ 
    		return taskName+" "+"[shape=rect,style=filled,color=green];";
    	}else if (taskInformation.get(taskName).equals("Join")){
    		return taskName+" "+"[shape=invtriangle,style=filled,color=magenta];";
    	}else if (taskInformation.get(taskName).equals("Fork")){
    		 return taskName+" "+"[shape=triangle,style=filled,color=magenta];";
    	}else if (taskInformation.get(taskName).equals("Decision")){
    		 return taskName+" "+"[shape=pentagon,style=filled,color=red];";
    	}else{
    		return "";
    	}
	}
	
}