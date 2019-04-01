package kanji;

import java.awt.Dimension;
import java.io.File;

public class Worker implements Runnable {

	String stem;
	String url;

	public Worker(String stem, String url) {
		this.stem = stem;
		this.url = url;
	}

	@Override
	public void run() {
		try {
			// rip data from the webpage
			PageData pageData = (new KanjiMeaningRipper(url)).getData();
			for (Dimension d : RipperMain.dimensions) {
				String outputFolder = "output/" + d.width + "x" + d.height + "/" + stem + "images";
				new File(outputFolder).mkdirs();
				ImageMaker imageMaker = new ImageMaker (pageData, d.width, d.height, outputFolder);
				imageMaker.makeImage();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		SyncCounter.decCounter();
	}

}
