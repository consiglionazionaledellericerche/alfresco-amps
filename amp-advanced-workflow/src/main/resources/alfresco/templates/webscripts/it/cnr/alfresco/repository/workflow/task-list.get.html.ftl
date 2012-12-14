<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">






<!--*************************!-->
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<script type="text/javascript">
/*********************
 * browser detection *
 *********************/

var ie=document.all;
var nn6=document.getElementById&&!document.all;

/*****************
 * drag and drop *
 *****************/
 
var isdrag=false;
var mouseStartX, mouseStartY;		// mouse position when drag starts
var elementStartX, elementStartY;	// element position when drag starts
var elementToMove;
var blockToMove;

// an array containing bounds to be respected while dragging elements,
// these bounds are left, top, left + width, top + height of the parent element.
var bounds = new Array(4);

function movemouse(e)
{
	if (isdrag)
	{
		var currentMouseX = nn6 ? e.clientX : event.clientX;
		var currentMouseY = nn6 ? e.clientY : event.clientY;
		var newElementX = elementStartX + currentMouseX - mouseStartX;
		var newElementY = elementStartY + currentMouseY - mouseStartY;

		// check bounds
		// note: the "-1" and "+1" is to avoid borders overlap
		if(newElementX < bounds[0])
			newElementX = bounds[0] + 1;
		if(newElementX + elementToMove.offsetWidth > bounds[2])
			newElementX = bounds[2] - elementToMove.offsetWidth - 1;
		if(newElementY < bounds[1])
			newElementY = bounds[1] + 1;
		if(newElementY + elementToMove.offsetHeight > bounds[3])
			newElementY = bounds[3] - elementToMove.offsetHeight - 1;
		
		// move element
		elementToMove.style.left = newElementX + 'px';
		elementToMove.style.top  = newElementY + 'px';

//		elementToMove.style.left = newElementX / elementToMove.parentNode.offsetWidth * 100 + '%';
//		elementToMove.style.top  = newElementY / elementToMove.parentNode.offsetHeight * 100 + '%';
	
		elementToMove.style.right = null;
		elementToMove.style.bottom = null;
		
		if(blockToMove)
			blockToMove.onMove();
		return false;
	}
}

/**
 * finds the innermost draggable element starting from the one that generated the event "e"
 * (i.e.: the html element under mouse pointer), then setup the document's onmousemove function to
 * move the element around.
 */
function selectmouse(e) 
{
	
	var eventSource = nn6 ? e.target : event.srcElement;
	
	while (eventSource != document.body && !hasClass(eventSource, "draggable"))
	{  	
		eventSource = nn6 ? eventSource.parentNode : eventSource.parentElement;
	}

	// if a draggable element was found, calculate its actual position
	if (hasClass(eventSource, "draggable"))
	{
		isdrag = true;
		elementToMove = eventSource;
		
		// calculate start point
		//elementStartX = calculateOffsetLeft(elementToMove);
		//elementStartY = calculateOffsetTop(elementToMove);
		elementStartX = elementToMove.offsetLeft;
		elementStartY = elementToMove.offsetTop;
		
		// calculate mouse start point
		mouseStartX = nn6 ? e.clientX : event.clientX;
		mouseStartY = nn6 ? e.clientY : event.clientY;
		
		// calculate bounds as left, top, width, height of the parent element
		if(elementToMove.parentNode.style.position == 'absolute')
		{
			bounds[0] = 0;
			bounds[1] = 0;
		}
		else
		{
			bounds[0] = calculateOffsetLeft(elementToMove.parentNode);
			bounds[1] = calculateOffsetTop(elementToMove.parentNode);
		}
		bounds[2] = bounds[0] + elementToMove.parentNode.offsetWidth;
		bounds[3] = bounds[1] + elementToMove.parentNode.offsetHeight;
		
		
		// either find the block related to the dragging element to call its onMove method
		blockToMove = findBlock(eventSource.id);
		document.onmousemove = movemouse;
		
		return false;
	}
}

document.onmousedown=selectmouse;
document.onmouseup=new Function("isdrag=false");



/*************
 * Constants *
 *************/
var AUTO = 0;
var HORIZONTAL = 1;
var VERTICAL = 2;

/**************
 * Inspectors *
 **************/

var inspectors = new Array();

/**
 * The canvas class.
 * This class is built on a div html element.
 */
function Canvas(htmlElement)
{
	/*
	 * initialization
	 */
	this.id = htmlElement.id;
	this.htmlElement = htmlElement;
	this.blocks = new Array();
	this.connectors = new Array();
	
	this.initCanvas = function()
	{
		// inspect canvas children to identify first level blocks
		this.findNestedBlocksAndConnectors(this.htmlElement);
		
		// init connectors
		var i;
		for(i = 0; i < this.connectors.length; i++)
		{
			this.connectors[i].initConnector();
		}
	}
	
	this.findNestedBlocksAndConnectors = function(node)
	{
		var children = node.childNodes;
		var i;
		var offsetLeft = calculateOffsetLeft(this.htmlElement);
		var offsetTop = calculateOffsetTop(this.htmlElement);
		
		for(i = 0; i < children.length; i++)
		{
			// move element in a "correct relative" position and set it size as fixed
			if(getStyle(children[i], "position") == 'absolute')
			{
				children[i].style.left = children[i].offsetLeft + offsetLeft + "px";
				children[i].style.top = children[i].offsetTop + offsetTop + "px";
				children[i].style.width = children[i].offsetWidth;
				children[i].style.height = children[i].offsetHeight;
			}
		
			if(isBlock(children[i]))
			{
				// block found initialize it
				var newBlock = new Block(children[i], this);
				newBlock.initBlock();
				this.blocks.push(newBlock);
			}
			else if(isConnector(children[i]))
			{
				// connector found, just create it, source or destination blocks may not 
				// have been initialized yet
				var newConnector = new Connector(children[i], this);
				this.connectors.push(newConnector);
			}
			else
			{
				// continue searching nested elements
				this.findNestedBlocksAndConnectors(children[i]);
			}
		}		
	}
	
	/*
	 * methods
	 */	
	this.print = function()
	{
		var output = '<ul><legend>canvas: ' + this.id + '</legend>';
		var i;
		for(i = 0; i < this.blocks.length; i++)
		{
			output += '<li>';
			output += this.blocks[i].print();
			output += '</li>';
		}
		output += '</ul>';
		return output;
	}
	
	/*
	 * This function searches for a nested block with a given id
	 */
	this.findBlock = function(blockId)
	{
		var result;
		var i;
		for(i = 0; i < this.blocks.length && !result; i++)
		{
			result = this.blocks[i].findBlock(blockId);
		}
		
		return result;
	}
	
	this.toString = function()
	{
		return 'canvas: ' + this.id;
	}
}

/*
 * Block class
 */
function Block(htmlElement, canvas)
{	
	/*
	 * initialization
	 */
	 
	this.canvas = canvas;
	this.htmlElement = htmlElement;
	this.id = htmlElement.id;
	this.blocks = new Array();
	this.moveListeners = new Array();	
	
	
	this.initBlock = function()
	{
		// inspect block children to identify nested blocks
		var children = this.htmlElement.childNodes;
		var i;		
		for(i = 0; i < children.length; i++)
		{
			if(isBlock(children[i]))
			{
				var innerBlock = new Block(children[i], this.canvas);
				innerBlock.initBlock();
				this.blocks.push(innerBlock);
				this.moveListeners.push(innerBlock);
			}
		}
		
		//this.htmlElement.onmousemove = new Function('if(isdrag) findBlock(\'' + this.id + '\').onMove();');
	}
	
	this.top = function()
	{
		return calculateOffsetTop(this.htmlElement);
	}
	
	this.left = function()
	{
		return calculateOffsetLeft(this.htmlElement);
	}
	
	this.width = function()
	{
		return this.htmlElement.offsetWidth;
	}
	
	this.height = function()
	{
		return this.htmlElement.offsetHeight;
	}
	
	/*
	 * methods
	 */	
	this.print = function()
	{
		var output = 'block: ' + this.id;
		if(this.blocks.length > 0)
		{
			output += '<ul>';
			var i;
			for(i = 0; i < this.blocks.length; i++)
			{
				output += '<li>';
				output += this.blocks[i].print();
				output += '</li>';
			}
			output += '</ul>';
		}
		return output;
	}
	
	/*
	 * This function searches for a nested block (or the block itself) with a given id
	 */
	this.findBlock = function(blockId)
	{
		if(this.id == blockId)
			return this;
			
		var result;
		var i;
		for(i = 0; i < this.blocks.length && !result; i++)
		{
			result = this.blocks[i].findBlock(blockId);
		}
		
		return result;
	}
	
	this.move = function(left, top)
	{		
		this.htmlElement.style.left = left;
		this.htmlElement.style.top = top;
		this.onMove();
	}
		
	this.onMove = function()
	{
		var i;
		
		// notify listeners
		for(i = 0; i < this.moveListeners.length; i++)
		{
			this.moveListeners[i].onMove();
		}
	}
	
	this.toString = function()
	{
		return 'block: ' + this.id;
	}
}

/*
 * Connector class.
 * The init function takes two Block objects as arguments representing 
 * the source and destination of the connector
 */
function Connector(htmlElement, canvas)
{
	this.htmlElement = htmlElement;
	this.canvas = canvas;
	this.source = null;
	this.destination = null;
	this.startX = null;
	this.startY = null;
	this.destX = null;
	this.destY = null;
	this.segment1 = null;
	this.segment2 = null;
	this.segment3 = null;
	this.preferredOrientation = AUTO;
	this.orientation = HORIZONTAL;
	this.size = 1;
	this.color = 'black';
	this.moveListeners = new Array();
	
	this.initConnector = function()
	{
		// detect the connector id
		if(this.htmlElement.id)
			this.id = this.htmlElement.id;
		else
			this.id = this.htmlElement.className;
			
		// split the class name to get the ids of the source and destination blocks
		var splitted = htmlElement.className.split(' ');
		if(splitted.length < 3)
		{
			alert('Unable to create connector \'' + id + '\', class is not in the correct format: connector <sourceBlockId>, <destBlockId>');
			return;
		}
		
		var connectorClass = splitted[0] + ' ' + splitted[1] + ' ' + splitted[2];
		
		this.source = this.canvas.findBlock(splitted[1]);
		if(!this.source)
		{
			alert('cannot find source block with id \'' + splitted[1] + '\'');
			return;
		}
		
		this.destination = this.canvas.findBlock(splitted[2]);
		if(!this.destination)
		{
			alert('cannot find destination block with id \'' + splitted[2] + '\'');
			return;
		}
		
		// check preferred orientation
		if(hasClass(this.htmlElement, 'vertical'))
			this.preferredOrientation = VERTICAL;
		else if(hasClass(this.htmlElement, 'horizontal'))
			this.preferredOrientation = HORIZONTAL;
		else
			this.preferredOrientation = AUTO;
		
		// build the segments
		this.segment1 = document.createElement('div');
		this.segment1.id = this.id + "_1";		
		this.canvas.htmlElement.appendChild(this.segment1);

		this.segment1.style.position = 'absolute';
		this.segment1.style.overflow = 'hidden';
		
		if(!getStyle(this.segment1, 'background-color'))
			this.segment1.style.backgroundColor = this.color;
		this.segment1.className = connectorClass;
		
		this.segment2 = document.createElement('div');
		this.segment2.id = this.id + "_2";
		this.canvas.htmlElement.appendChild(this.segment2);

		this.segment2.className = connectorClass;		
		this.segment2.style.position = 'absolute';
		this.segment2.style.overflow = 'hidden';
		
		if(!getStyle(this.segment2, 'background-color'))
			this.segment2.style.backgroundColor = this.color;
		
		this.segment3 = document.createElement('div');
		this.segment3.id = this.id + "_3";
		this.canvas.htmlElement.appendChild(this.segment3);

		this.segment3.style.position = 'absolute';
		this.segment3.style.overflow = 'hidden';
		
		if(!getStyle(this.segment3, 'background-color'))
			this.segment3.style.backgroundColor = this.color;			
		this.segment3.className = connectorClass;
		
		this.repaint();
		
		this.source.moveListeners.push(this);
		this.destination.moveListeners.push(this);
		
		// call inspectors for this connector
		var i;
		for(i = 0; i < inspectors.length; i++)
		{
			inspectors[i].inspect(this);
		}
		
		// remove old html element
		this.htmlElement.parentNode.removeChild(this.htmlElement);
	}
	
	/**
	 * Repaints the connector
	 */
	this.repaint = function()
	{
		var sourceLeft = this.source.left();
		var sourceTop = this.source.top();
		var sourceWidth = this.source.width();
		var sourceHeight = this.source.height();
		
		var destinationLeft = this.destination.left();
		var destinationTop = this.destination.top();
		var destinationWidth = this.destination.width();
		var destinationHeight = this.destination.height();
		
		if(this.preferredOrientation == HORIZONTAL)
		{
			// use horizontal orientation except if it is impossible
			if((destinationLeft - sourceLeft - sourceWidth) *
				(sourceLeft - destinationLeft - destinationWidth) > 0)
				this.orientation = VERTICAL;
			else
				this.orientation = HORIZONTAL;
		}
		else if(this.preferredOrientation == VERTICAL)
		{
			// use vertical orientation except if it is impossible
			if((destinationTop - sourceTop - sourceHeight) *
				(sourceTop - destinationTop - destinationHeight) > 0)
				this.orientation = HORIZONTAL;
			else
				this.orientation = VERTICAL;
		}
		else
		{
			// auto orientation: change current orientation if it is impossible to maintain
			if(this.orientation == HORIZONTAL &&
				(destinationLeft - sourceLeft - sourceWidth) *
				(sourceLeft - destinationLeft - destinationWidth) > 0)
			{
				this.orientation = VERTICAL;
			}
			else if(this.orientation == VERTICAL &&
				(destinationTop - sourceTop - sourceHeight) *
				(sourceTop - destinationTop - destinationHeight) > 0)
			{
				this.orientation = HORIZONTAL;
			}
		}
		
		if(this.orientation == HORIZONTAL)
		{
			// deduce which face to use on source and destination blocks
			if(sourceLeft + sourceWidth / 2 < destinationLeft + destinationWidth / 2)
			{
				// use left side of the source block and right side of the destination block
				this.startX = sourceLeft + sourceWidth;
				this.destX = destinationLeft;
			}
			else
			{
				// use right side of the source block and left side of the destination block
				this.startX = sourceLeft;
				this.destX = destinationLeft + destinationWidth;
			}

			this.startY = sourceTop + sourceHeight / 2;
			this.destY = destinationTop + destinationHeight /2;
			
			// first horizontal segment positioning
			this.segment1.style.left = Math.min(this.startX, (this.destX + this.startX) / 2) + 'px';
			this.segment1.style.top = this.startY + 'px';
			this.segment1.style.width = Math.abs((this.startX - this.destX) / 2) + this.size + 'px';
			this.segment1.style.height = this.size + 'px';
			
			// vertical segment positioning
			this.segment2.style.left = ((this.startX + this.destX) /2) + 'px';
			this.segment2.style.top = Math.min(this.startY, this.destY) + 'px';
			this.segment2.style.width = this.size + 'px';
			this.segment2.style.height = Math.abs(this.destY - this.startY) + 'px';
			
			// second horizontal segment positioning
			this.segment3.style.left = Math.min((this.startX + this.destX) /2, this.destX) + 'px';
			this.segment3.style.top = this.destY + 'px';
			this.segment3.style.width = Math.abs((this.destX - this.startX) / 2) + 'px';
			this.segment3.style.height = this.size + 'px';
			
			// label positioning
			//this.htmlElement.style.left = this.startX + 'px';
			//this.htmlElement.style.top = this.startY + this.size + 'px';
		}
		else
		{
			// deduce which face to use on source and destination blocks
			if(sourceTop + sourceHeight / 2 < destinationTop + destinationHeight / 2)
			{
				// use bottom side of the sheightblock and top side of thtopestination block
				this.startY = sourceTop + sourceHeight;
				this.destY = destinationTop;
			}
			else
			{
				// use top side of the source block and bottom side of the destination block
				this.startY = sourceTop;
				this.destY = destinationTop + destinationHeight;
			}
			
			this.startX = sourceLeft + sourceWidth / 2;
			this.destX = destinationLeft + destinationWidth / 2;
			
			// first vertical segment positioning
			this.segment1.style.left = this.startX + 'px';
			this.segment1.style.top = Math.min(this.startY, (this.destY + this.startY)/2) + 'px';
			this.segment1.style.width = this.size + 'px';
			this.segment1.style.height = Math.abs((this.startY - this.destY) / 2) + this.size + 'px';
			
			// horizontal segment positioning
			this.segment2.style.left = Math.min(this.startX, this.destX) + 'px';
			this.segment2.style.top = ((this.startY + this.destY) /2) + 'px';
			this.segment2.style.width = Math.abs(this.destX - this.startX) + 'px';
			this.segment2.style.height = this.size + 'px';
			
			// second vertical segment positioning
			this.segment3.style.left = this.destX + 'px';
			this.segment3.style.top = Math.min(this.destY, (this.destY + this.startY) / 2) + 'px';
			this.segment3.style.width = this.size + 'px';
			this.segment3.style.height = Math.abs((this.destY - this.startY) / 2) + 'px';
			
			// label positioning
			//this.htmlElement.style.left = this.startX + 'px';
			//this.htmlElement.style.top = this.startY + this.size + 'px';
		}
	}
	
	this.onMove = function()
	{
		this.repaint();
		
		// notify listeners
		var i;
		for(i = 0; i < this.moveListeners.length; i++)
			this.moveListeners[i].onMove();
	}
}

function ConnectorEnd(connector, htmlElement, segment)
{
	this.connector = connector;
	this.htmlElement = htmlElement;
	this.connector.segment1.parentNode.appendChild(htmlElement);
	// strip extension
	this.src = this.htmlElement.src.substring(0, this.htmlElement.src.lastIndexOf('.'));
	this.srcExtension = this.htmlElement.src.substring(this.htmlElement.src.lastIndexOf('.'));
	
	this.orientation;
	
	this.repaint = function()
	{
		this.htmlElement.style.position = 'absolute';
		
		var orientation;
		var left;
		var top;
		
		if(connector.orientation == HORIZONTAL)
		{
			left = segment.offsetLeft;
			orientation = "l";
			if(segment.offsetLeft == connector.segment2.offsetLeft)
			{
				left += segment.offsetWidth - this.htmlElement.offsetWidth;
				var orientation = "r";
			}
	
			top = segment.offsetTop - (this.htmlElement.offsetHeight / 2);
		}
		else
		{
			top = segment.offsetTop;
			orientation = "u";
			if(segment.offsetTop == connector.segment2.offsetTop)
			{
				top += segment.offsetHeight - this.htmlElement.offsetHeight;
				var orientation = "d";
			}
	
			left = segment.offsetLeft - (this.htmlElement.offsetWidth / 2);
		}
		
		this.htmlElement.style.left = Math.ceil(left) + "px";
		this.htmlElement.style.top = Math.ceil(top) + "px";
		
		if(this.htmlElement.tagName.toLowerCase() == "img" && this.orientation != orientation)
		{				
			this.htmlElement.src = this.src + "_" + orientation + this.srcExtension;
		}
		this.orientation = orientation;
	}
	
	this.onMove = function()
	{
		this.repaint();
	}
}

function SideConnectorLabel(connector, htmlElement, side)
{
	this.connector = connector;
	this.htmlElement = htmlElement;
	this.connector.segment1.parentNode.appendChild(htmlElement);
	if(side == 'source')
		this.segment = connector.segment1;
	else
		this.segment = connector.segment3;
	this.side = side;
		
	this.repaint = function()
	{
		this.htmlElement.style.position = 'absolute';
		
		var segmentOrientation;
		if(this.segment.offsetWidth < this.segment.offsetHeight)
			segmentOrientation = VERTICAL;
		else
			segmentOrientation = HORIZONTAL;
			
		var left = this.segment.offsetLeft;
		var top = this.segment.offsetTop;

		if(segmentOrientation == VERTICAL)
		{
			if(this.segment.offsetTop == connector.segment2.offsetTop)
			{
				// put label on the bottom of the connector (segment goes downward)
				top += this.segment.offsetHeight - this.htmlElement.offsetHeight;
			}
		}
		else
		{
			if(this.segment.offsetLeft == connector.segment2.offsetLeft)
			{
				// anchor the label on its right side to avoid overlap with the block
				left += this.segment.offsetWidth - this.htmlElement.offsetWidth;
			}
			if(this.segment.offsetTop < (this.side == 'source' ? connector.segment3.offsetTop : connector.segment1.offsetTop))
			{
				// put label over the connector rather than below
				top -= this.htmlElement.offsetHeight;
			}			
		}
		
		this.htmlElement.style.left = Math.ceil(left) + "px";
		this.htmlElement.style.top = Math.ceil(top) + "px";
	}
	
	this.onMove = function()
	{
		this.repaint();
	}
}

function MiddleConnectorLabel(connector, htmlElement)
{
	this.connector = connector;
	this.htmlElement = htmlElement;
	this.connector.segment2.parentNode.appendChild(htmlElement);
	
	this.repaint = function()
	{
		this.htmlElement.style.position = 'absolute';
		
		var segmentOrientation;
		if(connector.segment2.offsetWidth < connector.segment2.offsetHeight)
			segmentOrientation = VERTICAL;
		else
			segmentOrientation = HORIZONTAL;
			
		var left;
		var top;

		if(segmentOrientation == VERTICAL)
		{
			// put label at middle height on right side of the connector
			top = connector.segment2.offsetTop + (connector.segment2.offsetHeight - this.htmlElement.offsetHeight) / 2;
			left = connector.segment2.offsetLeft;
		}
		else
		{
			// put connector below the connector at middle widths
			top = connector.segment2.offsetTop;
			left = connector.segment2.offsetLeft + (connector.segment2.offsetWidth - this.htmlElement.offsetWidth) / 2;;
		}
		
		this.htmlElement.style.left = Math.ceil(left) + "px";
		this.htmlElement.style.top = Math.ceil(top) + "px";
	}
	
	this.onMove = function()
	{
		this.repaint();
	}
}

/*
 * Inspector classes
 */

function ConnectorEndsInspector()
{
	this.inspect = function(connector)
	{
		var children = connector.htmlElement.childNodes;
		var i;
		for(i = 0; i < children.length; i++)
		{
			if(hasClass(children[i], "connector-end"))
			{
				var newElement = new ConnectorEnd(connector, children[i], connector.segment3);
				newElement.repaint();
				connector.moveListeners.push(newElement);
			}
			else if(hasClass(children[i], "connector-start"))
			{
				var newElement = new ConnectorEnd(connector, children[i], connector.segment1);
				newElement.repaint();
				connector.moveListeners.push(newElement);
			}
		}
	}
}

function ConnectorLabelsInspector()
{
	this.inspect = function(connector)
	{
		var children = connector.htmlElement.childNodes;
		var i;
		for(i = 0; i < children.length; i++)
		{
			if(hasClass(children[i], "source-label"))
			{
				var newElement = new SideConnectorLabel(connector, children[i], "source");
				newElement.repaint();
				connector.moveListeners.push(newElement);
			}
			else if(hasClass(children[i], "middle-label"))
			{
				var newElement = new MiddleConnectorLabel(connector, children[i]);
				newElement.repaint();
				connector.moveListeners.push(newElement);
			}
			else if(hasClass(children[i], "destination-label"))
			{
				var newElement = new SideConnectorLabel(connector, children[i], "destination");
				newElement.repaint();
				connector.moveListeners.push(newElement);
			}
		}
	}
}

/*
 * Inspector registration
 */

inspectors.push(new ConnectorEndsInspector());
inspectors.push(new ConnectorLabelsInspector());

/*
 * an array containing all the canvases in document
 */
var canvases = new Array();

/*
 * This function initializes the js_graph objects inspecting the html document
 */
function initPageObjects()
{
	if(isCanvas(document.body))
	{
		var newCanvas = new Canvas(document.body);
		newCanvas.initCanvas();
		canvases.push(newCanvas);
	}
	else
	{
	
	var divs = document.getElementsByTagName('div');
	var i;
	for(i = 0; i < divs.length; i++)
	{
		if(isCanvas(divs[i]))
		{
			var newCanvas = new Canvas(divs[i]);
			newCanvas.initCanvas();
			canvases.push(newCanvas);
		}
	}
	}
}


/*
 * Utility functions
 */


function findCanvas(canvasId)
{	
	var i;
	for(i = 0; i < canvases.length; i++)
		if(canvases[i].id == canvasId)
			return canvases[i];
	return null;
}

function findBlock(blockId)
{
	var i;
	for(i = 0; i < canvases.length; i++)
	{
		var block = canvases[i].findBlock(blockId);
		if(block)
			return block;
	}
	return null;
}
 
/*
 * This function determines whether a html element is to be considered a canvas
 */
function isBlock(htmlElement)
{
	return hasClass(htmlElement, 'block');
}

/*
 * This function determines whether a html element is to be considered a block
 */
function isCanvas(htmlElement)
{
	return hasClass(htmlElement, 'canvas');
}

/*
 * This function determines whether a html element is to be considered a connector
 */
function isConnector(htmlElement)
{
	return htmlElement.className && htmlElement.className.match(new RegExp('connector .*'));
}

/*
 * This function calculates the absolute 'top' value for a html node
 */
function calculateOffsetTop(obj)
{
	var curtop = 0;
	if (obj.offsetParent)
	{
		while (obj.offsetParent)
		{
			curtop += obj.offsetTop;
			obj = obj.offsetParent;
		}
	}
	else if (obj.y)
		curtop += obj.y;
	return curtop;	
}

/*
 * This function calculates the absolute 'left' value for a html node
 */
function calculateOffsetLeft(obj)
{
	var curleft = 0;
	if (obj.offsetParent)
	{
		while (obj.offsetParent)
		{
			curleft += obj.offsetLeft;
			obj = obj.offsetParent;
		}
	}
	else if (obj.x)
		curleft += obj.x;
	return curleft;							
}


function hasClass(element, className)
{
	if(!element.className)
		return false;
		
	var classes = element.className.split(' ');
	var i;
	for(i = 0; i < classes.length; i++)
		if(classes[i] == className)
			return true;
	return false;
}

/**
 * This function retrieves the actual value of a style property even if it is set via css.
 */
function getStyle(node, styleProp)
{
	// if not an element
	if( node.nodeType != 1)
		return;
		
	var value;
	if (node.currentStyle)
	{
		// ie case
		styleProp = replaceDashWithCamelNotation(styleProp);
		value = node.currentStyle[styleProp];
	}
	else if (window.getComputedStyle)
	{
		// mozilla case
		value = document.defaultView.getComputedStyle(node, null).getPropertyValue(styleProp);
	}
	
	return value;
}

function replaceDashWithCamelNotation(value)
{
	var pos = value.indexOf('-');
	while(pos > 0 && value.length > pos + 1)
	{
		value = value.substring(0, pos) + value.substring(pos + 1, pos + 2).toUpperCase() + value.substring(pos + 2);
		pos = value.indexOf('-');
	}
	return value;
}

</script>


<style rel="stylesheet" type="text/css">
.draggable
{
	position: absolute;
	cursor: move;
}

.connector
{
	background-color: black;
}

.dock_point
{
	height: 1px;
	width: 1px;
	overflow: hidden;
	padding: 0px !important;
	border: none !important;
	margin: 0px;
	position: absolute;
	font-size: 1px;
	visibility: hidden;
}

div.block
{
	border: 2px outset #0C24FF;
	background-color: #BEFF0C;
	padding: 5px;
	font-size: 11px;
}

html
{
	padding: 0px;
	margin: 0px;
}

body
{
	font-family: verdana;
	color: #33333F;
	padding: 3px;
	margin: 0px;
	background-color: white;
}

h1
{
	color: #FF7521;
	margin: 0px;
}

h2
{
	font-size: 15px;
	margin: 0px;
}

.middle-label, .source-label, .destination-label
{
	font-size: 11px;
	font-weight: bold;
	padding: 5px;
}

div.connector
{
	background-color: #0C24FF;
	border: 0px dotted #0C24FF;
}

table.main_table 
{
	width: 100%; 
	border-collapse: separate;
}
	
td.menu
{
	padding: 5px;
}

.menu ul
{
	margin: 0px;
	padding: 0px;
	list-style-type: none;
	list-style-position: outside;
}

.menu li
{
	border: none;
	padding: 0px;
	font-size: 12px;
	margin-bottom: 3px;
}

.menu li a
{
	display: block;
	border: 1px solid #262A37;
	width: 100px;
	color: #262A37;
	text-decoration: none;
	padding: 1px;
	background-color: #E0E8FF;
}

.menu li a#active_menu
{
	color: #FF9900;
	border-color: #FF9900;
}

.menu li a:hover
{
	color: #FF9900;
	border-color: #FF9900;
}
</style>




</head>
<!--*************************!-->

<!--APERTURA DELLA STRUTTURA CANVAS!-->
<body onload="initPageObjects();">
	<table class="main_table">
		<tr>
			<td style="vertical-align: top; padding: 0px;">
			<div id="mainCanvas" class="canvas block" style="width: 100%; height: 400px; background-color: white; padding: 0px;">
<!--APERTURA DELLA STRUTTURA CANVAS!-->


	    		<#list tasks as i>
	    	 	<h1>Grafo del workflow monitorato</h1>	
			<div id="START" class="block draggable" style="left: 130px; top: 130px;">
				<h2>START</h2>
			</div>

			<div class="connector START ${i[0]}">
				<label class="middle-label"></label>
				<img class="connector-end" src="arrow.gif"/>
			</div>

			<table class="block draggable" style="left: 550px; top: 50px; border-collapse: collapse; cursor: default;" id=${i[0]} cellpadding="0" cellspacing="0">
	    			<tr>
	    			<td style="border: 1px solid #262A37; cursor: pointer;">${i[1]}: <a href=/alfresco/service/process/transitions?p=${i[0]}>${i[0]}</a> - <#if i[4]== "COMPLETED" > Completed <#else>In-Progress</#if> </td>
	    			<td style="border: 1px solid #262A37; cursor: pointer;">-><a href=/alfresco/service/api/workflow/task/end/{i[0]}>End Signal</a></td>
	    			</tr> 
	    		</table>

			<div class="connector ${i[0]} END">
				<label class="middle-label"></label>
				<img class="connector-end" src="arrow.gif"/>
			</div>


			<div id="END" class="block draggable" style="left: 1200px; top: 130px;">
				<h2>END</h2>
			</div>

	    		</#list>	






<!--CHIUSURA DELLA STRUTTURA CANVAS!-->
			</div>
			</td>
		</tr>
	</table>
	<div class="connector active_menu mainCanvas">
	</div>
<!--CHIUSURA DELLA STRUTTURA CANVAS!-->

</body></html>


