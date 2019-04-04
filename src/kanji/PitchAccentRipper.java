package kanji;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class PitchAccentRipper {

	public static PitchAccentOutput getData(String kanjiSentence) throws Exception {
		String safeSentence = kanjiSentence;
		for (int i = 0; i < safeSentence.length(); i++) {
			if (!(JapaneseCharacter.isKana(safeSentence.charAt(i))
					|| JapaneseCharacter.isKanji(safeSentence.charAt(i)))) {
				safeSentence = safeSentence.replace(safeSentence.charAt(i), '_');
			}
		}
		String cacheFileLoc = "pitchcache/" + safeSentence + ".data";
		File cacheFile = new File(cacheFileLoc);
		if (!cacheFile.exists() || cacheFile.length() == 0) { // maybe add option to override cache (separate from other
																// cache override)
			doPost(kanjiSentence, cacheFile);
		}
		System.out.println("reading pitch cache file");
		BufferedReader bufferedReader = new BufferedReader(new FileReader(cacheFile));
		PitchAccentOutput output = new PitchAccentOutput();
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			if (line.startsWith("k=")) {
				output.kana = line.substring(2);
			}
			if (line.startsWith("p=")) {
				output.pitch = line.substring(2);
			}
		}
		bufferedReader.close();
		return output;
	}

	private static synchronized void doPost(String kanjiSentence, File cacheFile)
			throws InterruptedException, IOException {
		System.out.println("starting accent post");
		Thread.sleep(3000);
		cacheFile.createNewFile();
		Document doc = Jsoup.connect("http://www.gavo.t.u-tokyo.ac.jp/ojad/phrasing/index").userAgent(
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36")
				.data("_method", "POST").data("data[Phrasing][text]", kanjiSentence)
				.data("data[Phrasing][curve]", "advanced").data("data[Phrasing][accent]", "advanced")
				.data("data[Phrasing][accent_mark]", "all").data("data[Phrasing][estimation]", "crf")
				.data("data[Phrasing][analyze]", "true").data("data[Phrasing][phrase_component]", "invisible")
				.data("data[Phrasing][param]", "invisible").data("data[Phrasing][subscript]", "visible")
				.data("data[Phrasing][jeita]", "invisible").timeout(10000).post();
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile), "UTF-8"));
		for (Element e : doc.getElementsByClass("phrasing_text")) {
			out.write("k=" + e.text() + "\n");
			break;
		}
		for (Element e : doc.getElementsByClass("phrasing_text")) {
			int type = 0;
			String pitches = "";
			for (Element d : e.getElementsByTag("span")) {
				for (String k : d.classNames()) {
					if (k.equals("accent_plain")) {
						type = 1;
					}
					if (k.equals("accent_top")) {
						type = 2;
					}
					if (k.contains("mola")) {
						pitches += Integer.toString(type);
						type = 0;
					}
				}
			}
			out.write("p=" + pitches + "\n");
			break;
		}
		out.close();
		System.out.println("ending accent post");
	}
}
