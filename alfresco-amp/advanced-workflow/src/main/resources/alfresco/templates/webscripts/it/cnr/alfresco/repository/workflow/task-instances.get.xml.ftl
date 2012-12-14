<tree id="tasks" mold="paging" width="99%" pageSize="15">
        <treecols sizable="true">
	    	<treecol id="t1" label="Task ID" width="10%" />        
            <treecol id="t2" label="Name" width="10%" />
            <treecol id="t3" label="Title" width="10%" />
            <treecol id="t4" label="Description" width="20%" />
            <treecol id="t5" label="Alfresco Status" width="10%" />
   	    	<treecol id="t6" label="End Task" width="10%" />            
   	    	<treecol id="t7" label="" width="10%" />
   	    	<treecol id="t8" label="" width="10%" />
   	    	<treecol id="t9" label="Started By" width="10%" />
        </treecols>
        <treechildren id="childrenList"/>
		<zscript>
		<![CDATA[
		HashMap hm= new HashMap();
     		<#list taskInstances as task>
	            Treeitem ti${task_index} = new Treeitem();
	            Treerow tr${task_index}=new Treerow();
				tr${task_index}.addEventListener(Events.ON_RIGHT_CLICK,new org.zkoss.zk.ui.event.EventListener(){
					public void onEvent(Event event) throws Exception{
					}
	   	    	}
		    	);	
		    tr${task_index}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
				public void onEvent(Event event) throws Exception{
					doEvent("transition-list","${task.id}");
				}
	   	    });	
	            
	            Treecell tcId${task_index}=new Treecell("${task.id}");
	            
	            Treecell tcName${task_index}=new Treecell("${task.name}");
	            Treecell tcTitle${task_index}=new Treecell("${task.title}");
	            Treecell tcDescription${task_index}=new Treecell("${task.description}");
	            Treecell tcState${task_index}=new Treecell("${task.state}");
	            Treecell tcEndTask${task_index}=new Treecell();
	            Button btnEnd=new Button("EndTask");
	            btnEnd.setParent(tcEndTask${task_index});
	            
	            
	            
		    	((javax.servlet.http.HttpSession) Sessions.getCurrent().getNativeSession()).getServletContext().setAttribute("EndButton-${task.id}",btnEnd);
	            Treecell tcParamTask${task_index}=new Treecell();
	            Button btnParams=new Button("Parameters");
	            btnParams.setParent(tcParamTask${task_index});	
	            btnParams.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
				public void onEvent(Event event) throws Exception{
						   Window window=new Window();
						   window.setBorder("normal");		
						   window.setTitle("Task Parameters");
						   window.setMode("highlighted");
						   window.setPosition("left,top");	
						   window.setMinimizable(true);	
						   window.setParent(page.getLastRoot());
						   window.setVisible(true);
						   window.setWidth("99%");	
	 					   window.setHeight("99%");	
						   window.setSizable(true);	 					   

						   Grid grid=new Grid();
//						grid.setAutopaging(true);   	
						grid.setMold("paging");
						grid.setPageSize(3);
						grid.setVflex(true);
						grid.setPagingPosition("both");
   						grid.setParent(window);
						Columns clms=new Columns();
						clms.setParent(grid);
						new Column("Property").setParent(clms);
						new Column("Content").setParent(clms);
						Rows rows=new Rows();						   	
						rows.setParent(grid);

						Row row1 = new Row();
						row1.setParent(rows);
						new Label("BPM Description: ").setParent(row1);	
						Textbox tb1=new Textbox("${task.properties.bpm_description!"Not Specified"}"); 
						tb1.setParent(row1);
						tb1.setRows(3);
						tb1.setWidth("100%");

						Row row2 = new Row();
						row2.setParent(rows);
   					        new Label("BPM Due Date: ").setParent(row2);
					        Textbox tb2=new Textbox("${task.properties.bpm_dueDate!"Not Specified"}"); 
	 					tb2.setParent(row2);
						tb2.setWidth("350px");


						Row row3 = new Row();
						row3.setParent(rows);
						   new Label("BPM Priority: ").setParent(row3);
					           Textbox tb3=new Textbox("${task.properties.bpm_priority!"Not Specified"}"); 
	 					   tb3.setParent(row3);
						   tb3.setWidth("50px");


						Row row4 = new Row();
						row4.setParent(rows);
						   new Label("BPM Start Date: ").setParent(row4);
					           Textbox tb4=new Textbox("${task.properties.cm_created!"Not Specified"}"); 
	 					   tb4.setParent(row4);
						   tb4.setWidth("350px");

						Row row5 = new Row();
						row5.setParent(rows);
						   new Label("BPM Comment: ").setParent(row5);
					           Textbox tb5=new Textbox("${task.properties.bpm_comment!"Not Specified"}"); 
	 					   tb5.setParent(row5);
					           tb5.setRows(3);
						   tb5.setWidth("350px");


						Row row6 = new Row();
						row6.setParent(rows);
						   new Label("BPM Completion Date: ").setParent(row6);
					           Textbox tb6=new Textbox("${task.properties.bpm_completionDate!"Not Specified"}"); 
	 					   tb6.setParent(row6);
						   tb6.setWidth("350px");


						Row row7 = new Row();
						row7.setParent(rows);
						   new Label("BPM Outcome: ").setParent(row7);
					           Textbox tb7=new Textbox("${task.properties.bpm_outcome!"Not Specified"}"); 
	 					   tb7.setParent(row7);
					           tb7.setRows(3);
						   tb7.setWidth("350px");


						Row row8 = new Row();
						row8.setParent(rows);
						   new Label("CM Owner: ").setParent(row8);
					           Textbox tb8=new Textbox("${task.owner.userName!"Not Specified"}"); 
	 					   tb8.setParent(row8);
						   tb8.setWidth("150px");



						Row row9 = new Row();
						row9.setParent(rows);
						   new Label("CM Description: ").setParent(row9);
					           Textbox tb9=new Textbox("${task.description!"Not Specified"}"); 
	 					   tb9.setParent(row9);
					           tb9.setRows(3);
						   tb9.setWidth("350px");

						Row row10 = new Row();
						row10.setParent(rows);
						   new Label("BPM Workflow Description: ").setParent(row10);
					           Textbox tb10=new Textbox("${task.properties.bpm_workflowDescription!"Not Specified"}"); 
	 					   tb10.setParent(row10);
					           tb10.setRows(3);
						   tb10.setWidth("350px");

						Row row11 = new Row();
						row11.setParent(rows);
						   new Label("BPM Status: ").setParent(row11);
					           Textbox tb11=new Textbox("${task.properties.bpm_status!"Not Specified"}"); 
	 					   tb11.setParent(row11);
						   tb11.setWidth("350px");

	 					   
						   Button b1=new Button("Cancella");
						   b1.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
								public void onEvent(Event event) throws Exception{
									window.setVisible(false);
								}
						   });	
						   b1.setParent(window);
						   Button b2=new Button("Salva");
						   b2.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
								public void onEvent(Event event) throws Exception{
								org.apache.commons.httpclient.HttpClient httpclient1= new org.apache.commons.httpclient.HttpClient(); 
							        httpclient1.setConnectionTimeout(8000);
							        org.apache.commons.httpclient.methods.PostMethod post = new org.apache.commons.httpclient.methods.PostMethod("${url.server}${url.context}/service/process/task-parameters.json");
							        org.zkoss.json.JSONObject json= new org.zkoss.json.JSONObject();

									json.put("{http://www.alfresco.org/model/bpm/1.0}description", tb1.getValue());
									//json.put("{http://www.alfresco.org/model/bpm/1.0}dueDate", tb2.getValue());
									json.put("{http://www.alfresco.org/model/bpm/1.0}priority", tb3.getValue());
									//json.put("{http://www.alfresco.org/model/bpm/1.0}startDate", tb4.getValue());
									json.put("{http://www.alfresco.org/model/bpm/1.0}comment", tb5.getValue());
									//json.put("{http://www.alfresco.org/model/bpm/1.0}completionDate", tb6.getValue());
									json.put("{http://www.alfresco.org/model/bpm/1.0}outcome", tb7.getValue());
									json.put("{http://www.alfresco.org/model/content/1.0}owner", tb8.getValue());
									json.put("{http://www.alfresco.org/model/bpm/1.0}assignee", tb8.getValue());
									json.put("{http://www.alfresco.org/model/content/1.0}description", tb9.getValue());
									json.put("{http://www.alfresco.org/model/bpm/1.0}workflowDescription", tb10.getValue());
									json.put("{http://www.alfresco.org/model/bpm/1.0}status", tb11.getValue());
								

							    String s=json.toString();
								System.out.println(s);
								post.setParameter("m",s);
								post.setParameter("p","${task.id}");
								post.setParameter("alf_ticket",ticket);
							    post.setRequestHeader("Accept-Language","en-us,en;q=0.5");
							    post.setRequestHeader("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
							    int statusCode = httpclient1.executeMethod(post);
								if (statusCode==200) alert("Successfull action");
								if (statusCode==500) alert("Unsuccessfull action! Please check logs");
								if (statusCode==401) alert("Unauthorized action! Please check logs");
							    post.releaseConnection();

								window.setVisible(false);
								}
					   	   });	
						   b2.setParent(window);

				}
		     });	
		    	((javax.servlet.http.HttpSession) Sessions.getCurrent().getNativeSession()).getServletContext().setAttribute("ParamsButton-${task.id}",btnParams);

	            Treecell tcExtra${task_index}=new Treecell();
		        hm.put("${task.id}", tcExtra${task_index}); 	
	            Treecell tcInitiator${task_index}=new Treecell("${task.initiator!"Not Specified"}");

		    tcId${task_index}.setParent(tr${task_index});
		    tcName${task_index}.setParent(tr${task_index});
		    tcTitle${task_index}.setParent(tr${task_index});
		    tcDescription${task_index}.setParent(tr${task_index});
		    tcState${task_index}.setParent(tr${task_index});
		    tcEndTask${task_index}.setParent(tr${task_index});
		    tcParamTask${task_index}.setParent(tr${task_index});
		    tcExtra${task_index}.setParent(tr${task_index});
		    tcInitiator${task_index}.setParent(tr${task_index});
		    
		    tr${task_index}.setParent(ti${task_index});
		    ti${task_index}.setParent(childrenList);
	         </#list>  
  		    
	         ((javax.servlet.http.HttpSession) Sessions.getCurrent().getNativeSession()).getServletContext().setAttribute("bundle",hm);	       

	 		t1.setLabel(org.zkoss.util.resource.Labels.getLabel("workflow.task.taskId"));	
	        if (org.zkoss.util.resource.Labels.getLabel("workflow.task.taskId").equals("HIDDEN")) t1.setVisible(false);
	 		t2.setLabel(org.zkoss.util.resource.Labels.getLabel("workflow.task.name"));	
	        if (org.zkoss.util.resource.Labels.getLabel("workflow.task.name").equals("HIDDEN")) t2.setVisible(false);
	 		t3.setLabel(org.zkoss.util.resource.Labels.getLabel("workflow.task.title"));	
	        if (org.zkoss.util.resource.Labels.getLabel("workflow.task.title").equals("HIDDEN")) t3.setVisible(false);
	 		t4.setLabel(org.zkoss.util.resource.Labels.getLabel("workflow.task.description"));	
	        if (org.zkoss.util.resource.Labels.getLabel("workflow.task.description").equals("HIDDEN")) t4.setVisible(false);
	 		t5.setLabel(org.zkoss.util.resource.Labels.getLabel("workflow.task.alfStatus"));	
	        if (org.zkoss.util.resource.Labels.getLabel("workflow.task.alfStatus").equals("HIDDEN")) t5.setVisible(false);
	 		t6.setLabel(org.zkoss.util.resource.Labels.getLabel("workflow.task.endTask"));	
	        if (org.zkoss.util.resource.Labels.getLabel("workflow.task.endTask").equals("HIDDEN")) t6.setVisible(false);
	 		t7.setLabel(org.zkoss.util.resource.Labels.getLabel("workflow.task.extra"));	
	        if (org.zkoss.util.resource.Labels.getLabel("workflow.task.extra").equals("HIDDEN")) t7.setVisible(false);
	 		t8.setLabel(org.zkoss.util.resource.Labels.getLabel("workflow.task.params"));	
	        if (org.zkoss.util.resource.Labels.getLabel("workflow.task.params").equals("HIDDEN")) t8.setVisible(false);
	 		t9.setLabel(org.zkoss.util.resource.Labels.getLabel("workflow.task.initiator"));	
	        if (org.zkoss.util.resource.Labels.getLabel("workflow.task.initiator").equals("HIDDEN")) t8.setVisible(false);

		
	       ]]> 
	       </zscript>
</tree>