import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    private static ResourceBundle bundle;

    static {
        loadBundle(Locale.getDefault());
    }

    public static void loadBundle(Locale locale) {
        bundle = ResourceBundle.getBundle("messages", locale);
    }

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static String get(String key, Object... params) { //varargs
        return MessageFormat.format(get(key), params);
    }
}
