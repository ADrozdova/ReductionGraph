package com.toposat;

import java.io.File;
import java.io.IOException;

public class LauncherSATSolver {
    public static void solveCNFPainless(String path, String resultFile, String questionFile) throws IOException, InterruptedException {
        File result = new File(resultFile);
        result.createNewFile();
        ProcessBuilder b = new ProcessBuilder(path, questionFile);
        b.redirectOutput(result);
        Process p = b.start();
        p.waitFor();
    }
}
