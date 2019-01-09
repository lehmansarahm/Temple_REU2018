package filegenerator;

import java.util.*;
import java.io.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class FileGenerator {

	private ArrayList<String> featureDataGeorgia;		//Features from Georgia Tech paper
	//private ArrayList<String> featureDataCalifornia;	//Features from UCLA

	private File rawDataFile;							//File containing raw data from device
	private File featureFileGeorgia;					//File containing Georgia tech features
	//private File featureFileCalifornia;

	private String[] values;							//Time, x, y, z for one line of data

	private DescriptiveStatistics xFeatures;			//Window for x
	private DescriptiveStatistics yFeatures;			//Window for y
	private DescriptiveStatistics zFeatures;			//Window for z

	private FileReader fr;								//to read raw data file
	private BufferedReader br;							//to read raw data file line by line

	private boolean isE4;								//true if the file came from E4

	/**
	 * Create a new file and readers for that file
	 * @param f - raw data file
	 */
	public void setRawDataFile(File f) {
		rawDataFile = f;

		try {
			fr = new FileReader(rawDataFile);
			br = new BufferedReader(fr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add values to windows and call feature methods
	 * @param timestamp
	 * @param values - x, y, z
	 */
	private void calculateFeatures(String timestamp, float[] values) {
		xFeatures.addValue(values[0]);
		yFeatures.addValue(values[1]);
		zFeatures.addValue(values[2]);

		calculateGeorgia(timestamp, values);
//		calculateCalifornia(timestamp, values);

	}

	/**
	 * Derive mean, variance, skewness, kurtosis, and RMS for each axis, add to ArrayList in CSV format
	 * @param timestamp
	 * @param values - x, y, z
	 */
	private void calculateGeorgia(String timestamp, float[] values) {
		String line = timestamp + "," + xFeatures.getMean() + "," + xFeatures.getVariance() + ","
				+ xFeatures.getSkewness() + "," + xFeatures.getKurtosis() + "," + calculateRMS(xFeatures) + ","
				+ yFeatures.getMean() + "," + yFeatures.getVariance() + "," + yFeatures.getSkewness() + ","
				+ yFeatures.getKurtosis() + "," + calculateRMS(yFeatures) + "," + zFeatures.getMean() + ","
				+ zFeatures.getVariance() + "," + zFeatures.getSkewness() + "," + zFeatures.getKurtosis() + ","
				+ calculateRMS(zFeatures) + "\n";

		featureDataGeorgia.add(line);
	}

//	private void calculateCalifornia(String timestamp, float[] accValues) {
//		double[] xSorted = xFeatures.getSortedValues();
//		double[] ySorted = yFeatures.getSortedValues();
//		double[] zSorted = zFeatures.getSortedValues();
//
//		double xMedian = xSorted[(int) xFeatures.getN() / 2];
//		double yMedian = ySorted[(int) yFeatures.getN() / 2];
//		double zMedian = zSorted[(int) zFeatures.getN() / 2];
//
//		String line = timestamp + "," + Math.abs(accValues[0]) + "," + xMedian + "," + xFeatures.getMean() + ","
//				+ xFeatures.getMax() + "," + xFeatures.getMin() + "," + (xFeatures.getMax() - xFeatures.getMin()) + ","
//				+ xFeatures.getVariance() + "," + xFeatures.getStandardDeviation() + "," + calculateRMS(xFeatures) + ","
//				+ xFeatures.getSkewness() + "," + Math.abs(accValues[1]) + "," + yMedian + "," + yFeatures.getMean()
//				+ "," + yFeatures.getMax() + "," + yFeatures.getMin() + "," + (yFeatures.getMax() - yFeatures.getMin())
//				+ "," + yFeatures.getVariance() + "," + yFeatures.getStandardDeviation() + "," + calculateRMS(yFeatures)
//				+ "," + yFeatures.getSkewness() + "," + Math.abs(accValues[2]) + "," + zMedian + ","
//				+ zFeatures.getMean() + "," + zFeatures.getMax() + "," + zFeatures.getMin() + ","
//				+ (zFeatures.getMax() - zFeatures.getMin()) + "," + zFeatures.getVariance() + ","
//				+ zFeatures.getStandardDeviation() + "," + calculateRMS(zFeatures) + "," + zFeatures.getSkewness()
//				+ "\n";
//
//		featureDataCalifornia.add(line);
//	}

	/**
	 * Calculates RMS (sum of squares divided by n) for a given axis
	 * @param signal - window for one axis
	 * @return stringified RMS
	 */
	private String calculateRMS(DescriptiveStatistics signal) {
		return "" + (signal.getSumsq() / signal.getN());
	}

	/**
	 * Create files, header lines in ArrayLists
	 */
	public void generateFiles() {

		int windowSize;
		
		//if is E4
		if(rawDataFile.getPath().startsWith("E4")) {
			windowSize = 32; //1 seconds
		} else {
			windowSize = 25; //1 seconds
		}

		featureDataGeorgia = new ArrayList<>();
		//featureDataCalifornia = new ArrayList<>();
		featureDataGeorgia.add("Time,Mean-X,Variance-X,Skewness-X,Kurtosis-X,RMS-X,Mean-Y,Variance-Y,"
				+ "Skewness-Y,Kurtosis-Y,RMS-Y,Mean-Z,Variance-Z,Skewness-Z,Kurtosis-Z,RMS-Z\n");
		/*featureDataCalifornia.add("Time,Amplitude-X,Median-X,Mean-X,Max-X,Min-X,Peak-to-Peak-X,"
				+ "Variance-X,Std Dev-X,RMS-X,Skewness-X,Amplitude-Y,Median-Y,Mean-Y,Max-Y,Min-Y,"
				+ "Peak-to-Peak-Y,Variance-Y,Std Dev-Y,RMS-Y,Skewness-Y,Amplitude-Z,Median-Z,Mean-Z,"
				+ "Max-Z,Min-Z,Peak-to-Peak-Z,Variance-Z,Std Dev-Z,RMS-Z,Skewness-Z\n");*/

		
		
		xFeatures = new DescriptiveStatistics(windowSize);
		yFeatures = new DescriptiveStatistics(windowSize);
		zFeatures = new DescriptiveStatistics(windowSize);

		featureFileGeorgia = new File(
				(rawDataFile.getPath().substring(0, rawDataFile.getPath().length() - 4) + "-GA3.csv")); //Number optional
		//featureFileCalifornia = new File(
		//		(rawDataFile.getPath().substring(0, rawDataFile.getPath().length() - 4) + "-CA3.csv"));
		try {
			if (!featureFileGeorgia.exists()) {
				featureFileGeorgia.createNewFile();
			}
			/*if (!featureFileCalifornia.exists()) {
				featureFileCalifornia.createNewFile();
			}*/

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Georgia path: " + featureFileGeorgia.getPath());
		//System.out.println("CA path: " + featureFileCalifornia.getPath());

		readFile();
		dumpData(featureFileGeorgia, featureDataGeorgia);
		//dumpData(featureFileCalifornia, featureDataCalifornia);

	}

	/**
	 * Read file line by line and calculate features
	 */
	private void readFile() {
		String line = "";
		String[] stringValues;
		float[] values = new float[3];
		try {
			br.readLine(); //Skip headers in first line
			while ((line = br.readLine()) != null) {

				stringValues = line.split(",");
				for (int i = 0; i < stringValues.length - 1; i++) {
					values[i] = Float.parseFloat(stringValues[i + 1]);		//Convert string X, Y, Z to floats
				}

				calculateFeatures(stringValues[0], values);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes data to file line by line
	 * @param file - file to be written to
	 * @param data - ArrayList containing the data in CSV format
	 */
	private void dumpData(File file, ArrayList<String> data) {
		try {
            FileWriter writer = new FileWriter(file);
            int size = data.size();
            for (int i = 0; i < size; i++) {
                writer.write(data.get(i));
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	/**
	 * Prompt user for file path, generate files, prompt to continue
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		String path;
		String response;
		File f;
		boolean moreFiles = true;

		FileGenerator program = new FileGenerator();

		while (moreFiles) {
			System.out.print("Enter file path: ");
			path = input.nextLine();
			f = new File(path);
			program.setRawDataFile(f);

			program.generateFiles();

			System.out.print("Continue? Y/N: ");
			response = input.nextLine();
			moreFiles = response.equalsIgnoreCase("Y");

		}
		System.out.println("End");
		input.close();
	}

}
