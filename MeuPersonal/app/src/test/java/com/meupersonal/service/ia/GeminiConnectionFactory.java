package com.meupersonal.service.ia;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public interface GeminiConnectionFactory {
    HttpURLConnection open(URL url) throws IOException;
}
