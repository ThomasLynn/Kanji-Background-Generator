package kanji;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class SentenceRipper {
	public static List<String> getSentences(String sentenceURL, List<String> outputList) throws Exception {
		if (outputList.size() >= 5) {
			return outputList;
		}
		
		String cacheFileLoc = "sentencecache/" + sentenceURL.lastIndexOf("/");
		File cacheFile = new File(cacheFileLoc);
		if (!cacheFile.exists()) { // maybe add option to override cache (separate from other cache override)
			doPost(sentenceURL, cacheFile, outputList.size());
		}
		System.out.println("reading sentence cache file");
		BufferedReader bufferedReader = new BufferedReader(new FileReader(cacheFile));
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			if (line.startsWith("w=")) {
				outputList.add(line);
			}
		}
		bufferedReader.close();
		
		
		return outputList;
	}
	
	private static synchronized void doPost(String sentenceURL, File cacheFile, int currentSize) throws Exception {
		Thread.sleep(3000);
		System.out.println("creating sentence cache file: "+sentenceURL);
		cacheFile.createNewFile();
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile), "UTF-8"));
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
				PitchAccentOutput output = PitchAccentRipper.getData(japaneseSentence);
				kanaSentence = output.kana;
				pitchSentence = output.pitch;

				out.write("w=" + japaneseSentence + "/-/" + englishSentence + "/-/" + kanaSentence + "/-/"
						+ pitchSentence + "\n");
				currentSize++;
				if (currentSize >= 5) {
					out.close();
					return;
				}
			}
		}
		out.close();
	}

	private static boolean areSimilarStrings(String str1, String str2) {
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
