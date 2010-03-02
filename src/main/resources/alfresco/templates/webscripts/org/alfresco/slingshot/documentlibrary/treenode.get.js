<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">

/**
 * Document List Component: treenode
 */
model.treenode = getTreenode();

/* Create collection of folders in the given space */
function getTreenode(siteId, path)
{
   try
   {
      var items = new Array(),
         hasSubfolders,
         ignoredTypes =
         {
            "{http://www.alfresco.org/model/forum/1.0}forum": true,
            "{http://www.alfresco.org/model/forum/1.0}topic": true,
            "{http://www.alfresco.org/model/content/1.0}systemfolder": true
         };
   
      // Use helper function to get the arguments
      var parsedArgs = ParseArgs.getParsedArgs();
      if (parsedArgs === null)
      {
         return;
      }

      // Look for folders in the parentNode
      for each(item in parsedArgs.parentNode.children)
      {
         if (item.isSubType("cm:folder") && !(item.type in ignoredTypes))
         {
            hasSubfolders = item.childFileFolders(false, true, "fm:forum").length > 0;
            
            items.push(
            {
               node: item,
               hasSubfolders: hasSubfolders
            });
         }
      }
   
      items.sort(sortByName);
   
      return (
      {
         "items": items
      });
   }
   catch(e)
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, e.toString());
      return;
   }
}


/* Sort the results by case-insensitive name */
function sortByName(a, b)
{
   return (b.node.name.toLowerCase() > a.node.name.toLowerCase() ? -1 : 1);
}