package com.datamining.sde.treematcher;

import com.datamining.sde.basictype.TagNode;
import com.datamining.sde.basictype.TagTree;
import com.datamining.sde.basictype.TreeAlignment;

public interface TreeMatcher
{
	public double matchScore(TagNode A, TagNode B);
	public double matchScore(TagNode[] A, TagNode[] B);
	public double matchScore(TagTree A, TagTree B);
	public double normalizedMatchScore(TagNode A, TagNode B);
	public double normalizedMatchScore(TagNode[] A, TagNode[] B);
	public double normalizedMatchScore(TagTree A, TagTree B);
	public TreeAlignment align(TagNode[] A, TagNode[] B);
	public TreeAlignment align(TagNode A, TagNode B);
}