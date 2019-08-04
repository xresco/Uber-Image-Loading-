package com.abed.app.ubertest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.abed.app.ubertest.network.chache.ImageMemoryCache;
import com.abed.app.ubertest.utils.ApplicationClass;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.annotation.Config;

import java.io.IOException;

@Config(application = ApplicationClass.class)
public class CacheTest {
    @Test
    public void memoryCacheTest() throws IOException {
        ImageMemoryCache imageMemoryCache = new ImageMemoryCache();
        Bitmap bm1 = BitmapFactory.decodeStream(getClass().getResourceAsStream("img1.jpg"));
        imageMemoryCache.put("1", bm1);

        Bitmap bm2 = BitmapFactory.decodeStream(getClass().getResourceAsStream("img2.png"));
        imageMemoryCache.put("2", bm2);

        Bitmap bm3 = BitmapFactory.decodeStream(getClass().getResourceAsStream("img3.png"));
        imageMemoryCache.put("3", bm3);

        Assert.assertEquals(imageMemoryCache.get("1"), bm1);
        Assert.assertEquals(imageMemoryCache.get("2"), bm2);
        Assert.assertEquals(imageMemoryCache.get("3"), bm3);

        imageMemoryCache.clear();
        Assert.assertNull(imageMemoryCache.get("1"));
        Assert.assertNull(imageMemoryCache.get("2"));
        Assert.assertNull(imageMemoryCache.get("3"));
    }
}