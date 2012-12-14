<tree id="transitions" mold="paging" width="99%">
        <treecols sizable="true">
    		<treecol label="Transition ID" width="25%" />        
    		<treecol label="Title" width="25%" />
    		<treecol label="Description" width="25%" />
            <treecol width="25%" />
            <treecol width="25%" />
			<treecol width="25%" />            
        </treecols>
        <treechildren id="childrenList"/>
		<zscript>
		<![CDATA[
			<#list transitions as transition>	
	            Treeitem ti${transition_index} = new Treeitem();
	            Treerow tr${transition_index}=new Treerow();
	            
	            Treecell tcId${transition_index}=new Treecell("${transition[0]}");
	            Treecell tcTitle${transition_index}=new Treecell("${transition[1]}");
	            Treecell tcDescription${transition_index}=new Treecell("${transition[2]}");
	            
	            Treecell tcAction1${transition_index}=new Treecell();
	            Treecell tcAction2${transition_index}=new Treecell();
	            Treecell tcAction3${transition_index}=new Treecell();
	            Button btn${transition_index+1}=new Button("Prosegui");
	            Button btn${transition_index+2}=new Button("Sospendi");
	            Button btn${transition_index+3}=new Button("Annulla");
	            
				tcId${transition_index}.setParent(tr${transition_index});
				tcTitle${transition_index}.setParent(tr${transition_index});
				tcDescription${transition_index}.setParent(tr${transition_index});
				tcAction1${transition_index}.setParent(tr${transition_index});
				tcAction2${transition_index}.setParent(tr${transition_index});
				tcAction3${transition_index}.setParent(tr${transition_index});
				btn${transition_index+1}.setParent(tcAction1${transition_index});
				btn${transition_index+2}.setParent(tcAction2${transition_index});
				btn${transition_index+3}.setParent(tcAction3${transition_index});
				
				tr${transition_index}.setParent(ti${transition_index});
				ti${transition_index}.setParent(childrenList);
	         </#list>  
	       ]]> 
	       </zscript>
</tree>