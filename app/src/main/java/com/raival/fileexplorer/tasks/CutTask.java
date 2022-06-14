package com.raival.fileexplorer.tasks;

import com.raival.fileexplorer.interfaces.QTask;
import com.raival.fileexplorer.interfaces.RegularTask;

import java.io.File;
import java.util.ArrayList;

public class CutTask implements QTask, RegularTask {
    private final ArrayList<File> filesToCut;

    public CutTask(ArrayList<File> selectedFiles) {
        filesToCut = selectedFiles;
    }

    public ArrayList<File> getFilesToCut() {
        return filesToCut;
    }

    @Override
    public String getName() {
        return "Cut";
    }

    @Override
    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (File file : filesToCut) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(file.getName());
            first = false;
        }
        return sb.toString();
    }

    @Override
    public ArrayList<File> getFilesList() {
        return getFilesToCut();
    }
}