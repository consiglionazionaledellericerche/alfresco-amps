<tree id="tasks" mold="paging" width="99%">
        <treecols sizable="true">
	    	<treecol id="t1" label="Task ID" width="15%" />        
            <treecol id="t2" label="Name" width="15%" />
            <treecol id="t3" label="Title" width="20%" />
            <treecol id="t4" label="Description" width="20%" />
            <treecol id="t5" label="JBPM Status" width="10%" />
   	    	<treecol id="t6" label="End Task" width="10%" />            
   	    	<treecol id="t7" label="Assegnee" width="10%" />
        </treecols>
        <treechildren id="childrenList"/>
		<zscript>
		<![CDATA[
        
		SimpleCategoryModel model = new SimpleCategoryModel();
		HashMap hm= new HashMap();
     	<#list taskInstances as task>
	       Treeitem ti${task_index} = new Treeitem();
	       Treerow tr${task_index}=new Treerow();
	            
           Treecell tcId${task_index}=new Treecell("${task.id}");
 	       tcId${task_index}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
				public void onEvent(Event event) throws Exception{
					doEvent("status-list","${task.id}");
				}
	   	   });	
           
           Treecell tcName${task_index}=new Treecell("${task.name}");
           Treecell tcTitle${task_index}=new Treecell("${task.title}");
           Treecell tcDescription${task_index}=new Treecell("${task.description}");
           Treecell tcState${task_index}=new Treecell("${task.properties.bpm_status}"); 
	   <#if "${task.properties.bpm_status}" == "Not Yet Started">tcState${task_index}.setStyle("background-color:orange");</#if>
	   <#if "${task.properties.bpm_status}" == "In Progress">tcState${task_index}.setStyle("background-color:green");</#if>
	   <#if "${task.properties.bpm_status}" == "On Hold">tcState${task_index}.setStyle("background-color:blue");</#if>				
	   <#if "${task.properties.bpm_status}" == "Cancelled">tcState${task_index}.setStyle("background-color:red");</#if>		
	   <#if "${task.properties.bpm_status}" == "Completed">tcState${task_index}.setStyle("background-color:magenta");</#if>		
           Treecell tcEndTask${task_index}=new Treecell();
           Button btnEnd=new Button("EndTask");
           btnEnd.setParent(tcEndTask${task_index});	
           Treecell tcExtra${task_index}=new Treecell("${task.owner.userName}");

		    hm.put("${task.id}", tcExtra${task_index}); 	

		    tcId${task_index}.setParent(tr${task_index});
		    tcName${task_index}.setParent(tr${task_index});
		    tcTitle${task_index}.setParent(tr${task_index});
		    tcDescription${task_index}.setParent(tr${task_index});
		    tcState${task_index}.setParent(tr${task_index});
		    tcEndTask${task_index}.setParent(tr${task_index});
		    tcExtra${task_index}.setParent(tr${task_index});
				
		    tr${task_index}.setParent(ti${task_index});
		    ti${task_index}.setParent(childrenList);
		    
		    model.setValue("jbpm","${task.id}",new Integer(${task.properties.bpm_percentComplete?number}));

	    </#list>  
		((javax.servlet.http.HttpSession) Sessions.getCurrent().getNativeSession()).getServletContext().setAttribute("bundle",hm);	       

		Flashchart chart=new Flashchart();
		chart.setModel(model);
		chart.setType("bar");
		((javax.servlet.http.HttpSession) Sessions.getCurrent().getNativeSession()).getServletContext().setAttribute("performance",chart);	    		

	       ]]> 
	       </zscript>
</tree>