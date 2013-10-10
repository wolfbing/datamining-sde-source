package com.datamining.sde.basictype;

import java.util.ArrayList;
import java.util.List;

public class TreeAlignment
{
	// firstNode secondNode鍒嗗埆瀛樺偍鏍硅妭鐐�	//  subTreeAlignment瀛樺偍瀛愯妭鐐圭殑鍖归厤锛屽苟涓嶅瓨鍌ㄦ墍鏈夊悗浠ｈ妭鐐逛腑鐨勫尮閰嶃�
	//  姣忎釜鍖归厤閮藉彧瀛樺偍涓�骇瀛愯妭鐐圭殑鍖归厤锛岃繖鏍锋渶缁堝皢寰楀埌瀹屾暣鐨勫尮閰�	
	private double score;
	private TagNode firstNode;
	private TagNode secondNode;
	// 璁板綍鍖归厤鐨勬墍鏈夎妭鐐癸紝涓嶅彧鏄竴绾ц妭鐐癸紝杩欑偣鍙互閫氳繃add()鍑芥暟鐪嬪嚭
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