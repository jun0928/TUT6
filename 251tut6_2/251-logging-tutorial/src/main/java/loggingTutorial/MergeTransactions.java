package loggingTutorial;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The purpose of this class is to read and merge financial transactions, and print a summary:
 * - total amount 
 * - highest/lowest amount
 * - number of transactions 
 * @author jens dietrich
 */
public class MergeTransactions {
	private static DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
	private static NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.getDefault());

	static  Logger logger1 = Logger.getLogger("FILE");
	static  Logger logger2 = Logger.getLogger("TRANSACTIONS");

	public static void setlever(Logger l,Level le){
		l.setLevel(le);
	}

	public static void main(String[] args) throws IOException {
		MergeTransactions mergeTransactions = new MergeTransactions();
		setlever(logger1,Level.DEBUG);
		setlever(logger2,Level.DEBUG);
		BasicConfigurator.configure();

//SimpleLayout
		Logger rootLogger = Logger.getRootLogger();
		Appender appender =
				(Appender)rootLogger.getAllAppenders().nextElement();
		appender.setLayout(new org.apache.log4j.SimpleLayout());

		//将logger1输出到logs.txt
		logger1.addAppender(
				new org.apache.log4j.FileAppender(
						new org.apache.log4j.TTCCLayout(), "logs.txt"
				)
		);

		List<Purchase> transactions = new ArrayList<Purchase>();
		
		// read data from 4 files
		readData("src/resources/transactions1.csv",transactions);
		readData("src/resources/transactions2.csv",transactions);
		readData("src/resources/transactions3.csv",transactions);
		readData("src/resources/transactions4.csv",transactions);
		
		// print some info for the user
		logger2.info("" + transactions.size() + " transactions imported");
		logger2.info("total value: " + CURRENCY_FORMAT.format(computeTotalValue(transactions)));
		logger2.info("max value: " + CURRENCY_FORMAT.format(computeMaxValue(transactions)));
//		System.out.println("" + transactions.size() + " transactions imported");
//		System.out.println("total value: " + CURRENCY_FORMAT.format(computeTotalValue(transactions)));
//		System.out.println("max value: " + CURRENCY_FORMAT.format(computeMaxValue(transactions)));

	}
	
	private static double computeTotalValue(List<Purchase> transactions) {
		double v = 0.0;
		for (Purchase p:transactions) {
			v = v + p.getAmount();
		}
		return v;
	}
	
	private static double computeMaxValue(List<Purchase> transactions) {
		double v = 0.0;
		for (Purchase p:transactions) {
			v = Math.max(v,p.getAmount());
		}
		return v;
	}

	// read transactions from a file, and add them to a list
	private static void readData(String fileName, List<Purchase> transactions) {
		
		File file = new File(fileName);
		String line = null;
		// print info for user
		logger1.info("import data from " + fileName);
		//System.out.println("import data from " + fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine())!=null) {
				String[] values = line.split(",");
				Purchase purchase = new Purchase(
						values[0],
						Double.parseDouble(values[1]),
						DATE_FORMAT.parse(values[2])
				);
				transactions.add(purchase);
				// this is for debugging only
				logger2.debug("imported transaction " + purchase);
				//System.out.println("imported transaction " + purchase);
			} 
		}
		catch (FileNotFoundException x) {
			// print warning
			logger1.warn("file " + fileName + " does not exist - skip",x);
//			x.printStackTrace();
//			System.err.println("file " + fileName + " does not exist - skip");
		}
		catch (IOException x) {
			// print error message and details
			logger1.error("problem reading file " + fileName,x);
//			x.printStackTrace();
//			System.err.println("problem reading file " + fileName);
		}
		// happens if date parsing fails
		catch (ParseException x) { 
			// print error message and details
			logger2.error("cannot parse date from string - please check whether syntax is correct: " + line,x);
//			x.printStackTrace();
//			System.err.println("cannot parse date from string - please check whether syntax is correct: " + line);
		}
		// happens if double parsing fails
		catch (NumberFormatException x) {
			// print error message and details
			logger2.error("cannot parse double from string - please check whether syntax is correct: " + line,x);
//			System.err.println("cannot parse double from string - please check whether syntax is correct: " + line);
		}
		catch (Exception x) {
			// any other exception 
			// print error message and details
			logger1.error("exception reading data from file " + fileName + ", line: " + line,x);
//			System.err.println("exception reading data from file " + fileName + ", line: " + line);
		}
		finally {
			try {
				if (reader!=null) {
					reader.close();
				}
			} catch (IOException e) {
				// print error message and details
				logger1.error("cannot close reader used to access " + fileName,e);
//				System.err.println("cannot close reader used to access " + fileName);
			}
		}
	}

}
