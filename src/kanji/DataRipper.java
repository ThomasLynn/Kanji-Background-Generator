package kanji;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DataRipper {
	
	private String url;
	
	public DataRipper(String url) {
		this.url = url;
	}
	
	public PageData getData() throws MalformedURLException, IOException {
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
}
