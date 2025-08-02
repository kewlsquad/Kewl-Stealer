package Kewl.Stealer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    public static List<File> getFiles(String path) {
        List<File> files = new ArrayList();
        File[] file1 = (new File(path)).listFiles();

        for(File file : file1) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                File[] file2 = (new File(file.getPath())).listFiles();

                for(File file3 : file2) {
                    if (file3.isFile()) {
                        files.add(file3);
                    }
                }
            }
        }

        return files;
    }
}