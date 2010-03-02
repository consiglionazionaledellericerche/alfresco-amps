/**
 * Get children
 */

function main()
{
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   
   var authorityType = args["authorityType"];
   
   var group = groups.getGroup(shortName);
   if (group == null)
   {
      // Group cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The group :" + shortName + ", does not exist.");
      return;
   }
   
   model.group = group;
   
   if (authorityType != null)
   {
      if (!authorityType.match("[GROUP|USER]"))
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The authorityType argument has does not have a correct value.");
         return;
      }
      if (authorityType == "GROUP")
      {
         model.children = group.getChildGroups();
      }
      if (authorityType == "USER")
      {
         model.children = group.getChildUsers();
      }
   }
   else
   {
      model.children = group.getChildAuthorities();
   }
}

main();