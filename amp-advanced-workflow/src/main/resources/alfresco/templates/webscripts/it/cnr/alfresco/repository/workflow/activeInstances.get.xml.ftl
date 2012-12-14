<tree mold="paging" width="99%">
        <treecols sizable="true">
            <treecol label="ActiveInstances ID" width="15%" />
            <treecol label="Instance Description" width="70%" />
            <treecol label="" width="15%" />
        </treecols>
        <treechildren id="childrenList"/>
		<zscript>
		<![CDATA[
			<#if (activeInstances?size-1)<0 >
	            Treeitem ti = new Treeitem();
	            Treerow tr=new Treerow();
				tr.setParent(ti);
				ti.setParent(childrenList); 
			<#else>
				<#list activeInstances as i>
		            Treeitem ti${i_index} = new Treeitem();
		            Treerow tr${i_index}=new Treerow();
		            Treecell tcId${i_index}=new Treecell("${i[0]}");
					Treecell tcDesc${i_index}=new Treecell("${i[1]!"Start-Task Not Yet Initialized!"}");
					tcId${i_index}.setParent(tr${i_index});
				    tcId${i_index}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
							public void onEvent(Event event) throws Exception{
								doEvent("activeInstances","${i[0]}");
							}
			   	    });	
				    Treecell tcEndInst${i_index}=new Treecell();
				    Button btnEnd${i_index}=new Button("End Instance");
				    btnEnd${i_index}.setParent(tcEndInst${i_index});
				    btnEnd${i_index}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
						public void onEvent(Event event) throws Exception{
							org.apache.commons.httpclient.HttpClient httpclient1= new org.apache.commons.httpclient.HttpClient(); 
					        httpclient1.setConnectionTimeout(8000);
					        org.apache.commons.httpclient.methods.PostMethod post = new org.apache.commons.httpclient.methods.PostMethod("${url.server}${url.context}/service/process/end-instance.json");
					        post.setParameter("p","${i[0]}");
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
					tcDesc${i_index}.setParent(tr${i_index});
					tcEndInst${i_index}.setParent(tr${i_index});
					tr${i_index}.setParent(ti${i_index});
					ti${i_index}.setParent(childrenList);
		         </#list>  
		     </#if>    
	       ]]> 
	       </zscript>
</tree>