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

public class KanjiMeaningRipper {

	private String url;
	private String character;
	private String meaning;
	private ArrayList<KanjiWord> words;

	public KanjiMeaningRipper(String url) {
		this.url = url;
		character = "#";
		meaning = "can't find character meaning";
		words = new ArrayList<KanjiWord>();
	}

	public PageData getData() throws MalformedURLException, IOException {
		String cacheFileLoc = "cache/" + url.substring("https://jisho.org/search/".length());
		File cacheFile = new File(cacheFileLoc);
		if (!cacheFile.exists()) {
			System.out.println("creating cache file");
			cacheFile.createNewFile();
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile), "UTF-8"));
			try {
				out.write("cache file for " + url + "\n");
				Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
				for (Element d : doc.getElementsByClass("character")) {
					String character = d.text();
					out.write("c=" + character + "\n");
					String currentWord;
					int wordCount = 0;
					for (int i = 0; i < RipperMain.frequentWords.size(); i++) {
						currentWord = RipperMain.frequentWords.get(i);

						if (currentWord.contains(character)) {
							String descURL = "https://jisho.org/word/" + currentWord;
							String furigana = "furigana goes here";
							String definition = "";
							Document descDoc = Jsoup.connect(descURL).get();
							// Jsoup.
							// Document descDoc = Jsoup.parse(new URL(descURL).openStream(), "UTF-8",
							// descURL);
							for (Element f:descDoc.getElementsByClass("furigana")) {
								furigana = f.text();
							}
							for (Element f : descDoc.getElementsByClass("meaning-wrapper")) {
								for (Element t : f.getElementsByClass("meaning-meaning")) {
									String text = t.text();
									if (text.toLowerCase().charAt(0) == text.charAt(0)) {
										if (definition.length() != 0) {
											definition += "; ";
										}
										System.out.println("testses: " + text);
										if (text.indexOf(";") >= 0) {
											definition += text.substring(0, text.indexOf(";"));
										} else {
											definition += text;
										}
										System.out.println("testses: " + text);
										System.out.println("tst: " + definition);
									}
								}
							}

							System.out.println(currentWord);
							String sentenceURL = "https://tatoeba.org/eng/sentences/search?query=" + currentWord
									+ "&from=jpn&to=eng";
							Document sentenceDoc = Jsoup.parse(new URL(sentenceURL).openStream(), "UTF-8", sentenceURL);
							for (Element japaneseSentence : sentenceDoc.getElementsByClass("text flex")) {
								System.out.println("fleeex: " + japaneseSentence.text());
							}
							for (Element f : sentenceDoc.getElementsByClass("sentence-and-translations")) {
								String[] strings = f.text().split(" info ");
								if (strings.length >= 2) {
									String japaneseSentence = strings[0].substring(strings[0].lastIndexOf(" "),
											strings[0].length() - 1);
									if (japaneseSentence.contains(currentWord)) {
										System.out.println(f.text());
										String englishSentence = strings[1].substring(strings[1].indexOf(" ", 13),
												strings[1].length() - 1);
										out.write("w=" + currentWord + " 【"+furigana+"】" + "/-/" + definition + "/-/"
												+ japaneseSentence + "---" + englishSentence + "\n");
										break;
									}
								}
							}
							wordCount++;
						}
						if (wordCount >= 4) {
							break;
						}
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
				String wordlyword = strings[0].substring(2, strings[0].indexOf("】") + 1);
				ArrayList<String> japaneseSentences = new ArrayList<String>();
				ArrayList<String> englishSentences = new ArrayList<String>();
				for (int i = 2; i < strings.length; i++) {
					String[] sentences = strings[i].split("---");
					japaneseSentences.add(sentences[0]);
					englishSentences.add(sentences[1]);
				}

				words.add(new KanjiWord(wordlyword, strings[1], japaneseSentences, englishSentences));
			}
		}
		bufferedReader.close();

		return new PageData(character, meaning, words, 6);
	}
}
