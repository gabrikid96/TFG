package grodrich7.tfg.Activities.Services;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

import grodrich7.tfg.Controller.StorageController;
import grodrich7.tfg.R;

import static android.content.Context.WINDOW_SERVICE;

public class CameraHandler implements
        SurfaceHolder.Callback {

    private Service service;
    private Camera mCamera;
    private Camera.Parameters parameters;
    private Bitmap bmp;
    private Camera.Size pictureSize;
    SurfaceView sv;
    private SurfaceHolder sHolder;
    private WindowManager windowManager;
    WindowManager.LayoutParams params;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    int width = 0, height = 0;
    Handler handler = new Handler();

    public CameraHandler(Service service){
        this.service = service;
        initSettings();
    }

    private void initSettings(){
        Log.d("CAMERA_SERVICE", "StartCommand()");
        pref = service.getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();

        windowManager = (WindowManager) service.getSystemService(WINDOW_SERVICE);
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.width = 1;
        params.height = 1;
        params.x = 0;
        params.y = 0;
    }

    public void start(){
        sv = new SurfaceView(service.getApplicationContext());
        try{
            windowManager.addView(sv, params);
            sHolder = sv.getHolder();
            sHolder.addCallback(this);
        }catch(Exception ex){
            //insufficient permissions.
            stop();
        }

        if (Build.VERSION.SDK_INT < 11)
            sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void stop(){
        try{
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }

                if (sv != null)
                    windowManager.removeView(sv);
        }catch (Exception ex){
            //Toast.makeText(service, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        Log.d("CAMERA_SERVICE", "Service end");

        //LocalBroadcastManager.getInstance(service).sendBroadcast(intent);
    }

    private class TakeImage extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            takeImage();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

    private synchronized void takeImage() {
        try{


        if (checkCameraHardware(service.getApplicationContext())) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = Camera.open();
            } else
                mCamera = getCameraInstance();

            try {
                if (mCamera != null) {
                    mCamera.setPreviewDisplay(sv.getHolder());
                    parameters = mCamera.getParameters();
                    parameters.setFlashMode("off");
                    setBesttPictureResolution();
                    mCamera.setParameters(parameters);
                    mCamera.startPreview();
                    Log.d("CAMERA_SERVICE", "OnTake()");
                    mCamera.takePicture(null, null, mCall);
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(service.getApplicationContext(),
                                    "Camera is unavailable !",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (IOException e) {
                Log.e("CAMERA_SERVICE", "CmaraHeadService()::takePicture", e);
            }
        } else {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(service.getApplicationContext(),
                            "Your Device dosen't have a Camera !",
                            Toast.LENGTH_LONG).show();
                }
            });
            //stopSelf();
            stop();
        }
        }catch (Exception ex){
            Toast.makeText(service, "CAMERA" + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    Camera.PictureCallback mCall = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // decode the data obtained by the camera into a Bitmap
            Log.d("CAMERA_SERVICE", "Done");
            if (bmp != null)
                bmp.recycle();
            System.gc();
            bmp = decodeBitmap(data);
            StorageController.getInstance().uploadImage(service, bmp);
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            Log.d("CAMERA_SERVICE", "Image Taken !");
            if (bmp != null) {
                bmp.recycle();
                bmp = null;
                System.gc();
            }
            mCamera = null;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(service.getApplicationContext(),
                            R.string.image_taken, Toast.LENGTH_SHORT)
                            .show();
                }
            });
            //stopSelf();
            stop();
        }
    };


    //region Image Settings
    private void setBesttPictureResolution() {
        // get biggest picture size
        width = pref.getInt("Picture_Width", 0);
        height = pref.getInt("Picture_height", 0);

        if (width == 0 | height == 0) {
            pictureSize = getBiggesttPictureSize(parameters);
            if (pictureSize != null)
                parameters
                        .setPictureSize(pictureSize.width, pictureSize.height);
            // save width and height in sharedprefrences
            width = pictureSize.width;
            height = pictureSize.height;
            editor.putInt("Picture_Width", width);
            editor.putInt("Picture_height", height);
            editor.commit();

        } else {
            // if (pictureSize != null)
            parameters.setPictureSize(width, height);
        }
    }

    private Camera.Size getBiggesttPictureSize(Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea > resultArea) {
                    result = size;
                }
            }
        }

        return (result);
    }

    public static Bitmap decodeBitmap(byte[] data) {

        Bitmap bitmap = null;
        BitmapFactory.Options bfOptions = new BitmapFactory.Options();
        bfOptions.inDither = false; // Disable Dithering mode
        bfOptions.inPurgeable = true; // Tell to gc that whether it needs free
        // memory, the Bitmap can be cleared
        bfOptions.inInputShareable = true; // Which kind of reference will be
        // used to recover the Bitmap data
        // after being clear, when it will
        // be used in the future
        bfOptions.inTempStorage = new byte[32 * 1024];

        if (data != null)
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                    bfOptions);

        return bitmap;
    }
    //endregion
    //region Camera Settings
    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    //endregion


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        new TakeImage().execute();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        Log.d("CAMERA_SERVICE", "SurfaceDestroyed");
    }
}
