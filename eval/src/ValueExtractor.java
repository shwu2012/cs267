import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class is used to extract NDCG and Normalized Errors of each query from the output file and calculate the average.
 * @author Shuang Wu
 *
 */
public class ValueExtractor {
	private final static int NUM_QUERY = 1000000;
	
	private static void readFile(String fileName) {
		ArrayList<Double> nDCGs = new ArrayList<Double>(NUM_QUERY);
		ArrayList<Double> normalizedErrors = new ArrayList<Double>(NUM_QUERY);
		double sumNDCG = 0.0;
		double sumNormalizedErrors = 0.0;
		int naNNDCG = 0;
		int naNNormalizedErrors = 0;
		
		try {
			File file = new File(fileName);
			Scanner scanner = new Scanner(file);	
			while (scanner.hasNext()) {
				String s = scanner.next();
				if (s.equals("NDCG:")){
					nDCGs.add(scanner.nextDouble());
				} else if (s.equals("Normalized")){
					scanner.next();
					normalizedErrors.add(scanner.nextDouble());					
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for(Double d : nDCGs) {
			if (d.isNaN()){
				naNNDCG++;
			} else {
				sumNDCG += d;
			}		
		}

		for(Double d : normalizedErrors) {
			if (d.isNaN()){
				naNNormalizedErrors++;
			} else {
				sumNormalizedErrors += d;
			}		
		}
		
		double avgNDCG = sumNDCG/(nDCGs.size() - naNNDCG);
		double avgNormalizedErrors = sumNormalizedErrors/(normalizedErrors.size() - naNNormalizedErrors);
		
		System.out.println("nDCGs.size(): " + nDCGs.size());
		System.out.println("nDCGs.size() - naNNDCG: " + (nDCGs.size() - naNNDCG));
		System.out.println("normalizedErrors.size(): " + normalizedErrors.size());
		System.out.println("normalizedErrors.size() - naNNormalizedErrors: " + (normalizedErrors.size() - naNNormalizedErrors));
		System.out.println();
		System.out.println("avgNDCG: " + avgNDCG);
		System.out.println("avgNormalizedErrors: " + avgNormalizedErrors);
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("usage: java ValueExtractor" + "file location");
			System.exit(0);
		}
		readFile(args[0]);
	}
}