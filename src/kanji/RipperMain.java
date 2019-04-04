package kanji;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RipperMain {

	/**
	 * ayy lamo who needs comments anyway?
	 */
	// amount of threads to run the program on
	public static int poolSize = 6;
	public static boolean ignorecache = false;
	//private static int poolSize = Runtime.getRuntime().availableProcessors();

	// stems of info used. This determines what txt file to read and what folder to
	// output to
	// public static String[] stems = new String[] { "n5", "n4", "n3", "n2" };
	// public static String[] stems = new String[] { "n1" };
	// uncomment this to do a proper run
	//private static String[] stems = new String[] { "ntest" };
	private static String[] stems = new String[] { "n5" };
	// uncomment this to do tests

	// lists all the image resolutions to output as
	public static Dimension[] dimensions = new Dimension[] { new Dimension(1280, 720), new Dimension(1920, 1080),
			new Dimension(2560, 1440), new Dimension(3840, 2160) };
	
	public static ArrayList<String> frequentWords;

	public static void main(String[] args) throws IOException, InterruptedException {
		
		System.out.println("Reading frequency list file");
		frequentWords = new ArrayList<String>();
		BufferedReader bufferedReader1 = new BufferedReader(new FileReader("japanese frequent words.txt"));
		String line1 = null;
		while ((line1 = bufferedReader1.readLine()) != null) {
			frequentWords.add(line1);
		}
		bufferedReader1.close();

		System.out.println("Thread count: " + poolSize);
		ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
		new File("cache").mkdirs();
		new File("pitchcache").mkdirs();
		new File("sentencecache").mkdirs();

		long ltimer = System.currentTimeMillis();

		SyncCounter.resetCounter();
		for (String stem : stems) {
			System.out.println("Reading url file");
			ArrayList<String> urls = new ArrayList<String>();
			BufferedReader bufferedReader = new BufferedReader(new FileReader(stem + ".txt"));
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				urls.add(line);
			}
			bufferedReader.close();

			for (String url : urls) {
				if (url.length() > 0) {
					SyncCounter.incCounter();
					executorService.execute(new Worker(stem, url));
				}
			}
		}
		executorService.shutdown();
		while (!executorService.isTerminated()) {
			System.out.println("Workers remaining: " + SyncCounter.getCounter() + " time: "
					+ ((System.currentTimeMillis() - ltimer) / 1000) + "s");
			executorService.awaitTermination(10, TimeUnit.SECONDS);
		}
		System.out.println("full execution time: " + (System.currentTimeMillis() - ltimer) + "ms");
		
	}

}