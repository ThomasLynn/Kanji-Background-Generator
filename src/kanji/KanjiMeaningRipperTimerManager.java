package kanji;

public class KanjiMeaningRipperTimerManager {
	private long timer;
	private boolean isLocked;
	
	public KanjiMeaningRipperTimerManager() {
		timer = System.currentTimeMillis();
		isLocked = false;
	}
	
	public synchronized long getRemainingTime() {
		while(isLocked) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		isLocked = true;
		long newTime = System.currentTimeMillis();
		long toWait = timer - newTime + 3000;
		if (toWait<0) {
			return 0;
		}
		return toWait;
	}
	
	public void releaseLock() {
		isLocked = false;
		timer = System.currentTimeMillis();
	}
}
