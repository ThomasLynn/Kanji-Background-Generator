package kanji;

public class KanjiSentence {
	String kanjiSentence;
	String englishSentence;
	String kanaSentence;
	String pitchSentence;
	
	public KanjiSentence(String japaneseSentences,String englishSentences,String kanaSentence, String pitchSentence) {
		this.kanjiSentence = japaneseSentences;
		this.englishSentence = englishSentences;
		this.kanaSentence = kanaSentence;
		this.pitchSentence = pitchSentence;
	}
}
