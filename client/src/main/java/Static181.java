import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;

public final class Static181 {

	@OriginalMember(owner = "client!oi", name = "h", descriptor = "Lclient!qf;")
	public static Sprite aClass3_Sub2_Sub1_9;

	@OriginalMember(owner = "client!oi", name = "m", descriptor = "I")
	public static int anInt4296;

	@OriginalMember(owner = "client!oi", name = "b", descriptor = "Lclient!na;")
	public static final JagString aClass100_810 = Static28.parse("::qa_op_test");

	@OriginalMember(owner = "client!oi", name = "j", descriptor = "Lclient!na;")
	public static final JagString aClass100_811 = Static28.parse("::wm1");

	@OriginalMember(owner = "client!oi", name = "a", descriptor = "(IIIIIIIIII)V")
	public static void method3340(@OriginalArg(0) int arg0, @OriginalArg(1) int arg1, @OriginalArg(2) int arg2, @OriginalArg(3) int arg3, @OriginalArg(4) int arg4, @OriginalArg(5) int arg5, @OriginalArg(6) int arg6, @OriginalArg(7) int arg7, @OriginalArg(8) int arg8) {
		if (arg5 >= Static172.anInt4164 && arg5 <= Static224.anInt5063 && arg0 >= Static172.anInt4164 && arg0 <= Static224.anInt5063 && arg6 >= Static172.anInt4164 && Static224.anInt5063 >= arg6 && Static172.anInt4164 <= arg1 && arg1 <= Static224.anInt5063 && Static267.anInt5773 <= arg4 && arg4 <= Static106.anInt2869 && arg7 >= Static267.anInt5773 && Static106.anInt2869 >= arg7 && arg2 >= Static267.anInt5773 && Static106.anInt2869 >= arg2 && arg3 >= Static267.anInt5773 && arg3 <= Static106.anInt2869) {
			Static38.method962(arg2, arg8, arg7, arg6, arg1, arg3, arg4, arg0, arg5);
		} else {
			Static165.method3162(arg5, arg0, arg7, arg8, arg3, arg2, arg1, arg6, arg4);
		}
	}

	@OriginalMember(owner = "client!oi", name = "a", descriptor = "(II)Lclient!na;")
	public static JagString method3341(@OriginalArg(0) int arg0) {
		return Static34.concatenate(new JagString[] { Static123.parseInt(arg0 >> 24 & 0xFF), Static233.aClass100_994, Static123.parseInt(arg0 >> 16 & 0xFF), Static233.aClass100_994, Static123.parseInt(arg0 >> 8 & 0xFF), Static233.aClass100_994, Static123.parseInt(arg0 & 0xFF) });
	}

	@OriginalMember(owner = "client!oi", name = "a", descriptor = "(I)V")
	public static void method3342() {
		Static67.aClass99_20.method3103();
	}

	@OriginalMember(owner = "client!oi", name = "a", descriptor = "(Lclient!ve;B)V")
	public static void load(@OriginalArg(0) Js5 archive) {
		if (Static18.loaded) {
			return;
		}

		if (GlRenderer.enabled) {
			Static46.clear();
		} else {
			Static129.clear();
		}

		@Pc(20) int height = GameShell.canvasHeight;
		@Pc(26) int width = height * 956 / 503;
		Static78.titleBg = Static130.loadSpriteAutoDetect(archive, Static262.bgId);
		Static78.titleBg.renderResizedTransparent((GameShell.canvasWidth - width) / 2, 0, width, height);
		Static243.logo = Static40.loadIndexedSpriteAutoDetect(Static136.logoId, archive);
		Static243.logo.renderTransparent(GameShell.canvasWidth / 2 - Static243.logo.width / 2, 18);
		Static18.loaded = true;
	}

	@OriginalMember(owner = "client!oi", name = "b", descriptor = "(II)V")
	public static void method3345(@OriginalArg(0) int arg0) {
		@Pc(8) DelayedStateChange local8 = Static238.method4143(5, arg0);
		local8.pushClient();
	}

	@OriginalMember(owner = "client!oi", name = "a", descriptor = "(I[I[Ljava/lang/Object;)V")
	public static void method3346(@OriginalArg(1) int[] arg0, @OriginalArg(2) Object[] arg1) {
		Static53.method1292(arg1, arg0.length - 1, arg0, 0);
	}

	@OriginalMember(owner = "client!oi", name = "b", descriptor = "(I)V")
	public static void method3347() {
		Static110.aClass99_15.clear();
	}

	@OriginalMember(owner = "client!oi", name = "a", descriptor = "(ILclient!ve;Lclient!ve;Z)V")
	public static void init(@OriginalArg(1) Js5 arg0, @OriginalArg(2) Js5 arg1) {
		Static30.aBoolean61 = true;
		Static121.aClass153_45 = arg1;
		Static146.aClass153_54 = arg0;
	}
}
