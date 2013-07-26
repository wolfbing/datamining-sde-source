package com.datamining.sde.test;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.List;
import java.util.ArrayList;

import org.xml.sax.SAXException;

import com.datamining.sde.basictype.DataRecord;
import com.datamining.sde.basictype.DataRegion;
import com.datamining.sde.basictype.TagNode;
import com.datamining.sde.basictype.TagTree;
import com.datamining.sde.columnaligner.ColumnAligner;
import com.datamining.sde.columnaligner.PartialTreeAligner;
import com.datamining.sde.datarecordsfinder.DataRecordsFinder;
import com.datamining.sde.datarecordsfinder.MiningDataRecords;
import com.datamining.sde.dataregionsfinder.DataRegionsFinder;
import com.datamining.sde.dataregionsfinder.MiningDataRegions;
import com.datamining.sde.tagtreebuilder.DOMParserTagTreeBuilder;
import com.datamining.sde.tagtreebuilder.TagTreeBuilder;
import com.datamining.sde.treematcher.EnhancedSimpleTreeMatching;
import com.datamining.sde.treematcher.SimpleTreeMatching;
import com.datamining.sde.treematcher.TreeMatcher;

public class MDRMainTest {
	
	public static Formatter output;
	
	public static void main(String args[])
	{
		String input="http://www.baidu.com/s?wd=oxpath&rsv_bp=0&ch=&tn=baidu&bar=&rsv_spt=3&ie=utf-8&rsv_sug3=2&rsv_sug1=2&rsv_sug4=143&inputT=2706";
		String resultOutput="MDR.html";
		double similarity=0.80;
		boolean ignoreFormattingTags=false;
		boolean useContentSimilarity=false;
		int maxNodeInGeneralizedNodes = 9;
		
		try
		{
			output=new Formatter(resultOutput);
			TagTreeBuilder builder=new DOMParserTagTreeBuilder();
			TagTree tagTree=builder.buildTagTree(input, ignoreFormattingTags );
			for(int i=0;i<tagTree.depth();i++)
			{
				System.out.println("lever:"+i+tagTree.getTagNodesAtLevel(i));
			}
			System.out.println(tagTree.getTagNodesAtLevel(3));
			System.out.println(tagTree);
			
			TreeMatcher matcher=new SimpleTreeMatching();
			DataRegionsFinder dataRegionFinder=new MiningDataRegions(matcher);
			List<DataRegion> dataRegions=dataRegionFinder.findDataRegions(tagTree.getRoot(), maxNodeInGeneralizedNodes, similarity);
			for(int i=0;i<dataRegions.size();i++)
			{
				System.out.println(dataRegions.listIterator().toString());
				//DataRegion dr=dataRegions.listIterator().toString()
				//System.out.println(dr.toString());
			}
			
			DataRecordsFinder dataRecordsFinder = new MiningDataRecords( matcher );
			DataRecord[][] dataRecords = new DataRecord[ dataRegions.size() ][];
			
			for( int dataRecordArrayCounter = 0; dataRecordArrayCounter < dataRegions.size(); dataRecordArrayCounter++)
			{
				DataRegion dataRegion = dataRegions.get( dataRecordArrayCounter );
				dataRecords[ dataRecordArrayCounter ] = dataRecordsFinder.findDataRecords(dataRegion, similarity);
			}
			
			for(int i=0;i<dataRegions.size();i++)
				for(int j=0;j<dataRecords[i].length;j++)
					System.out.println(dataRecords[i][j]);
			
			ColumnAligner aligner = null;
			if ( useContentSimilarity )
			{
				aligner = new PartialTreeAligner( new EnhancedSimpleTreeMatching() );
			}
			else
			{
				aligner = new PartialTreeAligner( matcher );
			}
			List<String[][]> dataTables = new ArrayList<String[][]>();
			
			for(int tableCounter=0; tableCounter< dataRecords.length; tableCounter++)
			{
				String[][] dataTable = aligner.alignDataRecords( dataRecords[tableCounter] );

				if ( dataTable != null )
				{
					dataTables.add( dataTable );
				}
			}
			
			int recordsFound = 0;

			for ( String[][] dataTable: dataTables )
			{
				recordsFound += dataTable.length;
			}	
			
			output.format("<html><head><title>Extraction Result</title>");
			output.format("<style type=\"text/css\">table {border-collapse: collapse;} td {padding: 5px} table, th, td { border: 3px solid black;} </style>");
			output.format("</head><body>");
			int tableCounter = 1;
			
			for ( String[][] table: dataTables)
			{
				output.format("<h2>Table %s</h2>\n<table>\n<thead>\n<tr>\n<th>Row Number</th>\n", tableCounter);
				for(int columnCounter=1; columnCounter<=table[0].length; columnCounter++)
				{
					output.format("<th></th>\n");
				}
				output.format("</tr>\n</thead>\n<tbody>\n");
				int rowCounter = 1;
				for (String[] row: table)
				{
					output.format("<tr>\n<td>%s</td>", rowCounter);
					int columnCounter = 1;
					for(String item: row)
					{
						String itemText = item;
						if (itemText == null)
						{
							itemText = "";
						}
						output.format("<td>%s</td>\n", itemText);
						columnCounter++;
					}
					output.format("</tr>\n");
					rowCounter++;
				}
				output.format("</tbody>\n</table>\n");
				tableCounter++;
			}
			output.format("</body></html>");
		}
		catch (SecurityException exception)
		{
			exception.printStackTrace();
			System.exit(1);
		}
		catch(FileNotFoundException exception)
		{
			exception.printStackTrace();
			System.exit(2);
		}
		catch(IOException exception)
		{
			exception.printStackTrace();
			System.exit(3);
		}
		catch(SAXException exception)
		{
			exception.printStackTrace();
			System.exit(4);
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
			System.exit(5);
		}
		finally
		{
			if ( output != null )
			{
				output.close();
			}
		}				
	}	
	
	@Override
	public String toString() {
		return "MDRMainTest []";
	}

	public static void printHTML(TagNode tagNode)
	{
		if (tagNode.getInnerText() == null)
		{
			output.format("<%s>", tagNode);
		}
		else
		{
			output.format("<%s>%s", tagNode, tagNode.getInnerText());
		}

		for ( TagNode child: tagNode.getChildren() )
		{
			printHTML(child);
			child = child.getNextSibling();
		}

		output.format("</%s>\n", tagNode);
	}
	
	public static void printTree(TagNode tagNode, String indent)
	{
		output.format("%s%s<br />", indent, tagNode.toString());
		output.format("%s%s<br />", indent, tagNode.getInnerText());

		for (TagNode child: tagNode.getChildren() )
		{
			printTree( child, "&nbsp;#&nbsp;"+indent);
		}
	}

}
