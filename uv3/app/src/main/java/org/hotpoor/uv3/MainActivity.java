package org.hotpoor.uv3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import static org.hotpoor.uv3.FloatVideo.isStarted;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import org.w3c.dom.Text;

//public class MainActivity extends BaseActivity implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback{
public class MainActivity extends BaseActivity{
    private Button FloatVideoBtn;


//    public View mTextureView;
//    private UVCCameraHelper mCameraHelper;
//    private CameraViewInterface mUVCCameraView;
//    private AlertDialog mDialog;
//    private boolean isRequest;
//    private boolean isPreview;
    LinearLayout scroll_log;



//    private UVCCameraHelper.OnMyDevConnectListener mDevConnectListener = new UVCCameraHelper.OnMyDevConnectListener() {
//
//        @Override
//        public void onAttachDev(UsbDevice device) {
//            // request open permission(must have)
//            print_log("onAttachDev:isRequest:");
//            print_log(""+isRequest);
//            if (!isRequest) {
//                isRequest = true;
//                if (mCameraHelper != null) {
//                    mCameraHelper.requestPermission(0);
//                }
//            }
//        }
//
//        @Override
//        public void onDettachDev(UsbDevice device) {
//            // close camera(must have)
//            print_log("onDettachDev:isRequest:");
//            print_log(""+isRequest);
//            if (isRequest) {
//                isRequest = false;
//                mCameraHelper.closeCamera();
//            }
//        }
//
//        @Override
//        public void onConnectDev(UsbDevice device, boolean isConnected) {
//            print_log("onConnectDev:isRequest:");
//            print_log(""+isRequest);
//            if(!isConnected){
//                isPreview = false;
//                print_log("fail to connect,please check resolution params");
//            }else{
//                isPreview = true;
//                print_log("connecting");
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(2500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        Looper.prepare();
//                        if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
//
//                        }
//                        Looper.loop();
//                    }
//                }).start();
//            }
//        }
//
//        @Override
//        public void onDisConnectDev(UsbDevice device) {
//            print_log("onDisConnectDev:isRequest:");
//            print_log(""+isRequest);
//            print_log("disconnecting");
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatVideoBtn = (Button)findViewById(R.id.FloatVideoBtn);
        FloatVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStarted) {
                    AlertDialog.Builder builder  = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("提示" ) ;
                    builder.setMessage("正在录制其他文件夹内视频，请结束浮窗录制后再切换文件夹。" ) ;
                    builder.setPositiveButton("我知道了" ,  null );
                    builder.show();
                    return;
                }else if(!isStarted) {
                    final Intent intent1 = new Intent(MainActivity.this, FloatVideo.class);
                    MainActivity.this.startService(intent1);
                    isStarted = true;
                }
            }
        });
        scroll_log = (LinearLayout)findViewById(R.id.scroll_log);
//        mTextureView = (UVCCameraTextureView)findViewById(R.id.camera1);
//        mUVCCameraView = (CameraViewInterface) mTextureView;
//        mUVCCameraView.setCallback(new CameraViewInterface.Callback (){
//            @Override
//            public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
//                // must have
//                print_log("new onSurfaceCreated");
//                if (!isPreview && mCameraHelper.isCameraOpened()) {
//                    mCameraHelper.startPreview(mUVCCameraView);
//                    isPreview = true;
//                }
//            }
//
//            @Override
//            public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
//                print_log("new onSurfaceChanged");
//            }
//
//            @Override
//            public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
//                // must have
//                print_log("new onSurfaceDestroy");
//                if (isPreview && mCameraHelper.isCameraOpened()) {
//                    mCameraHelper.stopPreview();
//                    isPreview = false;
//                }
//            }
//        });
//        mCameraHelper = UVCCameraHelper.getInstance();
//        mCameraHelper.setDefaultPreviewSize(640,360);
//        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG);
//        mCameraHelper.initUSBMonitor(this,mUVCCameraView,mDevConnectListener);

    }

    private void print_log(String string){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = new TextView(getApplicationContext());
                tv.setTextColor(Color.RED);
                tv.setText(string);
                scroll_log.addView(tv);
            }
        });
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        // step.2 register USB event broadcast
//        if (mCameraHelper != null) {
//            mCameraHelper.registerUSB();
//        }
//    }
//    @Override
//    protected void onStop() {
//        super.onStop();
//        // step.3 unregister USB event broadcast
//        if (mCameraHelper != null) {
//            mCameraHelper.unregisterUSB();
//        }
//    }

//    @Override
//    public USBMonitor getUSBMonitor() {
//        return null;
//    }
//
//    @Override
//    public void onDialogResult(boolean canceled) {
//
//    }
//
//    @Override
//    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
//
//    }
//
//    @Override
//    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {
//
//    }
//
//    @Override
//    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
//
//    }
}
