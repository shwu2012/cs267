package edu.sjsu.cs267.tools;

public class Record {

	private int relevance;
	private int queryId;
	private String csvInput;
	private double[] features;

	public Record(String csvInputLine) {
		String[] fields = csvInputLine.split(",");
		relevance = Integer.parseInt(fields[0]);
		queryId = Integer.parseInt(fields[1]);
		csvInput = csvInputLine;
		features = new double[136];
		for (int i = 0; i < 136; i++) {
			features[i] = Double.parseDouble(fields[i + 2]);
		}
	}

	public void SetRelevance(int newRelevance) {
		relevance = newRelevance;
	}

	public int GetRelevance() {
		return relevance;
	}

	public void SetQueryID(int newQID) {
		queryId = newQID;
	}

	public int GetQueryId() {
		return queryId;
	}

	public double GetFeatureVal(int i) {
		if (i >= 0 && i < features.length)
			return features[i];
		else {
			System.out
					.println("I should really learn about exception: out of bounds in GetFeatureVal call, i = "
							+ i);
			return 0;
		}
	}

	public void SetFeatureVal(int i, double val) {
		if (i > 0 && i < features.length)
			features[i] = val;
		else {
			System.out
					.println("I should really learn about exception: out of bounds in SetFeatureVal call");
		}
	}

	// a.Difference(b) -> a - b for printing
	// doesn't output QID
	public String Difference(Record b) {
		String toReturn = "";
		toReturn += this.relevance - b.GetRelevance() > 0 ? 1 : -1;
		for (int i = 0; i < features.length; i++) {

			toReturn += ",";
			// boolean values 95-99 produce categorical {-1, 0 , 1} cast to int
			// so they are enum for H20
			if (i > 94 && i < 100)
				toReturn += (int) (this.features[i])
						- (int) (b.GetFeatureVal(i));
			else
				toReturn += this.features[i] - b.GetFeatureVal(i);
		}

		return toReturn;
	}

	@Override
	public String toString() {
		return csvInput;
	}

}