package kanji;

import java.util.ArrayList;

public class PageData{
	public String character;
	public String meaning;
	public ArrayList<KanjiSentence> words;

	public PageData(String character, String meaning, ArrayList<KanjiSentence> words,
			int onyomiCutSize) {
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
	}
}
