package com.bridge187.corfupages.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.ImageView;
import com.bridge187.corfupages.R;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

/**
 * This class is used to cache a certain amount of images, and discard images as the cache grows, on a last read first out basis.
 * This is to stop Out Of Memory error when there are many images to display
 */
public class ImageCache
{
    private final LruCache<String, Bitmap> memoryCache;

    public ImageCache()
    {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory * 3 / 16;

        memoryCache = new LruCache<String, Bitmap>(cacheSize)
        {
            @Override
            protected int sizeOf(String key, Bitmap bitmap)
            {
                return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
            }
        };
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap)
    {
        if (bitmap == null)
        {
            return;
        }

        if (getBitmapFromMemCache(key) == null)
        {
            memoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key)
    {
        return memoryCache.get(key);
    }

    /**
     * Called from the listing page or the details page or the gallery when we want to display a bitmap
     * @param key can be anything, but we use the actual we address of the thumbnail, to keep it unique
     * @param imageView the ImageView we want to insert this image into
     * @param large false if thumbnail, tru if for gallery
     * @param id the related listing id, or -1, used only in listings adapter
     */
    public void loadBitmap(String key, ImageView imageView, boolean large, long id)
    {

        final Bitmap bitmap = getBitmapFromMemCache(key);

        if (bitmap != null)
        {
            imageView.setImageBitmap(bitmap);
        }
        else
        {
            imageView.setImageResource(large ? R.drawable.cat_loading_large : R.drawable.cat_loading);
            BitmapWorkerTask task = new BitmapWorkerTask(imageView, id);
            task.execute(key);
        }
    }

    class BitmapWorkerTask extends AsyncTask<String, Long, Bitmap>
    {
        private final WeakReference<ImageView> imageViewReference;
        private long id = -2;

        public BitmapWorkerTask(ImageView imageView, long id)
        {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
            this.id = id;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params)
        {
            Bitmap bitmap = createScaledBitmapFromStream(params[0]);
            addBitmapToMemoryCache(params[0], bitmap);
            return bitmap;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            final ImageView imageView = imageViewReference.get();

            if (imageView == null)
            {
                return;
            }
            long newId = -1;
            if (imageView.getTag() != null)
            {
                newId = (long)imageView.getTag();
            }

            boolean good = id == -1 || newId == id;
            if (bitmap != null)
            {
                if (good)
                {
                    imageView.setImageBitmap(bitmap);
                }
            }
            else
            {
                if (good)
                {
                    imageView.setImageResource(R.drawable.cat_other);
                }
            }

        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > 900 || width > 900)
        {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > 900
                    && (halfWidth / inSampleSize) > 900)
            {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    private Bitmap createScaledBitmapFromStream(String url)
    {
        BufferedInputStream bis = null;
        InputStream is;
        Bitmap bitmap = null;
        boolean fail = false;
        try
        {
            is = (InputStream) new URL(url).getContent();
            bis = new BufferedInputStream(is, 32 * 1024);

            final BitmapFactory.Options decodeBitmapOptions = new BitmapFactory.Options();

            final BitmapFactory.Options decodeBoundsOptions = new BitmapFactory.Options();
            decodeBoundsOptions.inJustDecodeBounds = true;
            bis.mark(32 * 1024); // 32k is probably overkill, but 8k is insufficient for some jpegs
            BitmapFactory.decodeStream(bis, null, decodeBoundsOptions);
            bis.reset();

            decodeBitmapOptions.inSampleSize = calculateInSampleSize(decodeBoundsOptions);

            bitmap = BitmapFactory.decodeStream(bis, null, decodeBitmapOptions);

        }
        catch( IOException e )
        {
            e.printStackTrace();
            fail = true;
        }
        finally
        {
            try
            {
                if (bis != null)
                {
                    bis.close();
                }

            }
            catch( IOException ignored )
            {
                ignored.printStackTrace();
            }
        }

        if (fail)
        {
            try
            {
                InputStream inp = (InputStream) new URL(url).getContent();
                bitmap = BitmapFactory.decodeStream(inp);
            }
            catch( IOException ignored )
            {
                ignored.printStackTrace();
            }
        }

        return bitmap;
    }
}
