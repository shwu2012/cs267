package edu.sjsu.cs267.ml;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.cs267.tools.RecordSet;
import edu.sjsu.cs267.tools.Triple;
import edu.sjsu.cs267.tools.WeightedRecord;

public class StumpClassifier {
	private static final int FEATURE_VALUE_RANGE_STEPS = 20;

	enum SplitType {
		GREATER_THAN, LESS_THAN
	}

	private int splitFeatureIndex;
	private double threshold;
	private SplitType splitType;

	public int getSplitFeatureIndex() {
		return splitFeatureIndex;
	}

	public void setSplitFeatureIndex(int splitFeatureIndex) {
		this.splitFeatureIndex = splitFeatureIndex;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public SplitType getSplitType() {
		return splitType;
	}

	public void setSplitType(SplitType splitType) {
		this.splitType = splitType;
	}

	private static List<Integer> stumpClassify(RecordSet records,
			int splitFeatureIndex, double threshold, SplitType ineqType) {
		List<Integer> predictedClasses = new ArrayList<Integer>(
				records.getNumRecords());
		for (int i = 0; i < records.getNumRecords(); i++) {
			predictedClasses.add(1);
		}
		for (int i = 0; i < records.getNumRecords(); i++) {
			if (ineqType == SplitType.LESS_THAN) { // less-than
				if (records.getRecords().get(i)
						.getFeatureVal(splitFeatureIndex) <= threshold) {
					predictedClasses.set(i, -1);
				}
			} else { // greater-than
				if (records.getRecords().get(i)
						.getFeatureVal(splitFeatureIndex) > threshold) {
					predictedClasses.set(i, -1);
				}
			}
		}
		return predictedClasses;
	}

	public static Triple<StumpClassifier, Double, List<Integer>> build(
			RecordSet records) {
		StumpClassifier bestStump = new StumpClassifier();
		int numRows = records.getNumRecords();
		int numFeatures = records.getNumFeatures();
		List<Integer> bestEstimatedClasses = new ArrayList<Integer>(numRows);
		double minError = Double.POSITIVE_INFINITY;
		// loop over all dimensions
		for (int i = 0; i < numFeatures; i++) {
			// [min_value, max_value]
			double[] valueRange = records.getFeatureValueRange(i);
			double stepSize = (valueRange[1] - valueRange[0])
					/ FEATURE_VALUE_RANGE_STEPS;
			// loop over all range in current dimension
			for (int j = -1; j <= FEATURE_VALUE_RANGE_STEPS; j++) {
				// go over less-than and greater-than
				for (SplitType ineqType : SplitType.values()) {
					double threshVal = valueRange[0] + j * stepSize;
					List<Integer> predictedVals = stumpClassify(records, i,
							threshVal, ineqType);
					double weightedError = calculateWeightedError(records,
							predictedVals);
					System.out.printf(
							"split-index %d, threshold: %f, ineq-type: %s,\n"
									+ "minError: %f, weightedError: %f\n", i,
							threshVal, ineqType, minError, weightedError);
					if (weightedError < minError) {
						minError = weightedError;
						bestEstimatedClasses = predictedVals;
						bestStump.setSplitFeatureIndex(i);
						bestStump.setThreshold(threshVal);
						bestStump.setSplitType(ineqType);
					}
				}
			}
		}
		return Triple.of(bestStump, minError, bestEstimatedClasses);
	}

	private static double calculateWeightedError(RecordSet records,
			List<Integer> predictedVals) {
		double error = 0.0;
		for (int i = 0; i < records.getNumRecords(); i++) {
			WeightedRecord record = records.getRecords().get(i);
			if (record.getRelevance() != predictedVals.get(i)) {
				error += record.getWeight();
			}
		}
		return error;
	}

	@Override
	public String toString() {
		return String.format(
				"{splitFeatureIndex=%d, threshold=%f, splitType=%s}",
				splitFeatureIndex, threshold, splitType);
	}

	public static void main(String[] args) {
		int numFeatures = 4;
		RecordSet records = new RecordSet(numFeatures);
		records.append(new WeightedRecord("1,23.5,1,2.5,3.9", false,
				numFeatures, 1.0));
		records.append(new WeightedRecord("1,33.7,2,4.4,4.6", false,
				numFeatures, 2.0));
		records.append(new WeightedRecord("1,45.1,5,3.7,6.7", false,
				numFeatures, 2.0));
		records.append(new WeightedRecord("-1,30.9,3,6.7,8.8", false,
				numFeatures, 4.0));
		records.append(new WeightedRecord("-1,29.3,4,1.0,1.1", false,
				numFeatures, 1.0));
		records.normalizeWeights();
		System.out.println(StumpClassifier.build(records));
	}

}
