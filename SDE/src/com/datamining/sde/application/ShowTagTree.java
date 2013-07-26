package com.datamining.sde.application;

import java.util.Formatter;

import com.datamining.sde.basictype.TagNode;


public class ShowTagTree {

	public ShowTagTree() {
		// TODO Auto-generated constructor stub
	}
	
	
	public static void printTreeToHTML(Formatter output, TagNode tagNode, String indent)
	{
		output.format("%s%s  (%s)<br/>", indent, tagNode.toString(), tagNode.getInnerText());
		//output.format("%s%s<br />", indent, tagNode.getInnerText());

		
		for (TagNode child: tagNode.getChildren() )
		{
			printTree(output, child, "&nbsp;——&nbsp;"+indent);
		}
	}
	
	public static void printTree(Formatter output, TagNode tagNode, String indent)
	{
		
		output.format("%s%s  (%s)<br/>", indent, tagNode.toString(), tagNode.getInnerText());
		//output.format("%s%s<br />", indent, tagNode.getInnerText());

		
		for (TagNode child: tagNode.getChildren() )
		{
			printTree(output, child, "&nbsp;——&nbsp;"+indent);
		}
	}
	
	public static void printTreeByTagName(Formatter output, TagNode tagNode, String indent)
	{
		
		output.format("%s%s  (%s)\n", indent, tagNode.getTagName(), tagNode.getInnerText());
		//output.format("%s%s<br />", indent, tagNode.getInnerText());

		
		for (TagNode child: tagNode.getChildren() )
		{
			printTreeByTagName(output, child, " —— "+indent);
		}
	}
	
	public static void printTreeByElement(Formatter output, TagNode tagNode, String indent)
   {
		
		output.format("%s%s  (%s)\n", indent, tagNode.toString(), tagNode.getInnerText().replace("\n", ""));
		//output.format("%s%s<br />", indent, tagNode.getInnerText());

		
		for (TagNode child: tagNode.getChildren() )
		{
			printTreeByElement(output, child, " —— "+indent);
		}
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
