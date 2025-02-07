package de.envite.proa.rest;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@ApplicationScoped
public class FileService {
    public String readFileToString(File file) {
        StringBuilder contentBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                contentBuilder.append(sCurrentLine).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }
}
