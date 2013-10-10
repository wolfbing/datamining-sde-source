package com.datamining.sde.columnaligner;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.datamining.sde.basictype.DataRecord;
import com.datamining.sde.basictype.DataRecordSizeComparator;
import com.datamining.sde.basictype.TagNode;
import com.datamining.sde.basictype.TreeAlignment;
import com.datamining.sde.treematcher.TreeMatcher;


public class PartialTreeAligner implements ColumnAligner
{
	private TreeMatcher treeAligner;
	
	public PartialTreeAligner(TreeMatcher treeAligner)
	{
		this.treeAligner = treeAligner;
	}

	/**
	 * 澶氭５鏍戠殑閮ㄥ垎瀵归綈
	 * 
	 * 杩斿洖鎶藉彇鍑烘潵鐨勬暟鎹〃
	 */
	public String[][] alignDataRecords(DataRecord[] dataRecordsArray)
	{
		String[][] alignedData = new String[ dataRecordsArray.length ][];
		// ubah menjadi List
		List<DataRecord> dataRecords = convertToList( dataRecordsArray );
		// urutkan data records secara ascending berdasarkan ukuran pohon data record
		Collections.sort(dataRecords, new DataRecordSizeComparator());
		// buat R
		List<DataRecord> R = new ArrayList<DataRecord>();
		// buat map of map
		DataRecord originalSeed = dataRecords.get( dataRecords.size()-1 );
		/*
		 * 鍦ㄧ瀛愭爲鍜屽叾浠栨爲鐨勮妭鐐圭殑鏄犲皠涓婏紝閲囩敤map瀛樺偍锛屼粠绉嶅瓙鏍戠殑鑺傜偣鏄犲皠鍒板叾浠栨爲
		 */
		Map<DataRecord, Map<TagNode, TagNode>> mapping = new HashMap<DataRecord, Map<TagNode, TagNode>>();
		mapping.put(originalSeed, new HashMap<TagNode, TagNode>() );
		// 绉嶅瓙鏍�		
		DataRecord seedDataRecord = copyDataRecord( dataRecords.get( dataRecords.size()-1 ) );
		// hilangkan seedDataRecord dari list data records
		dataRecords.remove( dataRecords.size()-1 );
		createSeedAlignment(seedDataRecord.getRecordElements(), originalSeed.getRecordElements(), mapping.get( originalSeed ));		
		
		while(dataRecords.size() != 0)
		{
			// ambil dan hapus subtree data record berikutnya
			DataRecord nextDataRecord = dataRecords.get( dataRecords.size()-1 );
			dataRecords.remove( dataRecords.size()-1 );
			// jajarkan subtree yang baru saja diambil dengan seed
			List<TreeAlignment> alignmentList = treeAligner.align( seedDataRecord.getRecordElements(), nextDataRecord.getRecordElements() ).getSubTreeAlignment();
			// buat map hasil penjajaran
			mapping.put(nextDataRecord, new HashMap<TagNode, TagNode>() );
			/// alignmentList 鍖呭惈浜嗘墍鏈夊榻�			
			for (TreeAlignment alignment: alignmentList)
			{
				mapping.get(nextDataRecord).put( alignment.getFirstNode(), alignment.getSecondNode() );
			}
			
			List< List<TagNode> > unalignedNodes = new ArrayList< List<TagNode> >();
			findUnalignedNodes( nextDataRecord.getRecordElements(), mapping.get(nextDataRecord), unalignedNodes);
			
			if ( ! unalignedNodes.isEmpty() )
			{
				boolean anyInsertion = false;
				Map<TagNode, TagNode> reverseMap = new HashMap<TagNode, TagNode>();
				
				for (TagNode key: mapping.get(nextDataRecord).keySet() )
				{
					reverseMap.put( mapping.get(nextDataRecord).get(key), key);
				}
			
				// coba menyisipkan unaligned nodes ke seed
				for (List<TagNode> unalignedNodesThisLevel: unalignedNodes)
				{
					// dapatkan elemen yang paling kiri
					TagNode leftMostUnaligned = unalignedNodesThisLevel.get(0);
					// dapatkan elemen yang paling kanan
					TagNode rightMostUnaligned = unalignedNodesThisLevel.get( unalignedNodesThisLevel.size()-1);
					// prev dan next pasti match
					TagNode prevSibling = leftMostUnaligned.getPrevSibling();
					TagNode nextSibling = rightMostUnaligned.getNextSibling();
				
					if ( prevSibling == null)
					{
						if ( nextSibling != null )
						{
							// berarti unalignedNodes berada pada posisi paling kiri
							TagNode nextSiblingMatch = reverseMap.get(nextSibling);
						
							if ( nextSiblingMatch.getPrevSibling() == null)
							{
								List<TagNode> unalignedNodesCopy = new ArrayList<TagNode>();

								for (TagNode unalignedNode: unalignedNodesThisLevel)
								{
									TagNode copy = new TagNode();
									copy.setTagElement( unalignedNode.getTagElement() );
									copy.setInnerText( unalignedNode.getInnerText() );
									unalignedNodesCopy.add( copy );
								}

								nextSiblingMatch.getParent().insertChildNodes( 1, unalignedNodesCopy );
								
								for (int counter=0; counter < unalignedNodesThisLevel.size(); counter++)
								{
									mapping.get( nextDataRecord ).put( unalignedNodesCopy.get( counter ), unalignedNodesThisLevel.get( counter ));
								}

								unalignedNodesThisLevel.clear();
								anyInsertion = true;
							}
						}
					}
					else if ( nextSibling == null)
					{
						// berarti unalignedNodes berada pada posisi paling kanan
						TagNode prevSiblingMatch = reverseMap.get(prevSibling);
					
						if ( prevSiblingMatch.getNextSibling() == null )
						{
							List<TagNode> unalignedNodesCopy = new ArrayList<TagNode>();

							for (TagNode unalignedNode: unalignedNodesThisLevel)
							{
								TagNode copy = new TagNode();
								copy.setTagElement( unalignedNode.getTagElement() );
								copy.setInnerText( unalignedNode.getInnerText() );
								unalignedNodesCopy.add( copy );
							}

							prevSiblingMatch.getParent().insertChildNodes( prevSiblingMatch.getChildNumber()+1, unalignedNodesCopy );
							
							for (int counter=0; counter < unalignedNodesThisLevel.size(); counter++)
							{
								mapping.get( nextDataRecord ).put( unalignedNodesCopy.get( counter ), unalignedNodesThisLevel.get( counter ));
							}

							unalignedNodesThisLevel.clear();
							anyInsertion = true;
						}
					}
					else
					{
						// berarti unalignedNodes diapit oleh dua node sibling
						TagNode prevSiblingMatch = reverseMap.get(prevSibling);
						TagNode nextSiblingMatch = reverseMap.get(nextSibling);
						
						// untuk mengatasi kasus di mana unaligned nodes berada pada bagian paling kiri/kanan dari top level generalized node
						if (prevSiblingMatch != null && nextSiblingMatch != null)
						{
							if ( nextSiblingMatch.getChildNumber() - prevSiblingMatch.getChildNumber() == 1 )
							{
								List<TagNode> unalignedNodesCopy = new ArrayList<TagNode>();

								for (TagNode unalignedNode: unalignedNodesThisLevel)
								{
									TagNode copy = new TagNode();
									copy.setTagElement( unalignedNode.getTagElement() );
									copy.setInnerText( unalignedNode.getInnerText() );
									unalignedNodesCopy.add( copy );
								}

								prevSiblingMatch.getParent().insertChildNodes( prevSiblingMatch.getChildNumber()+1, unalignedNodesCopy );
								
								for (int counter=0; counter < unalignedNodesThisLevel.size(); counter++)
								{
									mapping.get( nextDataRecord ).put( unalignedNodesCopy.get( counter ), unalignedNodesThisLevel.get( counter ));
								}

								unalignedNodesThisLevel.clear();
								anyInsertion = true;
							}
						}
					}
				}
				
				// cek apakah ada penyisipan yang berhasil dilakukan
				if (anyInsertion)
				{
					dataRecords.addAll( R );
					R.clear();
				}
				
				for (List<TagNode> unalignedNodesThisLevel: unalignedNodes)
				{
					if ( ! unalignedNodesThisLevel.isEmpty() )
					{
						R.add( nextDataRecord );
						break;
					}
				}
			}
		}
		
		/* 鍒拌繖閲屼负姝㈣矊浼兼病鏈夊鏈�粓閮芥湭鍖归厤鐨勯」鍋氬鐞嗭紙鍗曠嫭鍗犳嵁涓�垪锛�*/
		
		List< List<String> > tempOutput = new ArrayList< List<String> >();
		
		for (DataRecord dataRecord: dataRecordsArray)
		{
			List<String> row = new ArrayList<String>();
			extractDataItems( seedDataRecord.getRecordElements(), mapping.get( dataRecord ), row);
			tempOutput.add( row );
		}
		
		TagNode[] seedElements = seedDataRecord.getRecordElements();
		int nodesInSeedCount = 0;
		
		for (TagNode tagNode: seedElements)
		{
			nodesInSeedCount += tagNode.subTreeSize();
		}

		boolean[] isNotNullColumnArray = new boolean[ nodesInSeedCount ];
		
		for ( int columnCounter=0; columnCounter < isNotNullColumnArray.length; columnCounter++)
		{
			for (List<String> row: tempOutput)
			{
				if ( row.get( columnCounter ) != null )
				{
					isNotNullColumnArray[ columnCounter ] = true;
					break;
				}
			}
		}

		int notNullColumnCount = 0;
		
		for(boolean isNotNullColumn: isNotNullColumnArray)
		{
			if ( isNotNullColumn )
			{
				notNullColumnCount++;
			}
		}
		
		for ( int rowCounter=0; rowCounter < alignedData.length; rowCounter++)
		{
			List<String> row = tempOutput.get( rowCounter );
			alignedData[ rowCounter ] = new String[ notNullColumnCount ];
			
			int columnCounter = 0;

			for(int notNullColumnCounter=0; notNullColumnCounter < isNotNullColumnArray.length; notNullColumnCounter++)
			{
				if ( isNotNullColumnArray[ notNullColumnCounter] )
				{
					alignedData[ rowCounter ][ columnCounter ] = row.get( notNullColumnCounter );
					columnCounter++;
				}
			}
		}
		
		if ( alignedData[0].length == 0 )
		{
			return null;
		}
		else
		{
			return alignedData;
		}
	}
	
	public void getFinalSeedNode(List<TagNode> nodes,DataRecord seedDataRecord,List<DataRecord> dataRecords,
			Map<DataRecord, Map<TagNode, TagNode>> mapping)
	{
		DataRecord [] dataRecordsArray = new DataRecord[nodes.size()];
		for(int recordCounter=0;recordCounter<dataRecordsArray.length;++recordCounter)
		{
			dataRecordsArray[recordCounter] = new DataRecord(new TagNode[]{nodes.get(recordCounter)});
		}
		
		
		// ubah menjadi List
		dataRecords = convertToList( dataRecordsArray );
		// urutkan data records secara ascending berdasarkan ukuran pohon data record
		Collections.sort(dataRecords, new DataRecordSizeComparator());
		// buat R
		List<DataRecord> R = new ArrayList<DataRecord>();
		// buat map of map
		DataRecord originalSeed = dataRecords.get( dataRecords.size()-1 );
		/*
		 * 鍦ㄧ瀛愭爲鍜屽叾浠栨爲鐨勮妭鐐圭殑鏄犲皠涓婏紝閲囩敤map瀛樺偍锛屼粠绉嶅瓙鏍戠殑鑺傜偣鏄犲皠鍒板叾浠栨爲
		 */
		//Map<DataRecord, Map<TagNode, TagNode>> mapping = new HashMap<DataRecord, Map<TagNode, TagNode>>();
		mapping.put(originalSeed, new HashMap<TagNode, TagNode>() );
		// 绉嶅瓙鏍�		//DataRecord 
		seedDataRecord = copyDataRecord( dataRecords.get( dataRecords.size()-1 ) );
		// hilangkan seedDataRecord dari list data records
		dataRecords.remove( dataRecords.size()-1 );
		createSeedAlignment(seedDataRecord.getRecordElements(), originalSeed.getRecordElements(), mapping.get( originalSeed ));		
		
		while(dataRecords.size() != 0)
		{
			// ambil dan hapus subtree data record berikutnya
			DataRecord nextDataRecord = dataRecords.get( dataRecords.size()-1 );
			dataRecords.remove( dataRecords.size()-1 );
			// jajarkan subtree yang baru saja diambil dengan seed
			List<TreeAlignment> alignmentList = treeAligner.align( seedDataRecord.getRecordElements(), nextDataRecord.getRecordElements() ).getSubTreeAlignment();
			// buat map hasil penjajaran
			mapping.put(nextDataRecord, new HashMap<TagNode, TagNode>() );
			/// alignmentList 鍖呭惈浜嗘墍鏈夊榻�			
			for (TreeAlignment alignment: alignmentList)
			{
				mapping.get(nextDataRecord).put( alignment.getFirstNode(), alignment.getSecondNode() );
			}
			
			List< List<TagNode> > unalignedNodes = new ArrayList< List<TagNode> >();
			findUnalignedNodes( nextDataRecord.getRecordElements(), mapping.get(nextDataRecord), unalignedNodes);
			
			if ( ! unalignedNodes.isEmpty() )
			{
				boolean anyInsertion = false;
				Map<TagNode, TagNode> reverseMap = new HashMap<TagNode, TagNode>();
				
				for (TagNode key: mapping.get(nextDataRecord).keySet() )
				{
					reverseMap.put( mapping.get(nextDataRecord).get(key), key);
				}
			
				// coba menyisipkan unaligned nodes ke seed
				for (List<TagNode> unalignedNodesThisLevel: unalignedNodes)
				{
					// dapatkan elemen yang paling kiri
					TagNode leftMostUnaligned = unalignedNodesThisLevel.get(0);
					// dapatkan elemen yang paling kanan
					TagNode rightMostUnaligned = unalignedNodesThisLevel.get( unalignedNodesThisLevel.size()-1);
					// prev dan next pasti match
					TagNode prevSibling = leftMostUnaligned.getPrevSibling();
					TagNode nextSibling = rightMostUnaligned.getNextSibling();
				
					if ( prevSibling == null)
					{
						if ( nextSibling != null )
						{
							// berarti unalignedNodes berada pada posisi paling kiri
							TagNode nextSiblingMatch = reverseMap.get(nextSibling);
						
							if ( nextSiblingMatch.getPrevSibling() == null)
							{
								List<TagNode> unalignedNodesCopy = new ArrayList<TagNode>();

								for (TagNode unalignedNode: unalignedNodesThisLevel)
								{
									TagNode copy = new TagNode();
									copy.setTagElement( unalignedNode.getTagElement() );
									copy.setInnerText( unalignedNode.getInnerText() );
									unalignedNodesCopy.add( copy );
								}

								nextSiblingMatch.getParent().insertChildNodes( 1, unalignedNodesCopy );
								
								for (int counter=0; counter < unalignedNodesThisLevel.size(); counter++)
								{
									mapping.get( nextDataRecord ).put( unalignedNodesCopy.get( counter ), unalignedNodesThisLevel.get( counter ));
								}

								unalignedNodesThisLevel.clear();
								anyInsertion = true;
							}
						}
					}
					else if ( nextSibling == null)
					{
						// berarti unalignedNodes berada pada posisi paling kanan
						TagNode prevSiblingMatch = reverseMap.get(prevSibling);
					
						if ( prevSiblingMatch.getNextSibling() == null )
						{
							List<TagNode> unalignedNodesCopy = new ArrayList<TagNode>();

							for (TagNode unalignedNode: unalignedNodesThisLevel)
							{
								TagNode copy = new TagNode();
								copy.setTagElement( unalignedNode.getTagElement() );
								copy.setInnerText( unalignedNode.getInnerText() );
								unalignedNodesCopy.add( copy );
							}

							prevSiblingMatch.getParent().insertChildNodes( prevSiblingMatch.getChildNumber()+1, unalignedNodesCopy );
							
							for (int counter=0; counter < unalignedNodesThisLevel.size(); counter++)
							{
								mapping.get( nextDataRecord ).put( unalignedNodesCopy.get( counter ), unalignedNodesThisLevel.get( counter ));
							}

							unalignedNodesThisLevel.clear();
							anyInsertion = true;
						}
					}
					else
					{
						// berarti unalignedNodes diapit oleh dua node sibling
						TagNode prevSiblingMatch = reverseMap.get(prevSibling);
						TagNode nextSiblingMatch = reverseMap.get(nextSibling);
						
						// untuk mengatasi kasus di mana unaligned nodes berada pada bagian paling kiri/kanan dari top level generalized node
						if (prevSiblingMatch != null && nextSiblingMatch != null)
						{
							if ( nextSiblingMatch.getChildNumber() - prevSiblingMatch.getChildNumber() == 1 )
							{
								List<TagNode> unalignedNodesCopy = new ArrayList<TagNode>();

								for (TagNode unalignedNode: unalignedNodesThisLevel)
								{
									TagNode copy = new TagNode();
									copy.setTagElement( unalignedNode.getTagElement() );
									copy.setInnerText( unalignedNode.getInnerText() );
									unalignedNodesCopy.add( copy );
								}

								prevSiblingMatch.getParent().insertChildNodes( prevSiblingMatch.getChildNumber()+1, unalignedNodesCopy );
								
								for (int counter=0; counter < unalignedNodesThisLevel.size(); counter++)
								{
									mapping.get( nextDataRecord ).put( unalignedNodesCopy.get( counter ), unalignedNodesThisLevel.get( counter ));
								}

								unalignedNodesThisLevel.clear();
								anyInsertion = true;
							}
						}
					}
				}
				
				// cek apakah ada penyisipan yang berhasil dilakukan
				if (anyInsertion)
				{
					dataRecords.addAll( R );
					R.clear();
				}
				
				for (List<TagNode> unalignedNodesThisLevel: unalignedNodes)
				{
					if ( ! unalignedNodesThisLevel.isEmpty() )
					{
						R.add( nextDataRecord );
						break;
					}
				}
			}
		}
		
		/* 鍒拌繖閲屼负姝㈣矊浼兼病鏈夊鏈�粓閮芥湭鍖归厤鐨勯」鍋氬鐞嗭紙鍗曠嫭鍗犳嵁涓�垪锛�*/
		// 涓婇潰浠ｇ爜鈥滃畬鎴愨�浜嗘渶缁堢瀛愭爲锛屽拰绉嶅瓙鏍戜笌姣忎釜鏁版嵁璁板綍鐨勮繛鎺ュ叧绯�		

	}
	
	public String[][] getDataTableUnderNestedTagNode(DataRecord [] dataRecordsArray,
			DataRecord seedDataRecord,Map<DataRecord, Map<TagNode, TagNode>> mapping )
	{
		List< List<String> > tempOutput = new ArrayList< List<String> >();
		String[][] alignedData = new String[ dataRecordsArray.length ][];
		for (DataRecord dataRecord: dataRecordsArray)
		{
			List<String> row = new ArrayList<String>();
			extractDataItems( seedDataRecord.getRecordElements(), mapping.get( dataRecord ), row);
			tempOutput.add( row );
		}
		
		TagNode[] seedElements = seedDataRecord.getRecordElements();
		int nodesInSeedCount = 0;
		
		for (TagNode tagNode: seedElements)
		{
			nodesInSeedCount += tagNode.subTreeSize();
		}

		boolean[] isNotNullColumnArray = new boolean[ nodesInSeedCount ];
		
		for ( int columnCounter=0; columnCounter < isNotNullColumnArray.length; columnCounter++)
		{
			for (List<String> row: tempOutput)
			{
				if ( row.get( columnCounter ) != null )
				{
					isNotNullColumnArray[ columnCounter ] = true;
					break;
				}
			}
		}

		int notNullColumnCount = 0;
		
		for(boolean isNotNullColumn: isNotNullColumnArray)
		{
			if ( isNotNullColumn )
			{
				notNullColumnCount++;
			}
		}
		
		for ( int rowCounter=0; rowCounter < alignedData.length; rowCounter++)
		{
			List<String> row = tempOutput.get( rowCounter );
			alignedData[ rowCounter ] = new String[ notNullColumnCount ];
			
			int columnCounter = 0;

			for(int notNullColumnCounter=0; notNullColumnCounter < isNotNullColumnArray.length; notNullColumnCounter++)
			{
				if ( isNotNullColumnArray[ notNullColumnCounter] )
				{
					alignedData[ rowCounter ][ columnCounter ] = row.get( notNullColumnCounter );
					columnCounter++;
				}
			}
		}
		
		if ( alignedData[0].length == 0 )
		{
			return null;
		}
		else
		{
			return alignedData;
		}
		
	}
	
	
	private void findUnalignedNodes(TagNode[] elements, Map<TagNode, TagNode> matchMap, List<List<TagNode>> list)
	{
		List<TagNode> unalignedNodesThisLevel = null;
		boolean continuous = false;

		for (TagNode element: elements)
		{
			if ( ! matchMap.containsValue( element) )
			{
				// sekuen unaligned node berikutnya
				if ( continuous )
				{
					unalignedNodesThisLevel.add( element );
				}
				// sekuen unaligned node pertama
				else
				{
					unalignedNodesThisLevel = new ArrayList<TagNode>();
					unalignedNodesThisLevel.add( element );
					continuous = true;
				}
			}
			// sekuen berakhir
			else if( continuous )
			{
				list.add( unalignedNodesThisLevel );
				unalignedNodesThisLevel = null;
				continuous = false;
			}
		}
		
		if ( unalignedNodesThisLevel != null )
		{
			list.add( unalignedNodesThisLevel );
		}

		for (TagNode element: elements)
		{	
			findUnalignedNodes(element, matchMap, list);
		}
	}
	
	private void findUnalignedNodes(TagNode element, Map<TagNode, TagNode> matchMap, List<List<TagNode>> list)
	{
		List<TagNode> unalignedNodesThisLevel = null;
		boolean continuous = false;

		for (TagNode child: element.getChildren())
		{
			if ( ! matchMap.containsValue( child ) )
			{
				// sekuen unaligned node berikutnya
				if ( continuous )
				{
					unalignedNodesThisLevel.add( child );
				}
				// sekuen unaligned node pertama
				else
				{
					unalignedNodesThisLevel = new ArrayList<TagNode>();
					unalignedNodesThisLevel.add( child );
					continuous = true;
				}
			}
			// sekuen berakhir
			else if( continuous )
			{
				list.add( unalignedNodesThisLevel );
				unalignedNodesThisLevel = null;
				continuous = false;
			}
		}
		
		if ( unalignedNodesThisLevel != null )
		{
			list.add( unalignedNodesThisLevel );
		}

		for (TagNode child: element.getChildren())
		{
			findUnalignedNodes(child, matchMap, list);
		}
	}

	/**
	 * 缁欏畾涓�粍鑺傜偣锛屽緱鍒板瓙鏍戠殑鏁版嵁椤癸紝瀛樺偍鍦ㄤ竴涓猺ow涓�	 * @param seed
	 * @param matchMap
	 * @param row
	 */
	private void extractDataItems(TagNode[] seed, Map<TagNode, TagNode> matchMap, List<String> row)
	{
		for (TagNode element: seed)
		{
			TagNode original = matchMap.get( element );

			if ( original != null)
			{
				row.add( original.getInnerText() );
			}
			else
			{
				row.add( null );
			}
			
		
			for (TagNode child: element.getChildren())
			{
				extractDataItems(child, matchMap, row);
			}
		}
	}
	/**
	 * 缁欏畾鏍戠殑鏍硅妭鐐瑰悗鎶藉嚭鏍戠殑鏁版嵁椤癸紝杩欓噷鐨勬暟鎹」鍖呮嫭浜嗘墍鏈夌殑鑺傜偣锛屽嵆浣夸粬浠病鏈夋暟鎹�	 * @param seed
	 * @param matchMap
	 * @param row
	 */
	private void extractDataItems(TagNode seed, Map<TagNode, TagNode> matchMap, List<String> row)
	{
		TagNode original = matchMap.get( seed );

		if ( original != null)
		{
			row.add( original.getInnerText() );
		}
		else
		{
			row.add( null );
		}
		
		for (TagNode child: seed.getChildren())
		{
			extractDataItems(child, matchMap, row);
		}
	}
	
	private List<DataRecord> convertToList(DataRecord[] array)
	{
		List<DataRecord> list = new ArrayList<DataRecord>();
		
		for(DataRecord dataRecord: array)
		{
			list.add( dataRecord );
		}
		
		return list;
	}
	
	private DataRecord copyDataRecord(DataRecord originalDataRecord)
	{
		TagNode[] original = originalDataRecord.getRecordElements();
		TagNode[] copy = new TagNode[ original.length ];
		TagNode parentNodeOriginal = original[0].getParent();
		TagNode parentNodeCopy = new TagNode();
		parentNodeCopy.setTagElement( parentNodeOriginal.getTagElement() );
		parentNodeCopy.setInnerText( parentNodeOriginal.getInnerText() );
		
		for (int arrayCounter=0; arrayCounter < original.length; arrayCounter++)
		{
			TagNode tagNode = new TagNode();
			tagNode.setParent( parentNodeCopy );
			tagNode.setTagElement( original[ arrayCounter ].getTagElement() );
			tagNode.setInnerText( original[ arrayCounter ].getInnerText() );
			copy[ arrayCounter ] = tagNode;
			
			for (TagNode child: original[ arrayCounter ].getChildren() )
			{
				createTagNodes(child, tagNode);
			}
		}
		
		return new DataRecord(copy);
	}
	
	private void createTagNodes(TagNode childOriginal, TagNode parentCopy)
	{
		TagNode tagNode = new TagNode();
		tagNode.setParent( parentCopy );
		tagNode.setTagElement( childOriginal.getTagElement() );
		tagNode.setInnerText( childOriginal.getInnerText() );
		
		for (TagNode child: childOriginal.getChildren() )
		{
			createTagNodes(child, tagNode);
		}
	}
	/**
	 * 鍒濆鍖栫瀛愬榻�	 * @param seed - 绉嶅瓙鏍�	 * @param original - 鍒濆鐨勭敤鏉ュ榻愭爲鐨勬爲
	 * @param map - 绉嶅瓙鏍戠鐨勮妭鐐逛笌瀵归綈鏍戠殑鏄犲皠鍏崇郴锛屽湪鍙栨暟鎹褰曟椂鏂逛究锛岀洿鎺ユ寜鐓х瀛愭爲涓�釜涓�釜鍙�	 * 				濡傛灉鍚庨潰鐨勭瀛愭爲澧炲姞浜嗚妭鐐癸紝閭ｄ箞灏卞彇涓嶅埌浜嗭紝map搴旇杩斿洖null
	 */
	private void createSeedAlignment(TagNode[] seed, TagNode[] original, Map<TagNode, TagNode> map)
	{
		/*
		 * 澶勭悊鏂瑰紡搴旇鏄細
		 * 鍥犱负seed鍜宱riginal鐨勮妭鐐规槸涓�牱鐨勶紝鎵�互鎸夌収瀵瑰簲浣嶇疆閬嶅巻涓�笅鐒跺悗瀵瑰簲璁剧疆灏辫浜�		 * 杩欓噷鏄繁搴︿紭鍏堢殑閬嶅巻
		 * 杩欎釜鍑芥暟纭畾浜嗭細record鐨別lements鏄竴绾у瓙鑺傜偣
		 */
		for (int arrayCounter=0; arrayCounter < seed.length; arrayCounter++)
		{
			map.put( seed[ arrayCounter ], original[ arrayCounter ]);

			for(int childCounter= 1; childCounter <= seed[ arrayCounter ].childrenCount(); childCounter++)
			{
				createSeedAlignment( seed[ arrayCounter ].getChildAtNumber( childCounter ), original[ arrayCounter ].getChildAtNumber( childCounter ), map);
			}
		}
	}

	private void createSeedAlignment(TagNode seed, TagNode original, Map<TagNode, TagNode> map)
	{
		map.put( seed, original);
		
		for(int childCounter= 1; childCounter <= seed.childrenCount(); childCounter++)
		{
			createSeedAlignment( seed.getChildAtNumber( childCounter ), original.getChildAtNumber( childCounter ), map);
		}
	}
}