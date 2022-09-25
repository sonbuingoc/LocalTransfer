package com.bns.localtransfer;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class MyWebserver extends NanoHTTPD {
    Context context;
    fileCallback fileCallback;
    String imageFileName;
    String filename;
    String imagePath;

    public MyWebserver(int port, Context context, MyWebserver.fileCallback fileCallback) {
        super(port);
        this.context = context;
        this.fileCallback = fileCallback;
    }

    public MyWebserver(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        imagePath = image.getAbsolutePath();
        filename = imagePath.substring(imagePath.lastIndexOf("/")+1);

        InputStream inputStream = null;
        AssetManager assetManager = context.getAssets();
        try {
            inputStream = assetManager.open("index.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String html = MainActivity.htmlToString(inputStream);
        Map<String, String> files = new HashMap<>();
        ContentType ct = new ContentType(session.getHeaders().get("content-type")).tryUTF8();
        session.getHeaders().put("content-type", ct.getContentTypeHeader());

        if (session.getMethod() == Method.POST) {
            try {
                session.parseBody(files);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ResponseException e1) {
                e1.printStackTrace();
            }
            File file = new File(files.get("fileToUpload"));

            System.out.println("ngocson file: " + file);
            System.out.println("ngocson length: " + file.length());
            System.out.println("ngocson copy file: " + copyFile(file, image));
        }

        return newFixedLengthResponse(html);

    }

    @Override
    public TempFileManagerFactory getTempFileManagerFactory() {
        return super.getTempFileManagerFactory();
    }

    public static String saveFile(Context context, String name, String path) {
        String root = "/Pictures/";
        File direct = new File(context.getFilesDir() + root);
        File file = new File(context.getFilesDir() + root + name);
        if (!direct.exists()) {
            direct.mkdir();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileChannel src = new FileInputStream(path).getChannel();
                FileChannel dst = new FileOutputStream(file).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file.toString();
    }
    private boolean copyFile(File source, File target) {
        if (source.isDirectory()) {
            if (! target.exists()) {
                if (! target.mkdir()) {
                    return false;
                }
            }
            String[] children = source.list();
            for (int i = 0; i < source.listFiles().length; i++) {
                if (! copyFile(new File(source, children[i]), new File(target, children[i]))) {
                    return false;
                }
            }
        } else {
            try {
                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(target);

                byte[] buf = new byte[65536];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (IOException ioe) {
                return false;
            }
        }
        fileCallback.file(filename, imagePath);
        return true;
    }
    public interface fileCallback{
         void file(String imageFileName, String imagePath);
    }
}
