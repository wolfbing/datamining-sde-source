package com.datamining.sde.basictype;
/**
 * 数据记录类
 * @author Bing Liu
 *
 */
public class DataRecord
{
	/**
	 * 这些节点是一级子节点，因为一般数据记录是几个标签的组合，没有“单独”的根
	 * 
	 */
	private TagNode[] recordElements;
	/**
	 * 判断数据记录是否有根节点，如果只有一个recordelement，那么就把它当作根节点
	 */
	private boolean haveRoot = false;
	
	/**
	 * this method is added by wolf, 2013-7-21  23:40
	 * 获取数据记录的根节点
	 * @return
	 */
	public TagNode getRecordRoot()
	{
		if(recordElements.length==1)
		{
			return recordElements[0];
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * 以数据项对应的节点为参数的构造函数
	 * @param recordElements
	 */
	public DataRecord(TagNode[] recordElements)
	{
		this.recordElements = recordElements;
	}
	/**
	 * 获取数据项节点方法
	 * @return TagNode [] - 数据项节点数组
	 */
	public TagNode[] getRecordElements()
	{
		return recordElements;
	}
	/**
	 * 获取数据项（数据项节点）个数
	 * @return int - 数据项个数
	 */
	public int size()
	{
		int size = 0;
		
		for(TagNode recordElement: recordElements)
		{
			size += recordElement.subTreeSize();
		}
			
		return size;
	}
	/**
	 * print 输出，结果为所有数据项标签节点组成的字符串
	 */
	public String toString()
	{
		String s = "";
		
		for (TagNode recordElement: recordElements)
		{
			s += recordElement.toString() + " ";
		}

		return s;
	}
}