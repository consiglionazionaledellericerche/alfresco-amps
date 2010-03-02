<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

/**
 * Assign Workflow to single/multiple files and single/multiple people action
 * @method POST
 */

/**
 * Entrypoint required by action.lib.js
 *
 * @method runAction
 * @param p_params {object} Object literal containing files array
 * @return {object|null} object representation of action results
 */
function runAction(p_params)
{
   var result, results = [], file, files = [], i, j, workflowName, assignees, dueDate, description;

   // Must have workflow type
   if (json.isNull("type"))
   {
      status.setCode(status.STATUS_BAD_REQUEST, "No workflow type.");
      return;
   }
   workflowName = "jbpm$" + json.get("type");

   // Only add files that aren't containers
   for (i in p_params.files)
   {
      file = search.findNode(p_params.files[i]);
      if (file !== null && !file.isContainer)
      {
         files.push(file);
      }
   }
   
   if (!files || files.length == 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "No files.");
      return;
   }

   // Must also have array of people
   assignees = getMultipleInputValues("people");
   if (typeof assignees == "string")
   {
      status.setCode(status.STATUS_BAD_REQUEST, "No people assigned.");
      return;
   }
   if (!assignees || assignees.length == 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "No people assigned.");
      return;
   }
   for (i = 0, j = assignees.length; i < j; i++)
   {
      assignees[i] = people.getPerson(assignees[i]);
   }

   // Date supplied?
   dueDate = null;
   if (!json.isNull("date"))
   {
      if (json.get("date") != "")
      {
         dueDate = new Date(json.get("date"));
      }
   }
   
   // Description supplied?
   if (!json.isNull("description"))
   {
      description = json.get("description");
      description = description.substr(0, 255);
   }
   else
   {
      description = "";
   }
   
   // Get the workflow definition
   var workflowDefinition = workflow.getDefinitionByName(workflowName);

   // Create the workflow package to contain the file nodes
   var workflowPackage = workflow.createPackage();

   // Add each file to the workflowPackage as a child association
   for (i in files)
   {
      file = files[i];
      result =
      {
         nodeRef: file.nodeRef,
         action: "assignWorkflow",
         success: false
      };
      
      try
      {
         result.id = file.name;
         result.type = "document";
         // Add the file as a child assoc of the workflow node
         workflowPackage.addNode(file);
         result.success = true;
      }
      catch (e)
      {
         result.id = file.name;
         result.nodeRef = file.nodeRef;
         result.success = false;
      }
      
      results.push(result);
   }
   
   var workflowParameters =
   {
      "bpm:workflowDescription": description
   };

   if (assignees.length == 1)
   {
      workflowParameters["bpm:assignee"] = assignees[0];
   }
   else
   {
      workflowParameters["bpm:assignees"] = assignees;
   }
   if (dueDate)
   {
      workflowParameters["bpm:workflowDueDate"] = dueDate;
   }
   
   var workflowPath = workflowDefinition.startWorkflow(workflowPackage, workflowParameters);
   
   // Auto-end the start task
   var tasks = workflowPath.tasks;
   for (task in tasks)
   {
      tasks[task].endTask(null);
   }

   return results;
}

/* Bootstrap action script */
main();
