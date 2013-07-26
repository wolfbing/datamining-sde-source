package com.datamining.sde.columnaligner;

import com.datamining.sde.basictype.DataRecord;

public interface ColumnAligner
{
	public String[][] alignDataRecords(DataRecord[] dataRecords);
}