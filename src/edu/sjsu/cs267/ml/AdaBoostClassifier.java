package edu.sjsu.cs267.ml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.sjsu.cs267.tools.Pair;
import edu.sjsu.cs267.tools.RecordSet;
import edu.sjsu.cs267.tools.Triple;
import edu.sjsu.cs267.tools.WeightedRecord;

public class AdaBoostClassifier {
	private List<Pair<Double, StumpClassifier>> weakClassifiers;

	public List<Pair<Double, StumpClassifier>> getWeakClassifiers() {
		return weakClassifiers;
	}

	public void setWeakClassifiers(
			List<Pair<Double, StumpClassifier>> weakClassifiers) {
		this.weakClassifiers = weakClassifiers;
	}

	@Override
	public String toString() {
		return weakClassifiers.toString();
	}

	public List<Pair<Double, StumpClassifier>> getSortedWeakClassifiersByWeight() {
		List<Pair<Double, StumpClassifier>> result = new ArrayList<Pair<Double, StumpClassifier>>(
				weakClassifiers);
		Collections.sort(result,
				new Comparator<Pair<Double, StumpClassifier>>() {

					@Override
					public int compare(Pair<Double, StumpClassifier> o1,
							Pair<Double, StumpClassifier> o2) {
						return Double.compare(o1.first, o2.first);
					}
				});
		return result;
	}

	public static AdaBoostClassifier build(RecordSet records,
			int numIterations, int featureValueRangeSteps, boolean earlyQuit) {
		List<Pair<Double, StumpClassifier>> weightedWeakClassifisers = new ArrayList<Pair<Double, StumpClassifier>>(
				numIterations);
		records.equalizeWeights();
		int numRecords = records.getNumRecords();
		List<Double> aggregateEstimatedClasses = new ArrayList<Double>(
				numRecords);
		for (int j = 0; j < numRecords; j++) {
			aggregateEstimatedClasses.add(0.0);
		}
		for (int i = 0; i < numIterations; i++) {
			//System.out.println("recordset weights: " + records.getWeights());

			// Build stump to generate {bestStump, error, classEst}.
			Triple<StumpClassifier, Double, List<Integer>> stump = StumpClassifier
					.build(records, featureValueRangeSteps);
			StumpClassifier weakClassifier = stump.first;
			double error = stump.second;
			List<Integer> estimatedClasses = stump.third;
			//System.out.println("estimated classes: " + estimatedClasses);

			// Calculate alpha, throw in max(error, eps) to account for error=0.
			double alpha = 0.5 * Math.log((1.0 - error)
					/ Math.max(error, 1e-16));
			// Store current weighted weak classifier.
			weightedWeakClassifisers.add(Pair.of(alpha, weakClassifier));
			System.out.printf("stored weak classifier #%d: (alpha=%f) %s\n", i,
					alpha, weakClassifier.toString());

			// Adjust weights of recordset for next iteration.
			for (int j = 0; j < numRecords; j++) {
				if (records.getClasses().get(j) != estimatedClasses.get(j)) {
					// Weak classifier predicated incorrectly.
					records.getRecords()
							.get(j)
							.setWeight(
									records.getRecords().get(j).getWeight()
											/ (2 * error));
				} else {
					// Weak classifier predicated correctly.
					records.getRecords()
							.get(j)
							.setWeight(
									records.getRecords().get(j).getWeight()
											/ (2 * (1 - error)));
				}
			}
			records.normalizeWeights();

			// Calculate training error of all classifiers generated so far, if
			// this is 0 quit for loop early (use break).
			if (earlyQuit) {
				int numAggregateError = 0;
				for (int j = 0; j < numRecords; j++) {
					aggregateEstimatedClasses.set(j,
							aggregateEstimatedClasses.get(j) + alpha
									* estimatedClasses.get(j));
					int aggregateEstimatedClass = (aggregateEstimatedClasses
							.get(j) > 0) ? 1 : -1;
					if (aggregateEstimatedClass != records.getClasses().get(j)) {
						numAggregateError++;
					}
				}
				//System.out.println("aggregate estimated classes: " + aggregateEstimatedClasses);

				double errorRate = (double) numAggregateError / numRecords;
				//System.out.println("total error: " + errorRate);
				if (errorRate == 0.0) {
					break;
				}
			}
		}

		AdaBoostClassifier c = new AdaBoostClassifier();
		c.setWeakClassifiers(weightedWeakClassifisers);
		return c;
	}

	public static void main(String[] args) {
		int numFeatures = 4;
		RecordSet records = new RecordSet(numFeatures);
		for (int i = 0; i < 1000; i++) {
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
		}
		records.normalizeWeights();

		AdaBoostClassifier c = AdaBoostClassifier.build(records, 10, 20, false);
		System.out.println(c);
		System.out.println(c.getSortedWeakClassifiersByWeight());
	}
}
