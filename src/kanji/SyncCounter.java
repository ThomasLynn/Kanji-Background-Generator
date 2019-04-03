package kanji;

public class SyncCounter {
	
	static int counter = 0;

	public static synchronized void resetCounter() {
		counter = 0;
	}

	public static synchronized void incCounter() {
		counter++;
	}

	public static synchronized void decCounter() {
		counter--;
	}

	public static synchronized int getCounter() {
		return counter;
	}
}
