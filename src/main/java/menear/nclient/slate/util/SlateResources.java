package menear.nclient.slate.util;

import java.io.InputStream;

public final class SlateResources {
    private SlateResources() {
    }

    public static InputStream open(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }

        String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        ClassLoader classLoader = SlateResources.class.getClassLoader();
        InputStream input = classLoader.getResourceAsStream(normalized);
        if (input != null) {
            return input;
        }
        return SlateResources.class.getResourceAsStream(resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath);
    }
}
