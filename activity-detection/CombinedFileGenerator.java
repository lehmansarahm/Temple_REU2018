package combinedFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class CombinedFileGenerator {

	private File moto; // original Moto file
	private File e4; // original E4 file
	private File combined; // file containing data from both files
	private ArrayList<String> data; // for each line of data: Time, X-axis of Moto, Y-axis of Moto, Z-axis of Moto,
									// X-axis of E4, Y-axis of E4, Z-axis of E4

	private FileReader fr_moto; // to read Moto file
	private BufferedReader br_moto; // to read Moto file line by line
	private FileReader fr_e4; // to read E4 file
	private BufferedReader br_e4; // to read E4 file line by line

	/**
	 * Getter for the Moto file
	 * 
	 * @return original Moto File
	 */
	public File getMoto() {
		return moto;
	}

	/**
	 * Create a new File with the given path and set to Moto. Create file and
	 * buffered readers for the file.
	 * 
	 * @param path
	 *            - path of new file
	 */
	public void setMoto(String path) {
		this.moto = new File(path);
		try {
			fr_moto = new FileReader(moto);
			br_moto = new BufferedReader(fr_moto);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Getter for the E4 file
	 * 
	 * @return original E4 File
	 */
	public File getE4() {
		return e4;
	}

	/**
	 * Create a new File with the given path and set to E4. Create file and buffered
	 * readers for the file.
	 * 
	 * @param path
	 *            - path of new file
	 */
	public void setE4(String path) {
		this.e4 = new File(path);
		try {
			fr_e4 = new FileReader(e4);
			br_e4 = new BufferedReader(fr_e4);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the combined file, header line in data ArrayList, and calls extractData() to begin the reading process
	 */
	public void generateFile() {
		//get path minus file name
		String path = moto.getPath().substring(0, moto.getPath().length() - moto.getName().length());
		//combine path and new file name
		//new file name is Moto file name minus "Moto_", plus "_combined"
		path = path + moto.getName().substring(5, moto.getName().length() - 4) + "_combined.csv";

		data = new ArrayList<String>();
		data.add("Time,X-Moto,Y-Moto,Z-Moto,X-E4,Y-E4,Z-E4\n");		//Header line

		//Create file
		try {
			combined = new File(path);
			if (!combined.exists()) {
				combined.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		extractData();
	}

	/**
	 * Compare timestamps of 
	 */
	private void extractData() {
		String mLine = "";
		String eLine = "";
		String[] mValues, eValues;
		try {
			br_moto.readLine(); // Skip headers in first line
			br_e4.readLine();	// Skip headers in first line
			mLine = br_moto.readLine();
			eLine = br_e4.readLine();
			//Stop when either or both files run out of data
			while (mLine != null && eLine != null) {	
				mValues = mLine.split(",");		//separate into an array of size 4 (time, x, y, z)
				eValues = eLine.split(",");
				int comparison = addToArray(mValues, eValues);		//pos if E4 goes first, neg if Moto goes first, 0 if equal
				
				if (comparison >= 0) {				//If E4 line is written, E4 reader steps
					eLine = br_e4.readLine();
				} else if (comparison <= 0) {		//If Moto line is written, Moto reader steps
					mLine = br_moto.readLine();
				}
			}
			
			while (mLine != null) {				//Does Moto file still have data to be read?
				mValues = mLine.split(",");
				addToArray(mValues, true);		//write
				mLine = br_moto.readLine();		//step
			}
			
			while (eLine != null) {				//Does E4 file still have data to be read?
				eValues = eLine.split(",");
				addToArray(eValues, false);		//write
				eLine = br_e4.readLine();		//step
			}
			
			/* Here we have read all of the data from both files and written them line by line into the ArrayList. Next we need to put that data into the new file.*/
			dumpData(combined, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds one line of data to the array, assumes there are no values from the other file
	 * @param line - line of data to be added to the ArrayList
	 * @param isMoto - true if the line comes from the Moto file
	 */
	private void addToArray(String[] line, boolean isMoto) {
		if (isMoto) {
			data.add(line[0] + "," + line[1] + "," + line[2] + "," + line[3] + ",,,\n");		//blanks for E4 values
		} else {
			data.add(line[0] + ",,,," + line[1] + "," + line[2] + "," + line[3] + "\n");		//blanks for Moto values
		}
	}

	/**
	 * Compares timestamps of both lines and puts the appropriate values into the ArrayList
	 * @param motoTime - line of data from the Moto File
	 * @param e4Time - line of data from the E4 File
	 * @return positive int if E4 comes first, negative int if Moto comes first, 0 if equal
	 */
	private int addToArray(String[] motoTime, String[] e4Time) {
		int comparison = motoTime[0].compareTo(e4Time[0]);

		if (comparison > 0) {			//Did E4 come first
			data.add(e4Time[0] + ",,,," + e4Time[1] + "," + e4Time[2] + "," + e4Time[3] + "\n");					 //Blanks for Moto

		} else if (comparison < 0) {	//Did Moto come first
			data.add(motoTime[0] + "," + motoTime[1] + "," + motoTime[2] + "," + motoTime[3] + ",,,\n");			 //Blanks for E4

		} else {						//Equal timestamps
			data.add(motoTime[0] + "," + motoTime[1] + "," + motoTime[2] + "," + motoTime[3] + "," + e4Time[1] + ","//No blanks	
					+ e4Time[2] + "," + e4Time[3] + "\n");
		}

		return comparison;
	}

	/**
	 * Writes data to the file
	 * @param file - file to be written to
	 * @param data - ArrayList containing all of the data line by line in CSV format
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
	 * Prompts user for file paths of Moto and E4 files, starts the combining process, prompts for more files
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		String path;
		String response;
		boolean moreFiles = true;

		CombinedFileGenerator program = new CombinedFileGenerator();

		while (moreFiles) {
			System.out.print("Enter Moto file path: ");
			path = input.nextLine();
			program.setMoto(path);

			System.out.print("Enter E4 file path: ");
			path = input.nextLine();
			program.setE4(path);

			program.generateFile();

			System.out.print("Continue? Y/N: ");
			response = input.nextLine();
			moreFiles = response.equalsIgnoreCase("Y");

		}
		System.out.println("End");
		input.close();

	}
}
