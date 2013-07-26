package com.datamining.sde.basictype;

import java.util.ArrayList;
import java.util.List;

public class TreeAlignment
{
	// firstNode secondNode分别存储根节点
	//  subTreeAlignment存储子节点的匹配，并不存储所有后代节点中的匹配。
	//  每个匹配都只存储一级子节点的匹配，这样最终将得到完整的匹配
	private double score;
	private TagNode firstNode;
	private TagNode secondNode;
	// 记录匹配的所有节点，不只是一级节点，这点可以通过add()函数看出
	private List<TreeAlignment> subTreeAlignment = new ArrayList<TreeAlignment>();
	
	public TreeAlignment()
	{
		
	}

	public TreeAlignment(double score, TagNode firstNode, TagNode secondNode)
	{
		setScore( score );
		setFirstNode( firstNode );
		setSecondNode( secondNode );
	}
	
	public TreeAlignment(TagNode firstNode, TagNode secondNode)
	{
		setFirstNode( firstNode );
		setSecondNode( secondNode );
	}

	public void setScore(double score)
	{
		this.score = score;
	}
	
	public void setFirstNode(TagNode firstNode)
	{
		this.firstNode = firstNode;
	}
	
	public void setSecondNode(TagNode secondNode)
	{
		this.secondNode = secondNode;
	}
	
	public void add(TreeAlignment alignment)
	{
		subTreeAlignment.add( alignment );

		if ( alignment.getSubTreeAlignment().size() != 0)
		{
			subTreeAlignment.addAll( alignment.getSubTreeAlignment() );
		}
	}

	public void addSubTreeAlignment(List<TreeAlignment> listAlignment)
	{
		subTreeAlignment.addAll( listAlignment );
	}
	
	public double getScore()
	{
		return score;
	}
	
	public TagNode getFirstNode()
	{
		return firstNode;
	}
	
	public TagNode getSecondNode()
	{
		return secondNode;
	}
	
	public List<TreeAlignment> getSubTreeAlignment()
	{
		return subTreeAlignment;
	}
}