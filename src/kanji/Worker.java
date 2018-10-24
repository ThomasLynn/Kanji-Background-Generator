package kanji;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

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
			PageData pageData = (new DataRipper(url)).getData();
			for (Dimension d : RipperMain.dimensions) {
				String outputFolder = "output/" + d.width + "x" + d.height + "/" + stem + "images";
				new File(outputFolder).mkdirs();
				ImageMaker imageMaker = new ImageMaker (pageData, d.width, d.height, outputFolder);
				imageMaker.makeImage();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		SyncCounter.decCounter();
	}

}
