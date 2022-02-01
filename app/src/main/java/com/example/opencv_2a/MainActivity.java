package com.example.opencv_2a;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    // will point to our View widget for our image
    private CameraBridgeViewBase mOpenCvCameraView;
    private static final String TAG = "OCVSample::Activity";
    private TextView thresholdDisplay;
    private boolean captureFlag = false;
    private int threshold = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        //Load in the OpenCV dependency module code from the jni files you linked in this project
        // inside the OpenCV module
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(MainActivity.this, "Unable to load OpenCV", Toast.LENGTH_LONG).show();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        // Permissions for Android 6+
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA},
                1);

        // rest of the code...
        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCameraPermissionGranted();
        cameraBridgeViewBase.setCvCameraViewListener(this);

        // grab a "handle" to the OpenCV class responsible for viewing Image
        // look at the XML the id of our CameraBridgeViewBase is HelloOpenCVView
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        //set it visible, register the listener and enable the view so connected to camera
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this); // the activity will listen to events on Camera -call onCameraFrame
        mOpenCvCameraView.enableView();

        /* Create all relevant buttons and set listeners. */
        Button capture = findViewById(R.id.capture);
        capture.setOnClickListener(this);

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        thresholdDisplay = (TextView) findViewById(R.id.threshold);
        thresholdDisplay.setText("Threshold: " + threshold);

    }




    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    private CameraBridgeViewBase cameraBridgeViewBase;



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraBridgeViewBase.setCameraPermissionGranted();  // <------ THIS!!!
                } else {
                    // permission denied
                }
                return;
            }
        }
    }

    // disable JavaCameraView if app going on pause
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    //enable the JavaCameraView if app resuming
    @Override
    public void onResume() {
        super.onResume();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.enableView();
    }


    //Disable view of JavaCameraView if app is being destoryed
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        threshold = progress;
        thresholdDisplay.setText("Threshold: " + threshold);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(final View v) {

            switch (v.getId()) {

                case R.id.capture:
                    captureFlag = !captureFlag;
                    //captureFlag = (!captureFlag) ? true : false;
                    if (captureFlag){
                        Toast.makeText(getApplicationContext(), "Canny Edge Detection Enabled", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Canny Edge Detection Disabled", Toast.LENGTH_SHORT).show();
                    }

                    break;


            } // end switch

        } // end onClick function


    //method invoked when camera view is started
    public void onCameraViewStarted(int width, int height) {

    }


    //method invoked when camera view is stoped
    public void onCameraViewStopped() {

    }

    // THIS IS THE main method that is called each time you get a new Frame/Image
    // Implement to be a CVCameraViewListener2
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        //store in the imageMat (instance of OpenCV's Mat class, a 2D matrix) the RGB(A=alpha) image
        Mat imageMat = inputFrame.rgba();


        if (captureFlag) {
            Mat frame = inputFrame.gray();
            Mat detectedEdges = new Mat();

            // reduce noise with a 3x3 kernel
            Imgproc.blur(frame, detectedEdges, new Size(3, 3));

            // canny detector, with ratio of lower:upper threshold of 3:1
            Imgproc.Canny(detectedEdges, detectedEdges, threshold, threshold * 3, 3, false);
            // using Canny's output as a mask, display the result
            Mat res = new Mat();
            frame.copyTo(res, detectedEdges);

            return res;

        }


        //Return the Mat you want to be displayed in the JavaCameraView widget which invoked this method
        return imageMat;
    }

}