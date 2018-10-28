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

public class DataRipper {

	private String url;
	private String character;
	private String meaning;
	private ArrayList<String> words;

	public DataRipper(String url) {
		this.url = url;
		character = "#";
		meaning = "can't find character meaning";
		words = new ArrayList<String>();
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
					out.write("c=" + d.text() + "\n");
				}
				for (Element d : doc.getElementsByClass("kanji-details__main-meanings")) {
					out.write("m=" + d.text() + "\n");
				}

				for (Element d : doc.getElementsByClass("row compounds")) {
					Elements divs = d.getElementsByClass("no-bullet");
					try {
						for (Element e : divs.get(0).children()) {
							out.write("w=" + e.text() + "\n");
						}
					} catch (IndexOutOfBoundsException e) {

					}
					try {
						for (Element e : divs.get(1).children()) {
							out.write("w=" + e.text() + "\n");
						}
					} catch (IndexOutOfBoundsException e) {

					}
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
				words.add(line.substring(2));
			}
		}
		bufferedReader.close();

		return new PageData(character, meaning, words, 5, 5);
	}
}
