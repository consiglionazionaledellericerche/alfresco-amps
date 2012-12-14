<tree id="tasks" mold="paging" width="99%" pageSize="15">
	<treecols sizable="true">
		<treecol id="t1" label="Task ID" width="15%" />
		<treecol id="t2" label="Name" width="10%" />
		<treecol id="t3" label="Title" width="15%" />
		<treecol id="t4" label="Description" width="10%" />
		<treecol id="t5" label="Status" width="10%" />
		<treecol id="t6" label="End Task" width="10%" />
		<treecol id="t7" label="Param Task" width="10%" />
		<treecol id="t8" label="Bundle" width="10%" />
		<treecol id="t9" label="Assigned To" width="10%" />
	</treecols>
	<treechildren id="childrenList" />
	<zscript>
		<![CDATA[
HashMap hm= new HashMap();
		
	    Window window=new Window();
	    window.setBorder("normal");		
	    window.setTitle("Doing Task");
	    window.setMode("overlapped");
	    window.setMinimizable(true);	
	    window.setVisible(false);
	    window.setWidth("50%");	
		window.setHeight("100%");	

		<#list tasks as task>	
		    Treeitem ti${task_index} = new Treeitem();
	            Treerow tr${task_index}=new Treerow();
				tr${task_index}.addEventListener(Events.ON_RIGHT_CLICK,new org.zkoss.zk.ui.event.EventListener(){
public void onEvent(Event event) throws Exception{
						//alert("Task Properties");
					}
	   	    	}
		    	);	
			    tr${task_index}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
					public void onEvent(Event event) throws Exception{
						doEvent("task-filtered-list","${task[0]}");
					}
		   	    });	
	            
	            
	            Treecell tcId${task_index}=new Treecell("${task[0]}");
	            Treecell tcName${task_index}=new Treecell("${task[1]}");
	            Treecell tcTitle${task_index}=new Treecell("${task[2]}");
	            Treecell tcDescription${task_index}=new Treecell("${task[3]}");
	            Treecell tcState${task_index}=new Treecell("${task[4]}");
	            Treecell tcEndTask${task_index}=new Treecell();
	            Button btnEnd${task_index}=new Button("EndTask");
	            btnEnd${task_index}.setParent(tcEndTask${task_index});	
	            btnEnd${task_index}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
				public void onEvent(Event event) throws Exception{
					org.apache.commons.httpclient.HttpClient httpclient1= new org.apache.commons.httpclient.HttpClient(); 
				        httpclient1.setConnectionTimeout(8000);
				        org.apache.commons.httpclient.methods.PostMethod post = new org.apache.commons.httpclient.methods.PostMethod("${url.server}${url.context}/service/api/workflow/task/end/${task[0]}");
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
		    	((javax.servlet.http.HttpSession) Sessions.getCurrent().getNativeSession()).getServletContext().setAttribute("EndButton-${task[0]}",btnEnd${task_index});
	        	Treecell tcParamTask${task_index}=new Treecell();

				StringTokenizer st${task_index}=new StringTokenizer("${task[5]}",",");
				Map map${task_index}=new HashMap();
				while(st${task_index}.hasMoreTokens()){
					String metaDato${task_index}=st${task_index}.nextToken();
					//System.out.println(metaDato);
					StringTokenizer st1${task_index}=new StringTokenizer(metaDato${task_index},"===");
					String meta${task_index}=null;
					String content${task_index}=null;
					try{
						meta${task_index}=st1${task_index}.nextToken();
						content${task_index}=st1${task_index}.nextToken();
					}catch(java.util.NoSuchElementException nsee${task_index}){
						content${task_index}=new String();
					}
					//System.out.println(meta+" "+content);
					map${task_index}.put(meta${task_index},content${task_index});
				}


                Button btnParams${task_index}=new Button("Parameters");
        	    btnParams${task_index}.setParent(tcParamTask${task_index});
	            btnParams${task_index}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
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
						Textbox tb1=new Textbox((String)map${task_index}.get("bpm_description")); 
						tb1.setParent(row1);
						tb1.setRows(3);
						tb1.setWidth("100%");


						Row row2 = new Row();
						row2.setParent(rows);
   					    new Label("BPM Due Date: ").setParent(row2);
						StringTokenizer dateT=new StringTokenizer((String)map${task_index}.get("bpm_dueDate")," ");
						String dt=dateT.nextToken();
						String td=dateT.nextToken();
						java.text.DateFormat formatter ; 
						java.util.Date date ; 
						formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
						date = (Date)formatter.parse(dt); 
						Hbox hbox2=new Hbox();
						hbox2.setParent(row2);
						Datebox dateBox2=new Datebox();
						dateBox2.setFormat("yyyy-MM-dd");
						dateBox2.setValue(date);												
						dateBox2.setParent(hbox2);			
						//Timebox timeBox2=new Timebox();
						//timeBox2.setParent(hbox2);	
						//timeBox2.setFormat("hh:mm:ss");
						//timeBox2.setValue(td);	        
						//Textbox tb2=new Textbox((String)map${task_index}.get("{http://www.alfresco.org/model/bpm/1.0}dueDate")); 
	 					//tb2.setParent(row2);
						//tb2.setWidth("350px");


						Row row3 = new Row();
						row3.setParent(rows);
						   new Label("BPM Priority: ").setParent(row3);
					           Textbox tb3=new Textbox((String)map${task_index}.get("bpm_priority")); 
	 					   tb3.setParent(row3);
						   tb3.setWidth("50px");


						Row row4 = new Row();
						row4.setParent(rows);
						   new Label("BPM Start Date: ").setParent(row4);
						StringTokenizer dateT=new StringTokenizer((String)map${task_index}.get("cm_created")," ");
						String dt=dateT.nextToken();
						String td=dateT.nextToken();
						java.text.DateFormat formatter ; 
						java.util.Date date ; 
						formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
						date = (Date)formatter.parse(dt); 
						Hbox hbox4=new Hbox();
						hbox4.setParent(row4);
						Datebox dateBox4=new Datebox();
						dateBox4.setFormat("yyyy-MM-dd");
						dateBox4.setValue(date);
						dateBox4.setParent(hbox4);			
			  	                //Textbox tb4=new Textbox((String)map${task_index}.get("{http://www.alfresco.org/model/content/1.0}created")); 
	 					//tb4.setParent(row4);
						//tb4.setWidth("350px");

						Row row5 = new Row();
						row5.setParent(rows);
						   new Label("BPM Comment: ").setParent(row5);
					           Textbox tb5=new Textbox((String)map${task_index}.get("bpm_comment")); 
	 					   tb5.setParent(row5);
					           tb5.setRows(3);
						   tb5.setWidth("350px");


						Row row6 = new Row();
						row6.setParent(rows);
  					        new Label("BPM Completion Date: ").setParent(row6);
						StringTokenizer dateT=new StringTokenizer((String)map${task_index}.get("bpm_completionDate")," ");
						if(dateT.countTokens()<=1) dateT=new StringTokenizer("00-00-00 00:00:00"," ");
						String dt=dateT.nextToken();
						String td=dateT.nextToken();
						java.text.DateFormat formatter ; 
						java.util.Date date ; 
						formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
						date = (Date)formatter.parse(dt); 
					        Hbox hbox6=new Hbox();
						hbox6.setParent(row6);
						Datebox dateBox6=new Datebox();
						dateBox6.setFormat("yyyy-MM-dd");
						dateBox6.setValue(date);
						dateBox6.setParent(hbox6);			
  						//Textbox tb6=new Textbox((String)map${task_index}.get("{http://www.alfresco.org/model/bpm/1.0}completionDate")); 
	 					//tb6.setParent(row6);
						//tb6.setWidth("350px");


						Row row7 = new Row();
						row7.setParent(rows);
						   new Label("BPM Outcome: ").setParent(row7);
					           Textbox tb7=new Textbox((String)map${task_index}.get("bpm_outcome")); 
	 					   tb7.setParent(row7);
					           tb7.setRows(3);
						   tb7.setWidth("350px");


						Row row8 = new Row();
						row8.setParent(rows);
						   new Label("CM Owner: ").setParent(row8);
					           Textbox tb8=new Textbox((String)map${task_index}.get("cm_owner")); 
	 					   tb8.setParent(row8);
						   tb8.setWidth("150px");



						Row row9 = new Row();
						row9.setParent(rows);
						   new Label("CM Description: ").setParent(row9);
					           Textbox tb9=new Textbox((String)map${task_index}.get("cm_description")); 
	 					   tb9.setParent(row9);
					           tb9.setRows(3);
						   tb9.setWidth("350px");

						Row row10 = new Row();
						row10.setParent(rows);
						   new Label("BPM Workflow Description: ").setParent(row10);
					           Textbox tb10=new Textbox((String)map${task_index}.get("bpm_workflowDescription")); 
	 					   tb10.setParent(row10);
					           tb10.setRows(3);
						   tb10.setWidth("350px");

						Row row11 = new Row();
						row11.setParent(rows);
						   new Label("BPM Status: ").setParent(row11);
					           Textbox tb11=new Textbox((String)map${task_index}.get("bpm_status")); 
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
								//alert(dateBox2.getValue().getYear()+1900+"-"+dateBox2.getValue().getMonth()+"-"+dateBox2.getValue().getDay());
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
								post.setParameter("p","${task[0]}");
								post.setParameter("alf_ticket",ticket);
							    post.setRequestHeader("Accept-Language","en-us,en;q=0.5");
							    post.setRequestHeader("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
							    int statusCode = httpclient1.executeMethod(post);
							    //alert(statusCode);
							    post.releaseConnection();

   							    window.setVisible(false);
							}
					   	   });	
						   b2.setParent(window);



				}
		     });	
        	    
	    	((javax.servlet.http.HttpSession) Sessions.getCurrent().getNativeSession()).getServletContext().setAttribute("ParamsButton-${task[0]}",btnParams${task_index});

		    Treecell tcExtra${task_index}=new Treecell();
		    hm.put("${task[0]}", tcExtra${task_index}); 	

		    //Treecell tcInitiator${task_index}=new Treecell((String)map${task_index}.get("cm_userName"));
		    Treecell tcAssignee${task_index}=new Treecell((String)map${task_index}.get("cm_owner"));


		    tcId${task_index}.setParent(tr${task_index});
		    tcName${task_index}.setParent(tr${task_index});
		    tcTitle${task_index}.setParent(tr${task_index});
		    tcDescription${task_index}.setParent(tr${task_index});
		    tcState${task_index}.setParent(tr${task_index});
		    tcEndTask${task_index}.setParent(tr${task_index});
		    tcParamTask${task_index}.setParent(tr${task_index});
		    tcExtra${task_index}.setParent(tr${task_index});
		    tcAssignee${task_index}.setParent(tr${task_index});
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
	 		t9.setLabel(org.zkoss.util.resource.Labels.getLabel("workflow.task.assignee"));	
	        if (org.zkoss.util.resource.Labels.getLabel("workflow.task.assignee").equals("HIDDEN")) t8.setVisible(false);
]]> 
	       </zscript>
</tree>