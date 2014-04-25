package edu.sjsu.cs267;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.LineRecordReader;
import org.apache.hadoop.mapred.RecordReader;

public class DataPointsRecordReader implements RecordReader<IntWritable, Text> {

	private final LineRecordReader lineReader;
	private final LongWritable lineKey;
	private final Text lineValue;
	private final int numRows;

	public DataPointsRecordReader(JobConf job, FileSplit split, int numRows)
			throws IOException {
		lineReader = new LineRecordReader(job, split);
		lineKey = lineReader.createKey();
		lineValue = lineReader.createValue();
		this.numRows = numRows;
	}

	@Override
	public void close() throws IOException {
		lineReader.close();
	}

	@Override
	public IntWritable createKey() {
		return new IntWritable(0);
	}

	@Override
	public Text createValue() {
		return new Text("");
	}

	@Override
	public long getPos() throws IOException {
		return lineReader.getPos();
	}

	@Override
	public float getProgress() throws IOException {
		return lineReader.getProgress();
	}

	@Override
	public boolean next(IntWritable key, Text value) throws IOException {
		// Get the next line.
		int index = 0;
		StringBuilder sb = new StringBuilder();
		while (lineReader.next(lineKey, lineValue)) {
			sb.append(lineValue.toString());
			sb.append('\n');
			index++;
			if (index >= numRows) {
			    break;
			}
		}

		if (index == 0) {
			return false;
		}

		// Remove last line-feed.
		sb.setLength(sb.length() - 1);
		// Key is number of data points.
		key.set(index);
		// Value is the data points separated by line-feed.
		value.set(sb.toString());
		return true;
	}

}
