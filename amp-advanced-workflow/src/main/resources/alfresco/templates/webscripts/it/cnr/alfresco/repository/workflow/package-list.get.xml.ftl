		<menubar width="15%">
		<menu label="Task Bundle" >
		    <menupopup >
	  		<zscript>
				<![CDATA[
				import java.awt.datatransfer.Clipboard;
				import java.awt.datatransfer.ClipboardOwner;
				import java.awt.datatransfer.Transferable;
				import java.awt.datatransfer.StringSelection;
				import java.awt.datatransfer.DataFlavor;
				import java.awt.datatransfer.UnsupportedFlavorException;
				import java.awt.Toolkit;
				import java.io.*;
				<#list packages as package>	
					<#list package?split("|") as x>
						<#if x_index==0><#assign pack=x></#if>
						<#if x_index==1><#assign task=x></#if> 
					</#list>
				    Menuitem mi${package_index} = new Menuitem("${pack}");
				    
				    //mi${package_index}.setImage("http://t0.gstatic.com/images?q=tbn:ANd9GcQ4yeUP8OZ1uFMkFGsDLohKI_aB8ze3VGkuHdRsXDVgMhy9Qc_H");
				    mi${package_index}.setParent(self);
	   			    mi${package_index}.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
					public void onEvent(Event event) throws Exception{
							alert("Download method must be implemented");
					}
				    });
	   			    mi${package_index}.addEventListener(Events.ON_RIGHT_CLICK,new org.zkoss.zk.ui.event.EventListener(){
							public void onEvent(Event event) throws Exception{
							    StringSelection stringSelection = new StringSelection(mi${package_index}.getLabel());
//							    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//							    clipboard.setContents( stringSelection, this );	
//							    alert("Document associated with ${task} has been selected!");

							   Window window=new Window();
							   window.setBorder("normal");		
							   window.setTitle("Task Bundle Add/Remove");
							   window.setMode("embedded");
							   window.setMinimizable(true);	
							   window.setParent(page.getLastRoot());
							   window.setVisible(true);
							   window.setWidth("100%");	
		 					   window.setHeight("30%");	
							   window.setSizable(true);	 					   

							   									
                               				   Label l1= new Label("Document ${pack} is in Task: ${task}   ");
							   l1.setParent(window);
							   
							   Button b1=new Button("Cancella");
							   b1.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
									public void onEvent(Event event) throws Exception{
										window.setVisible(false);
									}
							   });	
							   b1.setParent(window);
							   Button b2=new Button("Rimuovi Documento");
							   b2.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
									public void onEvent(Event event) throws Exception{
									org.apache.commons.httpclient.HttpClient httpclient1= new org.apache.commons.httpclient.HttpClient(); 
							        httpclient1.setConnectionTimeout(8000);
							        org.apache.commons.httpclient.methods.PostMethod post = new org.apache.commons.httpclient.methods.PostMethod("${url.server}${url.context}/service/process/task-documents.json");
							        org.zkoss.json.JSONObject json= new org.zkoss.json.JSONObject();

								json.put("nodeRef", "${pack}");
							    String s=json.toString();
								System.out.println(s);
								post.setParameter("m",s);
								post.setParameter("p","${task}");
								post.setParameter("alf_ticket",ticket);
							        post.setRequestHeader("Accept-Language","en-us,en;q=0.5");
							        post.setRequestHeader("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
							        int statusCode = httpclient1.executeMethod(post);
							        alert(statusCode);
							        post.releaseConnection();

									window.setVisible(false);
									}
						   	   });	
							   b2.setParent(window);

							   Button b3=new Button("Aggiungi Documento");
							   b3.addEventListener(Events.ON_CLICK,new org.zkoss.zk.ui.event.EventListener(){
									public void onEvent(Event event) throws Exception{
										alert("Funzione non Implementata");
										window.setVisible(false);
									}
							   });	
							   b3.setParent(window);

							   
							}
			   	    });		            
			</#list>  
			]]> 
		    </zscript>
		   </menupopup>		
		</menu>
		</menubar> 	
  