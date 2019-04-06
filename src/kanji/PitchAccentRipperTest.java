package kanji;

import static org.junit.Assert.*;

import org.junit.Test;

public class PitchAccentRipperTest {

	@Test
	public void test() {
		try {
			RipperMain.invalidateCache = 3;
			PitchAccentOutput output = PitchAccentRipper.getData("今日、何曜日？");
			assertEquals(output.kana,"きょう、なんようひ？");
			assertEquals(output.pitch,"2001200");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
