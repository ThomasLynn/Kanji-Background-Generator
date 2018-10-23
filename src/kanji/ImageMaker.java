package kanji;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class ImageMaker {
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

		g2d.setFont(new Font("Meiryo", Font.PLAIN, scale(height, 0.06)));
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
