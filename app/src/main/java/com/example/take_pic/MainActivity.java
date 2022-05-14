package com.example.take_pic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};

    TextureView mPreviewView;
    Button captureImagebtn,open;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);

        mPreviewView = findViewById(R.id.textureView);
        captureImagebtn = findViewById(R.id.captureImg);
        open=findViewById(R.id.Open);
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(open.getText().equals("OPEN")){
                    open.setText("CLOSE");

                    captureImagebtn.setVisibility(View.VISIBLE);
                    mPreviewView.setVisibility(View.VISIBLE);
                    if(allPermissionsGranted()){
                        startCamera(); //start camera if permission has been granted by user
                    } else{
                        ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                    }
                }else {
                    CameraX.unbindAll();
                    open.setText("OPEN");
                    mPreviewView.setVisibility(View.GONE);
                    captureImagebtn.setVisibility(View.GONE);
                }


            }
        });

    }

    private void startCamera() {
        CameraX.unbindAll();
        Rational aspectratio=new Rational(mPreviewView.getWidth(),mPreviewView.getHeight());

        Size screen=new Size(mPreviewView.getWidth(),mPreviewView.getHeight());
        PreviewConfig pconfig=new PreviewConfig.Builder().setTargetAspectRatio(aspectratio).setTargetResolution(screen).build();
        Preview preview=new Preview(pconfig);

        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                ViewGroup parent= (ViewGroup) mPreviewView.getParent();
                parent.removeView(mPreviewView);
                parent.addView(mPreviewView);

                mPreviewView.setSurfaceTexture(output.getSurfaceTexture());

            }
        });


        ImageCaptureConfig imageCaptureConfig=new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY).setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).setTargetResolution(new Size(3555,3555)).build();
        final ImageCapture imgcap=new ImageCapture(imageCaptureConfig);
        captureImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //"storage/emulated/0/DCIM/Camera/"
                File file=new File("/storage/emulated/0/DCIM/Camera/"+System.currentTimeMillis()+".jpg");
                imgcap.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        Toast.makeText(getApplicationContext(), "successfull!!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                        if(cause!=null){
                            Toast.makeText(MainActivity.this, "erorr="+cause.toString(), Toast.LENGTH_LONG ).show();
                        }
                    }
                });
            }
        });
        CameraX.bindToLifecycle(this,preview,imgcap);
    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }


}