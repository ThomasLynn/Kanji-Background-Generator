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

	

	public PageData getData() throws Exception {
		String cacheFileLoc = "cache/" + url.substring("https://jisho.org/search/".length());
		File cacheFile = new File(cacheFileLoc);
		if (!cacheFile.exists() || RipperMain.ignorecache || cacheFile.length()==0) {
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
						SentenceRipper.getSentences(sentenceURL + "&page=" + Integer.toString(p), wList);
					}
					for (int strI = 0; strI < Math.min(5, wList.size()); strI++) {
						out.write(wList.get(strI)+"\n");
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
}
