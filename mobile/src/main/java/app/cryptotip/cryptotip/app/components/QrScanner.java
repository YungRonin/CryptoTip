package app.cryptotip.cryptotip.app.components;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gani.lib.ui.Ui;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import app.cryptotip.cryptotip.app.R;
import app.cryptotip.cryptotip.app.ReceiverAddressActivity;


public class QrScanner {
    private BarcodeDetector detector;
    private CameraSource cameraSource;
    final private SurfaceView qrView;
    private LinearLayout surfaceViewLayout;
    private ReceiverAddressActivity context;
    private LinearLayout layout;


    public QrScanner(ReceiverAddressActivity context, LinearLayout layout) {
        this.context = context;
        this.layout = layout;
        qrView = layout.findViewById(R.id.qr_view);
        surfaceViewLayout = layout.findViewById(R.id.surface_view_layout);

   }

    public void init(){
        surfaceViewLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, windowWidthPx()));

        detector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();

        cameraSource = TCameraSource.getInstance().getCameraSource(context, detector);

        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                //cameraSource.release();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    Ui.run(new Runnable() {
                        @Override
                        public void run() {
                            ((EditText)layout.findViewById(R.id.address_input_field)).setText(barcodes.valueAt(0).displayValue, TextView.BufferType.NORMAL);
                        }
                    });
                    cameraSource.stop();
                }
            }
        });

        qrView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                qrView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cameraFocus(cameraSource, Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    }
                });

                if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    try {
                        if(cameraSource != null) {
                            cameraSource.start(qrView.getHolder());
                        }
                    } catch (IOException ie) {
                        Log.e(this.getClass().getName(), ie.getMessage());
                    }
                }
                else{
                    ActivityCompat.requestPermissions((Activity) context,
                            new String[]{Manifest.permission.CAMERA},
                            1254);

                }
            }

            private boolean cameraFocus(@NonNull CameraSource cameraSource, @NonNull String focusMode) {
                java.lang.reflect.Field[] declaredFields = CameraSource.class.getDeclaredFields();

                for (java.lang.reflect.Field field : declaredFields) {
                    if (field.getType() == Camera.class) {
                        field.setAccessible(true);
                        try {
                            Camera camera = (Camera) field.get(cameraSource);
                            if (camera != null) {
                                Camera.Parameters params = camera.getParameters();
                                params.setFocusMode(focusMode);
                                camera.setParameters(params);
                                //todo hack for sending eth from emulator (click anywhere on camera surface view to call method)
                                //createTransactionAmoutDialog("insert receiver address here").show();
                                return true;
                            }

                            return false;
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        catch (NullPointerException e){
                            Log.e(getClass().getName(), "null pointer " + e);
                        }

                        break;
                    }
                }

                return false;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if(cameraSource != null){
                    cameraSource.release();
                }
                cameraSource = null;
            }
        });
    }

    private Point windowRawSize() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        return size;
//    Display display = wm.getDefaultDisplay();
//    return new Point(display.getWidth(), display.getHeight());
    }

    public int windowWidth() {
        return pxToDp(windowRawSize().x);
    }

    public int windowWidthPx(){
        return windowRawSize().x;
    }

    public int windowHeight() {
        return pxToDp(windowRawSize().y);
    }

    public int windowHeightPx(){
        return windowRawSize().y;
    }

    public int pxToDp(int px) {
        return Math.round(px / context.getResources().getDisplayMetrics().density);
    }
    public int dpToPx(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

}
