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
	 * ayy lamo no comments
	 */
	// amount of threads to run the program on
	private static int poolSize = Runtime.getRuntime().availableProcessors();
	
	// stems of info used. This determines what txt file to read and what folder to output to
	//public static String[] stems = new String[] { "n5", "n4", "n3", "n2" }; // uncomment this to run a full-scale run
	public static String[] stems = new String[] { "ntest" }; // uncomment this to do tests
	
	// lists all the image resolutions to output as
	public static Dimension[] dimensions = new Dimension[] { new Dimension(1280, 720), new Dimension(1920, 1080),
			new Dimension(2560, 1440), new Dimension(3840, 2160) };

	public static void main(String[] args) throws IOException {
		System.out.println("Thread count: "+poolSize);
		ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
		long ltimer = System.currentTimeMillis();
		for (String stem : stems) {
			String inputFileName = stem + ".txt";
			ArrayList<String> urls = new ArrayList<String>();
			FileReader fileReader = new FileReader(inputFileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				urls.add(line);
			}
			bufferedReader.close();

			for (String url : urls) {
				if (url.length() > 0) {
					System.out.println("processing: " + url);
					executorService.execute(new Runnable() {
						public void run() {
							try {
								long timer = System.currentTimeMillis();
								
								// rip data from the webpage
								PageData pageData = (new DataRipper(url)).getData();
								
								long datatime = System.currentTimeMillis() - timer;
								long imagetimer = System.currentTimeMillis();
								
								for (Dimension d : dimensions) {
									String outputFolder = "output/" + d.width + "x" + d.height + "/" + stem + "images";
									new File(outputFolder).mkdirs();
									ImageMaker.makeImage(pageData, d.width, d.height, outputFolder);
								}
								
								long time = System.currentTimeMillis();
								System.out.println("data: " + datatime + " image: " + (time - imagetimer) + " full: "
										+ (time - timer) + "ms");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}
		}
		try {
			executorService.shutdown();
			executorService.awaitTermination(1, TimeUnit.HOURS);
			System.out.println("---------full time: " + (System.currentTimeMillis() - ltimer) + "ms");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	

	
}