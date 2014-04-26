package edu.sjsu.cs267.tools;

import java.util.LinkedList;
import java.util.List;

public class RecordSet {
	private List<WeightedRecord> records;
	private final int numFeatures;

	public RecordSet(int numFeatures) {
		records = new LinkedList<WeightedRecord>();
		this.numFeatures = numFeatures;
	}

	public void append(WeightedRecord record) {
		if (record.getNumFeatures() != numFeatures) {
			throw new IllegalArgumentException(
					"Cannot add a record with mismatched number of features.");
		}
		records.add(record);
	}

	public int getNumRecords() {
		return records.size();
	}

	public int getNumFeatures() {
		return numFeatures;
	}

	public double[] getFeatureValueRange(int featureIndex) {
		double maxValue = Double.NEGATIVE_INFINITY;
		double minValue = Double.POSITIVE_INFINITY;
		for (WeightedRecord record : records) {
			if (record.getFeatureVal(featureIndex) > maxValue) {
				maxValue = record.getFeatureVal(featureIndex);
			}
			if (record.getFeatureVal(featureIndex) < minValue) {
				minValue = record.getFeatureVal(featureIndex);
			}
		}
		return new double[] { minValue, maxValue };
	}

	public List<WeightedRecord> getRecords() {
		return records;
	}

	public void normalizeWeights() {
		double sumWeight = 0.0;
		for (WeightedRecord record : records) {
			sumWeight += record.getWeight();
		}
		for (WeightedRecord record : records) {
			record.setWeight(record.getWeight() / sumWeight);
		}
	}
}
