package BasicInputQOL;

import plugin.Plugin;
import plugin.annotations.PluginMeta;
import plugin.api.*;
import rt4.Keyboard;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.SwingUtilities;

@PluginMeta(
   author = "Ceikry, Enova",
   description = "Provides some basic input QOL like scroll zoom, middle click panning, etc.",
   version = 1.1
)
public class plugin extends Plugin {
    private boolean cameraDebugEnabled = false;
    private boolean mouseDebugEnabled = false;
    private String minZoomKey = "min-zoom";
    private String maxZoomKey = "max-zoom";
    private String defaultZoomKey = "default-zoom";

    private int lastMouseWheelX = 0;
    private int lastMouseWheelY = 0;
    private int minZoom = 200;
    private int maxZoom = 1200;
    private CameraCoords defaultCameraPYZ = new CameraCoords(128.0, 0.0, 600);

    @Override
    public void Init() {
        Object val = API.GetData(minZoomKey);
        minZoom = (val != null) ? (int)val : minZoom;
        val = API.GetData(maxZoomKey);
        maxZoom = (val != null) ? (int)val : maxZoom;
        API.AddMouseListener(new MouseCallbacks());
        API.AddMouseWheelListener(new MouseWheelCallbacks());
    }

    @Override
    public void ProcessCommand(String commandStr, String[] args) {
        if (commandStr == null) return;

        switch(commandStr) {
            case "::minZoom":
                if (args == null) return;
                minZoom = Integer.parseInt(args[0]);
                API.StoreData(minZoomKey, minZoom);
                break;
            case "::maxZoom":
                if (args == null) return;
                maxZoom = Integer.parseInt(args[0]);
                API.StoreData(maxZoomKey, maxZoom);
                break;
        }

        if (API.PlayerHasPrivilege(Privileges.JMOD)) {
            switch(commandStr) {
                case "::mousedebug": 
                    mouseDebugEnabled = !mouseDebugEnabled;
                    break;
                case "::cameradebug": 
                    cameraDebugEnabled = !cameraDebugEnabled;
                    break;
            }
        }
    }

    @Override
    public void Tick() {
        if (!API.IsLoggedIn())
            return;

        storeDefaultZoom();
    }

    private void storeDefaultZoom() {
        API.StoreData(defaultZoomKey, API.GetCameraZoom());
    }

    private void loadDefaultZoom() {
        Object zoom = API.GetData(defaultZoomKey);
        if (zoom == null) return;

        API.SetCameraZoom((int)zoom, minZoom, maxZoom);
    }

    @Override
    public void Draw() {
        if (mouseDebugEnabled) {
            API.DrawText(
                FontType.SMALL,
                FontColor.YELLOW,
                TextModifier.LEFT,
                "Mouse Coords: (${API.GetMouseX()}, ${API.GetMouseY()})",
                10,
                40
            );
        }
        if (cameraDebugEnabled) {
            API.DrawText(
                FontType.SMALL,
                FontColor.YELLOW,
                TextModifier.LEFT,
                "Camera: [P=${API.GetCameraPitch()}, Y=${API.GetCameraYaw()}, Z=${API.GetCameraZoom()}]",
                10,
                50
            );
        }
    }

    @Override
    public void OnLogin() {
        loadDefaultZoom();
    }

    @Override
    public void OnLogout() {
        storeDefaultZoom();
    }

    class MouseWheelCallbacks implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e == null) return;
            if (API.IsKeyPressed(Keyboard.KEY_SHIFT)) {
                int previous = API.GetPreviousMouseWheelRotation();
                int current = API.GetMouseWheelRotation();
                int diff = current - previous;
                API.UpdateCameraZoom(diff, minZoom, maxZoom);
            }
        }
    }

    class MouseCallbacks extends MouseAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
           if (e == null) return;
           if (SwingUtilities.isMiddleMouseButton(e)) {
               int x = e.getX();
               int y = e.getY();
               int accelX = lastMouseWheelX - x;
               int accelY = lastMouseWheelY - y;
               lastMouseWheelX = x;
               lastMouseWheelY = y;
               API.UpdateCameraYaw(accelX * 2.0);
               API.UpdateCameraPitch(-accelY * 2.0);
           }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e == null) return;

/*            int width = API.GetWindowDimensions().width;
            int[] compassBordersX = intArrayOf(width - 165, width - 125)
            int[] compassBordersY = intArrayOf(0, 45)

            if (
                e.getX() in compassBordersX[0]..compassBordersX[1]
                && e.getY() in compassBordersY[0]..compassBordersY[1]
            )
            {
                API.SetCameraPitch(defaultCameraPYZ.first);
                API.SetCameraYaw(defaultCameraPYZ.second);
            }*/
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e == null) return;
            if (SwingUtilities.isMiddleMouseButton(e)) {
                lastMouseWheelX = e.getX();
                lastMouseWheelY = e.getY();
            }
        }
    }

    class CameraCoords {
        private double p;
        private double y;
        private double z;

        public CameraCoords(double p, double y, double z) {
            this.p = p;
            this.y = y;
            this.z = z;
        }
    }
}