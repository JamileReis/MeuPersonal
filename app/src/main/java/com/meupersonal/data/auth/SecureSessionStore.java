package com.meupersonal.data.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.meupersonal.model.auth.AuthSession;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class SecureSessionStore {
    private static final String PREFS = "auth_secure_session";
    private static final String VALUE = "encrypted_session";
    private static final String KEY_ALIAS = "meupersonal.auth.session.v1";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final int GCM_TAG_BITS = 128;

    private final SharedPreferences preferences;

    public SecureSessionStore(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public synchronized void save(AuthSession session) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] encrypted = cipher.doFinal(session.toJson().toString().getBytes(StandardCharsets.UTF_8));
        String payload = Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP)
                + ":" + Base64.encodeToString(encrypted, Base64.NO_WRAP);
        if (!preferences.edit().putString(VALUE, payload).commit()) {
            throw new IllegalStateException("Não foi possível persistir a sessão.");
        }
    }

    public synchronized AuthSession read() {
        String payload = preferences.getString(VALUE, null);
        if (payload == null) return null;
        try {
            String[] parts = payload.split(":", 2);
            if (parts.length != 2) throw new IllegalStateException("Sessão inválida.");
            byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] encrypted = Base64.decode(parts[1], Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            String json = new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
            return AuthSession.fromJson(new JSONObject(json));
        } catch (Exception error) {
            clear();
            return null;
        }
    }

    public synchronized void clear() {
        preferences.edit().remove(VALUE).apply();
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        java.security.Key existing = keyStore.getKey(KEY_ALIAS, null);
        if (existing instanceof SecretKey) return (SecretKey) existing;

        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build());
        return generator.generateKey();
    }
}
