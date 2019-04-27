package com.anest.top;

/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  org.apache.commons.io.FileUtils
 */

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileProcess {

    private final File file;
    private final List<String> lsLines;

    public FileProcess() throws IOException {
        File currentDir = new File(System.getProperty("user.dir"));
        this.file = currentDir.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".txt"))[0];
        this.lsLines = FileUtils.readLines(this.file, "UTF-8");
        this.lsLines.removeIf(line -> !line.contains("|"));
    }

    public List<String> search(String query) {
        ArrayList<String> lsResult = new ArrayList<>();
        query = query.replaceAll("[^a-zA-Z0-9|]", " ").replaceAll("  ", " ").toLowerCase().trim();
        for (String line : this.lsLines) {
            String fixedLine = line.replaceAll("[^a-zA-Z0-9|]", " ").replaceAll("  ", " ").toLowerCase().trim();
            if (!fixedLine.contains(query)) {
                continue;
            }
            lsResult.add(line);
        }
        return lsResult;
    }

}
