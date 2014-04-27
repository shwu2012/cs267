package edu.sjsu.cs267;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

public class DataPointsInputFormat extends FileInputFormat<IntWritable, Text> {

	private static final int SUB_DATA_SET_SIZE = 1000;

	@Override
	public RecordReader<IntWritable, Text> getRecordReader(InputSplit input,
			JobConf job, Reporter reporter) throws IOException {

		reporter.setStatus(input.toString());
		return new DataPointsRecordReader(job, (FileSplit) input,
				SUB_DATA_SET_SIZE);
	}
}
