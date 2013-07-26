package com.datamining.sde.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import com.datamining.sde.application.ShowTagTree;
import com.datamining.sde.basictype.DataRecord;
import com.datamining.sde.basictype.TagNode;
import com.datamining.sde.basictype.TagTree;
import com.datamining.sde.columnaligner.PartialTreeAligner;
import com.datamining.sde.tagtreebuilder.DOMParserTagTreeBuilder;
import com.datamining.sde.tagtreebuilder.TagTreeBuilder;
import com.datamining.sde.treematcher.SimpleTreeMatching;


public class Net {

	public Net() {
		// TODO Auto-generated constructor stub
	}
	
	public static void traverseAndMatch(TagNode node, double threshold)
	{
		Formatter tmpOut = new Formatter(System.out);
		//tmpOut.format("depth:%d \n", node.getDepth());
		//ShowTagTree.printTreeByTagName(tmpOut, node, " ");
		if(node.getDepth() >= 3)
		{
			for(TagNode child: node.getChildren() )
			{
				traverseAndMatch(child,threshold);
			}
			match(node, threshold);
		}
	}
	
	public static void match(TagNode node, double threshold)
	{
		Formatter out = new Formatter(System.out);
		//out.format("%s is matching .. ", node.getTagName());
		//ShowTagTree.printTreeByTagName(out, node, " ");
		List<TagNode> children = node.getChildren();
		List<TagNode> copyChildren = copyTagNodes(children);
		Map<TagNode,TagNode> map = new HashMap<TagNode,TagNode>();
		for(int i=0;i<copyChildren.size();++i)
		{
			map.put(copyChildren.get(i), children.get(i));
		}
		while( !copyChildren.isEmpty() )
		{
			TagNode firstChild = copyChildren.get(0);
			copyChildren.remove(firstChild);
			List<TagNode> similarNode = new ArrayList<TagNode>();
			similarNode.add(firstChild);
			//out.format("children number:%d ", children.size());
			for(int i=0;i<copyChildren.size();++i)
			{
				TagNode childR = copyChildren.get(i);
				if( new SimpleTreeMatching().normalizedMatchScore(firstChild, childR) > threshold )
				{
					//AlignAndLink();
					similarNode.add(childR);
					copyChildren.remove(childR);
					//node.removeChild(childR);
				}
			}
			if(similarNode.size()>1)
			{
				List<TagNode> originalSimilarNodes = new ArrayList<TagNode>();
				for(TagNode n: similarNode)
				{
					originalSimilarNodes.add(map.get(n));
					node.removeChild(map.get(n));
				}
				
				DataRecord seedDataRecord = new DataRecord(new TagNode[]{});
				List<DataRecord> dataRecords = new ArrayList<DataRecord>();
				Map<DataRecord, Map<TagNode, TagNode>> mapping = new HashMap<DataRecord, Map<TagNode, TagNode>>();
				new PartialTreeAligner(new SimpleTreeMatching()).getFinalSeedNode(originalSimilarNodes, seedDataRecord,
						dataRecords, mapping);
				TagNode root = seedDataRecord.getRecordRoot();
				if(root != null)
				{
					root.setNestedRecords(dataRecords, mapping);
					node.addChild(root);
				}
				
				
			}
			
			
			
		}
	}
	
	private static void copyTagNode(TagNode childOriginal, TagNode parentCopy)
	{
		TagNode tagNode = new TagNode();
		tagNode.setParent( parentCopy );
		tagNode.setTagElement( childOriginal.getTagElement() );
		tagNode.setInnerText( childOriginal.getInnerText() );
		
		for (TagNode child: childOriginal.getChildren() )
		{
			copyTagNode(child, tagNode);
		}
	}
	
	private static List<TagNode> copyTagNodes(List<TagNode> nodes)
	{
		List<TagNode> copyNodes = new ArrayList<TagNode>();
		for(TagNode node: nodes)
		{
			TagNode tmpNode = new TagNode();
			copyTagNode(node,tmpNode);
			copyNodes.add(tmpNode.getFirstChild());
		}
		return copyNodes;
	}
	
	
	
	
	public void AlignAndLink()
	{
		
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		TagNode node1 = new TagNode("N1",(short) 1);
//		TagNode node2 = new TagNode("N2",(short) 1);
//		TagNode node3 = new TagNode("N3",(short) 1);
//		TagNode node4 = new TagNode("N4",(short) 1);
//		TagNode node5 = new TagNode("N5",(short) 1);
//		TagNode node6 = new TagNode("N6",(short) 1);
//		TagNode t1 = new TagNode("t1",(short) 2);
//		TagNode t2 = new TagNode("t2",(short) 3);
//		TagNode t3 = new TagNode("t3",(short) 4);
//		TagNode t4 = new TagNode("t4",(short) 3);
//		TagNode t5 = new TagNode("t5",(short) 4);
//		TagNode t6 = new TagNode("t6",(short) 5);
//		TagNode t7 = new TagNode("t7",(short) 2);
//		TagNode t8 = new TagNode("t8",(short) 3);
//		TagNode t9 = new TagNode("t9",(short) 4);
//		
//		node1.addChild(node2);
//		node1.addChild(node3);
//		
//		node2.addChild(t1);
//		node2.addChild(node4);
//		node2.addChild(node5);
//		
//		node4.addChild(t2);
//		node4.addChild(t3);
//		
//		node5.addChild(t4);
//		node5.addChild(t5);
//		node5.addChild(t6);
//		
//		node3.addChild(t7);
//		node3.addChild(node6);
//		
//		node6.addChild(t8);
//		node6.addChild(t9);
//		
//		Net.traverseAndMatch(node1, 0.8);
		String input="file:///C:/Users/Admin/Desktop/test.html";
		boolean ignoreFormattingTags = false;
		TagTreeBuilder builder = new DOMParserTagTreeBuilder();
		TagTree tagTree;
		try {
			
			tagTree = builder.buildTagTree(input, ignoreFormattingTags);
			TagNode root = tagTree.getRoot();
			ShowTagTree.printTreeByElement(new Formatter(System.out), root, " ");
			Net.traverseAndMatch(root, 0.8);
			System.out.println("-------------------");
			ShowTagTree.printTreeByElement(new Formatter(System.out), root, " ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		//System.out.println(node1.getDepth());

		
		
	}

}
