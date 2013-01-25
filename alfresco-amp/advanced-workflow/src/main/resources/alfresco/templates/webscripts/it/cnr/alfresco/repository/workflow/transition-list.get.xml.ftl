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
					btn${transition_index+1}.setParent(tcAction1${transition_index});	
	            btn${transition_index+1}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
				public void onEvent(Event event) throws Exception{
					org.apache.commons.httpclient.HttpClient httpclient1= new org.apache.commons.httpclient.HttpClient(); 
   		      httpclient1.setConnectionTimeout(8000);
			      org.apache.commons.httpclient.methods.PostMethod post = new org.apache.commons.httpclient.methods.PostMethod("${url.server}${url.context}/service/api/workflow/task/end/${taskId}/${transition[0]}");
					post.setParameter("alf_ticket",ticket);
   			   post.setRequestHeader("Accept-Language","en-us,en;q=0.5");
	   		   post.setRequestHeader("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
					int statusCode = httpclient1.executeMethod(post);
					if (statusCode==200) alert("Successfull action");
					if (statusCode==500) alert("Unsuccessfull action! Please check logs");
					if (statusCode==401) alert("Unauthorized action! Please check logs");
					post.releaseConnection();
				}
		        });	
	            

	            Button btn${transition_index+2}=new Button("Sospendi");
					btn${transition_index+2}.setParent(tcAction1${transition_index});	
	            btn${transition_index+2}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
				public void onEvent(Event event) throws Exception{
					/*
					org.apache.commons.httpclient.HttpClient httpclient1= new org.apache.commons.httpclient.HttpClient(); 
   		      httpclient1.setConnectionTimeout(8000);
			      org.apache.commons.httpclient.methods.PostMethod post = new org.apache.commons.httpclient.methods.PostMethod("${url.server}${url.context}/service/api/workflow/task/end/${taskId}/${transition[0]}");
					post.setParameter("alf_ticket",ticket);
   			   post.setRequestHeader("Accept-Language","en-us,en;q=0.5");
	   		   post.setRequestHeader("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
					int statusCode = httpclient1.executeMethod(post);
					if (statusCode==200) alert("Successfull action");
					if (statusCode==500) alert("Unsuccessfull action! Please check logs");
					if (statusCode==401) alert("Unauthorized action! Please check logs");
					post.releaseConnection();
					*/
					alert("Unsupported Action!");
					}
					
		        });	



	            Button btn${transition_index+3}=new Button("Annulla");
					btn${transition_index+3}.setParent(tcAction1${transition_index});	
	            btn${transition_index+3}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
				public void onEvent(Event event) throws Exception{
					/*
					org.apache.commons.httpclient.HttpClient httpclient1= new org.apache.commons.httpclient.HttpClient(); 
   		      httpclient1.setConnectionTimeout(8000);
			      org.apache.commons.httpclient.methods.PostMethod post = new org.apache.commons.httpclient.methods.PostMethod("${url.server}${url.context}/service/api/workflow/task/end/${taskId}/${transition[0]}");
					post.setParameter("alf_ticket",ticket);
   			   post.setRequestHeader("Accept-Language","en-us,en;q=0.5");
	   		   post.setRequestHeader("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
					int statusCode = httpclient1.executeMethod(post);
					if (statusCode==200) alert("Successfull action");
					if (statusCode==500) alert("Unsuccessfull action! Please check logs");
					if (statusCode==401) alert("Unauthorized action! Please check logs");
					post.releaseConnection();
					*/
					alert("Unsupported Action!");
				}
		        });	


	            
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