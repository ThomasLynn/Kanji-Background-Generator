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
			strings.add("本当？");
			strings.add("本気？");
			strings.add("本当に？");
			strings.add("本当？なぜ？");
			strings.add("本当だよ！");
			strings.add("それ本当？");
			strings.add("本当なの。");
			strings.add("本気だよ。");
			strings.add("これ誰の本？");
			strings.add("いい本だ。");
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
