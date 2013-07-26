package com.datamining.sde.basictype;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.List;
import java.util.ArrayList;


import org.xml.sax.SAXException;

import com.datamining.sde.basictype.DataRecord;
import com.datamining.sde.basictype.DataRegion;
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
import com.sun.corba.se.spi.orb.DataCollector;

import sun.misc.JavaAWTAccess;

import java.net.URLEncoder;

import javax.swing.JApplet;

/**
 * Aplikasi utama yang berbasis konsol.
 * 
 * @author seagate
 *
 */
public class AppConsole
{
	/*
	 * Formatter untuk menulis ke file output
	 */
	public static Formatter output;

	/**
	 * Method main untuk aplikasi utama yang berbasis konsol. Ada empat argumen yang bisa diberikan, 
	 * urutannya URI file input, URI file output, similarity treshold, jumlah node maksimum dalam generalized node.
	 *  
	 * @param args Parameter yang dimasukkan pengguna aplikasi konsol
	 */
	public static void main(String args[])
	{
		// parameter default
		String input = "";
		String resultOutput = "MDR.html";
		double similarityTreshold = 0.80;
		boolean ignoreFormattingTags = false;
		boolean useContentSimilarity = false;
		int maxNodeInGeneralizedNodes = 9;
		
		// parameter dari pengguna, urutannya URI file input, URI file output, similarity treshold, jumlah node maksimum dalam generalized node
		// parameter yang wajib adalah parameter URI file input
		switch (args.length)
		{
			case 0:
				input="file:///D:/SDE%E6%B5%8B%E8%AF%95%E7%AB%99%E7%82%B9/Mockup/OceanSoft/GetFile_GetFileMain.htm";	
				break;
			case 1:
				input = args[0];
				break;
			case 2:
				input = args[0];
				resultOutput = args[1];
				break;
			case 3:
				input = args[0];
				resultOutput = args[1];
				similarityTreshold = Double.parseDouble( args[2] );
				break;
			case 4:
				input = args[0];
				resultOutput = args[1];
				similarityTreshold = Double.parseDouble( args[2] );
				ignoreFormattingTags = Boolean.parseBoolean( args[3] );
				break;
			case 5:
				input = args[0];
				resultOutput = args[1];
				similarityTreshold = Double.parseDouble( args[2] );
				ignoreFormattingTags = Boolean.parseBoolean( args[3] );
				useContentSimilarity = Boolean.parseBoolean( args[4] );
				break;
			case 6:
				input = args[0];
				resultOutput = args[1];
				similarityTreshold = Double.parseDouble( args[2] );
				ignoreFormattingTags = Boolean.parseBoolean( args[3] );
				useContentSimilarity = Boolean.parseBoolean( args[4] );
				maxNodeInGeneralizedNodes = Integer.parseInt( args[5] );
				break;
		}

		try
		{
			// siapkan file output
			output = new Formatter(resultOutput);
			// buat objek TagTreeBuilder yang berbasis parser DOM
			TagTreeBuilder builder = new DOMParserTagTreeBuilder();
			// bangun pohon tag dari file input menggunakan objek TagTreeBuilder yang telah dibuat
			TagTree tagTree = builder.buildTagTree(input, ignoreFormattingTags);
			printTree(tagTree.getRoot(),"$");
			printHTML(tagTree.getRoot());
			//print(A.getRoot," ")
			//printHTML( tagTree.getRoot());
			// buat objek TreeMatcher yang menggunakan algoritma simple tree matching
			TreeMatcher matcher = new SimpleTreeMatching();
			// buat objek DataRegionsFinder yang menggunakan algoritma mining data regions dan
			// menggunakan algoritma pencocokan pohon yang telah dibuat sebelumnya
			DataRegionsFinder dataRegionsFinder = new MiningDataRegions( matcher );
			// cari data region pada pohon tag menggunakan objek DataRegionsFinder yang telah dibuat
			List<DataRegion> dataRegions = dataRegionsFinder.findDataRegions(tagTree.getRoot(), maxNodeInGeneralizedNodes, similarityTreshold);
			
			//输出Data Region
			for(int i=0;i<dataRegions.size();i++)
			{
				output.format("DataRegion %s",i);
				output.format("<br>");
				printTree(dataRegions.get(i).getParent(),"@");
				output.format("<br>");
			}
			
			// buat objek DataRecordsFinder yang menggunakan metode mining data records dan
			// menggunakan algoritma pencocokan pohon yang telah dibuat sebelumnya
			DataRecordsFinder dataRecordsFinder = new MiningDataRecords( matcher );
			// buat matriks DataRecord untuk menyimpan data record yang teridentifikasi oleh 
			// DataRecordsFinder dari List<DataRegion> yang ditemukan
			DataRecord[][] dataRecords = new DataRecord[ dataRegions.size() ][];
			
			// identifikasi data records untuk tiap2 data region 
			for( int dataRecordArrayCounter = 0; dataRecordArrayCounter < dataRegions.size(); dataRecordArrayCounter++)
			{
				DataRegion dataRegion = dataRegions.get( dataRecordArrayCounter );
				dataRecords[ dataRecordArrayCounter ] = dataRecordsFinder.findDataRecords(dataRegion,similarityTreshold );
				
				//输出Data Record
				System.out.println("Data Record:"+dataRecordArrayCounter);
				for(int j=0;j<dataRecords[dataRecordArrayCounter].length;j++)
				{
					System.out.println("line:"+j+" "+dataRecords[dataRecordArrayCounter][j].toString());
				}
			}
			
			//实现Data Record一行一记录
			//DataRecord[][] dataRecordsAfterCutLine = new DataRecord[ dataRegions.size() ][];
			for(int dataRecordArrayCounter=0;dataRecordArrayCounter<dataRecords.length;dataRecordArrayCounter++)
			{
				int schemaCounter;
				String []tagsSchema=new String[20];
				
				TagNode []firstRecordTags=dataRecords[dataRecordArrayCounter][0].getRecordElements();
				String firstRecordTagsString=dataRecords[dataRecordArrayCounter][0].toString().trim();
				String strFirstRecordTagsString=firstRecordTagsString.replaceAll(" ", "");
				if(firstRecordTags.length<2)
				{
					//dataRecordsAfterCutLine[dataRecordArrayCounter]=dataRecords[dataRecordArrayCounter];
					continue;
				}
				else
				{
					System.out.println("Data Record:"+dataRecordArrayCounter);
					String tempTags;
					tempTags=dataRecords[dataRecordArrayCounter][0].toString();
					for(int dataRecordLineCounter=1;dataRecordLineCounter<dataRecords[dataRecordArrayCounter].length;dataRecordLineCounter++)
					{
						tempTags+=dataRecords[dataRecordArrayCounter][dataRecordLineCounter].toString();
					}
					System.out.println(tempTags);
					String []allTags=tempTags.split(" ");
					String []tagsTemp=new String[50];
					tagsSchema[0]=allTags[0];
					schemaCounter=1;
					int i=1;
					
					for(;i<allTags.length;i++)
					{
						if(!(allTags[i].equals(tagsSchema[0])))
						{
							tagsSchema[schemaCounter++]=allTags[i];
							//dataRecordsAfterCutLine[dataRecordArrayCounter]=dataRecords[dataRecordArrayCounter];
							continue;
						}
						else
						{
							int j=i;
							int k=0;
							int k1=0;
							for(;j<allTags.length&&k<schemaCounter;j++,k++,i++)
							{
								if(allTags[j].equals(tagsSchema[k]))
								{
									System.out.println("schemaCounter value is:"+schemaCounter);
									System.out.println("j value is:"+j);
									System.out.println("i value is:"+i);
									tagsTemp[k1++]=allTags[j];
									System.out.println("k value is:"+k);									
									//continue;
								}
								else
								{
									tagsTemp[k1++]=allTags[j];
									System.out.println("count of retrieve");
									for(int m=0;m<k1;m++)
									{
										System.out.println("schemaCounter:"+schemaCounter);
					     				tagsSchema[schemaCounter++]=tagsTemp[m];
										//continue;
									}
									k1=0;
									k=-1;
									continue;
								}
								if(k==schemaCounter-1)
								{
									k=-1;
									continue;
								}
							}
							
							//输出获取到的Schema中的标签内容
							System.out.println("Schema中的标签内容:");
							for(int sum=0;sum<schemaCounter;sum++)
							{
								System.out.println(tagsSchema[sum]);
							}
							System.out.println("输出完成！");
						}
				}
					System.out.println("the tagsSchema:");
				for(int j=0;j<tagsSchema.length;j++)
				{
					System.out.println(tagsSchema[j]);
				}
				System.out.println("the tagsSchema is over!");
			}
				//if(schemaCounter==1)
				//{
					//System.out.println("count==1,不用切分");
					//DataRecord tempDataRecord=new DataRecord(tempTagNode2);
					//dataRecords[dataRecordArrayCounter][i]=tempDataRecord;
				//}
				//else
				{
					String str="";
					for(int sum=0;sum<schemaCounter;sum++)
					{
						str+=tagsSchema[sum].toString();
					}
					if(str.equals(strFirstRecordTagsString))
					{
						System.out.println("the str is equals to firstRecordTagsString:");
						//dataRecordsAfterCutLine[dataRecordArrayCounter]=dataRecords[dataRecordArrayCounter];
					}
					else
					{
						TagNode []tagnode=new TagNode[100];
						int countOfTotalTag=0;
						//获取到所有的Tags
						for(int i=0;i<dataRecords[dataRecordArrayCounter].length;i++)
						{
							TagNode [] tempTagNodeOfLine=dataRecords[dataRecordArrayCounter][i].getRecordElements();
							for(int j=0;j<tempTagNodeOfLine.length;j++)
							{
								tagnode[countOfTotalTag++]=tempTagNodeOfLine[j];
							}
						}
						int countOfDataRecod;
						if(countOfTotalTag%schemaCounter==0)
						{
							countOfDataRecod=countOfTotalTag/schemaCounter;
						}
						else
						{
							countOfDataRecod=countOfTotalTag/schemaCounter+1;
						}
						//DataRecord[][]tempDataRecords=new DataRecord[1][countOfDataRecod];
						dataRecords[dataRecordArrayCounter]=new DataRecord[countOfDataRecod];
						int counter=0;
						for(int i=0;i<countOfDataRecod;i++)
						{
							int counterOfSchemaCounter=0;
							TagNode[]tempTagNode2=new TagNode[schemaCounter];
							for(;counterOfSchemaCounter<schemaCounter;counterOfSchemaCounter++)
							{
								tempTagNode2[counterOfSchemaCounter]=tagnode[counter++];
							}
							//if(counterOfSchemaCounter<2)
							//{
								//tempTagNode2[counterOfSchemaCounter+1]=(TagNode)null;
							//}
							DataRecord tempDataRecord=new DataRecord(tempTagNode2);
							dataRecords[dataRecordArrayCounter][i]=tempDataRecord;
						}
						//dataRecords[22][2]=dataRecords[22][1];
						//dataRecords[36][2]=dataRecords[36][1];
						//dataRecordsAfterCutLine[dataRecordArrayCounter]=tempDataRecords[0];
					}
				}
				System.out.println("After Silce Data Record:"+dataRecordArrayCounter);
				for(int j=0;j<dataRecords[dataRecordArrayCounter].length;j++)
				{
					System.out.println("line:"+j+" "+dataRecords[dataRecordArrayCounter][j].toString());
				}
		}

			
			
			//实现相似Data Record的合并
			int newDataRecordCounter=0;
			DataRecord[][] newDataRecords = new DataRecord[ dataRegions.size() ][];
			newDataRecords[0]=dataRecords[0];
			for(int dataRecordCounter=1;dataRecordCounter<dataRecords.length;dataRecordCounter++)
			{
				DataRecord preDataRecorde=dataRecords[dataRecordCounter][0];
				DataRecord curDataRecorde=dataRecords[newDataRecordCounter][0];
				//DataRecord curDataRecorde=dataRecords[dataRecordCounter+1][0];
				String preTag=preDataRecorde.toString();
				String curTag=curDataRecorde.toString();
				//System.out.println(preTag);
				TagNode [] preDataRecordeTag=preDataRecorde.getRecordElements();
				TagNode [] curDataRecordeTag=curDataRecorde.getRecordElements();
				if(preDataRecordeTag.length<2||curDataRecordeTag.length<2)
				{
					//for(int i=0;i<dataRecords[dataRecordCounter].length;i++)
					//{
						newDataRecords[++newDataRecordCounter]=dataRecords[dataRecordCounter];
					//}
					continue;
				}
				//else
				//{
					//if(curDataRecordeTag.length<2)
					//{
						//for(int i=0;i<dataRecords[dataRecordCounter].length;i++)
						//{
							//newDataRecords[newDataRecordCounter++]=dataRecords[dataRecordCounter];
						//}
						//for(int i=0;i<dataRecords[dataRecordCounter+1].length;i++)
						//{
							//newDataRecords[newDataRecordCounter++]=dataRecords[dataRecordCounter+1];
						//}
						//dataRecordCounter++;
						//continue;
					//}
					else
					{
						if(preTag.equals(curTag))
						{
							//System.out.println("nodes are equal!");
							int sizeOfRecord=dataRecords[dataRecordCounter].length+newDataRecords[newDataRecordCounter].length;
							//DataRecord [][] newDataRecorde=new DataRecord [sizeOfRecord][] ;
							//System.out.println(sizeOfRecord+"="+dataRecords[dataRecordCounter].length+"+"+newDataRecords[newDataRecordCounter].length);
							DataRecord []tempDataRecord=newDataRecords[newDataRecordCounter];
							newDataRecords[newDataRecordCounter]=new DataRecord[sizeOfRecord];
							for(int i=0,j=0;i<sizeOfRecord&&j<dataRecords[dataRecordCounter].length;i++)
							{
								
								if(i<tempDataRecord.length)
								{
									//System.out.println("enter if block!");
									newDataRecords[newDataRecordCounter][i]=tempDataRecord[i];
									//newDataRecords[newDataRecordCounter][i]=dataRecords[dataRecordCounter][i];
								}
								else
								{
									newDataRecords[newDataRecordCounter ][i]=dataRecords[dataRecordCounter][j];
									j++;	
								}
							}
						}
						else
						{
							//for(int i=0;i<dataRecords[dataRecordCounter].length;i++)
							//{
								newDataRecords[++newDataRecordCounter]=dataRecords[dataRecordCounter];
							//}
							//newDataRecordCounter++;
							
							//for(int i=0;i<dataRecords[dataRecordCounter+1].length;i++)
							//{
								//newDataRecords[newDataRecordCounter]=dataRecords[dataRecordCounter+1];
							//}
							//newDataRecordCounter++;
						}
					}
				}
				
			// buat objek ColumnAligner yang menggunakan algoritma partial tree alignment
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

			// bagi tiap2 data records ke dalam kolom sehingga berbentuk tabel
			// dan buang tabel yang null
			for(int tableCounter=0; tableCounter< newDataRecordCounter; tableCounter++)
			{
				String[][] dataTable = aligner.alignDataRecords( newDataRecords[tableCounter] );

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
			
			//int count=1;
			//for(String[][] dataTable:dataTables)
			//{
				//output.format("Table %s",count++);
				//output.format("<br>");
				//for(int i=0;i<dataTable.length;i++)
				//{
					//int c=1;
					//for(int j=0;j<dataTable[i].length;j++)
						//{
							//output.format("%s",dataTable[i][j]);
							//output.format(" coulum%s ",c++);
						//}
					//output.format("<br>");
				//}
				//output.format("<br>");
			//}

			// write extracted data to output file
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