<tree id="pathinstances" mold="paging" width="99%">
        <treecols sizable="true">
            <treecol label="Instance ID" width="25%" />
			<treecol label="Status" width="25%" />            
			<treecol label="WorkflowId" width="25%" />
        </treecols>
        <treechildren id="childrenList"/>
		<zscript>
		<![CDATA[
			<#list paths as i>	
	            Treeitem ti${i_index} = new Treeitem();
	            Treerow tr${i_index}=new Treerow();
	            Treecell tcId${i_index}=new Treecell("${i[0]}");
	            Treecell tcStatus${i_index}=new Treecell("${i[1]}");
	            Treecell tcWfId${i_index}=new Treecell("${i[2]}");
	            
				tcId${i_index}.setParent(tr${i_index});
			    tcId${i_index}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
					public void onEvent(Event event) throws Exception{
						doEvent("pathInstances","${i[0]}");
					}
		   	    });	
				tcStatus${i_index}.setParent(tr${i_index});
				tcWfId${i_index}.setParent(tr${i_index});
				
				tr${i_index}.setParent(ti${i_index});
				ti${i_index}.setParent(childrenList);
	         </#list>  
	       ]]> 
	       </zscript>
</tree>