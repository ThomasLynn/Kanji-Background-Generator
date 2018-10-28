package kanji;

import java.util.ArrayList;

public class PageData{
	public String character;
	public String meaning;
	public ArrayList<String> words;

	public PageData(String character, String meaning, ArrayList<String> words,
			int onyomiCutSize, int kunyomiCutSize) {
		this.character = character;
		String[] meaningList = meaning.split(", ");
		String newMeaning = "";
		for (int j = 0; true; j++) {
			String tempNewMeaning = newMeaning + meaningList[j];
			if (tempNewMeaning.length() > 40) {
				break;
			}
			if (j >= meaningList.length - 1) {
				newMeaning = tempNewMeaning;
				break;
			}
			newMeaning = tempNewMeaning + ", ";
		}
		if (newMeaning.charAt(newMeaning.length() - 2) == ',') {
			newMeaning = newMeaning.substring(0, newMeaning.length() - 2);
		}
		this.meaning = newMeaning;
		this.words = words;
		processDef(words, onyomiCutSize);
	}

	public void processDef(ArrayList<String> yomi, int cutSize) {
		for (int i = 0; i < yomi.size(); i++) {
			for (int j = 0; j < i; j++) {
				if (yomi.get(i).equals(yomi.get(j))) {
					yomi.remove(i);
					i--;
				}
			}
		}
		while (yomi.size() > cutSize) {
			yomi.remove(cutSize);
		}
		for (int i = 0; i < yomi.size(); i++) {
			String nStr = yomi.get(i);

			String info = nStr.substring(nStr.indexOf("】") + 2);

			ArrayList<String> def = new ArrayList<String>();

			for (String s : info.split(", (?![^(]*\\))")) {
				def.add(s);
			}
			def.sort((x, y) -> Integer.compare(x.length(), y.length()));
			String newInfo = "";
			for (int j = 0; true; j++) {
				String tempNewInfo = newInfo + def.get(j);
				if (tempNewInfo.length() > 80) {
					break;
				}
				if (j >= def.size() - 1) {
					newInfo = tempNewInfo;
					break;
				}
				newInfo = tempNewInfo + ", ";
			}
			if (newInfo.length() > 0 && nStr.contains(character)) {
				yomi.set(i, JapaneseCharacter.convertToKatakana(nStr.substring(0, nStr.indexOf("】") + 1)));
				i++;
				if (newInfo.charAt(newInfo.length() - 2) == ',') {
					newInfo = newInfo.substring(0, newInfo.length() - 2);
				}
				yomi.add(i, newInfo);

			} else {
				yomi.remove(i);
				i--;
			}
		}
	}
}
