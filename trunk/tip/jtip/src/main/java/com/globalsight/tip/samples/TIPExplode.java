package com.globalsight.tip.samples;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.globalsight.tip.TIPPackage;
import com.globalsight.tip.TIPPackageFactory;

public class TIPExplode {

    public static void main(String[] args) {
        try {
            new TIPExplode().run(args);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    void run(String[] args) throws Exception {
        if (args.length != 2) {
            usage();
        }
        File tipFile = verifyPackageFile(args[0]);
        File targetDir = prepareTargetDirectory(args[1]);
        
        InputStream is = new BufferedInputStream(new FileInputStream(tipFile));
        TIPPackage tip = TIPPackageFactory.openFromStream(is);
        is.close();
        tip.saveToDirectory(targetDir);
        tip.close();
        System.out.println("Wrote package contents to " + targetDir);
    }
    
    private File verifyPackageFile(String tipFilename) 
                                throws IOException {
        File tipFile = new File(tipFilename).getCanonicalFile();
        if (!tipFile.exists()) {
            die("File does not exist: " + tipFilename);
        }
        if (tipFile.isDirectory()) {
            die("Not a package: " + tipFilename);
        }
        return tipFile;
    }
    
    private File prepareTargetDirectory(String targetDirname) 
                                throws IOException {
        File targetDir = new File(targetDirname).getCanonicalFile();
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                die("Could not create directory: " + targetDirname);
            }
        }
        else {
            if (!targetDir.isDirectory()) {
                die("Not a directory: " + targetDirname);
            }
            if (targetDir.list().length > 0) {
                die("Target directory must be empty: " + targetDirname);
            }
        }
        return targetDir;
    }
    
    private void usage() {
        System.err.println("Usage: TIPExplode [tip-file] [target-directory]");
        System.exit(1);
    }
    
    private void die(String msg) {
        System.err.println(msg);
        System.exit(1);
    }
}
