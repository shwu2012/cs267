package edu.sjsu.cs267.tools;

public class Record {

	private static final int NUM_OF_FEATURES = 136;

	private int relevance;
	private int queryId;
	private String csvInput;
	private double[] features;

	public Record(String csvInputLine) {
		String[] fields = csvInputLine.split(",");
		relevance = Integer.parseInt(fields[0]);
		queryId = Integer.parseInt(fields[1]);
		csvInput = csvInputLine;
		features = new double[NUM_OF_FEATURES];
		for (int i = 0; i < NUM_OF_FEATURES; i++) {
			features[i] = Double.parseDouble(fields[i + 2]);
		}
	}

	public void setRelevance(int newRelevance) {
		relevance = newRelevance;
	}

	public int getRelevance() {
		return relevance;
	}

	public void setQueryId(int newQID) {
		queryId = newQID;
	}

	public int getQueryId() {
		return queryId;
	}

	public double getFeatureVal(int i) {
		return features[i];
	}

	public void setFeatureVal(int i, double val) {
		features[i] = val;
	}

	// a.Difference(b) -> a - b for printing
	// doesn't output QID
	public String difference(Record b) {
		String toReturn = "";
		toReturn += this.relevance - b.getRelevance() > 0 ? 1 : -1;
		for (int i = 0; i < features.length; i++) {

			toReturn += ",";
			// boolean values 95-99 produce categorical {-1, 0 , 1} cast to int
			// so they are enum for H20
			if (i > 94 && i < 100)
				toReturn += (int) (this.features[i])
						- (int) (b.getFeatureVal(i));
			else
				toReturn += this.features[i] - b.getFeatureVal(i);
		}

		return toReturn;
	}

	@Override
	public String toString() {
		return csvInput;
	}

}
