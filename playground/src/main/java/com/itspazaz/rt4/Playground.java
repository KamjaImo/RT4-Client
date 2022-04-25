package com.itspazaz.rt4;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.GLBuffers;
import rt4.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.KeyEvent;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Playground extends GameShell {
    public static Playground instance;

    public static BufferedFile cacheMasterIndex;
    public static BufferedFile cacheData;
    public static BufferedFile uid;
    public static BufferedFile[] cacheIndexes = new BufferedFile[28];

    public static Cache[] cacheArchives = new Cache[28];
    public static Cache masterCache;

    public static Js5MasterIndex js5MasterIndex;
    public static Js5CachedResourceProvider[] js5Providers = new Js5CachedResourceProvider[28];
    public static Js5[] archives = new Js5[28];
    public static Js5NetQueue js5NetQueue;
    public static Js5CacheQueue js5CacheQueue;
    private static PrivilegedRequest js5SocketRequest;
    private static BufferedSocket js5Socket;
    private static long js5ConnectTime;

    public static void main(String[] args) {
        instance = new Playground();
        instance.startApplication(32, "runescape");
        GameShell.frame.setLocation(40, 40);
    }

    @Override
    public void init() {
        instance = this;
        this.startApplet(32);
    }

    @Override
    protected void mainInit() {
        Keyboard.init();
        Keyboard.start(GameShell.canvas);
        Mouse.start(GameShell.canvas);
        SoftwareRaster.frameBuffer.makeTarget();

        try {
            if (GameShell.signLink.cacheData != null) {
                cacheData = new BufferedFile(GameShell.signLink.cacheData, 5200, 0);
                for (int i = 0; i < 28; i++) {
                    cacheIndexes[i] = new BufferedFile(GameShell.signLink.cacheIndexes[i], 6000, 0);
                    cacheArchives[i] = new Cache(i, cacheData, cacheIndexes[i], 1000000);
                }
                cacheMasterIndex = new BufferedFile(GameShell.signLink.cacheMasterIndex, 6000, 0);
                masterCache = new Cache(255, cacheData, cacheMasterIndex, 500000);
                uid = new BufferedFile(GameShell.signLink.uid, 24, 0);
                GameShell.signLink.cacheIndexes = null;
                GameShell.signLink.cacheMasterIndex = null;
                GameShell.signLink.uid = null;
                GameShell.signLink.cacheData = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        js5NetQueue = new Js5NetQueue();
        js5CacheQueue = new Js5CacheQueue();
    }

    public int percentage = 0;
    public int state = 0;

    public NpcType npcType;
    public Npc npc;

    public void stateLoop() {
        if (state == 0) {
            LoadingBarAwt.render(null, true, JagString.parse("Connecting to update server"), 1);

            if (js5MasterIndex == null) {
                js5MasterIndex = new Js5MasterIndex(js5NetQueue, js5CacheQueue);
            }

            if (js5MasterIndex.isReady()) {
                for (int i = 0; i < 28; i++) {
                    js5Providers[i] = js5MasterIndex.getResourceProvider(i, masterCache, cacheArchives[i]);
                    archives[i] = new Js5(js5Providers[i], false, false);
                }
                state++;
            }
        } else if (state == 1) {
            LoadingBarAwt.render(null, true, JagString.parse("Initializing sprites"), 1);
            percentage = 0;
            for (int i = 0; i < 28; i++) {
                percentage += js5Providers[i].getIndexPercentageComplete() * Static170.JS5_ARCHIVE_WEIGHTS[i] / 100;
            }
            if (percentage == 100) {
                Sprites.init(archives[8]);
                TitleScreen.init(archives[8]);
                Flames.init(archives[8]);
                state++;
            }
        } else if (state == 2) {
            LoadingBarAwt.render(null, true, JagString.parse("Preparing fonts"), 1);
            int ready = Fonts.getReady(archives[8], archives[13]);
            int total = Fonts.getTotal();
            if (ready >= total) {
                state++;
            }
        } else if (state == 3) {
            LoadingBarAwt.render(null, true, JagString.parse("Preparing title screen"), 1);
            int ready = TitleScreen.getReady(archives[8]);
            int total = TitleScreen.getTotal();
            if (ready >= total) {
                state++;
            }
        } else if (state == 4) {
            LoadingBarAwt.render(null, true, JagString.parse("Loading fonts"), 1);
            Fonts.load(archives[13], archives[8]);
            state++;
        } else if (state == 5) {
            LoadingBarAwt.render(null, true, JagString.parse("Prefetching"), 1);
            percentage = 0;
            for (int i = 0; i < 28; ++i) {
                archives[i].fetchAll();
                percentage += archives[i].getPercentageComplete();
            }
            if (percentage > 2700) {
                ParamTypeList.init(archives[2]);
                FloTypeList.init(archives[2]);
                FluTypeList.init(archives[2]);
                IdkTypeList.init(archives[7], archives[2]);
                LocTypeList.init(archives[16], archives[7]);
                NpcTypeList.init(archives[7], archives[18]);
                ObjTypeList.init(archives[19], Fonts.p11FullSoftware, archives[7]);
                StructTypeList.init(archives[2]);
                SeqTypeList.init(archives[1], archives[20], archives[0]);
                BasTypeList.init(archives[2]);
                SpotAnimTypeList.init(archives[7], archives[21]);
                VarbitTypeList.init(archives[22]);
                VarpTypeList.init(archives[2]);
                InterfaceList.init(archives[13], archives[8], archives[3], archives[7]);
                InvTypeList.init(archives[2]);
                EnumTypeList.init(archives[17]);
                QuickChatPhraseTypeList.init(archives[25], archives[24], new Js5QuickChatCommandDecoder());
                QuickChatCatTypeList.init(archives[25], archives[24]);
                LightTypeList.init(archives[2]);
                CursorTypeList.init(archives[2], archives[8]);
                MsiTypeList.init(archives[2], archives[8]);
                Equipment.init();
                state++;
            }
        } else if (state == 6) {
            LoadingBarAwt.render(null, true, JagString.parse("Loading sprites"), 1);
            int ready = Sprites.getReady(archives[8]);
            int total = Sprites.total();
            if (ready >= total) {
                Sprites.load(archives[8]);
                state++;
            }
        } else if (state == 7) {
            if (useGl) {
                GlRenderer.init(GameShell.canvas, 16);
                if (GlRenderer.enabled) {
                    GlRenderer.setCanvasSize(GameShell.canvasWidth, GameShell.canvasHeight);
                    GlRenderer.method4173();
                    float yaw = 0.4f * 360.0F / 6.2831855F;
                    float pitch = -0.4f * 360.0F / 6.2831855F;
                    GlRenderer.method4171(0, 0, GameShell.canvasWidth, GameShell.canvasHeight, GameShell.canvasWidth / 2, GameShell.canvasHeight / 2, yaw, pitch, 471, 471);
                    GlRenderer.setViewportBounds(0, 0, GameShell.canvasWidth, GameShell.canvasHeight);
                    GlRenderer.setDepthTestEnabled(true);
                    GlRenderer.setFogEnabled(true);
                    Static241.setWindowMode(false, 2, GameShell.canvasWidth, GameShell.canvasHeight);
                }
            }
            state++;
        } else if (state == 8) {
            Js5GlTextureProvider textureProvider = new Js5GlTextureProvider(archives[9], archives[26], archives[8], 20, false);
            Rasteriser.unpackTextures(textureProvider);
            Rasteriser.setBrightness(0.8F);
            Rasteriser.setBounds(GameShell.canvasWidth, GameShell.canvasHeight);
            Rasteriser.prepare();
            Rasteriser.prepareOffsets();
            npcType = NpcTypeList.get(0);
            npcType.name.print();
            npc = new Npc();
            npc.setNpcType(npcType);
            state++;
        }

        GameShell.frame.setTitle("" + state);
    }

    public static boolean useGl = true;

    private void exportGlImage(String filename) {
        try {
            GL2 gl = GLContext.getCurrentGL().getGL2();

            BufferedImage image = new BufferedImage(GameShell.canvasWidth, GameShell.canvasHeight, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = image.getGraphics();

            ByteBuffer buffer = GLBuffers.newDirectByteBuffer(GameShell.canvasWidth * GameShell.canvasHeight * 4);

            gl.glReadBuffer(GL2.GL_BACK);
            gl.glReadPixels(0, 0, GameShell.canvasWidth, GameShell.canvasHeight, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, buffer);

            for (int h = 0; h < GameShell.canvasHeight; h++) {
                for (int w = 0; w < GameShell.canvasWidth; w++) {
                    graphics.setColor(new Color((buffer.get() & 0xff), (buffer.get() & 0xff), (buffer.get() & 0xff)));
                    buffer.get();
                    graphics.drawRect(w, GameShell.canvasHeight - h, 1, 1);
                }
            }

            ImageIO.write(image, "PNG", new File(filename + ".png"));
        } catch (IOException ex) {
        }
    }

    private void exportImage(int[] pixels, String filename) {
        byte[] raw = new byte[pixels.length * 4];
        int offset = 0;
        for (int rgb : pixels) {
            raw[offset++] = (byte) (rgb >> 16); // red
            raw[offset++] = (byte) (rgb >> 8); // green
            raw[offset++] = (byte) (rgb); // blue

            // set transparency for background color
            if (rgb >> 24 == 0x7F) {
                raw[offset++] = (byte) 0;
            } else {
                raw[offset++] = (byte) 0xFF;
            }
        }

        try {
            DataBuffer buffer = new DataBufferByte(raw, raw.length);
            int samplesPerPixel = 4;
            int[] bandOffsets = { 0, 1, 2, 3 };
            ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
            WritableRaster raster = Raster.createInterleavedRaster(buffer, GameShell.canvasWidth, GameShell.canvasHeight, samplesPerPixel * GameShell.canvasWidth, samplesPerPixel, bandOffsets, null);
            BufferedImage image = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
            ImageIO.write(image, "PNG", new File(filename + ".png"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    int exportCounter = 0;

    public void inputLoop() {
        if (Keyboard.getKey(KeyEvent.VK_BACK_SLASH)) {
            if (GlRenderer.enabled) {
                exportGlImage("dump/" + exportCounter++);
            } else {
                exportImage(SoftwareRaster.pixels, "dump/" + exportCounter++);
            }
        }
    }

    @Override
    protected void mainLoop() {
        Keyboard.loop();
        Mouse.loop();

        js5NetLoop();
        stateLoop();
        inputLoop();
    }

    public int js5ConnectState = 0;

    public void js5NetLoop() {
        boolean idle = js5NetQueue.loop();
        if (!idle) {
            js5Connect();
        }
    }

    public void js5Connect() {
        try {
            if (js5ConnectState == 0) {
                js5SocketRequest = GameShell.signLink.openSocket(GlobalConfig.DEFAULT_HOSTNAME, GlobalConfig.DEFAULT_PORT + 1);
                js5ConnectState++;
            }
            if (js5ConnectState == 1) {
                if (js5SocketRequest.status == 2) {
                    setJs5Response(1000);
                    return;
                }
                if (js5SocketRequest.status == 1) {
                    js5ConnectState++;
                }
            }
            if (js5ConnectState == 2) {
                js5Socket = new BufferedSocket((Socket) js5SocketRequest.result, GameShell.signLink);
                Buffer buffer = new Buffer(5);
                buffer.p1(15);
                buffer.p4(530);
                js5Socket.write(buffer.data, 5);
                js5ConnectState++;
                js5ConnectTime = MonotonicClock.currentTimeMillis();
            }
            if (js5ConnectState == 3) {
                if (js5Socket.available() > 0) {
                    int response = js5Socket.read();
                    if (response != 0) {
                        setJs5Response(response);
                        return;
                    }
                    js5ConnectState++;
                } else if (MonotonicClock.currentTimeMillis() - js5ConnectTime > GlobalConfig.JS5_RESPONSE_TIMEOUT) {
                    setJs5Response(1001);
                    return;
                }
            }
            if (js5ConnectState == 4) {
                js5NetQueue.start(true, js5Socket);
                js5SocketRequest = null;
                js5ConnectState = 0;
                js5Socket = null;
            }
        } catch (IOException ex) {
            setJs5Response(1002);
        }
    }

    private void setJs5Response(int response) {
        js5NetQueue.response = response;
        js5Socket = null;
        js5NetQueue.errors++;
        js5ConnectState = 0;
        js5SocketRequest = null;
    }

    @Override
    protected void mainRedraw() {
        if (state == 9) {
            if (!GlRenderer.enabled) {
                SoftwareRaster.clear(0x7F666666);
            } else {
                GlRenderer.clearColorAndDepthBuffers(0x7F333333);
            }

            if (npc != null) {
                int orientation = 384;
                int x = 128;
                int z = 192;
                int y = 128;
                if (GlRenderer.enabled) {
                    x = 230;
                    z = 400;
                    y *= 4;
                }
                npc.render(orientation, 25079, 60547, -44308, 48222, x, z, y, 0L, 0, null);
            }

            if (!GlRenderer.enabled) {
                SoftwareRaster.frameBuffer.draw(GameShell.canvas.getGraphics());
            } else {
                GlRenderer.method4160();
                GlRenderer.swapBuffers();
            }
        }
    }

    @Override
    protected void mainQuit() {
        if (GlRenderer.enabled) {
            GlRenderer.quit();
        }

        if (GameShell.signLink != null) {
            GameShell.signLink.unloadGlNatives(this.getClass());
        }

        Keyboard.stop(GameShell.canvas);
        Mouse.stop(GameShell.canvas);
        Keyboard.quit();
        Mouse.quit();
    }

    @Override
    protected void reset() {
    }
}