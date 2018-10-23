package kanji;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

public class Worker implements Runnable {
	
	String stem;
	String url;
	
	public Worker(String stem, String url) {
		this.stem=stem;
		this.url=url;
	}

	@Override
	public void run() {
		try {
			long timer = System.currentTimeMillis();

			// rip data from the webpage
			PageData pageData = (new DataRipper(url)).getData();

			long datatime = System.currentTimeMillis() - timer;
			long imagetimer = System.currentTimeMillis();

			for (Dimension d : RipperMain.dimensions) {
				String outputFolder = "output/" + d.width + "x" + d.height + "/" + stem + "images";
				new File(outputFolder).mkdirs();
				ImageMaker.makeImage(pageData, d.width, d.height, outputFolder);
			}

			long time = System.currentTimeMillis();
			System.out.println(
					"data: " + datatime + " image: " + (time - imagetimer) + " full: " + (time - timer) + "ms");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
