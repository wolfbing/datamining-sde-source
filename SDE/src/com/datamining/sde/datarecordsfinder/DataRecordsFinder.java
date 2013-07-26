package com.datamining.sde.datarecordsfinder;

import com.datamining.sde.basictype.DataRecord;
import com.datamining.sde.basictype.DataRegion;

/**
 * Interface untuk mengidentifikasi data records dari suatu data region.
 * 
 * @author seagate
 *
 */

public interface DataRecordsFinder
{
	public DataRecord[] findDataRecords(DataRegion dataRegion, double similarityTreshold);
}