package rt4;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;
import plugin.PluginRepository;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

// This is the start point of the game application.
// The client is responsible for a lot of things, including:
// - Pre-launch validation checks
// - Parsing and handling of client settings
// - Initialization of the cache and networking queue
// - Launching of the pre-login loading screen
// - Marshalling of threads to their corresponding asynchronous loops
@OriginalClass("client!client")
public final class client extends GameShell {

	// LOADING PROCESS
	// The game client loads in a series of numbered stages that run sequentially
	// until the game is fully loaded and asynchronous processing is fully online.

	// Text displayed in the loading bar, below the percentage indicator.
	@OriginalMember(owner = "client!jm", name = "z", descriptor = "Lclient!na;")
	public static JagString mainLoadSecondaryText = EMPTY_STRING;

	// CACHE
	// The game's cache is stored in 31 files on the user's disk:
	// - 1 master index (`main_file_cache.idx255`)
	// - 28 index files (`main_file_cache.idx0` to `main_file_cache.idx27`).
	// - 1 data file (`main_file_cache.dat2`)
	// - 1 UID file (`random.dat`)
	// The specific file format of each is unique to RuneScape, and the individual files are tightly coupled with each other.

	// INDEX FILES
	// Index files do not contain game data, but rather point to specific pieces of game data within the larger data file.
	// Each index refers to a specific type of data (for example, `main_file_cache.idx7` contains references to model data).

	// List of file handles for each of the index files on the user's disk.
	@OriginalMember(owner = "client!dk", name = "j", descriptor = "[Lclient!en;")
	public static final BufferedFile[] cacheIndexes = new BufferedFile[28];

	// List of resource providers for each of the index files.
	// Resource providers are responsible for downloading cache data from the server so it can be saved to disk.
	@OriginalMember(owner = "client!wa", name = "Eb", descriptor = "[Lclient!bg;")
	public static final Js5CachedResourceProvider[] js5Providers = new Js5CachedResourceProvider[28];

	// List of weights for adjusting the loading bar while downloading each of the index files.
	// Some index files are large, so a linear progress bar doesn't accurately reflect the overhead of downloading the file.
	// To accommodate that, the loading bar is manually adjusted based on the weight in this array, 
	// where a higher weight means a fuller loading bar relative to the number of resources parsed out of the file.
	@OriginalMember(owner = "client!nh", name = "fb", descriptor = "[I")
	public static final int[] JS5_ARCHIVE_WEIGHTS = new int[]{4, 4, 1, 2, 6, 4, 2, 49, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

	// JS5 ARCHIVES
	// In order to extract any resource, information from the master index, index, and data files must be combined
	// into a format known as a Js5 archive. One archive contains all of the resources defined within a single index file,
	// bundled together with the concrete data in the data file. 
	// Js5 archives typically require substantial unpacking and post-processing before their contents are usable.
	@OriginalMember(owner = "client!sg", name = "k", descriptor = "Lclient!ve;")
	public static Js5 js5Archive23;

	// RESOURCES
	// Each individual piece of game data is referred to as a "resource".
	// (Sometimes they are also referred to as 'files', not to be confused for files on disk.)
	// A single Js5 archive contains one or more 'groups', and each group contains one or more resources.
	// Some specific resources are referenced in this class as part of the loading screen and early initialization,
	// and those resource names are defined below.

	// The login screen interface.
	@OriginalMember(owner = "client!km", name = "Mc", descriptor = "Lclient!na;")
	public static final JagString LOGINSCREEN = JagString.parse("loginscreen");

	// The title song "Scape Main", which plays over the title screen.
	@OriginalMember(owner = "client!a", name = "e", descriptor = "Lclient!na;")
	public static final JagString TITLE_SONG = JagString.parse("scape main");

	// The codec data used to build Huffman trees.
	// Huffman coding is used for compression of text resources - see HuffmanCodec.java for details.
	@OriginalMember(owner = "client!uh", name = "Y", descriptor = "Lclient!na;")
	public static final JagString HUFFMAN_GROUP = JagString.parse("huffman");

	// The group containing map data for all the maps in the game.
	// These must be pre-loaded on startup.
	@OriginalMember(owner = "client!nb", name = "n", descriptor = "Lclient!na;")
	public static final JagString MAPS_GROUP = JagString.parse("details");

	// NETWORKING
	// The game client communicates with the game server primarily through TCP socket connections.
	// Requests are delivered to the server in a FIFO queue, and responses are processed asynchronously as they are received.

	// Id of the world to connect to.
	// Each game world has its own dedicated connection, with the port number derived from the world id.
	// When the user launches the game, the client picks a world to connect to,
	// and from there the user can change worlds if desired to disconnect and reconnect to a new server.
	@OriginalMember(owner = "client!pb", name = "Q", descriptor = "I")
	public static int worldListId = 1;

	// STRING CONSTANTS
	// During early initialization, the game client does not have access to text resources within the cache files,
	// and is instead forced to hard-code specific strings into the client.
	// Below are the constants used for this purpose.
	// Note that some strings have special encodings - see JagString for notes on these.
	@OriginalMember(owner = "client!jm", name = "A", descriptor = "Lclient!na;")
	static final JagString EMPTY_STRING = JagString.parse("");
	@OriginalMember(owner = "client!si", name = "gb", descriptor = "Lclient!na;")
	public static final JagString LINE_BREAK = JagString.parse("<br>(X");
	@OriginalMember(owner = "client!qk", name = "a", descriptor = "Lclient!na;")
	public static final JagString aClass100_899 = JagString.parse("0(U");

	// DISCONTINUED BEHAVIOR

	// APPLET PARAMETERS
	// This version of the RuneScape client was designed to be run in the browser as an embedded Java applet.
	// Applets have many capabilities, among them being the ability to define "applet parameters"
	// that could be set on page load and retrieved by the applet at runtime, allowing user preferences to be 
	// saved and retrieved by the game without needing to interact with the browser at all.
	// The RT4 client has been adapted to run standalone, and as such, these parameters no longer have any meaning,
	// but they are listed here for historical posterity.
	// Each parameter has a string key and a value parsed from a string.

	// The language parameter would have contained an identifier for the user's preferred language.
	// At the time, RuneScape had translations for English, French, and German.
	@OriginalMember(owner = "client!lb", name = "v", descriptor = "I")
	public static int language = 0;

	// The country parameter would have contained an identifier for the user's country of origin.
	// This is used in some interface scripts, possibly for world selection logic.
	@OriginalMember(owner = "client!wk", name = "w", descriptor = "I")
	public static int country;

	// The settings parameter would have contained a unique key identifying the user's settings.
	// This key would be passed around in the URL of the applet, presumably to allow the client to 
	// keep track of a user when they hopped worlds, clicked the "buy membership" button, 
	// or took any other action that initiated a new HTTP request that would otherwise lose that state.
	@OriginalMember(owner = "client!sg", name = "e", descriptor = "Lclient!na;")
	public static final JagString SETTINGS = JagString.parse("settings");
	public static JagString settings = null;
	@OriginalMember(owner = "client!rh", name = "j", descriptor = "Lclient!client;")

	// The haveie6 parameter would have indicated whether IE6 is installed on the user's computer.
	// Presumably this would be used when opening browser links in-game.
	@OriginalMember(owner = "client!od", name = "n", descriptor = "Z")
	public static boolean haveIe6 = false;

	// The advertsuppressed parameter would have indicated whether or not to suppress banner ads on the page,
	// e.g. if the user is a paying member or underage.
	@OriginalMember(owner = "client!ud", name = "S", descriptor = "Z")
	public static boolean advertSuppressed = false;

	// The js parameter would have indicated whether the user's browser was capable of serving Javascript video ads.
	// Despite its name, it does not seem to toggle ALL Javascript behavior, just the video ads.
	@OriginalMember(owner = "client!lk", name = "U", descriptor = "Z")
	public static boolean javaScript = false;

	// The objecttag parameter was passed around via the applet URLs, but its function must have been internal,
	// as it doesn't seem to be used within the client itself.
	@OriginalMember(owner = "client!t", name = "x", descriptor = "Z")
	public static boolean objectTag = false;

	// AD-HOC JAVASCRIPT CALLS
	// Because applets could run in the browser, they had the ability to interact with the DOM,
	// including execution of custom Javascript.
	// The primary use for this would have been to display ads on free-to-play worlds.
	// Below are the Javascript functions that could be called (in the RT4 client, they are all no-ops).

	// Top-banner refresh, presumably to refresh the ad banner placed above the game window in free-to-play worlds.
	@OriginalMember(owner = "client!qk", name = "b", descriptor = "Lclient!na;")
	public static final JagString TB_REFRESH = JagString.parse("tbrefresh");

	// Shows a full-screen video ad. This was used as an additional monetization scheme for a brief period,
	// wherein players could opt into watching a video ad in exchange for RuneCoins or Squeal of Fortune keys.
	// These prizes weren't available as of 2009, so I'm either forgetting something (FunOrb points, perhaps?)
	// or this function exists as a piece of plumbing that wasn't yet hooked up to anything functional.
	@OriginalMember(owner = "client!al", name = "r", descriptor = "Lclient!na;")
	public static final JagString SHOW_VIDEO_AD = JagString.parse("showVideoAd");

	// COMMAND-LINE ARGUMENTS
	// The game client has support for command-line arguments, despite the fact that no native desktop application
	// existed for it at the time. Presumably these were used internally for development, in much the same way
	// that the RT4 client is launched today.

	// Runtime environment (QA or Prod).
	// The RT4 client has added a third runtime environment, Decomp, for overriding behaviors that 
	// interfere with the client's function when run in an unofficial capacity.
	@OriginalMember(owner = "client!gj", name = "b", descriptor = "I")
	public static int runEnv = RunEnvs.prod;

	// Runtime mode (Live, RC, or WIP).
	// This would presumably be used to produce development builds of the game, perhaps with debug logs
	// or test data bundled into the cache files.
	@OriginalMember(owner = "client!gg", name = "U", descriptor = "I")
	public static int runMode = RunModes.live;

	// Debug mode (0 for disabled, 1 for enabled).
	// This affects various things, such as rendering and shift-click behavior.
	@OriginalMember(owner = "client!vk", name = "n", descriptor = "I")
	public static int debug = 0;

	// UNKNOWNS (TODO)
	@OriginalMember(owner = "client!d", name = "S", descriptor = "Ljava/util/Random;")
	public static final Random aRandom1 = new Random();

	@OriginalMember(owner = "client!qi", name = "r", descriptor = "I")
	public static int affiliate = 0;
	@OriginalMember(owner = "client!dk", name = "h", descriptor = "Lclient!na;")
	public static client instance;
	@OriginalMember(owner = "client!ba", name = "D", descriptor = "Lclient!vh;")
	public static AudioChannel musicChannel;
	@OriginalMember(owner = "client!fk", name = "q", descriptor = "Lclient!uc;")
	public static MouseWheel mouseWheel;
	@OriginalMember(owner = "client!lh", name = "s", descriptor = "Lclient!vh;")
	public static AudioChannel soundChannel;
	@OriginalMember(owner = "client!id", name = "l", descriptor = "Lclient!jb;")
	public static Js5NetQueue js5NetQueue;
	@OriginalMember(owner = "client!gm", name = "T", descriptor = "Lclient!k;")
	public static Js5CacheQueue js5CacheQueue;
	@OriginalMember(owner = "client!nj", name = "f", descriptor = "Lclient!en;")
	public static BufferedFile cacheData;
	@OriginalMember(owner = "client!pf", name = "f", descriptor = "Lclient!en;")
	public static BufferedFile cacheMasterIndex;
	@OriginalMember(owner = "client!jg", name = "c", descriptor = "Lclient!en;")
	public static BufferedFile uid;
	@OriginalMember(owner = "client!tl", name = "d", descriptor = "I")
	public static int gameState = 0;
	@OriginalMember(owner = "client!id", name = "f", descriptor = "Z")
	public static boolean clean = false;
	@OriginalMember(owner = "client!bl", name = "P", descriptor = "I")
	public static int js5ConnectDelay = 0;
	@OriginalMember(owner = "client!wc", name = "c", descriptor = "I")
	public static int defaultPort;
	@OriginalMember(owner = "client!ee", name = "g", descriptor = "I")
	public static int alternatePort;
	@OriginalMember(owner = "client!ra", name = "s", descriptor = "I")
	public static int port;
	@OriginalMember(owner = "client!ud", name = "K", descriptor = "I")
	public static int worldListDefaultPort;
	@OriginalMember(owner = "client!r", name = "f", descriptor = "I")
	public static int worldListPort;
	@OriginalMember(owner = "client!v", name = "f", descriptor = "Lclient!al;")
	public static Js5MasterIndex js5MasterIndex;
	@OriginalMember(owner = "client!re", name = "B", descriptor = "Lclient!ve;")
	public static Js5 js5Archive0;
	@OriginalMember(owner = "client!ud", name = "J", descriptor = "Lclient!ve;")
	public static Js5 js5Archive1;
	@OriginalMember(owner = "client!wf", name = "g", descriptor = "Lclient!ve;")
	public static Js5 js5Archive2;
	@OriginalMember(owner = "client!dc", name = "z", descriptor = "Lclient!ve;")
	public static Js5 js5Archive3;
	@OriginalMember(owner = "client!uc", name = "c", descriptor = "Lclient!ve;")
	public static Js5 js5Archive4;
	@OriginalMember(owner = "client!ca", name = "Z", descriptor = "Lclient!ve;")
	public static Js5 js5Archive5;
	@OriginalMember(owner = "client!kc", name = "w", descriptor = "Lclient!ve;")
	public static Js5 js5Archive6;
	@OriginalMember(owner = "client!vl", name = "g", descriptor = "Lclient!ve;")
	public static Js5 js5Archive7;
	@OriginalMember(owner = "client!ra", name = "K", descriptor = "Lclient!ve;")
	public static Js5 js5Archive8;
	@OriginalMember(owner = "client!pk", name = "Z", descriptor = "Lclient!ve;")
	public static Js5 js5Archive9;
	@OriginalMember(owner = "client!ol", name = "U", descriptor = "Lclient!ve;")
	public static Js5 js5Archive10;
	@OriginalMember(owner = "client!rg", name = "z", descriptor = "Lclient!ve;")
	public static Js5 js5Archive11;
	@OriginalMember(owner = "client!bf", name = "s", descriptor = "Lclient!ve;")
	public static Js5 js5Archive12;
	@OriginalMember(owner = "client!ve", name = "l", descriptor = "Lclient!ve;")
	public static Js5 js5Archive13;
	@OriginalMember(owner = "client!kl", name = "r", descriptor = "Lclient!ve;")
	public static Js5 js5Archive14;
	@OriginalMember(owner = "client!km", name = "Oc", descriptor = "Lclient!ve;")
	public static Js5 js5Archive15;
	@OriginalMember(owner = "client!wl", name = "s", descriptor = "Lclient!ve;")
	public static Js5 js5Archive16;
	@OriginalMember(owner = "client!km", name = "Nc", descriptor = "Lclient!ve;")
	public static Js5 js5Archive17;
	@OriginalMember(owner = "client!nj", name = "l", descriptor = "Lclient!ve;")
	public static Js5 js5Archive18;
	@OriginalMember(owner = "client!ni", name = "k", descriptor = "Lclient!ve;")
	public static Js5 js5Archive19;
	@OriginalMember(owner = "client!ui", name = "cb", descriptor = "Lclient!ve;")
	public static Js5 js5Archive20;
	@OriginalMember(owner = "client!jh", name = "p", descriptor = "Lclient!ve;")
	public static Js5 js5Archive21;
	@OriginalMember(owner = "client!mf", name = "W", descriptor = "Lclient!ve;")
	public static Js5 js5Archive22;
	@OriginalMember(owner = "client!uj", name = "J", descriptor = "Lclient!ve;")
	public static Js5 js5Archive24;
	@OriginalMember(owner = "client!cd", name = "B", descriptor = "Lclient!ve;")
	public static Js5 js5Archive25;
	@OriginalMember(owner = "client!nd", name = "t", descriptor = "Lclient!ve;")
	public static Js5 js5Archive26;
	@OriginalMember(owner = "client!sf", name = "b", descriptor = "Lclient!ve;")
	public static Js5 js5Archive27;
	@OriginalMember(owner = "client!qc", name = "P", descriptor = "I")
	public static int mainLoadPercentage = 10;
	@OriginalMember(owner = "client!nc", name = "j", descriptor = "I")
	public static int mainLoadState = 0;
	@OriginalMember(owner = "client!li", name = "l", descriptor = "Lclient!ge;")
	public static Cache masterCache;
	@OriginalMember(owner = "client!li", name = "v", descriptor = "Lclient!va;")
	public static MidiPcmStream musicStream;
	@OriginalMember(owner = "client!qi", name = "C", descriptor = "Lclient!ei;")
	public static MixerPcmStream soundStream;
	@OriginalMember(owner = "client!ef", name = "p", descriptor = "Lclient!vj;")
	public static PcmResampler resampler;
	@OriginalMember(owner = "client!t", name = "F", descriptor = "I")
	public static int js5PrevErrors = 0;
	@OriginalMember(owner = "client!ld", name = "k", descriptor = "Ljava/lang/String;")
	public static String worldListHostname;
	@OriginalMember(owner = "client!hi", name = "g", descriptor = "I")
	public static int worldListAlternatePort;
	@OriginalMember(owner = "client!em", name = "v", descriptor = "Ljava/lang/String;")
	public static String hostname;
	@OriginalMember(owner = "client!vc", name = "db", descriptor = "[S")
	public static short[] aShortArray88;
	@OriginalMember(owner = "client!f", name = "T", descriptor = "[S")
	public static short[] aShortArray19 = new short[256];
	@OriginalMember(owner = "client!sm", name = "l", descriptor = "[S")
	public static short[] aShortArray74 = new short[256];
	@OriginalMember(owner = "client!vc", name = "bb", descriptor = "[S")
	public static short[] aShortArray87 = new short[256];
	@OriginalMember(owner = "client!cm", name = "f", descriptor = "Lsignlink!im;")
	public static PrivilegedRequest js5SocketRequest;
	@OriginalMember(owner = "client!qk", name = "g", descriptor = "Lclient!ma;")
	public static BufferedSocket js5Socket;
	@OriginalMember(owner = "client!ac", name = "c", descriptor = "I")
	public static int js5ConnectState = 0;
	@OriginalMember(owner = "client!rj", name = "Y", descriptor = "J")
	public static long js5ConnectTime;
	@OriginalMember(owner = "client!nm", name = "Y", descriptor = "J")
	public static long firstGc = 0L;
	@OriginalMember(owner = "client!mj", name = "A", descriptor = "J")
	public static long prevGc = 0L;
	@OriginalMember(owner = "client!gj", name = "d", descriptor = "I")
	public static int loop = 0;
	@OriginalMember(owner = "client!wj", name = "e", descriptor = "Lclient!na;")
	public static JagString mainLoadPrimaryText = null;
	@OriginalMember(owner = "client!sj", name = "p", descriptor = "I")
	public static int anInt5150 = 1;
	@OriginalMember(owner = "client!cn", name = "B", descriptor = "I")
	public static int anInt1196 = 1;
	@OriginalMember(owner = "client!ah", name = "t", descriptor = "I")
	public static int anInt986;

	public static class RunModes {
		public static final int live = 0;
		public static final int rc = 1;
		public static final int wip = 2;
	}

	public static class RunEnvs {
		public static final int prod = 0;
		public static final int qa = 1;
		public static final int decomp = 2;
	}

	public static class GameStates {
		public static final int error = 1000;
	}

	@OriginalMember(owner = "client!client", name = "main", descriptor = "([Ljava/lang/String;)V")
	public static void main(@OriginalArg(0) String[] args) {
		// Load config file
		try {
			GlobalJsonConfig.load(GlobalConfig.EXTENDED_CONFIG_PATH);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			// Use default command-line arguments if none provided
			if (args.length != 4) {
				args = new String[4];
				args[0] = "1";
				args[1] = "live";
				args[2] = "english";
				args[3] = "game0";
				// Static131.method2577("argument count");
			}

			// Get world from first arg
			worldListId = Integer.parseInt(args[0]);
			if (GlobalJsonConfig.instance != null) {
				worldListId = GlobalJsonConfig.instance.world;
			}

			// Set runtime environment to decomp so certain production validations won't trigger
			// (this is needed for client to run properly)
			runEnv = RunEnvs.decomp;
		 
			// Get run mode from second arg
			if (args[1].equals("live")) {
				runMode = RunModes.live;
			} else if (args[1].equals("rc")) {
				runMode = RunModes.rc;
			} else if (args[1].equals("wip")) {
				runMode = RunModes.wip;
			} else {
				printUsage("modewhat");
			}
			advertSuppressed = false;
		
			// Get language from third arg	
			@Pc(15) int langIdx = -1;
			try {
				@Pc(63) byte[] argLanguage = args[2].getBytes(StandardCharsets.ISO_8859_1);
				langIdx = LangUtils.getLanguageIndex(JagString.toJagString(argLanguage, argLanguage.length, 0));
			} catch (@Pc(74) Exception local74) {
			}
			if (langIdx != -1) {
				language = langIdx;
			} else if (args[2].equals("english")) {
				language = LangUtils.indexes.EN;
			} else if (args[2].equals("german")) {
				language = LangUtils.indexes.DE;
			} else {
				printUsage("language");
			}
			LocalizedText.setLanguage(language);

			// Unclear what objectTag does. Seems not to be used for anything.
			objectTag = false;

			// This parameter has no function in the standalone Java client,
			// but originally it served to indicate whether javascript was enabled/disabled
			// on the user's browser.
			javaScript = false;

			// Fourth arg toggles some custom plugin settings (I guess for debug purposes)
			if (args[3].equals("game0")) {
				debug = 0;
			} else if (args[3].equals("game1")) {
				debug = 1;
			} else {
				printUsage("game");
			}
			country = 0;
			haveIe6 = false;
			affiliate = 0;
			settings = JagString.EMPTY;
			@Pc(146) client c = new client();
			instance = c;
			c.startApplication(runMode + 32, "runescape");
			GameShell.frame.setLocationRelativeTo(null);
			GameShell.frame.setSize(1024, 768); // set a reasonable size by default
		} catch (@Pc(167) Exception ex) {
			TracingException.report(null, ex);
		}
	}

	@OriginalMember(owner = "client!kd", name = "a", descriptor = "(Ljava/lang/String;B)V")
	public static void printUsage(@OriginalArg(0) String arg0) {
		System.out.println("Bad " + arg0 + ", Usage: worldid, <live/rc/wip>, <english/german>, <game0/game1>");
		System.exit(1);
	}

	@OriginalMember(owner = "client!re", name = "a", descriptor = "(I)V")
	public static void clean() {
		FloTypeList.clean();
		FluTypeList.clean();
		IdkTypeList.clean();
		LocTypeList.clean();
		NpcTypeList.clean();
		ObjTypeList.clean();
		SeqTypeList.clean();
		SpotAnimTypeList.clean();
		VarbitTypeList.clean();
		VarpTypeList.clean();
		BasTypeList.clean();
		MsiTypeList.clean();
		LightTypeList.clean();
		CursorTypeList.clean();
		PlayerAppearance.clean();
		Component.clean();
		HintArrowManager.clean();
		ShadowModelList.clean();
		HitBarList.hitBars.clean(5);
		FontMetricsList.fontMetrics.clean(5);
	}

	@OriginalMember(owner = "client!pl", name = "a", descriptor = "(II)V")
	public static void setGameState(@OriginalArg(0) int arg0) {
		if (gameState == arg0) {
			return;
		}
		if (gameState == 0) {
			LoadingBarAwt.clear();
		}
		if (gameState == 25) {
			PluginRepository.OnLogin();
		}
		if (arg0 == 40) {
			LoginManager.clear();
		}
		@Pc(37) boolean local37 = arg0 == 5 || arg0 == 10 || arg0 == 28;
		if (arg0 != 40 && Protocol.aClass95_4 != null) {
			Protocol.aClass95_4.close();
			Protocol.aClass95_4 = null;
		}
		if (arg0 == 25 || arg0 == 28) {
			LoginManager.anInt5804 = 0;
			anInt5150 = 1;
			LoginManager.loadingScreenState = 0;
			anInt1196 = 1;
			LoginManager.mapFilesMissingCount = 0;
			WorldMap.clear(true);
		}
		if (arg0 == 25 || arg0 == 10) {
			topBannerRefresh();
		}
		if (arg0 == 5) {
			TitleScreen.load(js5Archive8);
		} else {
			TitleScreen.clear();
		}
		@Pc(106) boolean local106 = gameState == 5 || gameState == 10 || gameState == 28;
		if (local106 != local37) {
			if (local37) {
				MusicPlayer.groupId = MusicPlayer.titleSong;
				if (Preferences.musicVolume == 0) {
					MidiPlayer.playFadeOut();
				} else {
					MidiPlayer.playFadeOut(MusicPlayer.titleSong, js5Archive6, 255);
				}
				js5NetQueue.writeLoggedIn(false);
			} else {
				MidiPlayer.playFadeOut();
				js5NetQueue.writeLoggedIn(true);
			}
		}
		if (GlRenderer.enabled && (arg0 == 25 || arg0 == 28 || arg0 == 40)) {
			GlRenderer.draw();
		}
		gameState = arg0;
	}

	@OriginalMember(owner = "client!al", name = "a", descriptor = "(ZZZIZ)Lclient!ve;")
	public static Js5 createJs5(@OriginalArg(0) boolean discardPacked, @OriginalArg(1) boolean prefetch, @OriginalArg(2) boolean discardUnpacked, @OriginalArg(3) int archive) {
		@Pc(7) Cache cache = null;
		if (cacheData != null) {
			cache = new Cache(archive, cacheData, cacheIndexes[archive], 1000000);
		}
		js5Providers[archive] = js5MasterIndex.getResourceProvider(archive, masterCache, cache);
		if (prefetch) {
			js5Providers[archive].prefetchAll();
		}
		return new Js5(js5Providers[archive], discardPacked, discardUnpacked);
	}

	@OriginalMember(owner = "client!je", name = "h", descriptor = "(I)V")
	public static void unloadSoft() {
		FloTypeList.removeSoft();
		FluTypeList.removeSoft();
		IdkTypeList.removeSoft();
		LocTypeList.removeSoft();
		NpcTypeList.removeSoft();
		ObjTypeList.removeSoft();
		SeqTypeList.removeSoft();
		SpotAnimTypeList.removeSoft();
		VarbitTypeList.removeSoft();
		VarpTypeList.removeSoft();
		BasTypeList.removeSoft();
		MsiTypeList.removeSoft();
		LightTypeList.removeSoft();
		CursorTypeList.removeSoft();
		PlayerAppearance.removeSoft();
		Component.removeSoft();
		HintArrowManager.removeSoft();
		ShadowModelList.removeSoft();
		HitBarList.hitBars.removeSoft();
		FontMetricsList.fontMetrics.removeSoft();
	}

	@OriginalMember(owner = "client!rj", name = "f", descriptor = "(B)V")
	public static void unload() {
		FloTypeList.clear();
		FluTypeList.clear();
		IdkTypeList.clear();
		LocTypeList.clear();
		NpcTypeList.clear();
		ObjTypeList.clear();
		SeqTypeList.clear();
		SpotAnimTypeList.clear();
		VarbitTypeList.clear();
		VarpTypeList.clear();
		BasTypeList.clear();
		MsiTypeList.clear();
		LightTypeList.clear();
		CursorTypeList.clear();
		PlayerAppearance.clear();
		Component.clear();
		if (runMode != RunModes.live) {
			for (@Pc(54) int i = 0; i < Player.aByteArrayArray8.length; i++) {
				Player.aByteArrayArray8[i] = null;
			}
			Player.anInt2863 = 0;
		}
		HintArrowManager.clear();
		ShadowModelList.clear();
		FontMetricsList.fontMetrics.clear();
		if (!GlRenderer.enabled) {
			((Js5GlTextureProvider) Rasteriser.textureProvider).clear();
		}
		ClientScriptList.scripts.clear();
		js5Archive0.discardUnpacked();
		js5Archive1.discardUnpacked();
		js5Archive3.discardUnpacked();
		js5Archive4.discardUnpacked();
		js5Archive5.discardUnpacked();
		js5Archive6.discardUnpacked();
		js5Archive7.discardUnpacked();
		js5Archive8.discardUnpacked();
		js5Archive10.discardUnpacked();
		js5Archive11.discardUnpacked();
		js5Archive12.discardUnpacked();
		HitBarList.hitBars.clear();
	}

	@OriginalMember(owner = "client!id", name = "b", descriptor = "(I)V")
	public static void audioLoop() {
		if (soundChannel != null) {
			soundChannel.loop();
		}
		if (musicChannel != null) {
			musicChannel.loop();
		}
	}

	@OriginalMember(owner = "client!la", name = "a", descriptor = "(Lclient!wa;Z)V")
	public static void writeUid(@OriginalArg(0) Buffer arg0) {
		@Pc(15) byte[] local15 = new byte[24];
		if (uid != null) {
			try {
				uid.seek(0L);
				uid.read(local15);
				@Pc(28) int local28;
				for (local28 = 0; local28 < 24 && local15[local28] == 0; local28++) {
				}
				if (local28 >= 24) {
					throw new IOException();
				}
			} catch (@Pc(55) Exception local55) {
				for (@Pc(57) int local57 = 0; local57 < 24; local57++) {
					local15[local57] = -1;
				}
			}
		}
		arg0.pdata(local15, 24);
	}

	@OriginalMember(owner = "client!lb", name = "a", descriptor = "(Z)V")
	public static void method2721() {
		SceneGraph.clear();
		MiniMap.sprite = null;
		LightingManager.anInt2875 = -1;
		unload();
		DeadClass.cache.clear();
		LocType.aClass139_1 = new LocEntity();
		((Js5GlTextureProvider) Rasteriser.textureProvider).clear();
		LightingManager.lightCount = 0;
		LightingManager.lights = new Light[255];
		GlModel.method4120();
		ShadowManager.method4203();
		Sprites.clear();
		WorldMap.clear(false);
		TitleScreen.clear();
		for (@Pc(39) int local39 = 0; local39 < 2048; local39++) {
			@Pc(46) Player player = PlayerList.players[local39];
			if (player != null) {
				player.attachment = null;
			}
		}
		if (GlRenderer.enabled) {
			ShadowManager.method4201();
			ParticleSystem.load();
		}
		Fonts.load(js5Archive13, js5Archive8);
		Sprites.load(js5Archive8);
		LoginManager.aClass3_Sub2_Sub1_10 = null;
		LoginManager.aClass3_Sub2_Sub1_1 = null;
		LoginManager.aClass3_Sub2_Sub1_6 = null;
		LoginManager.aClass3_Sub2_Sub1_8 = null;
		LoginManager.aClass3_Sub2_Sub1_9 = null;
		if (gameState == 5) {
			TitleScreen.load(js5Archive8);
		}
		if (gameState == 10) {
			InterfaceList.method1596(false);
		}
		if (gameState == 30) {
			setGameState(25);
		}
	}

	@OriginalMember(owner = "client!tk", name = "a", descriptor = "(Z)V")
	public static void method4221() {
		Protocol.anInt4762 = 0;
		Protocol.prevFocus = true;
		Mouse.prevClickTime = 0L;
		MouseRecorder.instance.samples = 0;
		focus = true;
		ReflectionCheck.clear();
		Protocol.opcode4 = -1;
		Protocol.opcode3 = -1;
		Protocol.opcode = -1;
		Protocol.anInt5775 = 0;
		Player.rebootTimer = 0;
		Protocol.outboundBuffer.offset = 0;
		Protocol.opcode2 = -1;
		LoginManager.anInt1862 = 0;
		Protocol.inboundBuffer.offset = 0;
		@Pc(3506) int local3506;
		for (local3506 = 0; local3506 < MiniMap.hintMapMarkers.length; local3506++) {
			MiniMap.hintMapMarkers[local3506] = null;
		}
		MiniMenu.size = 0;
		Cs1ScriptRunner.aBoolean108 = false;
		Mouse.setIdleLoops(0);
		for (local3506 = 0; local3506 < 100; local3506++) {
			Chat.messages[local3506] = null;
		}
		MiniMenu.anInt5014 = 0;
		Camera.anInt3291 = (int) (Math.random() * 100.0D) - 50;
		LoginManager.mapFlagZ = 0;
		Camera.yawTarget = (int) (Math.random() * 20.0D) - 10 & 0x7FF;
		LightingManager.anInt2875 = -1;
		PlayerList.size = 0;
		MiniMap.state = 0;
		Camera.anInt4774 = (int) (Math.random() * 110.0D) - 55;
		MiniMenu.aBoolean302 = false;
		MiniMap.anInt4130 = (int) (Math.random() * 30.0D) - 20;
		SoundPlayer.size = 0;
		LoginManager.mapFlagX = 0;
		MiniMap.anInt1814 = (int) (Math.random() * 120.0D) - 60;
		Chat.size = 0;
		Camera.anInt5161 = (int) (Math.random() * 80.0D) - 40;
		NpcList.size = 0;
		for (local3506 = 0; local3506 < 2048; local3506++) {
			PlayerList.players[local3506] = null;
			PlayerList.appearanceCache[local3506] = null;
		}
		for (local3506 = 0; local3506 < 32768; local3506++) {
			NpcList.npcs[local3506] = null;
		}
		PlayerList.self = PlayerList.players[2047] = new Player();
		SceneGraph.projectiles.clear();
		SceneGraph.spotanims.clear();
		if (SceneGraph.objStacks != null) {
			for (local3506 = 0; local3506 < 4; local3506++) {
				for (@Pc(3663) int local3663 = 0; local3663 < 104; local3663++) {
					for (@Pc(3670) int local3670 = 0; local3670 < 104; local3670++) {
						SceneGraph.objStacks[local3506][local3663][local3670] = null;
					}
				}
			}
		}
		ChangeLocRequest.queue = new LinkedList();
		FriendsList.state = 0;
		FriendsList.size = 0;
		VarpDomain.reset();
		DelayedStateChange.clear();
		Camera.anInt5230 = 0;
		Camera.anInt5217 = 0;
		Camera.anInt5765 = 0;
		Camera.anInt5203 = 0;
		Camera.anInt4232 = 0;
		Camera.anInt5375 = 0;
		Camera.anInt1744 = 0;
		Camera.anInt5449 = 0;
		Camera.anInt4612 = 0;
		Camera.anInt5225 = 0;
		for (local3506 = 0; local3506 < VarcDomain.varcs.length; local3506++) {
			VarcDomain.varcs[local3506] = -1;
		}
		if (InterfaceList.topLevelInterface != -1) {
			InterfaceList.method2275(InterfaceList.topLevelInterface);
		}
		for (@Pc(3755) ComponentPointer local3755 = (ComponentPointer) InterfaceList.openInterfaces.head(); local3755 != null; local3755 = (ComponentPointer) InterfaceList.openInterfaces.next()) {
			InterfaceList.closeInterface(true, local3755);
		}
		InterfaceList.topLevelInterface = -1;
		InterfaceList.openInterfaces = new HashTable(8);
		InterfaceList.method1287();
		Cs1ScriptRunner.aClass13_10 = null;
		Cs1ScriptRunner.aBoolean108 = false;
		MiniMenu.size = 0;
		PlayerAppearance.DEFAULT.set(new int[]{0, 0, 0, 0, 0}, -1, false, null, -1);
		for (local3506 = 0; local3506 < 8; local3506++) {
			Player.options[local3506] = null;
			Player.secondaryOptions[local3506] = false;
			Player.cursors[local3506] = -1;
		}
		Inv.clear();
		ScriptRunner.aBoolean43 = true;
		for (local3506 = 0; local3506 < 100; local3506++) {
			InterfaceList.aBooleanArray100[local3506] = true;
		}
		ClanChat.size = 0;
		ClanChat.members = null;
		ClanChat.name = null;
		for (local3506 = 0; local3506 < 6; local3506++) {
			StockMarketManager.offers[local3506] = new StockMarketOffer();
		}
		for (local3506 = 0; local3506 < 25; local3506++) {
			PlayerSkillXpTable.boostedLevels[local3506] = 0;
			PlayerSkillXpTable.baseLevels[local3506] = 0;
			PlayerSkillXpTable.experience[local3506] = 0;
		}
		if (GlRenderer.enabled) {
			FogManager.setInstantFade();
		}
		Protocol.aBoolean228 = true;
		Protocol.verifyId = 0;
		MiniMenu.walkText = LocalizedText.WALKHERE;
		ScriptRunner.neverRemoveRoofs = false;
		aShortArray88 = aShortArray19 = aShortArray74 = aShortArray87 = new short[256];
		LoginManager.method4637();
		InterfaceList.aBoolean298 = false;
		ClientProt.sendWindowDetails();
	}

	@OriginalMember(owner = "client!rc", name = "d", descriptor = "(I)V")
	public static void method930() {
		if (musicChannel != null) {
			musicChannel.quit();
		}
		if (soundChannel != null) {
			soundChannel.quit();
		}
		AudioChannel.init(Preferences.stereo);
		musicChannel = AudioChannel.create(22050, signLink, canvas, 0);
		musicChannel.setStream(musicStream);
		soundChannel = AudioChannel.create(2048, signLink, canvas, 1);
		soundChannel.setStream(soundStream);
	}

	@OriginalMember(owner = "client!ag", name = "j", descriptor = "(I)V")
	public static void method84() {
		SceneGraph.clear();
		for (@Pc(9) int local9 = 0; local9 < 4; local9++) {
			PathFinder.collisionMaps[local9].clear();
		}
		System.gc();
	}

	@OriginalMember(owner = "client!jj", name = "a", descriptor = "(Z)V")
	public static void topBannerRefresh() {
		if (!advertSuppressed && runEnv != RunEnvs.decomp) {
			try {
				TB_REFRESH.callInBrowserAsJavascriptFunction(instance);
			} catch (@Pc(26) Throwable local26) {
			}
		}
	}

	@OriginalMember(owner = "client!gn", name = "a", descriptor = "(Z)Z")
	public static boolean showVideoAd() {
		// Plays a video ad by calling the showVideoAd() javascript function in browser.
		// Note that this does nothing in the standalone Java client.
		if (javaScript) {
			try {
				SHOW_VIDEO_AD.callInBrowserAsJavascriptFunction(signLink.applet);
				return true;
			} catch (@Pc(14) Throwable ex) {
			}
		}
		return false;
	}

	@OriginalMember(owner = "client!client", name = "f", descriptor = "(I)V")
	@Override
	protected final void mainRedraw() {
		if (gameState == GameStates.error) {
			return;
		}
		@Pc(15) boolean local15 = MidiPlayer.method2699();
		if (local15 && MidiPlayer.jingle && musicChannel != null) {
			musicChannel.method3570();
		}
		if ((gameState == 30 || gameState == 10) && (GameShell.replaceCanvas || DisplayMode.aLong89 != 0L && DisplayMode.aLong89 < MonotonicClock.currentTimeMillis())) {
			DisplayMode.setWindowMode(GameShell.replaceCanvas, DisplayMode.getWindowMode(), Preferences.fullScreenWidth, Preferences.fullScreenHeight);
		}
		@Pc(80) int local80;
		@Pc(84) int local84;
		if (GameShell.fullScreenFrame == null) {
			@Pc(65) Container currentFrame;
			if (GameShell.fullScreenFrame != null) {
				currentFrame = GameShell.fullScreenFrame;
			} else if (GameShell.frame == null) {
				currentFrame = GameShell.signLink.applet;
			} else {
				currentFrame = GameShell.frame;
			}
			local80 = currentFrame.getSize().width;
			local84 = currentFrame.getSize().height;
			if (currentFrame == GameShell.frame) {
				@Pc(90) Insets local90 = GameShell.frame.getInsets();
				local80 -= local90.right + local90.left;
				local84 -= local90.top + local90.bottom;
			}
			if (local80 != GameShell.frameWidth || local84 != GameShell.frameHeight) {
				GameShell.method3662();
				DisplayMode.aLong89 = MonotonicClock.currentTimeMillis() + 500L;
			}
		}
		if (GameShell.fullScreenFrame != null && !GameShell.focus && (gameState == 30 || gameState == 10)) {
			DisplayMode.setWindowMode(false, Preferences.favoriteWorlds, -1, -1);
		}
		@Pc(158) boolean isFullRedraw = false;
		if (GameShell.fullRedraw) {
			isFullRedraw = true;
			GameShell.fullRedraw = false;
		}
		if (isFullRedraw) {
			GameShell.drawMargins();
		}
		if (GlRenderer.enabled) {
			for (local80 = 0; local80 < 100; local80++) {
				InterfaceList.aBooleanArray100[local80] = true;
			}
		}
		if (gameState == 0) {
			LoadingBarAwt.render(null, isFullRedraw, mainLoadSecondaryText, mainLoadPercentage);
		} else if (gameState == 5) {
			LoadingBar.render(false, Fonts.b12Full);
		} else if (gameState == 10) {
			InterfaceList.method2460();
		} else if (gameState == 25 || gameState == 28) {
			if (LoginManager.loadingScreenState == 1) {
				if (anInt5150 < LoginManager.mapFilesMissingCount) {
					anInt5150 = LoginManager.mapFilesMissingCount;
				}
				local80 = (anInt5150 - LoginManager.mapFilesMissingCount) * 50 / anInt5150;
				Fonts.drawTextOnScreen(false, JagString.concatenate(new JagString[]{LocalizedText.LOADING, LINE_BREAK, JagString.parseInt(local80), Cs1ScriptRunner.aClass100_80}));
			} else if (LoginManager.loadingScreenState == 2) {
				if (anInt1196 < LoginManager.anInt5804) {
					anInt1196 = LoginManager.anInt5804;
				}
				local80 = (anInt1196 - LoginManager.anInt5804) * 50 / anInt1196 + 50;
				Fonts.drawTextOnScreen(false, JagString.concatenate(new JagString[]{LocalizedText.LOADING, LINE_BREAK, JagString.parseInt(local80), Cs1ScriptRunner.aClass100_80}));
			} else {
				Fonts.drawTextOnScreen(false, LocalizedText.LOADING);
			}
		} else if (gameState == 30) {
			LoginManager.method1841();
		} else if (gameState == 40) {
			Fonts.drawTextOnScreen(false, JagString.concatenate(new JagString[]{LocalizedText.CONLOST, JagString.aClass100_556, LocalizedText.ATTEMPT_TO_REESTABLISH}));
		}
		if (GlRenderer.enabled && gameState != 0) {
			GlRenderer.swapBuffers();
			for (local80 = 0; local80 < InterfaceList.rectangles; local80++) {
				InterfaceList.rectangleRedraw[local80] = false;
			}
		} 
		
		// Load login screen
		else {
			@Pc(388) Graphics local388;
			
			// Partial redraw if possible, for performance
			if ((gameState == 30 || gameState == 10) && Cheat.rectDebug == 0 && !isFullRedraw) {
				try {
					local388 = GameShell.canvas.getGraphics();
					for (local84 = 0; local84 < InterfaceList.rectangles; local84++) {
						if (InterfaceList.rectangleRedraw[local84]) {
							SoftwareRaster.frameBuffer.drawAt(InterfaceList.rectangleWidth[local84], InterfaceList.rectangleX[local84], InterfaceList.rectangleHeight[local84], local388, InterfaceList.rectangleY[local84]);
							InterfaceList.rectangleRedraw[local84] = false;
						}
					}
				} catch (@Pc(423) Exception local423) {
					GameShell.canvas.repaint();
				}
			} 
			
			// Full redraw
			else if (gameState != 0) {
				try {
					local388 = GameShell.canvas.getGraphics();
					SoftwareRaster.frameBuffer.draw(local388);
					for (local84 = 0; local84 < InterfaceList.rectangles; local84++) {
						InterfaceList.rectangleRedraw[local84] = false;
					}
				} catch (@Pc(453) Exception local453) {
					GameShell.canvas.repaint();
				}
			}
		}
		if (clean) {
			clean();
		}
		if (Preferences.safeMode && gameState == 10 && InterfaceList.topLevelInterface != -1) {
			Preferences.safeMode = false;
			Preferences.write(GameShell.signLink);
		}
	}

	@OriginalMember(owner = "client!client", name = "c", descriptor = "(B)V")
	@Override
	protected final void mainQuit() {
		if (GlRenderer.enabled) {
			GlRenderer.quit();
		}
		if (GameShell.fullScreenFrame != null) {
			DisplayMode.exitFullScreen(GameShell.fullScreenFrame, GameShell.signLink);
			GameShell.fullScreenFrame = null;
		}
		if (GameShell.signLink != null) {
			GameShell.signLink.unloadGlNatives(this.getClass());
		}
		if (MouseRecorder.instance != null) {
			MouseRecorder.instance.running = false;
		}
		MouseRecorder.instance = null;
		if (Protocol.socket != null) {
			Protocol.socket.close();
			Protocol.socket = null;
		}
		Keyboard.stop(GameShell.canvas);
		Mouse.stop(GameShell.canvas);
		if (mouseWheel != null) {
			mouseWheel.stop(GameShell.canvas);
		}
		Keyboard.quit();
		Mouse.quit();
		mouseWheel = null;
		if (musicChannel != null) {
			musicChannel.quit();
		}
		if (soundChannel != null) {
			soundChannel.quit();
		}
		js5NetQueue.quit();
		js5CacheQueue.quit();
		try {
			if (cacheData != null) {
				cacheData.method1455();
			}
			if (cacheIndexes != null) {
				for (@Pc(95) int local95 = 0; local95 < cacheIndexes.length; local95++) {
					if (cacheIndexes[local95] != null) {
						cacheIndexes[local95].method1455();
					}
				}
			}
			if (cacheMasterIndex != null) {
				cacheMasterIndex.method1455();
			}
			if (uid != null) {
				uid.method1455();
			}
		} catch (@Pc(129) IOException local129) {
		}
	}

	@OriginalMember(owner = "client!client", name = "init", descriptor = "()V")
	@Override
	public final void init() {
		if (!this.isHostnameValid()) {
			return;
		}
		worldListId = Integer.parseInt(this.getParameter("worldid"));
		runEnv = Integer.parseInt(this.getParameter("modewhere"));
		if (runEnv < 0 || runEnv > 2) {
			runEnv = RunEnvs.prod;
		}
		runMode = Integer.parseInt(this.getParameter("modewhat"));
		if (runMode < 0 || runMode > 2) {
			runMode = RunModes.live;
		}
		@Pc(50) String local50 = this.getParameter("advertsuppressed");
		advertSuppressed = local50 != null && local50.equals("1");
		try {
			language = Integer.parseInt(this.getParameter("lang"));
		} catch (@Pc(69) Exception local69) {
			language = 0;
		}
		LocalizedText.setLanguage(language);
		@Pc(78) String local78 = this.getParameter("objecttag");
		objectTag = local78 != null && local78.equals("1");
		@Pc(94) String local94 = this.getParameter("js");
		javaScript = local94 != null && local94.equals("1");
		@Pc(111) String local111 = this.getParameter("game");
		if (local111 != null && local111.equals("1")) {
			debug = 1;
		} else {
			debug = 0;
		}
		try {
			affiliate = Integer.parseInt(this.getParameter("affid"));
		} catch (@Pc(130) Exception local130) {
			affiliate = 0;
		}
		settings = SETTINGS.fromParameters(this);
		if (settings == null) {
			settings = JagString.EMPTY;
		}
		@Pc(146) String local146 = this.getParameter("country");
		if (local146 != null) {
			try {
				country = Integer.parseInt(local146);
			} catch (@Pc(153) Exception local153) {
				country = 0;
			}
		}
		@Pc(159) String local159 = this.getParameter("haveie6");
		haveIe6 = local159 != null && local159.equals("1");
		instance = this;
		this.startApplet(runMode + 32);
	}

	@OriginalMember(owner = "client!client", name = "g", descriptor = "(I)V")
	@Override
	protected final void mainInit() {
		GameShell.method3662();
		js5CacheQueue = new Js5CacheQueue();
		js5NetQueue = new Js5NetQueue();

		if (runMode != RunModes.live) {
			Player.aByteArrayArray8 = new byte[50][];
		}

		Preferences.read(GameShell.signLink);

		if (runEnv == RunEnvs.prod) {
			worldListHostname = GlobalConfig.DEFAULT_HOSTNAME; // this.getCodeBase().getHost();
			worldListAlternatePort = GlobalConfig.ALTERNATE_PORT + 1;
			worldListDefaultPort = GlobalConfig.DEFAULT_PORT + 1;
		} else if (runEnv == RunEnvs.qa) {
			worldListHostname = GlobalConfig.DEFAULT_HOSTNAME; // this.getCodeBase().getHost();
			worldListAlternatePort = GlobalConfig.ALTERNATE_PORT + worldListId;
			worldListDefaultPort = GlobalConfig.DEFAULT_PORT + worldListId;
		} else if (runEnv == RunEnvs.decomp) {
			worldListHostname = GlobalConfig.DEFAULT_HOSTNAME; // "127.0.0.1";
			worldListAlternatePort = GlobalConfig.ALTERNATE_PORT + worldListId;
			worldListDefaultPort = GlobalConfig.DEFAULT_PORT + worldListId;
		}

		if (GlobalJsonConfig.instance != null) {
			worldListHostname = GlobalJsonConfig.instance.ip_address;
			worldListAlternatePort = GlobalJsonConfig.instance.server_port + worldListId;
			worldListDefaultPort = GlobalJsonConfig.instance.server_port;
		}

		if (debug == 1) {
			Cheat.shiftClick = true;
			FogManager.defaultLightColorRgb = 16777215;
			FogManager.defaulFogColorRgb = 0;
			PlayerAppearance.destinationBodyColors = PlayerAppearance.GAME1_DESTINATION_BODY_COLORS;
			PlayerAppearance.destinationSkinColors = PlayerAppearance.GAME1_DESTINATION_SKIN_COLORS;
			PlayerAppearance.aShortArray65 = PlayerAppearance.GAME1_SOURCE_BODY_COLORS;
			PlayerAppearance.aShortArray41 = PlayerAppearance.GAME1_SOURCE_SKIN_COLORS;
		} else {
			PlayerAppearance.destinationBodyColors = PlayerAppearance.GAME0_DESTINATION_BODY_COLORS;
			PlayerAppearance.aShortArray41 = PlayerAppearance.GAME0_SOURCE_SKIN_COLORS;
			PlayerAppearance.destinationSkinColors = PlayerAppearance.GAME0_DESTINATION_SKIN_COLORS;
			PlayerAppearance.aShortArray65 = PlayerAppearance.GAME0_SOURCE_BODY_COLORS;
		}

		alternatePort = worldListAlternatePort;
		defaultPort = worldListDefaultPort;
		hostname = worldListHostname;
		worldListPort = worldListDefaultPort;
		aShortArray88 = aShortArray19 = aShortArray74 = aShortArray87 = new short[256];
		port = worldListPort;
		if ((SignLink.anInt5928 == 3 && runEnv != RunEnvs.decomp) || GlobalConfig.SELECT_DEFAULT_WORLD) {
			Player.worldId = worldListId;
		}

		Keyboard.init();
		Keyboard.start(GameShell.canvas);
		Mouse.start(GameShell.canvas);
		mouseWheel = MouseWheel.create();
		if (mouseWheel != null) {
			mouseWheel.start(GameShell.canvas);
		}
		anInt986 = SignLink.anInt5928;
		try {
			if (GameShell.signLink.cacheData != null) {
				cacheData = new BufferedFile(GameShell.signLink.cacheData, 5200, 0);
				for (@Pc(162) int i = 0; i < 28; i++) {
					cacheIndexes[i] = new BufferedFile(GameShell.signLink.cacheIndexes[i], 6000, 0);
				}
				cacheMasterIndex = new BufferedFile(GameShell.signLink.cacheMasterIndex, 6000, 0);
				masterCache = new Cache(255, cacheData, cacheMasterIndex, 500000);
				uid = new BufferedFile(GameShell.signLink.uid, 24, 0);
				GameShell.signLink.cacheIndexes = null;
				GameShell.signLink.cacheMasterIndex = null;
				GameShell.signLink.uid = null;
				GameShell.signLink.cacheData = null;
			}
		} catch (@Pc(220) IOException ex) {
			uid = null;
			cacheData = null;
			cacheMasterIndex = null;
			masterCache = null;
		}
		mainLoadPrimaryText = LocalizedText.GAME0_LOADING;
		if (runEnv != RunEnvs.prod) {
			//Cheat.displayFps = true;
		}
		PluginRepository.Init();
	}

	@OriginalMember(owner = "client!client", name = "c", descriptor = "(I)V")
	@Override
	protected final void reset() {
	}

	@OriginalMember(owner = "client!client", name = "a", descriptor = "(ZI)V")
	private void setJs5Response(@OriginalArg(1) int arg0) {
		js5NetQueue.errors++;
		js5SocketRequest = null;
		js5NetQueue.response = arg0;
		js5Socket = null;
		js5ConnectState = 0;
	}

	@OriginalMember(owner = "client!client", name = "d", descriptor = "(B)V")
	private void mainUpdate() {
		for (InterfaceList.keyQueueSize = 0; Keyboard.nextKey() && InterfaceList.keyQueueSize < 128; InterfaceList.keyQueueSize++) {
			InterfaceList.keyCodes[InterfaceList.keyQueueSize] = Keyboard.keyCode;
			InterfaceList.keyChars[InterfaceList.keyQueueSize] = Keyboard.keyChar;
		}
		Protocol.sceneDelta++;
		if (InterfaceList.topLevelInterface != -1) {
			InterfaceList.method1320(0, 0, 0, GameShell.canvasWidth, InterfaceList.topLevelInterface, 0, GameShell.canvasHeight);
		}
		InterfaceList.transmitTimer++;
		if (GlRenderer.enabled) {
			nextNpc:
			for (@Pc(57) int local57 = 0; local57 < 32768; local57++) {
				@Pc(66) Npc local66 = NpcList.npcs[local57];
				if (local66 != null) {
					@Pc(73) byte local73 = local66.type.loginscreenproperties;
					if ((local73 & 0x2) > 0 && local66.movementQueueSize == 0 && Math.random() * 1000.0D < 10.0D) {
						@Pc(98) int local98 = (int) Math.round(Math.random() * 2.0D - 1.0D);
						@Pc(106) int local106 = (int) Math.round(Math.random() * 2.0D - 1.0D);
						if (local98 != 0 || local106 != 0) {
							local66.movementQueueSpeed[0] = 1;
							local66.movementQueueX[0] = local98 + (local66.xFine >> 7);
							local66.movementQueueZ[0] = local106 + (local66.zFine >> 7);
							PathFinder.collisionMaps[Player.plane].unflagScenery(local66.xFine >> 7, local66.getSize(), false, 0, local66.getSize(), local66.zFine >> 7);
							if (local66.movementQueueX[0] >= 0 && local66.movementQueueX[0] <= 104 - local66.getSize() && local66.movementQueueZ[0] >= 0 && local66.movementQueueZ[0] <= 104 - local66.getSize() && PathFinder.collisionMaps[Player.plane].method3054(local66.zFine >> 7, local66.movementQueueZ[0], local66.movementQueueX[0], local66.xFine >> 7)) {
								if (local66.getSize() > 1) {
									for (@Pc(226) int local226 = local66.movementQueueX[0]; local66.movementQueueX[0] + local66.getSize() > local226; local226++) {
										for (@Pc(246) int local246 = local66.movementQueueZ[0]; local66.movementQueueZ[0] + local66.getSize() > local246; local246++) {
											if ((PathFinder.collisionMaps[Player.plane].flags[local226][local246] & 0x12401FF) != 0) {
												continue nextNpc;
											}
										}
									}
								}
								local66.movementQueueSize = 1;
							}
						}
					}
					NpcList.method2247(local66);
					NpcList.method949(local66);
					NpcList.method879(local66);
					PathFinder.collisionMaps[Player.plane].flagScenery(local66.xFine >> 7, false, local66.zFine >> 7, local66.getSize(), local66.getSize());
				}
			}
		}
		if (!GlRenderer.enabled) {
			Flames.update();
		} else if (LoginManager.step == 0 && CreateManager.step == 0) {
			if (Camera.cameraType == 2) {
				Camera.updateLockedCamera();
			} else {
				Camera.updateLoginScreenCamera();
			}
			if (Camera.renderX >> 7 < 14 || Camera.renderX >> 7 >= 90 || Camera.renderZ >> 7 < 14 || Camera.renderZ >> 7 >= 90) {
				LoginManager.setupLoadingScreenRegion();
			}
		}
		while (true) {
			@Pc(374) HookRequest priorityRequest;
			@Pc(379) Component prioritySource;
			@Pc(387) Component priorityComponent;
			do {
				priorityRequest = (HookRequest) InterfaceList.highPriorityRequests.removeHead();
				if (priorityRequest == null) {
					while (true) {
						do {
							priorityRequest = (HookRequest) InterfaceList.mediumPriorityRequests.removeHead();
							if (priorityRequest == null) {
								while (true) {
									do {
										priorityRequest = (HookRequest) InterfaceList.lowPriorityRequests.removeHead();
										if (priorityRequest == null) {
											if (Cs1ScriptRunner.aClass13_14 != null) {
												Cs1ScriptRunner.method28();
											}
											if (Protocol.openUrlRequest != null && Protocol.openUrlRequest.status == 1) {
												if (Protocol.openUrlRequest.result != null) {
													ScriptRunner.openUrl(ScriptRunner.url, Protocol.newTab);
												}
												Protocol.newTab = false;
												ScriptRunner.url = null;
												Protocol.openUrlRequest = null;
											}
											if (loop % 1500 == 0) {
												topBannerRefresh();
											}
											return;
										}
										prioritySource = priorityRequest.source;
										if (prioritySource.createdComponentId < 0) {
											break;
										}
										priorityComponent = InterfaceList.getComponent(prioritySource.overlayer);
									} while (priorityComponent == null || priorityComponent.createdComponents == null || priorityComponent.createdComponents.length <= prioritySource.createdComponentId || prioritySource != priorityComponent.createdComponents[prioritySource.createdComponentId]);
									ScriptRunner.run(priorityRequest);
								}
							}
							prioritySource = priorityRequest.source;
							if (prioritySource.createdComponentId < 0) {
								break;
							}
							priorityComponent = InterfaceList.getComponent(prioritySource.overlayer);
						} while (priorityComponent == null || priorityComponent.createdComponents == null || prioritySource.createdComponentId >= priorityComponent.createdComponents.length || prioritySource != priorityComponent.createdComponents[prioritySource.createdComponentId]);
						ScriptRunner.run(priorityRequest);
					}
				}
				prioritySource = priorityRequest.source;
				if (prioritySource.createdComponentId < 0) {
					break;
				}
				priorityComponent = InterfaceList.getComponent(prioritySource.overlayer);
			} while (priorityComponent == null || priorityComponent.createdComponents == null || priorityComponent.createdComponents.length <= prioritySource.createdComponentId || prioritySource != priorityComponent.createdComponents[prioritySource.createdComponentId]);
			ScriptRunner.run(priorityRequest);
		}
	}

	@OriginalMember(owner = "client!client", name = "d", descriptor = "(Z)V")
	private void js5NetworkLoop() {
		@Pc(3) boolean idle = js5NetQueue.loop();
		if (!idle) {
			this.js5Connect();
		}
	}

	@OriginalMember(owner = "client!client", name = "h", descriptor = "(I)V")
	private void js5Connect() {
		if (js5PrevErrors < js5NetQueue.errors) {
			js5ConnectDelay = 5 * 50 * (js5NetQueue.errors - 1);
			if (defaultPort == port) {
				port = alternatePort;
			} else {
				port = defaultPort;
			}
			if (js5ConnectDelay > 3000) {
				js5ConnectDelay = 3000;
			}
			if (js5NetQueue.errors >= 2 && js5NetQueue.response == 6) {
				this.error("js5connect_outofdate");
				gameState = GameStates.error;
				return;
			}
			if (js5NetQueue.errors >= 4 && js5NetQueue.response == -1) {
				this.error("js5crc");
				gameState = GameStates.error;
				return;
			}
			if (js5NetQueue.errors >= 4 && (gameState == 0 || gameState == 5)) {
				if (js5NetQueue.response == 7 || js5NetQueue.response == 9) {
					this.error("js5connect_full");
				} else if (js5NetQueue.response > 0) {
					this.error("js5connect");
				} else {
					this.error("js5io");
				}
				gameState = GameStates.error;
				return;
			}
		}
		js5PrevErrors = js5NetQueue.errors;
		if (js5ConnectDelay > 0) {
			js5ConnectDelay--;
			return;
		}
		try {
			if (js5ConnectState == 0) {
				if (GlobalJsonConfig.instance != null) {
					hostname = GlobalJsonConfig.instance.ip_management;
					port = GlobalJsonConfig.instance.server_port + worldListId;
				}
				js5SocketRequest = GameShell.signLink.openSocket(hostname, port);
				js5ConnectState++;
			}
			if (js5ConnectState == 1) {
				if (js5SocketRequest.status == 2) {
					this.setJs5Response(1000);
					return;
				}
				if (js5SocketRequest.status == 1) {
					js5ConnectState++;
				}
			}
			if (js5ConnectState == 2) {
				js5Socket = new BufferedSocket((Socket) js5SocketRequest.result, GameShell.signLink);
				@Pc(194) Buffer buffer = new Buffer(5);
				buffer.p1(15);
				buffer.p4(530);
				js5Socket.write(buffer.data, 5);
				js5ConnectState++;
				js5ConnectTime = MonotonicClock.currentTimeMillis();
			}
			if (js5ConnectState == 3) {
				if (gameState == 0 || gameState == 5 || js5Socket.available() > 0) {
					@Pc(258) int response = js5Socket.read();
					if (response != 0) {
						this.setJs5Response(response);
						return;
					}
					js5ConnectState++;
				} else if (MonotonicClock.currentTimeMillis() - js5ConnectTime > 30000L) {
					this.setJs5Response(1001);
					return;
				}
			}
			if (js5ConnectState == 4) {
				@Pc(296) boolean loggedOut = gameState == 5 || gameState == 10 || gameState == 28;
				js5NetQueue.start(!loggedOut, js5Socket);
				js5Socket = null;
				js5SocketRequest = null;
				js5ConnectState = 0;
			}
		} catch (@Pc(315) IOException ex) {
			this.setJs5Response(1002);
		}
	}

	@OriginalMember(owner = "client!client", name = "i", descriptor = "(I)V")
	private void mainLoad() {
		if (!Preferences.safeMode) {
			noSafeMode:
			while (true) {
				do {
					if (!Keyboard.nextKey()) {
						break noSafeMode;
					}
				} while (Keyboard.keyChar != 115 && Keyboard.keyChar != 83);
				Preferences.safeMode = true;
			}
		}

		@Pc(43) int i;
		if (mainLoadState == 0) {
			@Pc(34) Runtime runtime = Runtime.getRuntime();
			i = (int) (0L / 1024L);
			@Pc(46) long now = MonotonicClock.currentTimeMillis();
			if (firstGc == 0L) {
				firstGc = now;
			}
			if (i > 16384 && now - firstGc < 5000L) {
				if (now - prevGc > 1000L) {
					System.gc();
					prevGc = now;
				}
				mainLoadPercentage = 5;
				mainLoadSecondaryText = LocalizedText.MAINLOAD0;
			} else {
				mainLoadSecondaryText = LocalizedText.MAINLOAD0B;
				mainLoadState = 10;
				mainLoadPercentage = 5;
			}
			return;
		}
		@Pc(98) int percentage;
		if (mainLoadState == 10) {
			LightingManager.setSize();
			for (percentage = 0; percentage < 4; percentage++) {
				PathFinder.collisionMaps[percentage] = new CollisionMap(104, 104);
			}
			mainLoadPercentage = 10;
			mainLoadState = 30;
			mainLoadSecondaryText = LocalizedText.MAINLOAD10B;
		} else if (mainLoadState == 30) {
			if (js5MasterIndex == null) {
				js5MasterIndex = new Js5MasterIndex(js5NetQueue, js5CacheQueue);
			}
			if (js5MasterIndex.isReady()) {
				js5Archive0 = createJs5(false, true, true, 0);
				js5Archive1 = createJs5(false, true, true, 1);
				js5Archive2 = createJs5(true, true, false, 2);
				js5Archive3 = createJs5(false, true, true, 3);
				js5Archive4 = createJs5(false, true, true, 4);
				js5Archive5 = createJs5(true, true, true, 5);
				js5Archive6 = createJs5(true, false, true, 6);
				js5Archive7 = createJs5(false, true, true, 7);
				js5Archive8 = createJs5(false, true, true, 8);
				js5Archive9 = createJs5(false, true, true, 9);
				js5Archive10 = createJs5(false, true, true, 10);
				js5Archive11 = createJs5(false, true, true, 11);
				js5Archive12 = createJs5(false, true, true, 12);
				js5Archive13 = createJs5(false, true, true, 13);
				js5Archive14 = createJs5(false, false, true, 14);
				js5Archive15 = createJs5(false, true, true, 15);
				js5Archive16 = createJs5(false, true, true, 16);
				js5Archive17 = createJs5(false, true, true, 17);
				js5Archive18 = createJs5(false, true, true, 18);
				js5Archive19 = createJs5(false, true, true, 19);
				js5Archive20 = createJs5(false, true, true, 20);
				js5Archive21 = createJs5(false, true, true, 21);
				js5Archive22 = createJs5(false, true, true, 22);
				js5Archive23 = createJs5(true, true, true, 23);
				js5Archive24 = createJs5(false, true, true, 24);
				js5Archive25 = createJs5(false, true, true, 25);
				js5Archive26 = createJs5(true, true, true, 26);
				js5Archive27 = createJs5(false, true, true, 27);
				mainLoadPercentage = 15;
				mainLoadSecondaryText = LocalizedText.MAINLOAD30B;
				mainLoadState = 40;
			} else {
				mainLoadSecondaryText = LocalizedText.MAINLOAD30;
				mainLoadPercentage = 12;
			}
		} else if (mainLoadState == 40) {
			percentage = 0;
			for (i = 0; i < 28; i++) {
				percentage += js5Providers[i].getIndexPercentageComplete() * JS5_ARCHIVE_WEIGHTS[i] / 100;
			}
			if (percentage == 100) {
				mainLoadPercentage = 20;
				mainLoadSecondaryText = LocalizedText.MAINLOAD40B;
				Sprites.init(js5Archive8);
				TitleScreen.init(js5Archive8);
				Flames.init(js5Archive8);
				mainLoadState = 45;
			} else {
				if (percentage != 0) {
					mainLoadSecondaryText = JagString.concatenate(new JagString[]{LocalizedText.CHECKING_FOR_UPDATES, JagString.parseInt(percentage), JagString.PERCENT_SIGN});
				}
				mainLoadPercentage = 20;
			}
		} else if (mainLoadState == 45) {
			AudioChannel.init(Preferences.stereo);
			musicStream = new MidiPcmStream();
			musicStream.init();
			musicChannel = AudioChannel.create(22050, GameShell.signLink, GameShell.canvas, 0);
			musicChannel.setStream(musicStream);
			MidiPlayer.init(musicStream, js5Archive15, js5Archive14, js5Archive4);
			soundChannel = AudioChannel.create(2048, GameShell.signLink, GameShell.canvas, 1);
			soundStream = new MixerPcmStream();
			soundChannel.setStream(soundStream);
			resampler = new PcmResampler(22050, AudioChannel.sampleRate);
			MusicPlayer.titleSong = js5Archive6.getGroupId(TITLE_SONG);
			mainLoadPercentage = 30;
			mainLoadState = 50;
			mainLoadSecondaryText = LocalizedText.MAINLOAD45B;
		} else if (mainLoadState == 50) {
			percentage = Fonts.getReady(js5Archive8, js5Archive13);
			i = Fonts.getTotal();
			if (percentage >= i) {
				mainLoadSecondaryText = LocalizedText.MAINLOAD50B;
				mainLoadPercentage = 35;
				mainLoadState = 60;
			} else {
				mainLoadSecondaryText = JagString.concatenate(new JagString[]{LocalizedText.MAINLOAD50, JagString.parseInt(percentage * 100 / i), JagString.PERCENT_SIGN});
				mainLoadPercentage = 35;
			}
		} else if (mainLoadState == 60) {
			percentage = TitleScreen.getReady(js5Archive8);
			i = TitleScreen.getTotal();
			if (i <= percentage) {
				mainLoadSecondaryText = LocalizedText.MAINLOAD60B;
				mainLoadState = 65;
				mainLoadPercentage = 40;
			} else {
				mainLoadSecondaryText = JagString.concatenate(new JagString[]{LocalizedText.MAINLOAD60, JagString.parseInt(percentage * 100 / i), JagString.PERCENT_SIGN});
				mainLoadPercentage = 40;
			}
		} else if (mainLoadState == 65) {
			Fonts.load(js5Archive13, js5Archive8);
			mainLoadPercentage = 45;
			mainLoadSecondaryText = LocalizedText.MAINLOAD65B;
			setGameState(5);
			mainLoadState = 70;
		} else if (mainLoadState == 70) {
			js5Archive2.fetchAll();
			percentage = js5Archive2.getPercentageComplete();
			js5Archive16.fetchAll();
			percentage += js5Archive16.getPercentageComplete();
			js5Archive17.fetchAll();
			percentage += js5Archive17.getPercentageComplete();
			js5Archive18.fetchAll();
			percentage += js5Archive18.getPercentageComplete();
			js5Archive19.fetchAll();
			percentage += js5Archive19.getPercentageComplete();
			js5Archive20.fetchAll();
			percentage += js5Archive20.getPercentageComplete();
			js5Archive21.fetchAll();
			percentage += js5Archive21.getPercentageComplete();
			js5Archive22.fetchAll();
			percentage += js5Archive22.getPercentageComplete();
			js5Archive24.fetchAll();
			percentage += js5Archive24.getPercentageComplete();
			js5Archive25.fetchAll();
			percentage += js5Archive25.getPercentageComplete();
			js5Archive27.fetchAll();
			percentage += js5Archive27.getPercentageComplete();
			if (percentage >= 1100) {
				ParamTypeList.init(js5Archive2);
				FloTypeList.init(js5Archive2);
				FluTypeList.init(js5Archive2);
				IdkTypeList.init(js5Archive7, js5Archive2);
				LocTypeList.init(js5Archive16, js5Archive7);
				NpcTypeList.init(js5Archive7, js5Archive18);
				ObjTypeList.init(js5Archive19, Fonts.p11FullSoftware, js5Archive7);
				StructTypeList.init(js5Archive2);
				SeqTypeList.init(js5Archive1, js5Archive20, js5Archive0);
				BasTypeList.init(js5Archive2);
				SpotAnimTypeList.init(js5Archive7, js5Archive21);
				VarbitTypeList.init(js5Archive22);
				VarpTypeList.init(js5Archive2);
				InterfaceList.init(js5Archive13, js5Archive8, js5Archive3, js5Archive7);
				InvTypeList.init(js5Archive2);
				EnumTypeList.init(js5Archive17);
				QuickChatPhraseTypeList.init(js5Archive25, js5Archive24, new Js5QuickChatCommandDecoder());
				QuickChatCatTypeList.init(js5Archive25, js5Archive24);
				LightTypeList.init(js5Archive2);
				CursorTypeList.init(js5Archive2, js5Archive8);
				MsiTypeList.init(js5Archive2, js5Archive8);
				mainLoadPercentage = 50;
				mainLoadSecondaryText = LocalizedText.MAINLOAD70B;
				Equipment.init();
				mainLoadState = 80;
			} else {
				mainLoadSecondaryText = JagString.concatenate(new JagString[]{LocalizedText.MAINLOAD70, JagString.parseInt(percentage / 11), JagString.PERCENT_SIGN});
				mainLoadPercentage = 50;
			}
		} else if (mainLoadState == 80) {
			percentage = Sprites.getReady(js5Archive8);
			i = Sprites.total();
			if (i > percentage) {
				mainLoadSecondaryText = JagString.concatenate(new JagString[]{LocalizedText.MAINLOAD80, JagString.parseInt(percentage * 100 / i), JagString.PERCENT_SIGN});
				mainLoadPercentage = 60;
			} else {
				Sprites.load(js5Archive8);
				mainLoadState = 90;
				mainLoadPercentage = 60;
				mainLoadSecondaryText = LocalizedText.MAINLOAD80B;
			}
		} else if (mainLoadState == 90) {
			if (js5Archive26.fetchAll()) {
				@Pc(951) Js5GlTextureProvider textureProvider = new Js5GlTextureProvider(js5Archive9, js5Archive26, js5Archive8, 20, !Preferences.highDetailTextures);
				Rasteriser.unpackTextures(textureProvider);
				if (Preferences.brightness == 1) {
					Rasteriser.setBrightness(0.9F);
				}
				if (Preferences.brightness == 2) {
					Rasteriser.setBrightness(0.8F);
				}
				if (Preferences.brightness == 3) {
					Rasteriser.setBrightness(0.7F);
				}
				if (Preferences.brightness == 4) {
					Rasteriser.setBrightness(0.6F);
				}
				mainLoadSecondaryText = LocalizedText.MAINLOAD90B;
				mainLoadState = 100;
				mainLoadPercentage = 70;
			} else {
				mainLoadSecondaryText = JagString.concatenate(new JagString[]{LocalizedText.MAINLOAD90, JagString.parseInt(js5Archive26.getPercentageComplete()), JagString.PERCENT_SIGN});
				mainLoadPercentage = 70;
			}
		} else if (mainLoadState == 100) {
			if (Flames.isReady(js5Archive8)) {
				mainLoadState = 110;
			}
		} else if (mainLoadState == 110) {
			MouseRecorder.instance = new MouseRecorder();
			GameShell.signLink.startThread(10, MouseRecorder.instance);
			mainLoadSecondaryText = LocalizedText.MAINLOAD110B;
			mainLoadPercentage = 75;
			mainLoadState = 120;
		} else if (mainLoadState == 120) {
			if (js5Archive10.isFileReady(JagString.EMPTY, HUFFMAN_GROUP)) {
				@Pc(1060) HuffmanCodec codec = new HuffmanCodec(js5Archive10.fetchFile(JagString.EMPTY, HUFFMAN_GROUP));
				WordPack.init(codec);
				mainLoadSecondaryText = LocalizedText.MAINLOAD120B;
				mainLoadState = 130;
				mainLoadPercentage = 80;
			} else {
				mainLoadSecondaryText = JagString.concatenate(new JagString[]{LocalizedText.MAINLOAD120, aClass100_899});
				mainLoadPercentage = 80;
			}
		} else if (mainLoadState == 130) {
			if (!js5Archive3.fetchAll()) {
				mainLoadSecondaryText = JagString.concatenate(new JagString[]{LocalizedText.MAINLOAD130, JagString.parseInt(js5Archive3.getPercentageComplete() * 3 / 4), JagString.PERCENT_SIGN});
				mainLoadPercentage = 85;
			} else if (!js5Archive12.fetchAll()) {
				mainLoadSecondaryText = JagString.concatenate(new JagString[]{LocalizedText.MAINLOAD130, JagString.parseInt(js5Archive12.getPercentageComplete() / 10 + 75), JagString.PERCENT_SIGN});
				mainLoadPercentage = 85;
			} else if (!js5Archive13.fetchAll()) {
				mainLoadSecondaryText = JagString.concatenate(new JagString[]{LocalizedText.MAINLOAD130, JagString.parseInt(js5Archive13.getPercentageComplete() / 20 + 85), JagString.PERCENT_SIGN});
				mainLoadPercentage = 85;
			} else if (js5Archive23.isGroupReady(MAPS_GROUP)) {
				MapList.init(Sprites.mapfunctions, js5Archive23);
				mainLoadPercentage = 95;
				mainLoadSecondaryText = LocalizedText.MAINLOAD130B;
				mainLoadState = 135;
			} else {
				mainLoadSecondaryText = JagString.concatenate(new JagString[]{LocalizedText.MAINLOAD130, JagString.parseInt(js5Archive23.getPercentageComplete(MAPS_GROUP) / 10 + 90), JagString.PERCENT_SIGN});
				mainLoadPercentage = 85;
			}
		} else if (mainLoadState == 135) {
			percentage = WorldList.fetch();
			if (percentage == -1) {
				mainLoadPercentage = 95;
				mainLoadSecondaryText = LocalizedText.MAINLOAD135;
			} else if (percentage == 7 || percentage == 9) {
				this.error("worldlistfull");
				setGameState(GameStates.error);
			} else if (WorldList.loaded) {
				mainLoadSecondaryText = LocalizedText.MAINLOAD135B;
				mainLoadState = 140;
				mainLoadPercentage = 96;
			} else {
				this.error("worldlistio_" + percentage);
				setGameState(GameStates.error);
			}
		} else if (mainLoadState == 140) {
			LoginManager.loginScreenId = js5Archive3.getGroupId(LOGINSCREEN);
			js5Archive5.discardNames(false);
			js5Archive6.discardNames(true);
			js5Archive8.discardNames(true);
			js5Archive13.discardNames(true);
			js5Archive10.discardNames(true);
			js5Archive3.discardNames(true);
			mainLoadPercentage = 97;
			mainLoadSecondaryText = LocalizedText.MAINLOAD140;
			mainLoadState = 150;
			clean = true;
		} else if (mainLoadState == 150) {
			MaterialManager.method2807();
			if (Preferences.safeMode) {
				Preferences.windowMode = 0;
				Preferences.antiAliasingMode = 0;
				Preferences.favoriteWorlds = 0;
				Preferences.buildArea = 0;
			}
			Preferences.safeMode = true;
			Preferences.write(GameShell.signLink);
			DisplayMode.setWindowMode(false, Preferences.favoriteWorlds, -1, -1);
			mainLoadPercentage = 100;
			mainLoadState = 160;
			mainLoadSecondaryText = LocalizedText.MAINLOAD150B;
		} else if (mainLoadState == 160) {
			InterfaceList.method1596(true);
		}
	}

	@OriginalMember(owner = "client!client", name = "a", descriptor = "(B)V")
	@Override
	protected final void mainLoop() {
		if (gameState == GameStates.error) {
			return;
		}
		loop++;
		if (loop % 1000 == 1) {
			@Pc(24) GregorianCalendar gregorianCalendar = new GregorianCalendar();
			MiniMenu.gregorianDateSeed = gregorianCalendar.get(Calendar.HOUR_OF_DAY) * 600 + gregorianCalendar.get(Calendar.MINUTE) * 10 + gregorianCalendar.get(Calendar.SECOND) / 6;
			aRandom1.setSeed(MiniMenu.gregorianDateSeed);
			PluginRepository.Tick();
		}
		PluginRepository.Update();
		this.js5NetworkLoop();
		if (js5MasterIndex != null) {
			js5MasterIndex.loop();
		}
		MidiPlayer.loop();
		audioLoop();
		Keyboard.loop();
		Mouse.loop();
		if (GlRenderer.enabled) {
			GlCleaner.process();
		}
		if (mouseWheel != null) {
			@Pc(75) int wheelRotation = mouseWheel.getRotation();
			MouseWheel.wheelRotation = wheelRotation;
		}
		if (gameState == 0) {
			this.mainLoad();
			GameShell.resetTimer();
		} else if (gameState == 5) {
			this.mainLoad();
			GameShell.resetTimer();
		} else if (gameState == 25 || gameState == 28) {
			LoginManager.rebuildMap();
		}
		if (gameState == 10) {
			this.mainUpdate();
			CreateManager.loop();
			LoginManager.loopAuto();
			LoginManager.loop();
		} else if (gameState == 30) {
			Protocol.method1756();
		} else if (gameState == 40) {
			LoginManager.loop();
			if (LoginManager.reply != -3) {
				if (LoginManager.reply == 15) {
					LoginManager.reconnect();
				} else if (LoginManager.reply != 2) {
					LoginManager.processLogout();
				}
			}
		}
	}
}
