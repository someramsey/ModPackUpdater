package com.ramsey.updater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Dw {
    public static void main(String[] args) {
        try (InputStream in = Dw.class.getResourceAsStream("/modpack/modpack.json")) {
            assert in != null;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

    }
}
