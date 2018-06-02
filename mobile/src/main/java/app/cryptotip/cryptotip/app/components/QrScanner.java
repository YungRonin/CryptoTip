package app.cryptotip.cryptotip.app.components;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import app.cryptotip.cryptotip.app.R;
import app.cryptotip.cryptotip.app.SendActivity;


public class QrScanner {
    private BarcodeDetector detector;
    private CameraSource cameraSource;
    final private SurfaceView qrView;
    private LinearLayout surfaceViewLayout;
    private TextView tView;
    private android.widget.Switch flashSwitch;
    private SendActivity context;
    private LinearLayout layout;
    private Double ethPrice;
    private String walletPath;

    public QrScanner(SendActivity context, String walletPath) {
        this.context = context;
        this.walletPath = walletPath;
        layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.qr_scanner_layout, null);
        qrView = layout.findViewById(R.id.qr_view);
        surfaceViewLayout = layout.findViewById(R.id.surface_view_layout);
        tView = layout.findViewById(R.id.qr_text_view);
        tView.setText("result shown here");
        flashSwitch = layout.findViewById(R.id.flash_switch);
    }

    public void init(){
        surfaceViewLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, windowWidthPx()));

        detector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();

        cameraSource = TCameraSource.getInstance().getCameraSource(context, detector);



        flashSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(flashSwitch.isChecked()){


                    java.lang.reflect.Field[] declaredFields = null;
                    try {
                        declaredFields = TCameraSource.class.getDeclaredFields();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    // TODO: refactor below
                    if(declaredFields != null) {
                        for (java.lang.reflect.Field field : declaredFields) {
                            if (field.getType() == Camera.class) {
                                field.setAccessible(true);
                                try {
                                    Camera camera = (Camera) field.get(cameraSource);
                                    if (camera != null) {
                                        Camera.Parameters params = camera.getParameters();
                                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                                        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                        camera.setParameters(params);
                                    }
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                }
                else{
                    java.lang.reflect.Field[] declaredFields = null;
                    try {
                        declaredFields = TCameraSource.class.getDeclaredFields();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    if(declaredFields != null) {
                        for (java.lang.reflect.Field field : declaredFields) {
                            if (field.getType() == Camera.class) {
                                field.setAccessible(true);
                                try {
                                    Camera camera = (Camera) field.get(cameraSource);
                                    if (camera != null) {
                                        Camera.Parameters params = camera.getParameters();
                                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                                        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                                        camera.setParameters(params);
                                    }
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                }
            }
        });

        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                //cameraSource.release();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    tView.post(new Runnable() {    // Use the post method of the TextView
                        public void run() {
                            tView.setText(    // Update the TextView
                                    barcodes.valueAt(0).displayValue
                            );

                            createTransactionAmoutDialog(barcodes.valueAt(0).displayValue).show();
                            cameraSource.stop();
                         }
                    });
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

    private AlertDialog createTransactionAmoutDialog(final String address){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Trasaction Amount");
        final EditText inputField = new EditText(context);
        inputField.setHint("enter tip amount");
        inputField.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(inputField);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new SendActivity.AsyncSendTask(context).execute(address, inputField.getText().toString(), walletPath);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    public View getView(){
        return layout;
    }

    public void setEthPrice(Double price){
        ethPrice = price;
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
