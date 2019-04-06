package kanji;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class PitchAccentRipperTest {

	@Test
	public void test() {
		try {
			RipperMain.invalidateCache = 3;
			List<String> strings = new LinkedList<String>();
			strings.add("早く、こっち！");
			strings.add("彼は早起きだ。");
			strings.add("彼は早く寝た。");
			strings.add("彼は手が早い。");
			strings.add("早く来なさい。");
			List<PitchAccentOutput> outputs = PitchAccentRipper.getData(strings);
			for(PitchAccentOutput w: outputs) {
				System.out.println(":::");
				System.out.println(w.kana);
				System.out.println(w.pitch);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
