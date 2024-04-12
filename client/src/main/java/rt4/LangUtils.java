package rt4;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;

public final class LangUtils {

	@OriginalMember(owner = "client!mc", name = "fb", descriptor = "Lclient!na;")
	private static final JagString LANG_PT = JagString.parse("pt");
	@OriginalMember(owner = "client!mc", name = "mb", descriptor = "Lclient!na;")
	private static final JagString LANG_EN = JagString.parse("en");
	@OriginalMember(owner = "client!mc", name = "vb", descriptor = "Lclient!na;")
	private static final JagString LANG_DE = JagString.parse("de");
	@OriginalMember(owner = "client!mc", name = "pb", descriptor = "Lclient!na;")
	private static final JagString LANG_FR = JagString.parse("fr");
	@OriginalMember(owner = "client!mc", name = "ob", descriptor = "[Lclient!na;")
	public static final JagString[] LANGUAGES = new JagString[]{LANG_EN, LANG_DE, LANG_FR, LANG_PT};

	public class indexes {
		public static final int EN = 0;
		public static final int DE = 1;
		public static final int FR = 2;
		public static final int PT = 3;
	}

	@OriginalMember(owner = "client!hm", name = "a", descriptor = "(Lclient!na;B)I")
	public static int getLanguageIndex(@OriginalArg(0) JagString langCode) {
		for (@Pc(12) int idx = 0; idx < LANGUAGES.length; idx++) {
			if (LANGUAGES[idx].equalsIgnoreCase(langCode)) {
				return idx;
			}
		}
		return -1;
	}
}
