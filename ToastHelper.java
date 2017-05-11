public class ToastHelper {

    private static Toast t;

    public static void show(Context c, Object text) {
        cancel();
        t = Toast.makeText(c, text.toString(), Toast.LENGTH_SHORT);
        t.show();
    }

    public static void cancel() {
        if (t != null)
            t.cancel();

    }
}
