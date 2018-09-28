package com.mlsdev.rximagepicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class RxImageConverters {

    public static File uriToFile(final Context context, final Uri uri, final File file) {

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            copyInputStreamToFile(inputStream, file);
            return file;
        } catch (Exception e) {
            Log.e(RxImageConverters.class.getSimpleName(), "Error converting uri", e);
            return null;
        }

    }

    public static Bitmap uriToBitmap(final Context context, final Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);

        } catch (IOException e) {
            Log.e(RxImageConverters.class.getSimpleName(), "Error converting uri", e);
            return null;
        }
    }
    /*
     public static Observable<File> uriToFile(final Context context, final Uri uri, final File file) {
        return Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<File> emitter) throws Exception {
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(uri);
                    copyInputStreamToFile(inputStream, file);
                    emitter.onNext(file);
                    emitter.onComplete();
                } catch (Exception e) {
                    Log.e(RxImageConverters.class.getSimpleName(), "Error converting uri", e);
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }
    public static Observable<Bitmap> uriToBitmapold(final Context context, final Uri uri) {
        return Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Bitmap> emitter) throws Exception {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                    emitter.onNext(bitmap);
                    emitter.onComplete();
                } catch (IOException e) {
                    Log.e(RxImageConverters.class.getSimpleName(), "Error converting uri", e);
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }*/

    private static void copyInputStreamToFile(InputStream in, File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[10 * 1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        in.close();
    }

}
