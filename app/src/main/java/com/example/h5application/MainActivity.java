package com.example.h5application;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;


import com.example.h5application.base.BaseActivity;
import com.example.h5application.utils.AppConstant;
import com.example.h5application.utils.BitmapUtils;
import com.example.h5application.utils.CameraUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "CameraActivity";
    ImageView btnBack, btnTake;
    private Camera mCamera;
    private CameraPreview mPreview;
    private SurfaceHolder mHolder;
    /**
     * 拍照id  1： 前摄像头  0：后摄像头
     */
    private int mCameraId = 0;

    /**
     * 屏幕宽高
     */
    private int screenWidth;
    private int screenHeight;

    /**
     * 图片宽高
     */
    private int picWidth;
    private int picHeight;

    private CameraUtil cameraInstance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission("请给予相机、存储权限，以便app正常工作", null,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});

        //去除标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        initData();

        btnBack = findViewById(R.id.iv_back);
        btnBack.setOnClickListener(this);
        btnTake = findViewById(R.id.img_camera);
        btnTake.setOnClickListener(this);


        // Create an instance of Camera
        mCamera = getCameraInstance();
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.frame_layout);
        preview.addView(mPreview);
    }

    protected void initData() {
        cameraInstance = CameraUtil.getInstance();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = getCamera(mCameraId);
            if (mHolder != null) {
                startPreview(mCamera, mHolder);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.img_camera:
                mCamera.takePicture(null, null, mPicture);
//                mCamera.lockFocus();
//                takePhoto();
                break;
            default:
                break;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }
    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 预览相机
     */
    private void startPreview(Camera camera, SurfaceHolder holder) {
        try {
            if(camera == null){
                return;
            }
//            setupCamera(camera);  加个这个图片像素很低
            camera.setPreviewDisplay(holder);
            /*mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    try{
                        // YUV转为RGB
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        image.compressToJpeg(new Rect(0, 0, size.width/2, size.height/2), 100, stream);
//                        Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
//                        svShow.setBitmap(BitmapUtils.rotateMyBitmap(BitmapUtils.ImgaeToNegative(bmp)));
                        stream.close();
                    }catch(Exception ex){
                        Log.e("Sys","Error:"+ex.getMessage());
                    }
                }
            });*/
            cameraInstance.setCameraDisplayOrientation(this, mCameraId, camera);
            camera.startPreview();
//            isView = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置surfaceView的尺寸 因为camera默认是横屏，所以取得支持尺寸也都是横屏的尺寸
     * 我们在startPreview方法里面把它矫正了过来，但是这里我们设置设置surfaceView的尺寸的时候要注意 previewSize.height<previewSize.width
     * previewSize.width才是surfaceView的高度
     * 一般相机都是屏幕的宽度 这里设置为屏幕宽度 高度自适应 你也可以设置自己想要的大小
     */
    private void setupCamera(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        //根据屏幕尺寸获取最佳 大小
        Camera.Size previewSize = cameraInstance.getPicPreviewSize(parameters.getSupportedPreviewSizes(),
                screenHeight, screenWidth);
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        Camera.Size pictrueSize = cameraInstance.getPicPreviewSize(parameters.getSupportedPictureSizes(),
                screenHeight,screenWidth);
        parameters.setPictureSize(pictrueSize.width, pictrueSize.height);
        camera.setParameters(parameters);
//        picHeight = (screenWidth * pictrueSize.width) / pictrueSize.height;
        picWidth = pictrueSize.width;
        picHeight = pictrueSize.height;
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth,
//                (screenWidth * pictrueSize.width) / pictrueSize.height);
//        svContent.setLayoutParams(params);
    }


    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            startPreview(mCamera, holder);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            releaseCamera();
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            if(mCamera == null){
                return;
            }
            mCamera.stopPreview();
            startPreview(mCamera, holder);
        }
    }
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            /*File pictureFile = getOutputMediaFile();
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }*/

            /*try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Intent intent = new Intent();
                intent.putExtra(AppConstant.KEY.IMG_PATH, pictureFile.getPath());
                setResult(AppConstant.RESULT_CODE.RESULT_OK, intent);
                finish();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }*/

            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap saveBitmap = cameraInstance.setTakePicktrueOrientation(mCameraId, bitmap);
                DisplayMetrics dm = getResources().getDisplayMetrics();
                saveBitmap = Bitmap.createScaledBitmap(saveBitmap, dm.widthPixels, dm.heightPixels, true);
                String imgpath = getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath() +
                        File.separator + System.currentTimeMillis() + ".jpeg";
                Log.e("TAG", "imgpath: ---  " + imgpath);
                BitmapUtils.saveJPGE_After(getApplicationContext(), saveBitmap, imgpath, 100);
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                if (!saveBitmap.isRecycled()) {
                    saveBitmap.recycle();
                }
                Intent intent = new Intent();
                intent.putExtra(AppConstant.KEY.IMG_PATH, imgpath);
                intent.putExtra(AppConstant.KEY.PIC_WIDTH, picWidth);
                intent.putExtra(AppConstant.KEY.PIC_HEIGHT, picHeight);
                setResult(AppConstant.RESULT_CODE.RESULT_OK, intent);
                finish();
            }catch (Exception e) {
                Log.d("TAG", "Error accessing file: " + e.getMessage());
            }
        }
    };
    private static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }

    /**
     * 获取Camera实例
     *
     * @return Camera
     */
    private Camera getCamera(int id) {
        Camera camera = null;
        try {
            camera = Camera.open(id);
        } catch (Exception e) {
            Log.e(TAG, "getCamera: " + e);
        }
        return camera;
    }
}
