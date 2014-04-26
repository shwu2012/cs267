package edu.sjsu.cs267.tools;

public class WeightedRecord extends Record {

	private double weight = 1.0;

	public WeightedRecord(String csvInputLine, boolean hasQueryId,
			int numFeatures, double weight) {
		super(csvInputLine, hasQueryId, numFeatures);
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return String
				.format("{weight: %f, data: %s}", weight, super.toString());
	}
}
