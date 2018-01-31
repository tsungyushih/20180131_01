package com.frandog.a20180131_01;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = findViewById(R.id.imageView);
    }

//    開啟照相功能並配合onActivityResult將照片縮圖放上ImageView裡
    public void click1(View v)
    {
//        獲得照相功能
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(it,123);
    }

//    與click1相同，但這次是存到自創資料夾，是原照片而非縮圖
//    在模擬器看不出來，要用Device Monitor看，在storage/emulated/0/專案/files/PHOTO裡，做到click2時尚未需要開啟任何權限
    public void click2(View v)
    {
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

//        創資料夾
        File f = new File(getExternalFilesDir("PHOTO"),"myphoto.jpg");

//        將資料f丟入
        it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

        startActivityForResult(it,456);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123)
        {
            if (resultCode == RESULT_OK)
            {
                Bundle pBundle = data.getExtras();
                Bitmap bmp = (Bitmap) pBundle.get("data");
                img.setImageBitmap(bmp);        //獲得的只是縮圖
            }
        }

//        古早寫法，由於Bitmap不會壓縮圖，以前照相機畫素不高時還可用此方法，對現今的高畫素相機，一下子傳入大檔案可能會塞爆記憶體導致當機(本案中尚可承受)
//        if (requestCode == 456)
//        {
//            if(resultCode == RESULT_OK)
//            {
//                File f = new File(getExternalFilesDir("PHOTO"),"myphoto.jpg");
//
////                將指定路徑中的檔案轉成Bitmap
//                Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
//                img.setImageBitmap(bmp);
//            }


//        配合getFitImage和readStream避免當機的寫法
        if (requestCode == 456)
        {
            if (resultCode == RESULT_OK)
            {
                File f = new File(getExternalFilesDir("PHOTO"), "myphoto.jpg");
                try {
                    InputStream is = new FileInputStream(f);
                    Log.d("BMP", "Can READ:" + is.available());
                    Bitmap bmp = getFitImage(is);
                    img.setImageBitmap(bmp);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
    public static Bitmap getFitImage(InputStream is)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        byte[] bytes = new byte[0];
        try {
            bytes = readStream(is);
            //BitmapFactory.decodeStream(inputStream, null, options);
            Log.d("BMP", "byte length:" + bytes.length);
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            System.gc();
            // Log.d("BMP", "Size:" + bmp.getByteCount());
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }



}
