package kanji;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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

	private List<String> stuff(String sentenceURL, List<String> outputList) throws Exception {
		if (outputList.size() >= 5) {
			return outputList;
		}
		Document sentenceDoc = Jsoup.parse(new URL(sentenceURL).openStream(), "UTF-8", sentenceURL);
		for (Element japaneseSentence : sentenceDoc.getElementsByClass("text flex")) {
			System.out.println("fleeex: " + japaneseSentence.text());
		}
		List<String> japaneseSentences = new LinkedList<String>();
		for (Element f : sentenceDoc.getElementsByClass("sentence-and-translations")) {
			boolean success = true;
			int type = 0;
			String japaneseSentence = null;
			String englishSentence = null;
			String kanaSentence = null;
			String pitchSentence = null;
			for (Element g : f.getElementsByAttributeValue("layout", "column")) {

				for (Element k : g.getElementsByAttributeValue("class", "text")) {
					if (type == 0) {
						boolean hasSimilar = false;
						for (String s : japaneseSentences) {
							if (areSimilarStrings(k.text(), s)) {
								System.out.println("clash!");
								hasSimilar = true;
								success = false;
								break;
							}
						}
						if (hasSimilar == false) {
							if (k.text().length() <= 6) {
								success = false;
							} else {
								japaneseSentence = k.text();
								japaneseSentences.add(k.text());
							}
						}
					} else {
						if (englishSentence == null) {
							englishSentence = k.text();
							if (englishSentence.length() > 70) {
								success = false;
							}
						} else {
							if (areSimilarStrings(englishSentence, k.text())) {
								type--;
							} else {
								String oldSentence = englishSentence;
								englishSentence += " / " + k.text();
								if (englishSentence.length() > 70) {
									englishSentence = oldSentence;
								}
							}
						}
					}
					type++;
					break;
				}
				if (type >= 3) {
					break;
				}
			}
			if (success) {
				PitchAccentPoster poster = new PitchAccentPoster();
				poster.sendPost(japaneseSentence);
				kanaSentence = poster.getKana();
				pitchSentence = poster.getPitch();

				outputList.add("w=" + japaneseSentence + "/-/" + englishSentence + "/-/" + kanaSentence + "/-/"
						+ pitchSentence + "\n");
				if (outputList.size() >= 5) {
					return outputList;
				}
			}
		}
		return outputList;
	}

	public PageData getData() throws Exception {
		String cacheFileLoc = "cache/" + url.substring("https://jisho.org/search/".length());
		File cacheFile = new File(cacheFileLoc);
		if (!cacheFile.exists() || RipperMain.ignorecache) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("creating cache file");
			cacheFile.createNewFile();
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile), "UTF-8"));
			try {
				Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
				for (Element d : doc.getElementsByClass("character")) {
					String character = d.text();
					String sentenceURL = "https://tatoeba.org/eng/sentences/search?query=" + character
							+ "&from=jpn&to=eng";
					out.write("cache file for " + url + " and " + sentenceURL
							+ " and http://www.gavo.t.u-tokyo.ac.jp/ojad/phrasing/index" + "\n");
					out.write("c=" + character + "\n");
					List<String> wList = new LinkedList<String>();
					for (int p = 1; p <= 6; p++) {
						stuff(sentenceURL + "&page=" + Integer.toString(p), wList);
					}
					for (int strI = 0; strI < Math.min(5, wList.size()); strI++) {
						out.write(wList.get(strI));
					}
				}
				for (Element d : doc.getElementsByClass("kanji-details__main-meanings")) {
					out.write("m=" + d.text() + "\n");
				}

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
				words.add(new KanjiSentence(strings[0].substring(2), strings[1], strings[2], strings[3]));

			}
		}
		bufferedReader.close();

		return new PageData(character, meaning, words, 6);
	}

	boolean areSimilarStrings(String str1, String str2) {
		if (str1.length() + 1 == str2.length()) {
			for (int i = 0; i < str1.length(); i++) {
				if (str1.charAt(i) != str2.charAt(i)) {
					return false;
				}
			}
			return true;
		}
		if (str1.length() - 1 == str2.length()) {
			return areSimilarStrings(str2, str1);
		}
		if (str1.length() != str2.length()) {
			return false;
		}
		int differences = 0;
		for (int i = 0; i < str1.length(); i++) {
			if (str1.charAt(i) != str2.charAt(i)) {
				if (++differences > 1) {
					return false;
				}
			}
		}
		// if the execution is here, then there are 0, or 1 differences, so return true
		return true;
	}
}
