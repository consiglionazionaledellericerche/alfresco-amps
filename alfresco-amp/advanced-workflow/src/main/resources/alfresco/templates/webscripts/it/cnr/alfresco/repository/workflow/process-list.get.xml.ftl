<tree rows="${defarraylength}" mold="paging" width="99%" multiple="true" checkmark="true">
			        <treecols sizable="true">
			            <treecol label="Definition ID" width="15%" />
			            <treecol label="Definition Title" width="20%" />
			      		<treecol label="Definition Name" width="25%" />
			      		<treecol label="" width="10%" />
			      		<treecol label="" width="15%" />
			      		<treecol label="" width="15%" />
			        </treecols>
			        <treechildren id="childrenList"/>
					<zscript>
					<![CDATA[
					   ArrayList selectedProcess=new ArrayList();						   

			 		   Iframe iframe;
					   Window window=new Window();
					   window.setBorder("normal");		
					   window.setTitle("Struttura Visuale Processo");
					   window.setMode("overlapped");
					   window.setMinimizable(true);	
					   window.setMaximizable(true);	
					   window.setParent(page.getLastRoot());
					   window.setVisible(false);
					   window.setWidth("25%");	
 					   window.setHeight("100%");	
 					   
 					   <#if defarraylength != 1>
				 		   <#list defarray as i>	
						    Treeitem ti${i_index} = new Treeitem();
						    ti${i_index}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
							public void onEvent(Event event) throws Exception{
								if(ti${i_index}.isSelected()){
								 selectedProcess.add("${i.id}");
								 alert("Selected "+selectedProcess.size()+" definitions"); 	
								}
								if(!ti${i_index}.isSelected()){
								 int i=selectedProcess.indexOf("${i.id}");
								 selectedProcess.remove(i);	
								 alert("Selected "+selectedProcess.size()+" definitions");	
								}
								((javax.servlet.http.HttpSession)Sessions.getCurrent().getNativeSession()).getServletContext().setAttribute("selectedProcess",selectedProcess);
							}
					   	    });		
						    Treerow tr${i_index}=new Treerow();
						    Treecell tcId${i_index}=new Treecell("${i.id}");
						    Treecell tcTitle${i_index}=new Treecell("${i.title}");
						    Treecell tcName${i_index}=new Treecell("${i.name}");
						    Treecell tcStartTask${i_index}=new Treecell();
						    Treecell tcWkfGraph${i_index}=new Treecell();
						    Treecell tcUndef${i_index}=new Treecell();
						    if (new String("i${i_index}").equals("i0")) {
							iframe=new Iframe();
							iframe.setParent(window);
						    }
						    tcId${i_index}.addEventListener(Events.ON_RIGHT_CLICK,new org.zkoss.zk.ui.event.EventListener(){
									public void onEvent(Event event) throws Exception{
									}
					   	    	}
						    	);
								tcId${i_index}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
									public void onEvent(Event event) throws Exception{
										doEvent("process-list","${i.id}");
									}
					   	    	}
						    	);	
								
								Button b${i_index}=new Button("Start Task");
								b${i_index}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
									public void onEvent(Event event) throws Exception{
										Window w=new Window();
										w.setTitle("Start Task Parameters");
										w.setParent(page.getLastRoot());
										w.setHeight("100%");
										Grid grid=new Grid();
										grid.setAutopaging(false);   	
										grid.setMold("paging");
										grid.setVflex(false);
										grid.setPagingPosition("both");
				   						grid.setParent(w);
										Columns clms=new Columns();
										clms.setParent(grid);
										new Column("Property").setParent(clms);
										new Column("Content").setParent(clms);
										Rows rows=new Rows();						   	
										rows.setParent(grid);
										
										Row row1 = new Row();
										row1.setParent(rows);
										new Label("Definition Name: ").setParent(row1);
										Textbox tb1=new Textbox("${i.name}");
										tb1.setParent(row1);
										Row row2 = new Row();
										row2.setParent(rows);
										new Label("Description: ").setParent(row2);
										Textbox tb2=new Textbox();
										tb2.setParent(row2);
										Row row3 = new Row();
										row3.setParent(rows);
										new Label("Assignee: ").setParent(row3);
										Textbox tb3=new Textbox();
										tb3.setParent(row3);
										tb3.setConstraint("no empty");
										Row row4 = new Row();
										row4.setParent(rows);
										new Label("Document NodeRef: ").setParent(row4);
										Textbox tb4=new Textbox();
										tb4.setParent(row4);
										tb4.setConstraint("no empty");
										Row row5 = new Row();
										row5.setParent(rows);
										//org.joda.time.format.DateTimeFormatter parser2 = org.joda.time.format.ISODateTimeFormat.dateTimeNoMillis();
										String jtdate = "2012-01-01T12:00:00.000Z00:00";
										new Label("Due Date: ").setParent(row5);
										//Textbox tb5=new Textbox(parser2.parseDateTime(jtdate).toString());
										Textbox tb5=new Textbox(jtdate);
										tb5.setParent(row5);
										Row row6 = new Row();
										row6.setParent(rows);
										new Label("Priority: ").setParent(row6);
										Textbox tb6=new Textbox();
										tb6.setParent(row6);
										tb6.setConstraint("no empty");


										Button submitStart=new Button("Submit");
										submitStart.setParent(w);
										submitStart.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
											public void onEvent(Event event) throws Exception{
											org.apache.commons.httpclient.HttpClient httpclient1= new org.apache.commons.httpclient.HttpClient(); 
										        httpclient1.setConnectionTimeout(8000);
											String url="${url.server}${url.context}/service/process/startWorkflow?workflowName="+tb1.getValue()+"&description="+tb2.getValue()+"&assignee="+tb3.getValue()+"&documentnoderef="+tb4.getValue()+"&duedate="+tb5.getValue()+"&priority="+tb6.getValue()+"&alf_ticket="+ticket;
											System.out.println(url);
									        org.apache.commons.httpclient.methods.GetMethod get = new org.apache.commons.httpclient.methods.GetMethod(url);
									        get.setRequestHeader("Accept-Language","en-us,en;q=0.5");
									        get.setRequestHeader("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
									        int statusCode = httpclient1.executeMethod(get);
									        if (statusCode==200){
									        	alert("Workflow successfully started");
									        }else{
									        	alert("Workflow not started! See log files");
									        }
									        get.releaseConnection();
											
											w.setVisible(false);		
											}
										});	

										Button submitCancel=new Button("Cancel");
										submitCancel.setParent(w);
										submitCancel.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
											public void onEvent(Event event) throws Exception{
												w.setVisible(false);		
											}
										});	
								        
								        
									}
					   	    	}
						    	);
								
								
								Button b1${i_index}=new Button("Definition Img");
								b1${i_index}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
									public void onEvent(Event event) throws Exception{
										String url="${url.server}/alfresco/service/process/deftasks.xml?p="+tcId${i_index}.getLabel()+"&alf_ticket="+ticket;
										System.out.println(url);
										iframe.setSrc(url);
										iframe.setVisible(true); 	
										iframe.setWidth("100%");
										iframe.setHeight("100%");
										iframe.setScrolling("true");
										iframe.setVisible(true);
										window.setVisible(true);
					 					//((javax.servlet.http.HttpSession)Sessions.getCurrent().getNativeSession()).getServletContext().setAttribute("graph",iframe);
										//((javax.servlet.http.HttpSession)Sessions.getCurrent().getNativeSession()).getServletContext().setAttribute("wkfImg",window);
									}
					   	    	});
								Button b2${i_index}=new Button("Undeploy Def");
								b2${i_index}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
									public void onEvent(Event event) throws Exception{
										org.apache.commons.httpclient.HttpClient httpclient1= new org.apache.commons.httpclient.HttpClient(); 
								        httpclient1.setConnectionTimeout(8000);
										String url="${url.server}/alfresco/service/process/process-undeploy?p="+tcId${i_index}.getLabel()+"&alf_ticket="+ticket;
										System.out.println(url);
								        org.apache.commons.httpclient.methods.GetMethod get = new org.apache.commons.httpclient.methods.GetMethod(url);
								        get.setRequestHeader("Accept-Language","en-us,en;q=0.5");
								        get.setRequestHeader("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
								        int statusCode = httpclient1.executeMethod(get);
								        if (statusCode==200){
								        	alert("Workflow successfully undployed");
							    	    }else{
							        		alert("Workflow not undployed! See log files");
								        }
								        get.releaseConnection();
									}
					   	    	});

								
								
								tcId${i_index}.setParent(tr${i_index});
								tcTitle${i_index}.setParent(tr${i_index});
								tcName${i_index}.setParent(tr${i_index});
								tcStartTask${i_index}.setParent(tr${i_index});
								tcWkfGraph${i_index}.setParent(tr${i_index});
								tcUndef${i_index}.setParent(tr${i_index});
								b1${i_index}.setParent(tcWkfGraph${i_index});
								b${i_index}.setParent(tcStartTask${i_index});
								b2${i_index}.setParent(tcUndef${i_index});
								tr${i_index}.setParent(ti${i_index});
								ti${i_index}.setParent(childrenList);
						 </#list>  
					 </#if>
   				         <#if defarraylength == 1>
				 		   <#list defarray as i>
						    Treeitem ti = new Treeitem();
						    ti.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
							public void onEvent(Event event) throws Exception{
								if(ti.isSelected()) selectedProcess.add("${i.id}");
								if(!ti.isSelected()){
								 int i=selectedProcess.indexOf("${i.id}");
								 selectedProcess.remove(i);	
								}
								((javax.servlet.http.HttpSession)Sessions.getCurrent().getNativeSession()).getServletContext().setAttribute("selectedProcess",selectedProcess);
							}
					   	    });		

						    Treerow tr=new Treerow();
						    Treecell tcId=new Treecell("${i.id}");
						    Treecell tcTitle=new Treecell("${i.title}");
						    Treecell tcName=new Treecell("${i.name}");
						    Treecell tcStartTask=new Treecell();
						    Treecell tcWkfGraph=new Treecell();
						    Treecell tcUndef=new Treecell();
  						    iframe=new Iframe();
						    iframe.setParent(window);
 						    tcId.addEventListener(Events.ON_RIGHT_CLICK,new org.zkoss.zk.ui.event.EventListener(){
									public void onEvent(Event event) throws Exception{
									}
					   	     });
 						     tcId.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
									public void onEvent(Event event) throws Exception{
										doEvent("process-list","${i.id}");
									}
					   	     });	
	
 						     
 						     
							Button b=new Button("Start Task");
								b.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
									public void onEvent(Event event) throws Exception{
				
										Window w=new Window();
										w.setTitle("Start Task Parameters");
										w.setParent(page.getLastRoot());
										w.setHeight("100%");
										Grid grid=new Grid();
										grid.setAutopaging(false);   	
										grid.setMold("paging");
										grid.setVflex(false);
										grid.setPagingPosition("both");
				   						grid.setParent(w);
										Columns clms=new Columns();
										clms.setParent(grid);
										new Column("Property").setParent(clms);
										new Column("Content").setParent(clms);
										Rows rows=new Rows();						   	
										rows.setParent(grid);



										Row row1 = new Row();
										row1.setParent(rows);
										new Label("Definition Name: ").setParent(row1);
										Textbox tb1=new Textbox("${i.name}");
										tb1.setParent(row1);
										Row row2 = new Row();
										row2.setParent(rows);
										new Label("Description: ").setParent(row2);
										Textbox tb2=new Textbox();
										tb2.setParent(row2);
										Row row3 = new Row();
										row3.setParent(rows);
										new Label("Assignee: ").setParent(row3);
										Textbox tb3=new Textbox();
										tb3.setParent(row3);
										tb3.setConstraint("no empty");
										Row row4 = new Row();
										row4.setParent(rows);
										new Label("Document NodeRef: ").setParent(row4);
										Textbox tb4=new Textbox();
										tb4.setParent(row4);
										tb4.setConstraint("no empty");
										Row row5 = new Row();
										row5.setParent(rows);
										//org.joda.time.format.DateTimeFormatter parser2 = org.joda.time.format.ISODateTimeFormat.dateTimeNoMillis();
										String jtdate = "2012-01-01T12:00:00.000Z00:00";
										new Label("Due Date: ").setParent(row5);
										//Textbox tb5=new Textbox(parser2.parseDateTime(jtdate).toString());
										Textbox tb5=new Textbox(jtdate);
										tb5.setParent(row5);
										Row row6 = new Row();
										row6.setParent(rows);
										new Label("Priority: ").setParent(row6);
										Textbox tb6=new Textbox();
										tb6.setParent(row6);
										tb6.setConstraint("no empty");


										Button submitStart=new Button("Submit");
										submitStart.setParent(w);
										submitStart.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
											public void onEvent(Event event) throws Exception{
											org.apache.commons.httpclient.HttpClient httpclient1= new org.apache.commons.httpclient.HttpClient(); 
										        httpclient1.setConnectionTimeout(8000);
											String url="${url.server}${url.context}/service/process/startWorkflow?workflowName="+tb1.getValue()+"&description="+tb2.getValue()+"&assignee="+tb3.getValue()+"&documentnoderef="+tb4.getValue()+"&duedate="+tb5.getValue()+"&priority="+tb6.getValue()+"&alf_ticket="+ticket;
											System.out.println(url);
										        org.apache.commons.httpclient.methods.GetMethod get = new org.apache.commons.httpclient.methods.GetMethod(url);
										        get.setRequestHeader("Accept-Language","en-us,en;q=0.5");
										        get.setRequestHeader("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
										        int statusCode = httpclient1.executeMethod(get);
										        if (statusCode==200){
										        	alert("Workflow successfully started");
										        }else{
										        	alert("Workflow not started! See log files");
										        }

										        get.releaseConnection();
											
											w.setVisible(false);		
											}
										});	

										Button submitCancel=new Button("Cancel");
										submitCancel.setParent(w);
										submitCancel.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
											public void onEvent(Event event) throws Exception{
												w.setVisible(false);		
											}
										});	
								        
								        
									}
					   	    	}
						    	);

 							Button b1=new Button("Definition Img");
								b1.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
									public void onEvent(Event event) throws Exception{
										//iframe.setSrc("http://test2.cedrc.cnr.it:8080/alfresco/service/process/deftasks.xml?p="+tcId.getLabel());
										iframe.setSrc("${url.server}/alfresco/service/process/deftasks.xml?p="+tcId.getLabel()+"&alf_ticket="+ticket);
										iframe.setVisible(true); 	
										iframe.setWidth("100%");
										iframe.setHeight("100%");
										iframe.setScrolling("true");
										iframe.setVisible(true);
										window.setVisible(true);

									}
					   	    	});
 						     
								
							Button b2=new Button("Undeploy Def");
								b2.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
									public void onEvent(Event event) throws Exception{
										org.apache.commons.httpclient.HttpClient httpclient1= new org.apache.commons.httpclient.HttpClient(); 
								        httpclient1.setConnectionTimeout(8000);
										String url="${url.server}/alfresco/service/process/process-undeploy?p="+tcId.getLabel()+"&alf_ticket="+ticket;
										System.out.println(url);
								        org.apache.commons.httpclient.methods.GetMethod get = new org.apache.commons.httpclient.methods.GetMethod(url);
								        get.setRequestHeader("Accept-Language","en-us,en;q=0.5");
								        get.setRequestHeader("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
								        int statusCode = httpclient1.executeMethod(get);
								        if (statusCode==200){
								        	alert("Workflow successfully undployed");
							    	    }else{
							        		alert("Workflow not undployed! See log files");
								        }
								        get.releaseConnection();
									}
				   	    	});
								
						     tcId.setParent(tr);
						     tcTitle.setParent(tr);
						     tcName.setParent(tr);
							 tcStartTask.setParent(tr);
							 tcWkfGraph.setParent(tr);
							 tcUndef.setParent(tr);
							 b.setParent(tcStartTask);
							 b1.setParent(tcWkfGraph);
							 b2.setParent(tcUndef);
						     tr.setParent(ti);
						     ti.setParent(childrenList);
						 </#list>  
					 </#if>

				       ]]> 
				       </zscript>
</tree>