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

				// 鎴戣涓鸿繖閲屽彧鍋氫簡寰堢畝鍗曠殑鍒ゆ柇锛屽鏋滆繛缁袱涓妭鐐规槸涓嶇浉浼肩殑锛岄偅涔堝氨鏁翠釜骞夸箟鑺傜偣鐪嬩綔涓�潯璁板綍锛�				// 褰撴瘡涓や釜杩炵画鑺傜偣閮芥槸鐩镐技鐨勬椂锛屾瘡涓崟鐙殑瀛愯妭鐐瑰綋浣滄暟鎹褰�				//  杩欓噷娌℃湁鐢╩dr绠楁硶
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
				// 骞夸箟鑺傜偣鍙湁涓�釜鏍囩鑺傜偣锛岄亶鍘嗗箍涔夎妭鐐圭殑姣忎釜瀛愯妭鐐癸紝姣忎釜瀛愯妭鐐硅褰撲綔涓�潯鏁版嵁璁板綍
				for (TagNode childOfGeneralizedNode: generalizedNode.getChildren())
				{
					DataRecord dataRecord = new DataRecord( new TagNode[] { childOfGeneralizedNode} );
					dataRecordList.add( dataRecord );
				}
			}
			
			TagNode recordTagRoot = parentNode.getChildAtNumber(startPoint).getChildren().get(0);
			// 瀵绘壘閬楁紡鐨勬暟鎹褰�			
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
	 * 鍒╃敤coveredNum灏嗘暟鎹褰曟彁鍙栧嚭鏉�	 * 
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