package com.example.h5application.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Bitmap处理工具
 *
 * @date 2019年1月25日 14:27:33
 */
public class BitmapUtils {
    private BitmapUtils() {
    }

    /**
     * 800*480
     */
    private static final int CONFIG_480P = 1;
    /**
     * 1280*720
     */
    private static final int CONFIG_720P = 2;
    /**
     * 1920*1080
     */
    private static final int CONFIG_1080P = 3;
    /**
     * 2560*1440
     */
    private static final int CONFIG_2K = 4;

    /**
     * 旋转 bitmap
     *
     * @param bmp bitmap
     * @return 旋转后的 bitmap
     */
    public static Bitmap rotateMyBitmap(Bitmap bmp) {
        //*****旋转一下
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Config.ARGB_8888);

        Bitmap nbmp2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        return nbmp2;
    }
    /**
     * 将图片 D65 转换 为位图
     *
     * @param bitmap 原来图片
     * @return 新图片
     */
    public static Bitmap ImgaeToNegative(Bitmap bitmap) {
        //其实我们获得宽和高就是图片像素的宽和高
        //它们的乘积就是总共一张图片拥有的像素点数
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        //用来存储旧的色素点的数组
        int[] oldPx = new int[width * height];
        //用来存储新的像素点的数组
        int[] newPx = new int[width * height];
        int color;//用来存储原来颜色值
        int r, g, b, a;//存储颜色的四个分量：红，绿，蓝，透明度

        //该方法用来将图片的像素写入到oldPx中，我们这样子设置，就会获取全部的像素点
        //第一个参数为写入的数组，第二个参数为读取第一个的像素点的偏移量，一般设置为0
        //第三个参数为写入时，多少个像素点作为一行,第三个和第四个参数为读取的起点坐标
        //第五个参数表示读取的长度，第六个表示读取的高度
        bitmap.getPixels(oldPx, 0, width, 0, 0, width, height);
        // 存放 rgb
        double[] rgbmap = new double[3];
        //下面用循环来处理每一个像素点
        long startTime = System.currentTimeMillis();
        int index = 0;
        for (int i = 0; i < width * height; i++) {
            //获取一个原来的像素点
            color = oldPx[i];
            r = Color.red(color);
            g = Color.green(color);
            b = Color.blue(color);
            a = Color.alpha(color);
            rgbmap[0] = r;
            rgbmap[1] = g;
            rgbmap[2] = b;
            // D65 光源 换算
            double[] xyz = LabUtil.sRGB2XYZ(rgbmap);
            double[] lab = LabUtil.XYZ2Lab(xyz);

            double[] xyz2 = LabUtil.Lab2XYZ(lab);
            double[] rgb = LabUtil.XYZ2sRGB(xyz2);

            //下面计算生成新的颜色分量
            r = (int) rgb[0];
            g = (int) rgb[1];
            b = (int) rgb[2];

            if(rgbmap[0]!=r || rgbmap[1]!=g || rgbmap[2]!=b){
                index ++;
            }

            //下面主要保证r g b 的值都必须在0~255之内
            if (r > 255) {
                r = 255;
            } else if (r < 0) {
                r = 0;
            }
            if (g > 255) {
                g = 255;
            } else if (g < 0) {
                g = 0;
            }
            if (b > 255) {
                b = 255;
            } else if (b < 0) {
                b = 0;
            }
            //下面合成新的像素点，并添加到newPx中
            color = Color.argb(a, r, g, b);
            newPx[i] = color;
        }
        //然后重要的一步，为bmp设置新颜色了,该方法中的参数意义与getPixels中的一样
        //无非是将newPx写入到bmp中
        bmp.setPixels(newPx, 0, width, 0, 0, width, height);
        Log.e("camera2", "图片转换需要时间 ："+ (System.currentTimeMillis()-startTime)
                +" 修改次数："+ index);
        return bmp;
    }

    /**
     * 保存图片为JPEG
     *
     * @param bitmap
     * @param path
     */
    public static void saveJPGE_After(Context context, Bitmap bitmap, String path, int quality) {
        File file = new File(path);
        makeDir(file);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                out.flush();
                out.close();
            }
            updateResources(context, file.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void makeDir(File file) {
        File tempPath = new File(file.getParent());
        if (!tempPath.exists()) {
            tempPath.mkdirs();
        }
    }
    public static void updateResources(Context context, String path) {
        MediaScannerConnection.scanFile(context, new String[]{path}, null, null);
    }
}