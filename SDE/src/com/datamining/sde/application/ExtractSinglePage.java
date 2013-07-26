package com.datamining.sde.application;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

/**
 * 
 * 这是单独抽取一张网页的类
 * 
 * 
 * @author wolf
 * @version 1.0
 * 
 * <br>
 * <br>
 * create date: 2013-7-21
 * 
 */
public class ExtractSinglePage {
	
	private String outEncoding;

	/**
	 * 无参数构造函数,
	 * 输出编码默认设置为"gbk";
	 */
	public ExtractSinglePage() {
		// TODO Auto-generated constructor stub
		outEncoding = "gbk";
	}
	/**
	 * 设置输出编码的方法;
	 * 
	 * @param encode - 编码格式，如"utf8","gbk"
	 * 
	 * @return - 返回当前的类
	 */
	public ExtractSinglePage setOutEncoding(String encode)
	{
		outEncoding = encode;
		return this;
	}
	
	/**
	 * 打印树的方法，将树的节点以HTML编码的方式输出
	 * @param output - 输出的目的
	 * @param tagNode - 树的根节点
	 * @param indent - 缩进，表示树的层次间缩进方式
	 */
	private static void printTree(Formatter output, TagNode tagNode, String indent)
	{
		
		output.format("%s%s  (%s)<br/>", indent, tagNode.toString(), tagNode.getInnerText());
		//output.format("%s%s<br />", indent, tagNode.getInnerText());

		
		for (TagNode child: tagNode.getChildren() )
		{
			printTree(output, child, "&nbsp;——&nbsp;"+indent);
		}
	}
	
	/**
	 * 抽取页面的方法
	 * @param argMap - 是字典类型的参数<br>
	 * key: url, -o, -s, -e, -en, -i, -m<br><br>
	 * key对应的值：<br>
	 * 				url - 待抽取的网页的URL,如本地可以是 "file:///C:/Users/Admin/Desktop/Mockup/SBI/FX%20TOP.html"，
	 * 							远程可以是 "http://www.baidu.com";<br>
	 * 						     这个参数必须有;<br><br>
	 * 				-o - 结果的输出路径，包括文件名，如 "C:/Users/Admin/Desktop/MDR.html";<br>
	 * 						     默认值："MDR.html";<br><br>
	 *              -e - 输出文件的编码，比如utf8，gbk
	 *                     默认值：gbk
	 * 				-s - 相似度阈值，范围0-1，因为这里是归一化后的阈值;<br>
	 * 						     默认值：0.80;<br><br>
	 * 				-i - 是否忽略一些标签，如果为"true",构建tag tree时将会忽略 B, I, U 等标签，这些标签将会被当作普通文本处理;<br>
	 * 						      默认值：false;<br><br>
	 * 				-en - 是否使用加强的树匹配，如果"true"，则在计算树的归一化的相似度时用到叶子节点，否则计算树的归一化的相似度时忽略叶子节点;<br>
	 * 						      默认值：false	;<br><br>
	 * 				-m	- 搜索广义节点时，广义节点的最大个数;<br>
	 * 						      默认值：9;<br><br>
	 */
	public void ExtractPage(Map<String,String> argMap)
	{
		Formatter output;
		// parameter default
		String input = argMap.get("url");
		String resultOutput = argMap.get("-o")!=null? argMap.get("-o"):"MDR.html";
		outEncoding = argMap.get("-e")!=null?argMap.get("-e"):"gbk";
		double similarityTreshold = argMap.get("-s")!=null? Double.parseDouble(argMap.get("-s")):0.80;
		boolean ignoreFormattingTags = argMap.get("-i")!=null? Boolean.parseBoolean(argMap.get("-i")):false;
		boolean useContentSimilarity = argMap.get("-en")!=null?Boolean.parseBoolean(argMap.get("-en")):false;
		int maxNodeInGeneralizedNodes = argMap.get("-m")!=null? Integer.parseInt(argMap.get("-m")):9;
		

		try
		{
			// siapkan file output
			output = new Formatter(resultOutput,outEncoding);
			
			// buat objek TagTreeBuilder yang berbasis parser DOM
			TagTreeBuilder builder = new DOMParserTagTreeBuilder();
			// bangun pohon tag dari file input menggunakan objek TagTreeBuilder yang telah dibuat
			TagTree tagTree = builder.buildTagTree(input, ignoreFormattingTags);
			//print(A.getRoot(), " ");
			//printHTML( A.getRoot());
			// buat objek TreeMatcher yang menggunakan algoritma simple tree matching
			TreeMatcher matcher = new SimpleTreeMatching();
			// buat objek DataRegionsFinder yang menggunakan algoritma mining data regions dan
			// menggunakan algoritma pencocokan pohon yang telah dibuat sebelumnya
			DataRegionsFinder dataRegionsFinder = new MiningDataRegions( matcher );
			// cari data region pada pohon tag menggunakan objek DataRegionsFinder yang telah dibuat
			List<DataRegion> dataRegions = dataRegionsFinder.findDataRegions(tagTree.getRoot(), maxNodeInGeneralizedNodes, similarityTreshold);
			// buat objek DataRecordsFinder yang menggunakan metode mining data records dan
			// menggunakan algoritma pencocokan pohon yang telah dibuat sebelumnya
			DataRecordsFinder dataRecordsFinder = new MiningDataRecords( matcher );
			// buat matriks DataRecord untuk menyimpan data record yang teridentifikasi oleh 
			// DataRecordsFinder dari List<DataRegion> yang ditemukan
			DataRecord[][] dataRecords = new DataRecord[ dataRegions.size() ][];
			
			// 用来保存每个data region的tag tree的根节点�?
			TagNode dataRegionRootNodes[] = new TagNode[ dataRegions.size() ];
			// identifikasi data records untuk tiap2 data region 
			for( int dataRecordArrayCounter = 0; dataRecordArrayCounter < dataRegions.size(); dataRecordArrayCounter++)
			{
				dataRegionRootNodes[dataRecordArrayCounter] = dataRegions.get(dataRecordArrayCounter).getParent();
				DataRegion dataRegion = dataRegions.get( dataRecordArrayCounter );
				dataRecords[ dataRecordArrayCounter ] = dataRecordsFinder.findDataRecords(dataRegion, similarityTreshold);
			}
			
			// added by Taiyun Liu , 2013-7-21 17:19
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
					//System.out.println("Data Record:"+dataRecordArrayCounter);
					String tempTags;
					tempTags=dataRecords[dataRecordArrayCounter][0].toString();
					for(int dataRecordLineCounter=1;dataRecordLineCounter<dataRecords[dataRecordArrayCounter].length;dataRecordLineCounter++)
					{
						tempTags+=dataRecords[dataRecordArrayCounter][dataRecordLineCounter].toString();
					}
					//System.out.println(tempTags);
					String []allTags=tempTags.split(" ");
					String []tagsTemp=new String[100];
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
										//System.out.println("schemaCounter:"+schemaCounter);
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
							//System.out.println("Schema中的标签内容:");
							//for(int sum=0;sum<schemaCounter;sum++)
							//{
								//System.out.println(tagsSchema[sum]);
							//}
							//System.out.println("输出完成！");
						}
				}
					//System.out.println("the tagsSchema:");
				//for(int j=0;j<tagsSchema.length;j++)
				//{
					//System.out.println(tagsSchema[j]);
				//}
				//System.out.println("the tagsSchema is over!");
				}
				{
					String str="";
					for(int sum=0;sum<schemaCounter;sum++)
					{
						str+=tagsSchema[sum].toString();
					}
					if(str.equals(strFirstRecordTagsString))
					{
						//System.out.println("the str is equals to firstRecordTagsString:");
						//dataRecordsAfterCutLine[dataRecordArrayCounter]=dataRecords[dataRecordArrayCounter];
						//不做切分处理
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
							boolean flag=true;
							for(;counterOfSchemaCounter<schemaCounter;counterOfSchemaCounter++)
							{
								flag=true;
								tempTagNode2[counterOfSchemaCounter]=tagnode[counter++];
								if(tempTagNode2[counterOfSchemaCounter]==null)
								{
									flag=false;
								}
							}
							if(flag)
							{
								DataRecord tempDataRecord=new DataRecord(tempTagNode2);
								dataRecords[dataRecordArrayCounter][i]=tempDataRecord;
							}
							else
							{
								dataRecords[dataRecordArrayCounter][i]=dataRecords[dataRecordArrayCounter][i-1];
							}
						}
					}
				}
				System.out.println("After Silce Data Record:"+dataRecordArrayCounter);
				for(int j=0;j<dataRecords[dataRecordArrayCounter].length;j++)
				{
					System.out.println("line:"+j+" "+dataRecords[dataRecordArrayCounter][j].toString());
				}
		    }
			
			// added by Taiyun Liu, 2013-7-21 17:25
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
							newDataRecords[++newDataRecordCounter]=dataRecords[dataRecordCounter];
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
			for(int tableCounter=0; tableCounter< dataRecords.length; tableCounter++)
			{
				String[][] dataTable = aligner.alignDataRecords( dataRecords[tableCounter] );
				// 不做过滤处理  输出时再判断
				//if ( dataTable != null )
				//{
					dataTables.add( dataTable );
				//}
			}
			
			int recordsFound = 0;
						
			for ( String[][] dataTable: dataTables )
			{
				// dataTable 取消过滤了，里面可能包含null
				if(dataTable != null)
				{
					recordsFound += dataTable.length;
				}
				
			}			

			// write extracted data to output file
			output.format("<html><head><title>Extraction Result</title>");
			//编码声明
			output.format("<meta http-equiv='content-type' content='text/html;charset=%s' />", outEncoding);
			output.format("<style type=\"text/css\">table {border-collapse: collapse;} td {padding: 5px} table, th, td { border: 3px solid black;} </style>");
			output.format("</head><body>");
			
			// 输出整个页面呢的tag tree
			//printTree(output,tagTree.getRoot(),"&nbsp;");
			int tableCounter = 1;
			// 原来的遍历方式，为了输出tag tree就把遍历方式改成下面到了
			//for ( String[][] table: dataTables)
			//{
			for(int loop=0;loop<dataTables.size();++loop)
			{
				String[][] table = dataTables.get(loop);
				if(table != null)
				{
					output.format("<br/><b>Data Region %d</b><br/>", tableCounter);
					// 打印每个数据区域�?tag tree
					printTree(output, dataRegionRootNodes[loop],"&nbsp;");
					// 打印数据记录
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
			}		
			output.format("</body></html>");
			
			if ( output != null )
			{
				output.close();
			}
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
			System.out.println("======  extracting completed  =========");
		}
	}
		
	/**
	 * 抽取页面的方法
	 * Usage with this format:<br>
     * java -jar datamining-sde-runnable.jar url [-o outdir] [-e encode] [-s threshold] [-i ignore] [-en enhace] [-m maxnum] <br>
     * For example:<br>
     * java -jar datamining-sde-runnable.jar http://www.baidu.com -o MDR.html -e gbk -s 0.8 -i true -en true -m 9<br>
     * or just like this:<br>
     * java -jar datamining-sde-runnable.jar http://www.baidu.com<br><br>
	 * 
	 * Params Explaination:<br><br>
	 * 				url - 待抽取的网页的URL,如本地可以是 "file:///C:/Users/Admin/Desktop/Mockup/SBI/FX%20TOP.html"，
	 * 							远程可以是 "http://www.baidu.com";<br>
	 * 						     这个参数必须有;<br><br>
	 * 				outdir - 结果的输出路径，包括文件名，如 "C:/Users/Admin/Desktop/MDR.html";<br>
	 * 						     默认值："MDR.html";<br><br>
	 * 				encode - 设置输出文件的编码格式，如 gbk,utf8<br><br>
	 * 				threshold - 相似度阈值，范围0-1，因为这里是归一化后的阈值;<br>
	 * 						     默认值：0.80;<br><br>
	 * 				ignore - 是否忽略一些标签，如果为"true",构建tag tree时将会忽略 B, I, U 等标签，这些标签将会被当作普通文本处理;<br>
	 * 						      默认值：false;<br><br>
	 * 				enhance - 是否使用加强的树匹配，如果"true"，则在计算树的归一化的相似度时用到叶子节点，否则计算树的归一化的相似度时忽略叶子节点;<br>
	 * 						      默认值：false	;<br><br>
	 * 				maxnum	- 搜索广义节点时，广义节点的最大个数;<br>
	 * 						      默认值：9;<br><br>
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] params) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		if(params.length ==0 )
		{
			System.out.println(
   "   Usage with this format:\n"+
   "   java -jar datamining-sde-runnable.jar url [-o outdir] [-e encode] [-s threshold] [-i ignore] [-en enhance] [-m maxnum] \n"+
   "   For example:\n"+
   "   java -jar datamining-sde-runnable.jar http://www.baidu.com -o MDR.html -e gbk -s 0.8 -i true -en true -m 9\n"+
   "   or just like this:\n"+
   "   java -jar datamining-sde-runnable.jar http://www.baidu.com\n\n"+
   "   Params Explaination:\n\n"+
   "必填参数："+
   " * 	url - 待抽取的网页的URL,如本地可以是 file:///C:/Users/Admin/Desktop/Mockup/SBI/FX%20TOP.html，远程可以是 http://www.baidu.com;\n\n" +
   "可选参数："+
   " * 				outdir - 结果的输出路径，包括文件名，如  C:/Users/Admin/Desktop/MDR.html;\n" +
   " * 						     默认值：MDR.html;\n\n" +
   " * 				encode - 设置输出文件的编码格式，如 gbk,utf8 \n" +
   " * 				threshold - 相似度阈值，范围0-1，因为这里是归一化后的阈值;\n" +
   " * 						     默认值：0.80;\n\n" +
   " * 				ignore - 是否忽略一些标签，如果为true,构建tag tree时将会忽略 B, I, U 等标签，这些标签将会被当作普通文本处理;\n" +
   " * 						      默认值：false;\n\n" +
   " * 				enhance - 是否使用加强的树匹配，如果true，则在计算树的归一化的相似度时用到叶子节点，否则计算树的归一化的相似度时忽略叶子节点;\n" +
   " * 						      默认值：false	;\n\n" +
   " * 				maxnum	- 搜索广义节点时，广义节点的最大个数;<br>" +
   " * 						      默认值：9;\n\n" 
					);
//			Map<String,String> map = new HashMap<String,String>();
//			map.put("url", "file:///C:/Users/Admin/Desktop/test.html");
//			new ExtractSinglePage().ExtractPage(map);
			System.exit(1);
		}
		else
		{
			Map<String,String> paramMap = new HashMap<String,String>();
			paramMap.put("url", params[0]);
			for(int i=1;i<params.length;i+=2)
			{
				if(i!=params.length)
				{
					paramMap.put(params[i], params[i+1]);
				}
				
			}
			Set<String> keys = new HashSet<String>();
			keys = paramMap.keySet();
			for(String key: keys)
			{

				if(!key.equals("url") && !key.equals("-o") && !key.equals("-e") &&
						!key.equals("-s") && !key.equals("-i") && !key.equals("-en") && !key.equals("-m"))
				{
					System.out.println("params error!\nYou can refer to how to use by just input : java -jar datamining-sde-runnable.jar ");
					System.exit(1);
				}
			}
			
			new ExtractSinglePage().ExtractPage(paramMap);
		}
		
	}

}
