package me.catdev.utils;

import java.io.*;

public final class FileUtils {

    public static void copy(File src, File dst) throws IOException {
        if (src.isDirectory()) {
            if (!dst.exists()) {
                dst.mkdir();
            }
            String[] files = src.list();
            if (files == null) return;
            for (String file : files) {
                File nSrc = new File(src, file);
                File nDst = new File(dst, file);
                copy(nSrc, nDst);
            }
        } else {
            InputStream is = new FileInputStream(src);
            OutputStream os = new FileOutputStream(dst);
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            is.close();
            os.close();
        }
    }

    public static void delete(File f) {
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            if (fs == null) return;
            for (File c : fs) {
                delete(c);
            }
        }
        f.delete();
    }

}
