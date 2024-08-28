package com.thesis.dermocura.classes;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClassBase64Convert {

    public static String convertUriToBase64(Context context, Uri uri) throws IOException {
        byte[] bytes = readBytesFromUri(context, uri);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private static byte[] readBytesFromUri(Context context, Uri uri) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {

            if (inputStream == null) {
                throw new IOException("Unable to open input stream from URI");
            }

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        }
    }
}
