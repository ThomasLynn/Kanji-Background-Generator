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

	PageData pageData;
	int endWidth;
	int endHeight;
	String directory;

	public ImageMaker(PageData pageData, int endWidth, int endHeight, String directory) {
		this.pageData = pageData;
		this.endWidth = endWidth;
		this.endHeight = endHeight;
		this.directory = directory;
	}

	public void makeImage() throws IOException {
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

		g2d.setFont(new Font("Sans-serif", Font.PLAIN, scale(height, 0.06)));
		g2d.drawString(pageData.meaning, scale(height, 1.677777) - g2d.getFontMetrics().stringWidth(pageData.meaning),
				scale(height, 0.09));
		g2d.drawString(pageData.meaning, scale(height, 0.1), scale(height, 0.9));

		// g2d.setFont(new Font("Meiryo", Font.PLAIN, scale(height, 0.04)));
		drawDef(g2d, pageData.words, 0.19, height);
		g2d.dispose();

		// Save as PNG
		File file = new File(directory + "/kanji - " + pageData.character + " - "
				+ pageData.meaning.replaceAll("[^a-zA-Z0-9 -]", "_") + ".png");
		ImageIO.write(bufferedImage, "png", file);
	}

	private int scale(int height, double val) {
		return (int) (height * val);
	}

	private boolean isSmallKana(char ch) {
		if (ch == 'ょ') {
			return true;
		}
		if (ch == 'ゅ') {
			return true;
		}
		if (ch == 'ゃ') {
			return true;
		}
		return false;
	}

	private void drawDef(Graphics2D g2d, ArrayList<KanjiSentence> kanjiSentences, double startY, int height) {
		int line = 0;
		Font kanjiFont = new Font("Meiryo", Font.PLAIN, scale(height, 0.033));
		Font englishFont = new Font("Sans-serif", Font.PLAIN, scale(height, 0.03));
		Font kanaFont = new Font("Meiryo", Font.PLAIN, scale(height, 0.02));
		for (int i = 0; i < kanjiSentences.size(); i++) {
			g2d.setFont(kanaFont);
			int drawHeight = scale(height, startY + getLineOffset(line) + 0.01f);
			g2d.drawString(kanjiSentences.get(i).kanaSentence, scale(height, 0.8), drawHeight);
			drawHeight = scale(height, startY + getLineOffset(line) + 0.01f) - g2d.getFontMetrics(kanaFont).getAscent();
			int oldStringPos = 0;
			int stringPos = 0;
			for (int k = 0; k < kanjiSentences.get(i).pitchSentence.length(); k++) {
				oldStringPos = stringPos;
				if (stringPos + 1 < kanjiSentences.get(i).kanaSentence.length()) {
					if (isSmallKana(kanjiSentences.get(i).kanaSentence.charAt(stringPos + 1))) {
						stringPos++;
					}else if(kanjiSentences.get(i).kanaSentence.charAt(stringPos) == '、'
							|| kanjiSentences.get(i).kanaSentence.charAt(stringPos) == '？'
							|| kanjiSentences.get(i).kanaSentence.charAt(stringPos) == '。'
							|| kanjiSentences.get(i).kanaSentence.charAt(stringPos) == '！') {
						stringPos++;
						oldStringPos++;
					}
				}
				stringPos++;
				if (kanjiSentences.get(i).pitchSentence.charAt(k) == '1') {
					Color tempColor = g2d.getColor();
					g2d.setColor(Color.LIGHT_GRAY);
					g2d.fillRect(
							scale(height, 0.8) + g2d.getFontMetrics(kanaFont)
									.stringWidth(kanjiSentences.get(i).kanaSentence.substring(0, oldStringPos)),
							drawHeight,
							g2d.getFontMetrics(kanaFont)
									.stringWidth(kanjiSentences.get(i).kanaSentence.substring(0, stringPos))
									- g2d.getFontMetrics(kanaFont)
											.stringWidth(kanjiSentences.get(i).kanaSentence.substring(0, oldStringPos)),
							3);
					g2d.setColor(tempColor);
				}
				if (kanjiSentences.get(i).pitchSentence.charAt(k) == '2') {
					Color tempColor = g2d.getColor();
					g2d.setColor(Color.LIGHT_GRAY);
					g2d.fillRect(
							scale(height, 0.8) + g2d.getFontMetrics(kanaFont)
									.stringWidth(kanjiSentences.get(i).kanaSentence.substring(0, oldStringPos)),
							drawHeight,
							g2d.getFontMetrics(kanaFont)
									.stringWidth(kanjiSentences.get(i).kanaSentence.substring(0, stringPos))
									- g2d.getFontMetrics(kanaFont)
											.stringWidth(kanjiSentences.get(i).kanaSentence.substring(0, oldStringPos)),
							3);
					g2d.fillRect(
							scale(height, 0.8) + g2d.getFontMetrics(kanaFont)
									.stringWidth(kanjiSentences.get(i).kanaSentence.substring(0, stringPos)),
							drawHeight, 3, g2d.getFontMetrics(kanaFont).getAscent() / 3);
					g2d.setColor(tempColor);
				}

			}
			line++;
			g2d.setFont(kanjiFont);
			g2d.drawString(kanjiSentences.get(i).kanjiSentence, scale(height, 0.8),
					scale(height, startY + getLineOffset(line)));
			line++;
		}
		line++;
		for (int i = 0; i < kanjiSentences.size(); i++) {
			g2d.setFont(englishFont);
			g2d.drawString(kanjiSentences.get(i).englishSentence, scale(height, 0.8),
					scale(height, startY + getLineOffset(line)));
			line++;

		}
	}

	private float getLineOffset(int line) {
		return line * 0.042f;
	}

	private void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
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
