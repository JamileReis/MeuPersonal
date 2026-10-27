package com.meupersonal.service.treino;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidationResult {
    private boolean valid = true;
    private final List<String> messages = new ArrayList<>();

    public void addMessage(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            return;
        }

        messages.add(msg);
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }
}
