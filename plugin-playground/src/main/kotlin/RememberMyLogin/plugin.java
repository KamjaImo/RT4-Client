package RememberMyLogin;

import plugin.Plugin;
import plugin.annotations.PluginMeta;
import plugin.api.API;
import rt4.Component;
import rt4.JagString;
import rt4.Player;

@PluginMeta (
    author = "Ceikry",
    description = "Stores your last used login for automatic reuse",
    version = 1.0
)
public class plugin extends Plugin {
    boolean hasRan = false;
    String username = "";
    String password = "";

    @Override
    public void Init() {
        Object val = API.GetData("login-user");
        if (val != null)
            username = (String)val;
        val = API.GetData("login-pass");
        if (val != null)
            password = (String)val;
    }

    @Override
    public void ComponentDraw(int componentIndex, Component component, int screenX, int screenY) {
        if (hasRan || API.IsLoggedIn() || component == null) return;
        if (component.text.equals(JagString.of("Please Log In"))) {
            if (username != null)
                API.SetVarcStr(32, username);
            if (password != null)
                API.SetVarcStr(33, password);
            hasRan = true;
        }
    }

    @Override
    public void OnLogin() {
        username = Player.usernameInput.toString();
        password = Player.password.toString();
        API.StoreData("login-user", username);
        API.StoreData("login-pass", password);
    }

    @Override
    public void OnLogout() {
        hasRan = false;
    }
}