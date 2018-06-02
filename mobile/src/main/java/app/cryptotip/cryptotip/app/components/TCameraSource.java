package app.cryptotip.cryptotip.app.components;

import android.content.Context;

import com.google.android.gms.vision.barcode.BarcodeDetector;

public class TCameraSource {
    private static TCameraSource cSauceInstance;
    private com.google.android.gms.vision.CameraSource cameraSource;

    protected TCameraSource(){}

    public synchronized static TCameraSource getInstance(){
        if(cSauceInstance == null){
            cSauceInstance = new TCameraSource();
        }

        return cSauceInstance;
    }

    public com.google.android.gms.vision.CameraSource getCameraSource(Context context, BarcodeDetector detector){
        if(cameraSource == null){
            cameraSource = new com.google.android.gms.vision.CameraSource
                    .Builder(context, detector)
                    .setFacing(com.google.android.gms.vision.CameraSource.CAMERA_FACING_BACK)
                    .build();
        }
        else{
            cameraSource = null;
            cameraSource = new com.google.android.gms.vision.CameraSource
                    .Builder(context, detector)
                    .setFacing(com.google.android.gms.vision.CameraSource.CAMERA_FACING_BACK)
                    .build();
        }
        return cameraSource;
    }
}