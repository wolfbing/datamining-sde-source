/*
 *  TagNode.java  - Kelas untuk merepresentasikan tag node pada HTML Document Object Model (DOM)
 *  Copyright (C) 2009 Sigit Dewanto
 *  sigitdewanto11@yahoo.co.uk
 *  http://sigit.web.ugm.ac.id
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.datamining.sde.basictype;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.cyberneko.html.HTMLElements;

/**
 * modified by wolf, 2013-7-21 13：00
 * 
 * 需要注意的一些地方：
 * 1. 父节点或者子节点的设置应该是同时的，当添加子节点，那么同时要设置子节点的父节点为当前节点
 *    同理，如果设置了父节点，那么就要在父节点中增加这个节点为子节点
 * 2. 避免在setParent()、addChild()或者之类含义的方法中互相调用，可能会出现循环调用
 * 3. 如果每个节点都知道自己的父节点和子节点，那么只要给定树的一个节点，就能遍历出整个树
 * 4. 父节点为null不是随便都可以的，只有根节点可以这样设置，如果一个节点这样设置，就意味着这棵子树独立出来成为一棵单独的树
 * 5. 节点的位置是易变的，所以获取level的时候，动态计算level比较好，仅代表wolf的个人观点
 * @author Sigit Dewanto
 *
 */
public class TagNode
{
	/**
	 * 父节点，每个节点只需要知道自己的父节点和子节点，就能从根节点得到整棵树
	 * 
	 */
	private TagNode parent;
	
	/**
	 * 子节点
	 * 
	 */
	private List<TagNode> children;
	/**
	 * 应该是HTML标签都有唯一id标识，这个tagElement应该就是这个标签对应的id.
	 * tagElement是做节点相似性比较的基础
	 * org.cyberneko.html.HTMLElements.
	 * 
	 */
	private short tagElement;
	
	/**
	 * Isi teks yang dimiliki oleh TagNode ini.
	 */
	private String innerText = null;
	// added by wolf,2013-7-21 20:29
	//  子树被折叠的数据记录,
	private List<List<String> > nestedRecords = null;
	private Map<DataRecord, Map<TagNode, TagNode>> mapping;
	private List<DataRecord> dataRecords;
	private boolean isNested = false;
	
	/**
	 * Nomor TagNode ini dihitung dari root secara preorder.
	 */
	private int tagNodeNumber;
	
	/**
	 * 节点在树的第几次，根节点是第0层
	 * 
	 */
	private int level = 0;
	
	/**
	 * 节点作为父节点的子节点之一的编号
	 * 
	 */
	private int childNumber;

	/**
	 * 无参数构造函数
	 *
	 */
	public TagNode()
	{
		children = new ArrayList<TagNode>();
	}

	/**
	 * modified by wolf, 2013-7-21  13:41
	 * 
	 * 增加节点，新增的节点的编号也要更新
	 * @param newChild
	 */
	public void addChild(TagNode newChild)
	{
		// 为什么没有调整新增节点的父节点？？？ 不也要更新level吗？？  2013-7-11 13：00  by  wolf
		// 后来我发现原来是因为 setparent()函数中会调用addChild(),这样就形成了调用循环   2013-7-21 13：21  by wolf
		//newChild.setParent(this);  // modified by Wolf 2013-7-21
		children.add(newChild);
		newChild.justSetParent(this);
		newChild.setChildNumber(children.size());
	}
	
	public void setNestedRecords(List<DataRecord> records,Map<DataRecord, Map<TagNode, TagNode>> map)
	{
		this.dataRecords = records;
		this.mapping = map;
	}
	
	/**
	 * 节点的编号，不是数量
	 *  
	 * @param childNumber
	 */
	private void setChildNumber(int childNumber)
	{
		this.childNumber = childNumber;
	}
	
	/**
	 * 插入一些节点，关键是节点的编号需要更新了 
	 * @param pos
	 * @param insertNodes
	 */
	public void insertChildNodes(int pos, List<TagNode> insertNodes)
	{
		for (TagNode insertNode: insertNodes)
		{
			// 这一步在增加节点函数里没有，我觉得应该都有
			// 有个疑问，parent是私有属性，为什么能直接访问？？
			//  测试了一下，果然可以访问私有。
			//insertNode.parent = this; 
			insertNode.justSetParent(this);  // 我还是不知道为什么可以访问私有，所以又写了一个放心的方法。  modified by wolf,
												//	2013-7-21  14:06
			//insertNode.setParent(insertNode);  // 如果这里调用setParent()就陷入了调用循环 , modified by wolf, 2013-7-21
			insertNode.setLevel();
		}

		children.addAll(pos-1, insertNodes);
		
		for (int childListCounter=pos-1; childListCounter < children.size(); childListCounter++)
		{
			// 更新节点的编号
			children.get( childListCounter ).setChildNumber( childListCounter+1 );
		}
	}

	/**
	 * Menghitung tingkat kedalaman (level) dari TagNode ini pada TagTree dan menyimpannya dalam 
	 * field level.
	 */
	private void setLevel()
	{
		level = countLevel();
	}

	/**
	 * 计算节点本身在它所在的树的第几层。根节点是第0层
	 * 
	 * @return tingkat kedalaman (level) dari TagNode ini pada TagTree
	 */
	private int countLevel()
	{
		if (parent == null)
		{
			return 0;
		}
		else
		{
			return 1 + parent.countLevel();
		}
	}
	
	/**
	 * 计算子树最深的是在第多少层
	 * 
	 * @return level terdalam dari descendant TagNode ini
	 */
	private int countSubTreeDepth()
	{
		if (children.isEmpty())
		{
			return level; 
		}
		else
		{
			int subLevel = level + 1;
			int currentSubLevel = subLevel;
			
			for (TagNode child: children)
			{
				currentSubLevel = child.countSubTreeDepth();
				
				if (currentSubLevel > subLevel)
				{
					subLevel = currentSubLevel;
				}
			}
			
			return subLevel;
		}
	}
	/**
	 * 获取以当前节点为根节点的子树的深度
	 * @return
	 */
	public int getDepth()
	{
		if(children.isEmpty())
		{
			return 1;
		}
		else
		{
			int currentDepth = 1;
			for(TagNode child: children)
			{
				if(child.getDepth()>currentDepth)
				{
					currentDepth = child.getDepth();
				}
			}
			return 1+currentDepth;
		}
	}

	/**
	 * 设置本节点的父节点，同时父节点增加了一个子节点
	 * 
	 * @param parent Parent
	 */
	public void setParent(TagNode parent)
	{
		if (parent != null)
		{
			this.parent = parent;
			parent.addChild(this);
			setLevel();
		}
	}
	/**
	 * this method is added by wolf, 2013-7-21 13:28
	 * 
	 * 仅仅设置父节点，其他什么都不管；
	 * 这个函数的出现是为了弥补addChild()中子节点无法调用setParent()导致成了无父节点的问题
	 * 
	 * @param parent
	 */
	private void justSetParent(TagNode parent)
	{
		if(parent!=null)
		{
			this.parent = parent;
			
		}
	}

	/**
	 * Mengeset elemen tag yang dimiliki oleh TagNode ini. Merujuk pada daftar di org.cyberneko.html.HTMLElements.
	 * 
	 * @param tagElement elemen tag yang dimiliki oleh TagNode ini. Merujuk pada daftar di org.cyberneko.html.HTMLElements.
	 */
	public void setTagElement(short tagElement)
	{
		this.tagElement = tagElement;
	}
	
	/**
	 * Mengeset isi teks yang dimiliki TagNode ini.
	 * 
	 * @param innerText isi teks yang dimiliki TagNode ini
	 */
	public void setInnerText(String innerText)
	{
		this.innerText = innerText;
	}
	
	/**
	 * Mengeset nomor TagNode ini pada TagTree secara preorder. Nilainya diiisi menggunakan method 
	 * assignNodeNumber() pada TagTree.
	 * 
	 * @param tagNodeNumber nomor TagNode ini dalam TagTree secara preorder.
	 */
	public void setTagNodeNumber(int tagNodeNumber)
	{
		this.tagNodeNumber = tagNodeNumber;
	}
	
	/**
	 * Menggabungkan text dengan innerText yang lama ke dalam innerText yang baru.
	 * 
	 * @param text
	 */
	public void appendInnerText(String text)
	{
		if ( innerText == null )
		{
			innerText = text;
		}
		else
		{
			innerText += text;
		}
	}
	
	/**
	 * Mendapatkan referansi ke Parent dari TagNode ini. Jika tidak memiliki Parent kembaliannya 
	 * adalah null.
	 * 
	 * @return referensi ke Parent dari TagNode ini. null jika TagNode ini tidak memiliki Parent.
	 */
	public TagNode getParent()
	{
		return parent;
	}

	/**
	 * Mendapatkan referensi ke sibling sebelumnya dari TagNode ini. Jika TagNode ini merupakan child 
	 * pertama, maka kembaliannya null.
	 * 
	 * @return referensi ke sibling sebelumnya dari TagNode ini. null jika TagNode ini 
	 * merupakan child pertama.
	 */
	public TagNode getPrevSibling()
	{
		if ( childNumber > 1 && parent != null )
		{
			return parent.getChildAtNumber(childNumber-1);
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Mendapatkan referensi ke sibling berikutnya dari TagNode ini. Jika TagNode ini merupakan child 
	 * terakhir, maka kembaliannya null.
	 * 
	 * @return referensi ke sibling berikutnya dari TagNode ini. null jika TagNode ini 
	 * merupakan child terakhir.
	 */
	public TagNode getNextSibling()
	{
		if ( childNumber < parent.childrenCount() && parent != null )
		{
			return parent.getChildAtNumber(childNumber+1);
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Mendapatkan List dari children yang dimiliki oleh TagNode ini. Mengembalikan List kosong jika tidak memiliki child.
	 * 
	 * @return List dari children yang dimiliki oleh TagNode ini
	 */
	public List<TagNode> getChildren()
	{
		return children;
	}

	/**
	 * Mendapatkan child pertama yang dimiliki oleh TagNode ini, Mengembalikan null jika TagNode ini tidak memiliki child.
	 * 
	 * @return child pertama yang dimiliki oleh TagNode ini
	 */
	public TagNode getFirstChild()
	{
		if (children.isEmpty())
		{
			return null;
		}
		else
		{
			return children.get(0);
		}
	}
	
	/**
	 * Mendapatkan child terakhir yang dimiliki oleh TagNode ini, Mengembalikan null jika TagNode ini tidak memiliki child.
	 * 
	 * @return child terakhir yang dimiliki oleh TagNode ini
	 */
	public TagNode getLastChild()
	{
		if (children.isEmpty())
		{
			return null;
		}
		else
		{
			return children.get(children.size()-1);
		}
	}
	
	/**
	 * 根据子节点的编号获取子节点，编号从1开始，size结束
	 * 
	 * @param childNumber nomor child pada TagNode ini
	 * @return referensi ke child dengan nomor childNumber pada TagNode ini
	 */
	public TagNode getChildAtNumber(int childNumber)
	{
		if (children.isEmpty() || childNumber < 1 || childNumber > children.size())
		{
			return null;
		}
		else
		{
			return children.get(childNumber-1);
		}
	}

	/**
	 * Mengembalikan kode elemen HTML yang dimiliki oleh TagNode ini. Merujuk pada daftar di 
	 * org.cyberneko.html.HTMLElements.
	 * 
	 * @return kode elemen HTML yang dimiliki oleh TagNode ini
	 */
	public short getTagElement()
	{
		return tagElement;
	}
	
	/**
	 * Mendapatkan isi teks yang dimiliki TagNode ini. Mengembalikan null jika TagNode ini tidak memiliki isi teks. Mengembalikan String 
	 * kosong jika TagNode ini memiliki isi teks yang berupa String kosong.
	 * 
	 * @return isi teks yang dimiliki TagNode ini
	 */
	public String getInnerText()
	{
		return innerText;
	}

	/**
	 * Mendapatkan nomor TagNode ini pada TagTree secara preorder.
	 * 
	 * @return nomor TagNode ini pada TagTree secara preorder
	 */
	public int getTagNodeNumber()
	{
		return tagNodeNumber;
	}
	
	/**
	 * 获取节点在树的第几层，根节点是第0层
	 * 
	 * @return tingkat kedalaman (level) TagNode ini diukur dari root
	 */
	public int getLevel()
	{
		// 为什么要这样获取呢，level会随着节点位置的改变而改变，随时都可能变，为什么不动态计算呢
		// 当然，如果频繁获取就会增加计算量，但是一般不会频繁获取，倒是位置的改变更频繁
		//return level;
		return countLevel();
	}
	
	/**
	 * Mengembalikan nomor yang menunjukkan TagNode ini merupakan child keberapa dari Parent-nya. 
	 * Penomoran mulai dari 1 sampai N, dengan N merupakan jumlah child yang dimiliki oleh Parent.
	 * 
	 * @return nomor yang menunjukkan TagNode ini merupakan child keberapa dari Parent-nya
	 */
	public int getChildNumber()
	{
		return childNumber;
	}
	
	/**
	 * 子节点的个数
	 * 
	 * @return jumlah child yang dimiliki oleh TagNode ini
	 */
	public int childrenCount()
	{
		return children.size();
	}
	/**
	 * 获取以当前节点为根的树的节点个数，不包括parent节点。
	 * @return - 节点个数
	 */
	public int subTreeSize()
	{
		int size = 0;
		
		for (TagNode child: children)
		{
			size += child.subTreeSize();
		}
		
		return size + 1;
	}

	/**
	 * Mendapatkan tingkat kedalaman maksimum subtree dengan TagNode ini sebagai root. Mengembalikan 0 jika TagNode ini tidak memiliki child.
	 * 
	 * @return tingkat kedalaman maksimum subtree dengan TagNode ini sebagai root
	 */
	public int subTreeDepth()
	{
		return countSubTreeDepth() - level;
	}
	
	public void removeChild(TagNode node)
	{
		children.remove(node);
		node.setParent(null);
	}
	
	
	/**
	 * Mengembalikan nama elemen TagNode ini. Menggunakan rujukan pada kelas 
	 * org.cyberneko.HTMLElements.
	 * 
	 * @return nama elemen TagNode ini
	 */
	public String toString()
	{
		return HTMLElements.getElement(tagElement).name;
	}
	
	private String tagName;
	
	public void setTagName(String name)
	{
		this.tagName = name;
	}
	
	public String getTagName()
	{
		return this.tagName;
	}
	
	public TagNode(String name)
	{
		this.tagName = name;
		children = new ArrayList<TagNode>();
	}
	
	public TagNode(String name, short type)
	{
		this.tagName = name;
		this.tagElement = type;
		children = new ArrayList<TagNode>();
	}
	
}