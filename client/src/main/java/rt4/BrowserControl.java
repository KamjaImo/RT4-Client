package rt4;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalMember;

import java.applet.Applet;

// This class is used to invoke javascript functions arbitrary javascript code
// when client is run in the browser.
// Since the client can no longer be played in browser, 
// it had to be ported to a standalone Java application.
// As part of that port, all code executed by these methods has been removed or replaced,
// rendering this class obsolete.
public final class BrowserControl {

	@OriginalMember(owner = "client!gh", name = "a", descriptor = "(Ljava/lang/String;BLjava/applet/Applet;)Ljava/lang/Object;")
	public static Object call(@OriginalArg(0) String functionName, @OriginalArg(2) Applet applet) throws Throwable {
		// Originally, this function would call a specified javascript function by name.
		return null; // JSObject.getWindow(applet).call(functionName, (Object[]) null);
	}

	@OriginalMember(owner = "client!gh", name = "a", descriptor = "(Ljava/applet/Applet;Ljava/lang/String;[Ljava/lang/Object;B)Ljava/lang/Object;")
	public static Object call(@OriginalArg(0) Applet applet, @OriginalArg(1) String functionName, @OriginalArg(2) Object[] args) throws Throwable {
		// Originally, this function would call a specified javascript function with parameters.
		return null; // JSObject.getWindow(applet).call(functionName, args);
	}

	@OriginalMember(owner = "client!gh", name = "a", descriptor = "(Ljava/applet/Applet;ZLjava/lang/String;)V")
	public static void eval(@OriginalArg(0) Applet applet, @OriginalArg(2) String js) throws Throwable {
		// Originally, this function would evaluate a specified arbitrary javascript snippet
		// JSObject.getWindow(applet).eval(js);
	}
}
