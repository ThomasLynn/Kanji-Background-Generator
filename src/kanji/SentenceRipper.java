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

	private static Object syncLock = new Object();

	public static void getSentences(String sentenceURL, List<String> outputList) throws Exception {
		if (outputList.size() >= 5) {
			return;
		}

		String cacheFileLoc = "sentencecache/"
				+ sentenceURL.substring(sentenceURL.lastIndexOf("query=") + 6, sentenceURL.lastIndexOf("&from"));
		cacheFileLoc += sentenceURL.substring(sentenceURL.lastIndexOf("page=") + 5) + ".data";
		File cacheFile = new File(cacheFileLoc);
		if (!cacheFile.exists() || RipperMain.invalidateCache>=2 || cacheFile.length()==0) {
			doPost(sentenceURL, cacheFile, outputList.size());
		}
		BufferedReader bufferedReader = new BufferedReader(new FileReader(cacheFile));
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			if (line.startsWith("w=")) {
				outputList.add(line);
			}
		}
		bufferedReader.close();
	}

	private static synchronized void doPost(String sentenceURL, File cacheFile, int currentSize) throws Exception {
		List<String> japaneseSentences = new LinkedList<String>();
		List<String> englishSentences = new LinkedList<String>();
		System.out.println("starting sentence post");
		Thread.sleep(3000);
		cacheFile.createNewFile();
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile), "UTF-8"));
		synchronized (syncLock) {
			Document sentenceDoc = Jsoup.parse(new URL(sentenceURL).openStream(), "UTF-8", sentenceURL);
			List<String> japaneseCheckSentences = new LinkedList<String>();
			for (Element f : sentenceDoc.getElementsByClass("sentence-and-translations")) {
				boolean success = true;
				int type = 0;
				String japaneseSentence = null;
				String englishSentence = null;
				for (Element g : f.getElementsByAttributeValue("layout", "column")) {

					for (Element k : g.getElementsByAttributeValue("class", "text")) {
						if (type == 0) {
							boolean hasSimilar = false;
							for (String s : japaneseCheckSentences) {
								if (areSimilarStrings(k.text(), s)) {
									// has clash
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
									japaneseCheckSentences.add(k.text());
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
					japaneseSentences.add(japaneseSentence);
					englishSentences.add(englishSentence);
					// this needs to be changed as you're calling a sync inside of another sync

					currentSize++;
					if (currentSize >= 5) {
						break;
					}
				}
			}
			System.out.println("ending sentence post sync");
		}
		for (int i = 0; i < japaneseSentences.size(); i++) {
			PitchAccentOutput output = PitchAccentRipper.getData(japaneseSentences.get(i));
			String kanaSentence = output.kana;
			String pitchSentence = output.pitch;

			out.write("w=" + japaneseSentences.get(i) + "/-/" + englishSentences.get(i) + "/-/" + kanaSentence + "/-/"
					+ pitchSentence + "\n");
		}
		out.close();

		System.out.println("ending sentence post");
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
