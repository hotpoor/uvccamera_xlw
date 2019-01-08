package org.hotpoor.uv3;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.serenegiant.common.BaseService;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.IStatusCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import org.hotpoor.widget.SimpleUVCCameraTextureView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class FloatVideo extends BaseService implements CameraDialog.CameraDialogParent{
    public static boolean isStarted = false;
    public static boolean hasDisplayView = false;
    public static boolean isRecording = false;
    public static String Video_SERIALNO = null;
    public static String path = "";
    public static boolean isScreenPhoto = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;



    private Button start_record;
    private Button stop_record;
    private Button restart_record;
    private Button button_hide;
    private Button screen_photo;

    private View displayView;
    private int dm_heigth;
    private int dm_width;
    private ImageView video_logo;
    private ImageButton video_logo_btn;
    private TextView video_serialno;
    private Boolean video_area_show = false;
    private int last_x;
    private int last_y;

    private FrameLayout video_frame;

    private Boolean touch_move_lock = false;

    private int frame_padding = 60;






    public View mTextureView;

    private final Object mSync = new Object();
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private SurfaceView mUVCCameraView;

    private Button mCameraButton;

    private DeviceListAdapter mDeviceListAdapter;
    private boolean isActive, isPreview;
    private Surface mPreviewSurface;

    LinearLayout scroll_log;
    Button camera_usb_info;


    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        dm_heigth = dm.heightPixels;
        dm_width = dm.widthPixels;
        System.out.println("width:"+dm_width+";height:"+dm_heigth+";");

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
//        layoutParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
//        | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        layoutParams.width = dm_width/2;
//        layoutParams.height = dm_heigth/2;
        layoutParams.width = 180;
        layoutParams.height = 180;
        layoutParams.x = dm_width - 180;
        layoutParams.y = 300;
        last_x = layoutParams.x;
        last_y = layoutParams.y;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showFloatingWindow() {
        hasDisplayView = true;
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        displayView = layoutInflater.inflate(R.layout.float_video, null);
        displayView.setFocusableInTouchMode(true);
        displayView.setOnTouchListener(new FloatingOnTouchListener());
        displayView.setOnClickListener(new FloatingOnClickListener());

        video_frame = displayView.findViewById(R.id.video_frame);
        video_serialno = displayView.findViewById(R.id.video_serialno);
        video_serialno.setText("ID:"+Video_SERIALNO);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dm_width - frame_padding, dm_heigth - frame_padding - 180);
        lp.gravity = Gravity.LEFT|Gravity.TOP;
        lp.leftMargin = frame_padding/2; //矩形距离原点最近的点距离X轴的距离
        lp.topMargin = frame_padding/2; //矩形距离原点最近的点距离Y轴的距离
        //以上两个值，即坐标(x,y);
        video_frame.setLayoutParams(lp);

        button_hide = displayView.findViewById(R.id.btn_hide);
        button_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopFloatingButtonService();
            }
        });
        video_logo = displayView.findViewById(R.id.video_logo);
        video_logo_btn = displayView.findViewById(R.id.video_logo_btn);
        video_logo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide_view_area();
            }
        });

        start_record = displayView.findViewById(R.id.start_record);
        start_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                start_record();
                otg_start_record();
            }
        });
        stop_record = displayView.findViewById(R.id.stop_record);
        stop_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                stop_record();
                otg_stop_record();
            }
        });
        restart_record = displayView.findViewById(R.id.restart_record);
        restart_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                restart_record();
            }
        });

        screen_photo = displayView.findViewById(R.id.screen_photo);
        screen_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                screen_photo();
            }
        });


        scroll_log = displayView.findViewById(R.id.scroll_log);

        mUVCCameraView = (SurfaceView)displayView.findViewById(R.id.camera_surface_view);
        mUVCCameraView.getHolder().addCallback(mSurfaceViewCallback);
//        mUVCCameraView = (SimpleUVCCameraTextureView)displayView.findViewById(R.id.UVCCameraTextureView1);
//        mUVCCameraView.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float)UVCCamera.DEFAULT_PREVIEW_HEIGHT);

        mCameraButton = (Button)displayView.findViewById(R.id.camera_button);
        mCameraButton.setOnClickListener(mOnClickListener);

//        synchronized (mSync) {
//            if (mUSBMonitor != null) {
//                mUSBMonitor.register();
//            }
//        }
        mUSBMonitor = new USBMonitor(displayView.getContext(), mOnDeviceConnectListener);
        camera_usb_info=displayView.findViewById(R.id.camera_usb_info);
        camera_usb_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDevices();
            }
        });
//        mUSBMonitor.register();
        windowManager.addView(displayView, layoutParams);

    }

    private void otg_start_record(){

    }
    private void otg_stop_record(){

    }


    private void stopFloatingButtonService(){
        hide_view_area();
        windowManager.removeView(displayView);
        isStarted = false;
        hasDisplayView = false;
        video_area_show = false;
        touch_move_lock = false;


//        synchronized (mSync) {
            isActive = isPreview = false;
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
                mUVCCamera = null;
            }
            if (mUSBMonitor != null) {
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
//        }
        mUVCCameraView = null;
        mCameraButton = null;

        super.onDestroy();
    }
    private Toast mToast;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (mUVCCamera == null) {
                // XXX calling CameraDialog.showDialog is necessary at only first time(only when app has no permission).
//                updateDevices();
                print_log("mOnClickListener updateDevices");
                mUSBMonitor.register();
                final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(getApplicationContext(), R.xml.device_filter);
                mDeviceListAdapter = new DeviceListAdapter(displayView.getContext(), mUSBMonitor.getDeviceList(filter.get(0)));
                System.out.println("===");
                System.out.println(mDeviceListAdapter.mList.size());
                System.out.println("===");
                if(mDeviceListAdapter.mList.size()>0) {
                    mUSBMonitor.requestPermission(mUSBMonitor.getDeviceList((filter.get(0))).get(0));
                    if (mUVCCamera != null) {
                        mUVCCamera.startPreview();//开启预览
                    }
                }
            } else {
                print_log("mOnClickListener destroy");
//                synchronized (mSync) {
                    mUVCCamera.destroy();
                    mUVCCamera = null;
                    isActive = isPreview = false;
//                }
            }
        }
    };


    private void hide_view_area(){
        if(video_area_show){
            video_frame.setVisibility(View.GONE);
            video_logo_btn.setVisibility(View.GONE);
            video_logo.setVisibility(View.VISIBLE);

            if(isScreenPhoto){
                layoutParams.width = dm_width/2;
                layoutParams.height = dm_heigth/2;
            }else {
                layoutParams.width = 180;
                layoutParams.height = 180;
            }
            layoutParams.x = last_x;
            layoutParams.y = last_y;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            windowManager.updateViewLayout(displayView, layoutParams);
            video_area_show = false;
            System.out.println("video_area_show:"+video_area_show);
        }
    }

    private class FloatingOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            if(!video_area_show&&!touch_move_lock) {
                video_frame.setVisibility(View.VISIBLE);
                video_logo_btn.setVisibility(View.VISIBLE);
                video_logo.setVisibility(View.GONE);
                layoutParams.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
                layoutParams.width = dm_width;
                layoutParams.height = dm_heigth - 180;
                layoutParams.x = 0;
                layoutParams.y = 0;

                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dm_width - frame_padding, dm_heigth - frame_padding - 180);
                lp.gravity = Gravity.LEFT|Gravity.TOP;
                lp.leftMargin = frame_padding/2; //矩形距离原点最近的点距离X轴的距离
                lp.topMargin = frame_padding/2; //矩形距离原点最近的点距离Y轴的距离
                //以上两个值，即坐标(x,y);
                video_frame.setLayoutParams(lp);

                displayView.setFocusableInTouchMode(true);
                windowManager.updateViewLayout(view, layoutParams);
                video_area_show = true;
                System.out.println("click video_area_show:"+video_area_show);
            }
        }
    }
    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (!video_area_show) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touch_move_lock = false;
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                        System.out.println("ACTION_DOWN video_area_show:"+video_area_show);
                        break;
                    case MotionEvent.ACTION_MOVE:
//                        touch_move_lock = true;
                        int nowX = (int) event.getRawX();
                        int nowY = (int) event.getRawY();
                        int movedX = nowX - x;
                        int movedY = nowY - y;
                        x = nowX;
                        y = nowY;
                        layoutParams.x = layoutParams.x + movedX;
                        layoutParams.y = layoutParams.y + movedY;
                        last_x = layoutParams.x;
                        last_y = layoutParams.y;
                        windowManager.updateViewLayout(view, layoutParams);
                        System.out.println("ACTION_MOVE video_area_show:"+video_area_show);
                        break;
                    case MotionEvent.ACTION_UP:
                        //采用直接的磁吸，如果需要动画效果，需要额外加一个frame层级来做
//                        if (touch_move_lock){
                        if (last_x<=((dm_width-180)/2)){
                            layoutParams.x = 0;
                        }else{
                            layoutParams.x = dm_width-180;
                        }
                        last_x = layoutParams.x;
                        windowManager.updateViewLayout(view, layoutParams);
//                        }
                        System.out.println("ACTION_UP video_area_show:"+video_area_show);
                        break;
                    default:
                        break;
                }
                return false;

            }else{
                return false;
            }
        }
    }

    private void print_log(String string){
//        TextView tv = new TextView(displayView.getContext());
//        tv.setTextColor(Color.RED);
//        tv.setText(string);
//        scroll_log.addView(tv);
        System.out.println(string);
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
//    protected void onStop () {
//        super.onStop();
//        // step.3 unregister USB event broadcast
//        if (mCameraHelper != null) {
//            mCameraHelper.unregisterUSB();
//        }
//    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            Toast.makeText(getApplicationContext(), "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
            print_log("USB_DEVICE_ATTACHED");
//            updateDevices();
//            if(mUSBMonitor!=null) {
//                mUSBMonitor.requestPermission((UsbDevice)this.getUsbDeviceList().get(index));
//            }
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            print_log("onConnect ");
//            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.destroy();
                }
                isActive = isPreview = false;
//            }
//            queueEvent(new Runnable() {
//                @Override
//                public void run() {
//                    synchronized (mSync) {
                        final UVCCamera camera = new UVCCamera();
                        camera.open(ctrlBlock);
                        print_log("supportedSize: "+camera.getSupportedSize());
                        try {
//                            camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG);
                            camera.setPreviewSize(1280, 720, UVCCamera.FRAME_FORMAT_MJPEG);
                        } catch (final IllegalArgumentException e) {
                            try {
                                // fallback to YUV mode
//                                camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                                camera.setPreviewSize(1280, 720, UVCCamera.DEFAULT_PREVIEW_MODE);
                            } catch (final IllegalArgumentException e1) {
                                camera.destroy();
                                return;
                            }
                        }
                        mPreviewSurface = mUVCCameraView.getHolder().getSurface();
                        if (mPreviewSurface != null) {
                            isActive = true;
                            camera.setPreviewDisplay(mPreviewSurface);
                            camera.startPreview();
                            isPreview = true;
                        }
//                        synchronized (mSync) {
                            mUVCCamera = camera;
//                        }
//                    }
//                }
//            }, 0);
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            // XXX you should check whether the coming device equal to camera device that currently using
//            releaseCamera();
//            if (DEBUG) Log.v(TAG, "onDisconnect: 数据断开");
            print_log("onDisconnect: 数据断开");
            // XXX you should check whether the comming device equal to camera device that currently using
//            queueEvent(new Runnable() {
//                @Override
//                public void run() {
//                    synchronized (mSync) {
                        if (mUVCCamera != null) {
                            mUVCCamera.close();
                            if (mPreviewSurface != null) {
                                mPreviewSurface.release();
                                mPreviewSurface = null;
                            }
                            isActive = isPreview = false;
                        }
//                    }
//                }
//            }, 0);
        }

        @Override
        public void onDettach(final UsbDevice device) {
            Toast.makeText(getApplicationContext(), "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
            print_log("USB_DEVICE_DETACHED");
        }

        @Override
        public void onCancel(final UsbDevice device) {
        }
    };

//    private synchronized void releaseCamera() {
////        synchronized (mSync) {
//            if (mUVCCamera != null) {
//                try {
//                    mUVCCamera.setStatusCallback(null);
//                    mUVCCamera.setButtonCallback(null);
//                    mUVCCamera.close();
//                    mUVCCamera.destroy();
//                } catch (final Exception e) {
//                    //
//                }
//                mUVCCamera = null;
//            }
//            if (mPreviewSurface != null) {
//                mPreviewSurface.release();
//                mPreviewSurface = null;
//            }
////        }
//    }


    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {

    }

    private static final class DeviceListAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private final List<UsbDevice> mList;

        public DeviceListAdapter(final Context context, final List<UsbDevice>list) {
            mInflater = LayoutInflater.from(context);
            mList = list != null ? list : new ArrayList<UsbDevice>();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public UsbDevice getItem(final int position) {
            if ((position >= 0) && (position < mList.size()))
                return mList.get(position);
            else
                return null;
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(com.serenegiant.uvccamera.R.layout.listitem_device, parent, false);
            }
            if (convertView instanceof CheckedTextView) {
                final UsbDevice device = getItem(position);
                ((CheckedTextView)convertView).setText(
                        String.format("UVC Camera:(%x:%x:%s)", device.getVendorId(), device.getProductId(), device.getDeviceName()));
            }
            return convertView;
        }
    }

    public void updateDevices() {
//		mUSBMonitor.dumpDevices();
        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(displayView.getContext(), com.serenegiant.uvccamera.R.xml.device_filter);
        mDeviceListAdapter = new DeviceListAdapter(displayView.getContext(), mUSBMonitor.getDeviceList(filter.get(0)));
        print_log(mDeviceListAdapter.toString());
        print_log("设备列表===");
        print_log(mDeviceListAdapter.mList.toString());
        print_log("设备具体列表===");
        if( mDeviceListAdapter.mList.size()>0){
            print_log(mDeviceListAdapter.getItem(0).toString());
            if(mUSBMonitor!=null){
                print_log("null and requestPermission");
                mUSBMonitor.register();
                mUSBMonitor.requestPermission(mUSBMonitor.getDeviceList((filter.get(0))).get(0));
//                synchronized (mSync) {
                    if (mUVCCamera != null) {
                        mUVCCamera.startPreview();
                    }
//                }
            }else{
                print_log("mUSBMonitor register");
                mUSBMonitor.register();
//                synchronized (mSync) {
                    if (mUVCCamera != null) {
                        mUVCCamera.startPreview();
                    }
//                }
            }
        }
    }

    private final SurfaceHolder.Callback mSurfaceViewCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(final SurfaceHolder holder) {
            print_log("surfaceCreated 开始渲染");
        }

        @Override
        public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
            if ((width == 0) || (height == 0)) return;
            print_log("surfaceChanged 渲染更新");
            mPreviewSurface = holder.getSurface();
//            synchronized (mSync) {
                if (isActive && !isPreview && (mUVCCamera != null)) {
                    mUVCCamera.setPreviewDisplay(mPreviewSurface);
                    mUVCCamera.startPreview();
                    isPreview = true;
                }
//            }
        }

        @Override
        public void surfaceDestroyed(final SurfaceHolder holder) {
            print_log("surfaceChanged 销毁渲染");
//            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.stopPreview();
                }
                isPreview = false;
//            }
            mPreviewSurface = null;
        }
    };



}
