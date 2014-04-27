package edu.sjsu.cs267;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

import edu.sjsu.cs267.ml.AdaBoostClassifier;
import edu.sjsu.cs267.ml.StumpClassifier;
import edu.sjsu.cs267.tools.DataPrep;
import edu.sjsu.cs267.tools.Pair;
import edu.sjsu.cs267.tools.RecordSet;
import edu.sjsu.cs267.tools.WeightedRecord;

public class MainDriver extends Configured implements Tool {

	private static final IntWritable CONSTANT_ONE = new IntWritable(1);

	private static final int NUM_WEAK_CLASSIFIERS = 5;

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

			RecordSet records = new RecordSet(DataPrep.NUM_FEATURES);
			for (String row : rows) {
				WeightedRecord record = new WeightedRecord(row, false,
						DataPrep.NUM_FEATURES, 1.0);
				records.append(record);
				if (record.getRelevance() == 1) {
					reporter.incrCounter(Counters.POSITIVE_DATA_POINTS, 1);
				} else if (record.getRelevance() == -1) {
					reporter.incrCounter(Counters.NEGATIVE_DATA_POINTS, 1);
				}
				reporter.incrCounter(Counters.INPUT_DATA_POINTS, 1);
				numRecords++;
			}

			AdaBoostClassifier c = AdaBoostClassifier.build(records,
					NUM_WEAK_CLASSIFIERS, 10, false);
			if (c.getWeakClassifiers().size() != NUM_WEAK_CLASSIFIERS) {
				throw new IllegalArgumentException(
						"Mismatched number of weak classifiers.");
			}
			StringBuilder sortedWeakClassifiersInText = new StringBuilder();
			for (Pair<Double, StumpClassifier> p : c
					.getSortedWeakClassifiersByWeight()) {
				sortedWeakClassifiersInText.append(p.first);
				sortedWeakClassifiersInText.append(',');
				sortedWeakClassifiersInText.append(p.second
						.getSplitFeatureIndex());
				sortedWeakClassifiersInText.append(',');
				sortedWeakClassifiersInText.append(p.second.getThreshold());
				sortedWeakClassifiersInText.append(',');
				sortedWeakClassifiersInText.append(p.second.getSplitType());
				sortedWeakClassifiersInText.append('\n');
			}
			sortedWeakClassifiersInText.setLength(sortedWeakClassifiersInText
					.length() - 1);
			// Weak classifiers have been sorted here, so no need to sort again
			// in reducer.
			output.collect(CONSTANT_ONE,
					new Text(sortedWeakClassifiersInText.toString()));

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
			List<List<Pair<Double, StumpClassifier>>> weakClassifierSetsByStrength = new ArrayList<List<Pair<Double, StumpClassifier>>>(
					NUM_WEAK_CLASSIFIERS);
			for (int i = 0; i < NUM_WEAK_CLASSIFIERS; i++) {
				// Each classifier set should contain the same number of weak
				// classifiers as the number of mappers; those weak classifiers
				// have the same strength.
				weakClassifierSetsByStrength
						.add(new LinkedList<Pair<Double, StumpClassifier>>());
			}
			int sum = 0;
			while (values.hasNext()) {
				Text text = values.next();
				String[] rows = text.toString().split("\\n");
				if (rows.length != NUM_WEAK_CLASSIFIERS) {
					throw new IllegalArgumentException(
							"Mismatched number of weak classifiers.");
				}
				int strengthIndex = 0;
				for (Pair<Double, StumpClassifier> p : createWeakClassifiers(rows)) {
					weakClassifierSetsByStrength.get(strengthIndex).add(p);
					strengthIndex++;
				}
				sum++;
			}

			for (int i = 0; i < weakClassifierSetsByStrength.size(); i++) {
				output.collect(new IntWritable(i), new Text(
						weakClassifierSetsByStrength.get(i).toString()));
			}

			reporter.setStatus("Finished collecting " + sum
					+ " sub-adaboost classifier.");
		}

		private static List<Pair<Double, StumpClassifier>> createWeakClassifiers(
				String[] rows) {
			List<Pair<Double, StumpClassifier>> result = new ArrayList<Pair<Double, StumpClassifier>>(
					rows.length);
			for (String row : rows) {
				String[] values = row.split(",");
				double alpha = Double.parseDouble(values[0]);
				StumpClassifier c = new StumpClassifier();
				c.setSplitFeatureIndex(Integer.parseInt(values[1]));
				c.setThreshold(Double.parseDouble(values[2]));
				c.setSplitType(StumpClassifier.SplitType.valueOf(values[3]));
				result.add(Pair.of(alpha, c));
			}
			return result;
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		JobConf conf = new JobConf(getConf(), MainDriver.class);
		conf.setJobName("ada_boost");

		conf.setOutputKeyClass(IntWritable.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);

		conf.setNumMapTasks(20); // Just a hint.

		// Set number of reduce tasks to 0 for map-only jobs.
		// conf.setNumReduceTasks(0);

		// Combiners can only be used on the reduce-functions that are
		// 1. commutative(a.b = b.a), and
		// 2. associative {a.(b.c) = (a.b).c}.
		// conf.setCombinerClass(Reduce.class);
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
