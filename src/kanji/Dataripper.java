package kanji;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Dataripper {

	/**
	 * ayy lamo no comments
	 */
	private static int poolSize = 12;
	public static String[] stems = new String[] { "n5", "n4", "n3", "n2" }; // uncomment this to run a full-scale run
	//public static String[] stems = new String[] { "ntest" }; // uncomment this to do tests
	public static Dimension[] dimensions = new Dimension[] { new Dimension(1280, 720), new Dimension(1920, 1080),
			new Dimension(2560, 1440), new Dimension(3840, 2160) };

	public static void main(String[] args) throws IOException {
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
								PageData pageData = getData(url);
								long datatime = System.currentTimeMillis() - timer;
								long imagetimer = System.currentTimeMillis();
								for (Dimension d : dimensions) {
									String outputFolder = "output/" + d.width + "x" + d.height + "/" + stem + "images";
									new File(outputFolder).mkdirs();
									makeImage(pageData, d.width, d.height, outputFolder);
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

	public static PageData getData(String url) throws MalformedURLException, IOException {
		Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
		String character = "#";
		String meaning = "can't find character meaning";
		ArrayList<String> onyomi = new ArrayList<String>();
		ArrayList<String> kunyomi = new ArrayList<String>();
		for (Element d : doc.getElementsByClass("character")) {
			character = d.text();
		}
		for (Element d : doc.getElementsByClass("kanji-details__main-meanings")) {
			meaning = d.text();
		}
		for (Element d : doc.getElementsByClass("row compounds")) {
			Elements divs = d.getElementsByClass("no-bullet");
			try {
				for (Element e : divs.get(0).children()) {
					onyomi.add(e.text());
				}
			} catch (IndexOutOfBoundsException e) {

			}
			try {
				for (Element e : divs.get(1).children()) {
					kunyomi.add(e.text());
				}
			} catch (IndexOutOfBoundsException e) {

			}
		}

		return new PageData(character, meaning, onyomi, kunyomi, 5, 5);
	}

	public static void makeImage(PageData pageData, int endWidth, int endHeight, String directory) throws IOException {
		int width = endWidth;
		int height = endHeight;

		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// fill all the image with black
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);

		// fill in all information
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("Meiryo", Font.PLAIN, scale(height, 0.78)));
		Rectangle rect = new Rectangle(scale(height, 0.01), scale(height, 0.11), g2d.getFont().getSize(),
				g2d.getFont().getSize());
		drawCenteredString(g2d, pageData.character, rect, g2d.getFont());

		g2d.setFont(new Font("Meiryo", Font.BOLD, scale(height, 0.06)));
		g2d.drawString(pageData.meaning, scale(height, 1.677777) - g2d.getFontMetrics().stringWidth(pageData.meaning),
				scale(height, 0.09));
		g2d.drawString(pageData.meaning, scale(height, 0.1), scale(height, 0.9));

		g2d.setFont(new Font("Meiryo", Font.PLAIN, scale(height, 0.04)));
		g2d.drawString("Kun reading compounds", scale(height, 0.79), scale(height, 0.16));
		drawDef(g2d, pageData.kunyomi, 0.19, height);
		g2d.setFont(new Font("Meiryo", Font.PLAIN, scale(height, 0.04)));
		g2d.drawString("On reading compounds", scale(height, 0.79), scale(height, 0.49));
		drawDef(g2d, pageData.onyomi, 0.52, height);
		g2d.dispose();

		// Save as PNG
		File file = new File(directory + "/kanji - " + pageData.character + " - "
				+ pageData.meaning.replaceAll("[^a-zA-Z0-9 -]", "_") + ".png");
		ImageIO.write(bufferedImage, "png", file);
	}

	public static int scale(int height, double val) {
		return (int) (height * val);
	}

	public static void drawDef(Graphics2D g2d, ArrayList<String> yomi, double startY, int height) {
		for (int i = 0; i < yomi.size(); i++) {
			if (yomi.get(i).indexOf("】") < 0) {
				g2d.setFont(new Font("Meiryo", Font.PLAIN, scale(height, 0.02)));
				g2d.drawString(yomi.get(i), scale(height, 0.8), scale(height, startY + 0.028 * i));
			} else {
				g2d.setFont(new Font("Meiryo", Font.PLAIN, scale(height, 0.025)));
				g2d.drawString(yomi.get(i), scale(height, 0.8), scale(height, startY + 0.008 + 0.028 * i));
			}
		}
	}

	public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
		// Get the FontMetrics
		FontMetrics metrics = g.getFontMetrics(font);
		// Determine the X coordinate for the text
		int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
		int y = rect.y + rect.height / 2 + +metrics.getHeight() / 4;
		// Set the font
		g.setFont(font);
		// Draw the String
		g.drawString(text, x, y);
	}
}