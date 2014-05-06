import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class AdaboostModel implements MSLRbinaryModel{

	private final static String MODEL_FILE = "C:/Users/jo/Dropbox/CS267/model.txt";
	private ArrayList<Double> featureValues = null;
	private ArrayList<ArrayList<WeakClassifier>> model = new ArrayList<ArrayList<WeakClassifier>>(10);
	
	public AdaboostModel() {
		try {
			buildModelFromFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// The format of each line is
	// "weight, featureIndex, threshold, splitType, weight, featureIndex, threshold, splitType, ..."
	// Each line represents weak classifiers in the same strength group. The
	// first line is the weakest while the last line is the strongest.
	public void buildModelFromFile() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(MODEL_FILE));
		String line = in.readLine();
		int lineNum = 0;
		while(line != null){
			String[] ss = line.split(", ");
			model.add(new ArrayList<WeakClassifier>(250));
			for(int i = 0; i < ss.length; i=i+4){
				model.get(lineNum).add(new WeakClassifier());
				model.get(lineNum).get(i/4).setWeight(Double.parseDouble(ss[i]));
				model.get(lineNum).get(i/4).setFeatureIndex(Integer.parseInt(ss[i+1]));
				model.get(lineNum).get(i/4).setThreshold(Double.parseDouble(ss[i+2]));
				model.get(lineNum).get(i/4).setSplitType(SplitType.valueOf(ss[i+3]));
			}
			lineNum++;
			line = in.readLine();
		}	
	}
	
	@Override
	public int predict(String difference) {		
		featureValues = getFeatureValues(difference);
		int numRows = model.size();
		int numCols = model.get(0).size();
		ArrayList<Integer> groupVotes = new ArrayList<Integer>(numRows);
		ArrayList<Double> groupWeights = new ArrayList<Double>(numRows);		
		for(int row = 0; row < numRows; row++){
			int voteResult = 0;
			double weight = 0.0;
			for(int col = 0; col < numCols; col++){
				voteResult += model.get(row).get(col).evaluate(featureValues);
				weight += model.get(row).get(col).getWeight();
			}
			groupVotes.add(Integer.signum(voteResult));
			groupWeights.add(weight / numCols);
		}
		
		double result = 0.0;
		for(int i = 0; i < numRows; i++){
			result += groupVotes.get(i)*groupWeights.get(i);
		}
		
		
		return result > 0 ? 1 : -1;
	}
	
	public ArrayList<Double> getFeatureValues(String difference){
		ArrayList<Double> featureValues = new ArrayList<Double>();
		String[] s = difference.split(",");
		for (int i = 0; i < 136; i++){
			featureValues.add(i, Double.parseDouble(s[i+1]));
		}
		return featureValues;
	}
	
	public enum SplitType {
		GREATER_THAN, LESS_THAN
	}
	
	private static class WeakClassifier {
		
		private double weight;
		private int featureIndex;
		private double threshold;
		private SplitType splitType;
		
		public double getWeight() {
			return weight;
		}
		public void setWeight(double weight) {
			this.weight = weight;
		}
		public void setFeatureIndex(int featureIndex) {
			this.featureIndex = featureIndex;
		}
		public void setThreshold(double threshold) {
			this.threshold = threshold;
		}
		public void setSplitType(SplitType splitType) {
			this.splitType = splitType;
		}
		
		public int evaluate(ArrayList<Double> featureValues){
			double featureValue = featureValues.get(featureIndex);
			if (splitType == SplitType.GREATER_THAN){
				return featureValue > threshold ? -1:1;  
			} else {
				return featureValue <= threshold ? -1:1;
			}
		}
		
	}

}
