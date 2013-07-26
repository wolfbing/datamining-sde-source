package com.datamining.sde.datarecordsfinder;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Formatter;
import java.util.List;
import java.util.ArrayList;

import com.datamining.sde.basictype.DataRecord;
import com.datamining.sde.basictype.DataRegion;
import com.datamining.sde.basictype.TagNode;
import com.datamining.sde.treematcher.TreeMatcher;


public class MiningDataRecords implements DataRecordsFinder
{
	private TreeMatcher treeMatcher;

	public MiningDataRecords(TreeMatcher treeMatcher)
	{
		this.treeMatcher = treeMatcher;
	}

	public DataRecord[] findDataRecords(DataRegion dataRegion, double similarityTreshold)
	{
		// jika tidak kembalikan dataRegion karena merupakan data records
		
		// cek apakah ukuran generalized node-nya = 1
		if ( dataRegion.getCombinationSize() == 1)
		{
			TagNode parentNode = dataRegion.getParent();
			int startPoint = dataRegion.getStartPoint();
			int nodesCovered = dataRegion.getNodesCovered();
			
			// untuk setiap generalized node G dalam dataRegion
			for (int generalizedNodeCounter = startPoint; generalizedNodeCounter < startPoint + nodesCovered; generalizedNodeCounter++)
			{
				TagNode generalizedNode = parentNode.getChildAtNumber( generalizedNodeCounter );
				
				// cek apakah G merupakan data table row, jika ya kembalikan tiap generalized node sebagai data records
				if ( generalizedNode.subTreeDepth() <= 2)
				{
					return sliceDataRegion(dataRegion);
				}
				
				TagNode prevChild = generalizedNode.getChildAtNumber( 1 );

				// 我认为这里只做了很简单的判断，如果连续两个节点是不相似的，那么就整个广义节点看作一条记录，
				// 当每两个连续节点都是相似的时，每个单独的子节点当作数据记录
				//  这里没有用mdr算法
				// cek apakah semua anak dari G mirip, jika tidak kembalikan tiap generalized node sebagai data records
				for (int childCounter=2; childCounter <= generalizedNode.childrenCount() ; childCounter++ )
				{
					TagNode nextChild = generalizedNode.getChildAtNumber( childCounter );
					
					if ( treeMatcher.normalizedMatchScore(prevChild, nextChild) < similarityTreshold )
					{
						return sliceDataRegion(dataRegion);
					}
					
					prevChild = nextChild;
				}
			}
			
			List<DataRecord> dataRecordList = new ArrayList<DataRecord>();

			// kembalikan setiap node children dari tiap2 generalized node dari data region ini sebagai data records
			for (int generalizedNodeCounter = startPoint; generalizedNodeCounter < startPoint + nodesCovered; generalizedNodeCounter++)
			{
				TagNode generalizedNode = parentNode.getChildAtNumber( generalizedNodeCounter );
				// 广义节点只有一个标签节点，遍历广义节点的每个子节点，每个子节点被当作一条数据记录
				for (TagNode childOfGeneralizedNode: generalizedNode.getChildren())
				{
					DataRecord dataRecord = new DataRecord( new TagNode[] { childOfGeneralizedNode} );
					dataRecordList.add( dataRecord );
				}
			}
			
			TagNode recordTagRoot = parentNode.getChildAtNumber(startPoint).getChildren().get(0);
			// 寻找遗漏的数据记录
			List<DataRecord> foreRegionDatalist = new ArrayList<DataRecord>();
			for(int nodeBeforeGeneralizedNodeCounter=1; nodeBeforeGeneralizedNodeCounter<startPoint;
					++nodeBeforeGeneralizedNodeCounter)
			{
				
				TagNode tmpNode = parentNode.getChildAtNumber(nodeBeforeGeneralizedNodeCounter);

				for(TagNode tmpChildNode: tmpNode.getChildren())
				{
					if(treeMatcher.normalizedMatchScore(recordTagRoot, tmpChildNode)>similarityTreshold)
					{
						foreRegionDatalist.add(new DataRecord(new TagNode[] {tmpChildNode} ) );
					}
				}		
				
			}
			
			List<DataRecord> behindRegionDataList = new ArrayList<DataRecord>();
			for(int nodeBehindGeneralizedNodeCounter=startPoint+nodesCovered; 
					nodeBehindGeneralizedNodeCounter<parentNode.childrenCount()+1;
					++nodeBehindGeneralizedNodeCounter)
			{
				TagNode tmpNode = parentNode.getChildAtNumber(nodeBehindGeneralizedNodeCounter);
				for(TagNode tmpChildNode: tmpNode.getChildren())
				{
					if(treeMatcher.normalizedMatchScore(recordTagRoot, tmpChildNode)>similarityTreshold)
					{
						behindRegionDataList.add(new DataRecord(new TagNode[] {tmpChildNode} ) );
					}
				}
			}
			
			if(!foreRegionDatalist.isEmpty())
			{
				foreRegionDatalist.addAll(dataRecordList);
				dataRecordList = foreRegionDatalist;
			}
			
			if(!behindRegionDataList.isEmpty())
			{
				dataRecordList.addAll(behindRegionDataList);
			}
			
			return dataRecordList.toArray( new DataRecord[0] );
		}
		
		// jika data region generalized node-nya terdiri lebih dari 1 node, 
		// maka kembalikan tiap generalized node sebagai data records
		return sliceDataRegion(dataRegion);
	}
	
	/**
	 * 利用coveredNum将数据记录提取出来
	 * 
	 * @param dataRegion
	 * @return
	 */
	private DataRecord[] sliceDataRegion(DataRegion dataRegion)
	{
		TagNode parentNode = dataRegion.getParent();
		int combinationSize = dataRegion.getCombinationSize();
		int startPoint = dataRegion.getStartPoint();
		int nodesCovered = dataRegion.getNodesCovered();
		DataRecord[] dataRecords = new DataRecord[ nodesCovered / combinationSize ];
		
		int arrayCounter = 0;
		for (int childCounter = startPoint; childCounter + combinationSize <= startPoint + nodesCovered; childCounter += combinationSize)
		{
			TagNode[] recordElements = new TagNode[combinationSize];
			
			int tagNodeCounter = 0;
			for(int generalizedNodeChildCounter = childCounter; generalizedNodeChildCounter < childCounter + combinationSize; generalizedNodeChildCounter++ )
			{
				recordElements[tagNodeCounter] = parentNode.getChildAtNumber( generalizedNodeChildCounter ); 
				tagNodeCounter++;
			}

			DataRecord dataRecord = new DataRecord( recordElements );
			dataRecords[ arrayCounter ] = dataRecord;
			arrayCounter++;
		}

		return dataRecords;
	}
}