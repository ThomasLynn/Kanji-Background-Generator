package kanji;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class KanjiMeaningRipper {

	private String url;
	private String character;
	private String meaning;
	private ArrayList<KanjiSentence> words;

	public KanjiMeaningRipper(String url) {
		this.url = url;
		character = "#";
		meaning = "can't find character meaning";
		words = new ArrayList<KanjiSentence>();
	}

	public PageData getData() throws MalformedURLException, IOException {
		String cacheFileLoc = "cache/" + url.substring("https://jisho.org/search/".length());
		File cacheFile = new File(cacheFileLoc);
		if (!cacheFile.exists() || RipperMain.ignorecache) {
			System.out.println("creating cache file");
			cacheFile.createNewFile();
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile), "UTF-8"));
			try {
				out.write("cache file for " + url + "\n");
				Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
				for (Element d : doc.getElementsByClass("character")) {
					String character = d.text();
					out.write("c=" + character + "\n");
					int wordCount = 0;
					String sentenceURL = "https://tatoeba.org/eng/sentences/search?query=" + character
							+ "&from=jpn&to=eng";
					Document sentenceDoc = Jsoup.parse(new URL(sentenceURL).openStream(), "UTF-8", sentenceURL);
					for (Element japaneseSentence : sentenceDoc.getElementsByClass("text flex")) {
						System.out.println("fleeex: " + japaneseSentence.text());
					}
					for (Element f : sentenceDoc.getElementsByClass("sentence-and-translations")) {
						boolean success = true;
						System.out.println("tESTSETSETETSETS");
						int type = 0;
						String japaneseSentence = null;
						String englishSentence = null;
						for (Element g : f.getElementsByAttributeValue("layout", "column")) {
							System.out.println("REEEEEEEEEEEEEEE");

							for (Element k : g.getElementsByAttributeValue("class", "text")) {
								System.out.println("firest " + k.text());
								// String strings = k.text();
								/*
								 * String japaneseSentence = strings[0].substring(strings[0].lastIndexOf(" "),
								 * strings[0].length() - 1); String englishSentence =
								 * strings[1].substring(strings[1].indexOf(" ", 13), strings[1].length() - 1);
								 */
								if (type == 0) {
									japaneseSentence = k.text();
								} else {
									if (englishSentence == null) {
										englishSentence = k.text();
										if (englishSentence.length()>70) {
											success = false;
										}
									} else {
										String oldSentence = englishSentence;
										englishSentence += " / " + k.text();
										if (englishSentence.length()>70) {
											englishSentence = oldSentence;
										}
									}
								}
								type += 1;
								break;
								// Elements g = f.getElementsByClass("text");
								// System.out.println(g.getElementsByClass("text"));
								/*
								 * String[] strings = k.text().split(" info "); if (strings.length >= 2) {
								 * String japaneseSentence = strings[0].substring(strings[0].lastIndexOf(" "),
								 * strings[0].length() - 1); System.out.println(k.text()); if
								 * (japaneseSentence.contains(character)) { String englishSentence =
								 * strings[1].substring(strings[1].indexOf(" ", 13), strings[1].length() - 1);
								 * out.write("w=" + japaneseSentence + "/-/" + englishSentence + "\n"); break;
								 * }} }
								 */
							}
							/*
							 * for (Element k : g.getElementsByAttributeValue("dir", "ltr")) {
							 * System.out.println("sencond " + k.text()); // String englishSentence =
							 * strings[1].substring(strings[1].indexOf(" ", 13), // strings[1].length() -
							 * 1); englishSentence = k.text(); break;
							 * 
							 * }
							 */
							if (type >= 3) {
								break;
							}
						}
						if (success) {
							out.write("w=" + japaneseSentence + "/-/" + englishSentence + "\n");
							wordCount++;
						}
						if (wordCount >= 8) {
							break;
						}
					}
					if (wordCount >= 8) {
						break;
					}
				}
				for (Element d : doc.getElementsByClass("kanji-details__main-meanings")) {
					out.write("m=" + d.text() + "\n");
				}

				/*
				 * for (Element d : doc.getElementsByClass("row compounds")) { Elements divs =
				 * d.getElementsByClass("no-bullet"); try { for (Element e :
				 * divs.get(0).children()) { out.write("w=" + e.text() +
				 * "/-/日本語sentence 1---englishsentence1\n"); } } catch
				 * (IndexOutOfBoundsException e) {
				 * 
				 * } try { for (Element e : divs.get(1).children()) { out.write("w=" + e.text()
				 * + "/-/日本語sentence 1---englishsentence1\n"); } } catch
				 * (IndexOutOfBoundsException e) {
				 * 
				 * } }
				 */

			} finally {
				out.close();
			}
		}
		System.out.println("reading cache file");

		BufferedReader bufferedReader = new BufferedReader(new FileReader(cacheFile));
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			if (line.startsWith("c=")) {
				character = line.substring(2);
			}
			if (line.startsWith("m=")) {
				meaning = line.substring(2);
			}
			if (line.startsWith("w=")) {
				String[] strings = line.split("/-/");
				words.add(new KanjiSentence(strings[0].substring(2), strings[1]));

			}
		}
		bufferedReader.close();

		return new PageData(character, meaning, words, 6);
	}
}
