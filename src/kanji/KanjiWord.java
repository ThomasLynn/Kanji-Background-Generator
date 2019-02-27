package kanji;

import java.util.ArrayList;

public class KanjiWord {
	String word;
	String definition;
	ArrayList<String> japaneseSentences;
	ArrayList<String> englishSentences;
	
	public KanjiWord(String word,String definition, ArrayList<String> japaneseSentences,ArrayList<String> englishSentences) {
		this.word = word;
		this.definition = definition;
		this.japaneseSentences = japaneseSentences;
		this.englishSentences = englishSentences;
	}
}
