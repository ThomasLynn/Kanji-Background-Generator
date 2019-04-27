package kanji;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class PitchAccentRipper {

	private static long timer = 0;

	public static List<PitchAccentOutput> getData(List<String> kanjiSentences) throws Exception {
		List<File> files = new LinkedList<File>();
		List<String> needsPosting = new LinkedList<String>();
		List<File> postingFiles = new LinkedList<File>();
		System.out.println("current sentences:");
		for (String w : kanjiSentences) {
			System.out.println(w);

			String cacheFileLoc = getFileLocation(w);
			File cacheFile = new File(cacheFileLoc);
			files.add(cacheFile);
			if (!cacheFile.exists() || cacheFile.length() == 0) {
				needsPosting.add(w);
				postingFiles.add(cacheFile);
			}
		}
		if (needsPosting.size() > 0) {
			// System.out.println("size: "+needsPosting.size());
			List<PitchAccentOutput> outputs = doPost(needsPosting);
			System.out.println("looping over outputs");
			for (int i = 0; i < needsPosting.size(); i++) {
				System.out.println(needsPosting.get(i) + " : " + outputs.get(i).kana);
				File cacheFile = postingFiles.get(i);
				cacheFile.createNewFile();
				Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile), "UTF-8"));
				out.write("k=" + outputs.get(i).kana + "\n");
				out.write("p=" + outputs.get(i).pitch + "\n");
				out.close();
			}
		}
		// System.out.println("reading pitch cache file");
		List<PitchAccentOutput> outputs = new LinkedList<PitchAccentOutput>();
		for (File w : files) {
			PitchAccentOutput output = new PitchAccentOutput();
			BufferedReader bufferedReader = new BufferedReader(new FileReader(w));
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
			outputs.add(output);
		}
		return outputs;
	}

	private static String getFileLocation(String kanjiSentence) {
		return "pitchcache/" + makeSafeString(kanjiSentence) + ".data";
	}

	private static String makeSafeString(String unsafeString) {
		String safeSentence = unsafeString;
		for (int i = 0; i < safeSentence.length(); i++) {
			if (!(JapaneseCharacter.isKana(safeSentence.charAt(i))
					|| JapaneseCharacter.isKanji(safeSentence.charAt(i)))) {
				safeSentence = safeSentence.replace(safeSentence.charAt(i), '_');
			}

		}
		return safeSentence;
	}

	private static synchronized List<PitchAccentOutput> doPost(List<String> kanjiSentences)
			throws InterruptedException, IOException {
		// System.out.println("starting accent post");
		long toWait = timer - System.currentTimeMillis() + 3000;
		System.out.println("sleeping: " + toWait);
		if (toWait > 0) {
			Thread.sleep(toWait);
		}
		List<PitchAccentOutput> output = new LinkedList<PitchAccentOutput>();
		String combinedSentence = "";
		// System.out.println("reee");
		// System.out.println(kanjiSentences.size());
		System.out.println("posting sentences:");
		List<Integer> connectLocations = new LinkedList<Integer>();
		for (int i = 0; i < kanjiSentences.size(); i++) {
			System.out.println(kanjiSentences.get(i));
			if (kanjiSentences.get(i).substring(0, kanjiSentences.get(i).length() - 1).contains("？")
					|| kanjiSentences.get(i).substring(0, kanjiSentences.get(i).length() - 1).contains("！")
					|| kanjiSentences.get(i).substring(0, kanjiSentences.get(i).length() - 1).contains("。")) {
				connectLocations.add(i);
			}
			combinedSentence += kanjiSentences.get(i) + "\n";
			kanjiSentences.set(i, kanjiSentences.get(i) + "\n");
		}
		combinedSentence = combinedSentence.substring(0, combinedSentence.length() - 1);
		Document doc = Jsoup.connect("http://www.gavo.t.u-tokyo.ac.jp/ojad/phrasing/index").userAgent(
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36")
				.data("_method", "POST").data("data[Phrasing][text]", combinedSentence)
				.data("data[Phrasing][curve]", "advanced").data("data[Phrasing][accent]", "advanced")
				.data("data[Phrasing][accent_mark]", "all").data("data[Phrasing][estimation]", "crf")
				.data("data[Phrasing][analyze]", "true").data("data[Phrasing][phrase_component]", "invisible")
				.data("data[Phrasing][param]", "invisible").data("data[Phrasing][subscript]", "visible")
				.data("data[Phrasing][jeita]", "invisible").timeout(10000).post();
		int i = 0;
		for (Element c : doc.getElementsByClass("phrasing_row_wrapper clearfix")) {
			boolean doReset = true;
			String oldKana = null;
			if (connectLocations.size() > 0) {
				for (int w : connectLocations) {
					if (i == w + 1) {
						doReset = false;
						break;
					}
				}
			}
			PitchAccentOutput newOutput;
			if (doReset) {
				newOutput = new PitchAccentOutput();
				newOutput.kana = "";
				newOutput.pitch = "";
			} else {
				newOutput = output.get(output.size()-1);
				oldKana = newOutput.kana;
				System.out.println(newOutput.kana);
				System.out.println(newOutput.pitch);
			}
			int type = 0;
			for (Element e : c.getElementsByClass("phrasing_text")) {
				newOutput.kana += e.text();
				for (Element d : e.getElementsByTag("span")) {
					for (String k : d.classNames()) {
						if (k.equals("accent_plain")) {
							type = 1;
						}
						if (k.equals("accent_top")) {
							type = 2;
						}
						if (k.contains("mola")) {
							newOutput.pitch += Integer.toString(type);
							type = 0;
						}
					}
				}

			}
			if (newOutput.kana.length() != 0) {
				if (oldKana == null || !oldKana.equals(newOutput.kana)) {
					i++;
					if (!output.contains(newOutput)) {
						output.add(newOutput);
					}
				}
			}
		}
		/*
		 * if (connectLocations.size() > 0) { for (int i = 0; i < output.size(); i++) {
		 * for (int w : connectLocations) { if (w == i) {
		 * System.out.println("merging object"); o.set(i,
		 * kanjiSentences.get(i)+kanjiSentences.get(i+1)); kanjiSentences.remove(i+1);
		 * //kanjiSentences.remove(i+1); }
		 * 
		 * } } }
		 */

		if (kanjiSentences.size() != output.size()) {
			System.out.println("Pitch Accent Ripper not giving same output size as input");
		}

		timer = System.currentTimeMillis();
		return output;
		// System.out.println("ending accent post");
	}
}
