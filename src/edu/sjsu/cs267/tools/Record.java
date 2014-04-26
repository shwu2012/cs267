package edu.sjsu.cs267.tools;

public class Record {

	private int relevance = -1;
	private int queryId = -1;
	private double[] features;

	public Record(String csvInputLine, boolean hasQueryId, int numFeatures) {
		// Check number of features.
		int numCsvFields = hasQueryId ? numFeatures + 2 : numFeatures + 1;
		String[] fields = csvInputLine.split(",");
		if ((fields == null) || (fields.length != numCsvFields)) {
			throw new IllegalArgumentException("Mismatched CSV row.");
		}

		features = new double[numFeatures];
		relevance = Integer.parseInt(fields[0]);
		int featuresIndexOffset = 1;
		if (hasQueryId) {
			queryId = Integer.parseInt(fields[1]);
			featuresIndexOffset = 2;
		}
		for (int i = 0; i < features.length; i++) {
			features[i] = Double.parseDouble(fields[i + featuresIndexOffset]);
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

	public int getNumFeatures() {
		return features.length;
	}

	// a.Difference(b) -> (a - b)
	public Record difference(Record b) {
		StringBuilder sb = new StringBuilder();
		// Output label: 1 or -1.
		sb.append(this.relevance - b.getRelevance() > 0 ? 1 : -1);
		// Does not output queryId.
		// Output features.
		for (int i = 0; i < features.length; i++) {
			sb.append(',');
			sb.append(this.features[i] - b.getFeatureVal(i));
		}
		return new Record(sb.toString(), false, features.length);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(relevance);
		if (queryId >= 0) {
			sb.append(',');
			sb.append(queryId);
		}
		for (double val : features) {
			sb.append(',');
			sb.append(val);
		}
		return sb.toString();
	}
}
