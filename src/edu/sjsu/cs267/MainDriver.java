package edu.sjsu.cs267;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import edu.sjsu.cs267.tools.Record;

public class MainDriver extends Configured implements Tool {

	private static final IntWritable CONSTANT_ONE = new IntWritable(1);

	public static class Map extends MapReduceBase implements
			Mapper<IntWritable, Text, IntWritable, Text> {

		static enum Counters {
			INPUT_DATA_POINTS, POSITIVE_DATA_POINTS, NEGATIVE_DATA_POINTS,
		}

		private long numRecords = 0;

		@Override
		public void configure(JobConf job) {
			// Read configurations here.
		}

		@Override
		public void map(IntWritable key, Text value,
				OutputCollector<IntWritable, Text> output, Reporter reporter)
				throws IOException {
			if (key.get() < 1 || value.toString().trim() == "") {
				return;
			}

			String[] rows = value.toString().split("\\n");
			if (rows.length != key.get()) {
				throw new IllegalArgumentException(
						"Mismatched number of data points.");
			}

			for (String row : rows) {
				Record dataRecord = new Record(row, false);
				if (dataRecord.getRelevance() == 1) {
					reporter.incrCounter(Counters.POSITIVE_DATA_POINTS, 1);
				} else if (dataRecord.getRelevance() == -1) {
					reporter.incrCounter(Counters.NEGATIVE_DATA_POINTS, 1);
				}
				reporter.incrCounter(Counters.INPUT_DATA_POINTS, 1);
				numRecords++;
			}

			output.collect(CONSTANT_ONE, new Text("sub_ada_boost_classifier"));

			if ((++numRecords % 100) == 0) {
				reporter.setStatus("Finished processing " + numRecords
						+ " records.");
			}
		}
	}

	public static class Reduce extends MapReduceBase implements
			Reducer<IntWritable, Text, IntWritable, Text> {
		@Override
		public void reduce(IntWritable key, Iterator<Text> values,
				OutputCollector<IntWritable, Text> output, Reporter reporter)
				throws IOException {
			int sum = 0;
			while (values.hasNext()) {
				sum++;
			}
			output.collect(CONSTANT_ONE,
					new Text(String.format("Merged %d classifiers.", sum)));
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		JobConf conf = new JobConf(getConf(), MainDriver.class);
		conf.setJobName("ada_boost");

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);
		conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(DataPointsInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new MainDriver(), args);
		System.exit(res);
	}
}
