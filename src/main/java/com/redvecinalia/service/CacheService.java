package com.redvecinalia.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;

@Service
public class CacheService {

    public String normalizar(String texto) {
        if (texto == null) return "";
        String n = texto.toLowerCase();
        n = Normalizer.normalize(n, Normalizer.Form.NFD);
        n = n.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        n = n.replaceAll("[^a-z0-9\\s]", "");
        n = n.replaceAll("\\s+", " ").trim();
        return n;
    }

    public int levenshtein(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int costo = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + costo
                );
            }
        }
        return dp[a.length()][b.length()];
    }
}