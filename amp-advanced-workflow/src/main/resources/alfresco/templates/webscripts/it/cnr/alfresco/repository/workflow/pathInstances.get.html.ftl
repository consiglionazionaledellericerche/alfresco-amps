<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta content="text/html;charset=ISO-8859-1" http-equiv="Content-Type">
  <script type="text/javascript">
/* This script and many more are available free online at
The JavaScript Source :: http://javascript.internet.com
Created by: Travis Beckham :: http://www.squidfingers.com | http://www.podlob.com
version date: 06/02/03 :: If want to use this code, feel free to do so,
but please leave this message intact. (Travis Beckham) */

// Node Functions

if(!window.Node){
  var Node = {ELEMENT_NODE : 1, TEXT_NODE : 3};
}

function checkNode(node, filter){
  return (filter == null || node.nodeType == Node[filter] || node.nodeName.toUpperCase() == filter.toUpperCase());
}

function getChildren(node, filter){
  var result = new Array();
  var children = node.childNodes;
  for(var i = 0; i < children.length; i++){
    if(checkNode(children[i], filter)) result[result.length] = children[i];
  }
  return result;
}

function getChildrenByElement(node){
  return getChildren(node, "ELEMENT_NODE");
}

function getFirstChild(node, filter){
  var child;
  var children = node.childNodes;
  for(var i = 0; i < children.length; i++){
    child = children[i];
    if(checkNode(child, filter)) return child;
  }
  return null;
}

function getFirstChildByText(node){
  return getFirstChild(node, "TEXT_NODE");
}

function getNextSibling(node, filter){
  for(var sibling = node.nextSibling; sibling != null; sibling = sibling.nextSibling){
    if(checkNode(sibling, filter)) return sibling;
  }
  return null;
}
function getNextSiblingByElement(node){
        return getNextSibling(node, "ELEMENT_NODE");
}

// Menu Functions & Properties

var activeMenu = null;

function showMenu() {
  if(activeMenu){
    activeMenu.className = "";
    getNextSiblingByElement(activeMenu).style.display = "none";
  }
  if(this == activeMenu){
    activeMenu = null;
  } else {
    this.className = "active";
    getNextSiblingByElement(this).style.display = "block";
    activeMenu = this;
  }
  return false;
}

function initMenu(){
  var menus, menu, text, a, i;
  menus = getChildrenByElement(document.getElementById("menu"));
  for(i = 0; i < menus.length; i++){
    menu = menus[i];
    text = getFirstChildByText(menu);
    a = document.createElement("a");
    menu.replaceChild(a, text);
    a.appendChild(text);
    a.href = "#";
    a.onclick = showMenu;
    a.onfocus = function(){this.blur()};
  }
}

if(document.createElement) window.onload = initMenu;
</script>

<style rel="stylesheet" type="text/css">
ul#menu {
  width: 500px;
  list-style-type: none;
  border-top: solid 1px #b9a894;
  margin: 0;
  padding: 0;
}

ul#menu ol {
  display: none;
  text-align: left;
  list-style-type: none;
  margin: 0;
  padding: 10px;
}

ul#menu li, 
  ul#menu a {
  font-family: verdana, sans-serif;
  font-size: 15px;
  color: #785a3c;
}

ul#menu li {
  border-bottom: solid 1px #b9a894;
  line-height: 15px;
}

ul#menu ol li {
  border-bottom: none;
}

ul#menu ol li:before {
  content: "- ";
}

ul#menu a {
  text-decoration: none;
  outline: none;
}

ul#menu a:hover {
  color: #539dbc;
}

ul#menu a.active {
  color: #be5028;
}
</style>

</head>

  <body>

<ul id="menu">
  <li>Descrizione dell' istanza selezionata: 
    <ol>
		<#list paths as i>
      <!--li!-->
			Id: <a href=/alfresco/service/process/tasks?p=${i[2]}>${i[0]}</a>
			<br>
			Status: ${i[1]}</a>	
			<br>
      
      <!--/li!-->
		</#list>  
    </ol>
  </li>
</ul>

</body>
</html>
