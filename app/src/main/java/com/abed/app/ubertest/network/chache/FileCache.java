package com.abed.app.ubertest.network.chache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class FileCache implements ImageCache {

    @NonNull
    private File cacheDir;


    public FileCache(Context context) {
        //Find the dir to save cached images{
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "TempImages");
        } else {
            cacheDir = context.getCacheDir();
        }

        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    @Nullable
    @Override
    public Bitmap get(@NonNull String url) {
        String id = generateNameFromUrl(url);
        return decodeFile(new File(cacheDir, id));
    }

    @Override
    public void put(@NonNull String url, @NonNull Bitmap bitmap) {
        String id = generateNameFromUrl(url);
        try {
            //create a file to write bitmap data
            File file = new File(cacheDir, id);
            file.createNewFile();

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {
        File[] files = cacheDir.listFiles();

        if (files == null) {
            return;
        }

        for (File f : files) {
            f.delete();
        }

    }


    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File file) {
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, o);

            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 200;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(file), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String generateNameFromUrl(String url) {
        // Replace useless chareacters with UNDERSCORE
        String uniqueName = url.replace("://", "_").replace(".", "_").replace("/", "_");
        // Replace last UNDERSCORE with a DOT
        uniqueName = uniqueName.substring(0, uniqueName.lastIndexOf('_'))
                + "." + uniqueName.substring(uniqueName.lastIndexOf('_') + 1, uniqueName.length());
        return uniqueName;
    }
}
