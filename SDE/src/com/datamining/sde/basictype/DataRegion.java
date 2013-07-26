package com.datamining.sde.basictype;

public class DataRegion
{
	private TagNode parent; // parent节点
	private int combinationSize; // 每个广义节点的组成节点的个数
	private int startPoint;  // 组成广义节点的标签节点的开始位置
	private int nodesCovered;  // 组成广义节点的总共标签节点的个数
	
	public DataRegion(TagNode parent, int combinationSize, int startPoint, int nodesCovered)
	{
		setParent(parent);
		setCombinationSize(combinationSize);
		setStartPoint(startPoint);
		setNodesCovered(nodesCovered);
	}

	public void setParent(TagNode parent)
	{
		this.parent = parent; 
	}

	public void setCombinationSize(int combinationSize)
	{
		this.combinationSize = combinationSize;
	}
	
	public void setStartPoint(int startPoint)
	{
		this.startPoint = startPoint;
	}
	
	public void setNodesCovered(int nodesCovered)
	{
		this.nodesCovered = nodesCovered;
	}
	
	public TagNode getParent()
	{
		return parent;
	}

	public int getCombinationSize()
	{
		return combinationSize;
	}
	
	public int getStartPoint()
	{
		return startPoint;
	}
	
	public int getNodesCovered()
	{
		return nodesCovered;
	}
}