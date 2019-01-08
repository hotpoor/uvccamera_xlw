package org.hotpoor.uv3;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Environment;

import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;

import java.io.File;
import java.util.List;

public class UVCCameraHelper {
    public static final String ROOT_PATH;
    public static final String SUFFIX_JPEG = ".jpg";
    public static final String SUFFIX_MP4 = ".mp4";
    private static final String TAG = "UVCCameraHelper";
    private int previewWidth = 640;
    private int previewHeight = 480;
    public static final int FRAME_FORMAT_YUYV = 0;
    public static final int FRAME_FORMAT_MJPEG = 1;
    public static final int MODE_BRIGHTNESS = -2147483647;
    public static final int MODE_CONTRAST = -2147483646;
    private int mFrameFormat = 1;
    private static UVCCameraHelper mCameraHelper;
    private USBMonitor mUSBMonitor;
    private UVCCameraHandler mCameraHandler;
    private USBMonitor.UsbControlBlock mCtrlBlock;
    private Activity mActivity;
    private CameraViewInterface mCamView;

    private UVCCameraHelper() {
    }

    public static UVCCameraHelper getInstance() {
        if (mCameraHelper == null) {
            mCameraHelper = new UVCCameraHelper();
        }

        return mCameraHelper;
    }

    public void closeCamera() {
        if (this.mCameraHandler != null) {
            this.mCameraHandler.close();
        }

    }

    public void initUSBMonitor(Activity activity, CameraViewInterface cameraView, final OnMyDevConnectListener listener) {
        this.mActivity = activity;
        this.mCamView = cameraView;
        this.mUSBMonitor = new USBMonitor(activity.getApplicationContext(), new USBMonitor.OnDeviceConnectListener() {
            public void onAttach(UsbDevice device) {
                if (listener != null) {
                    listener.onAttachDev(device);
                }

            }

            public void onDettach(UsbDevice device) {
                if (listener != null) {
                    listener.onDettachDev(device);
                }

            }

            public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                UVCCameraHelper.this.mCtrlBlock = ctrlBlock;
                UVCCameraHelper.this.openCamera(ctrlBlock);
                (new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException var2) {
                            var2.printStackTrace();
                        }

                        UVCCameraHelper.this.startPreview(UVCCameraHelper.this.mCamView);
                    }
                })).start();
                if (listener != null) {
                    listener.onConnectDev(device, true);
                }

            }

            public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                if (listener != null) {
                    listener.onDisConnectDev(device);
                }

            }

            public void onCancel(UsbDevice device) {
            }
        });
        this.createUVCCamera();
    }

    public void createUVCCamera() {
        if (this.mCamView == null) {
            throw new NullPointerException("CameraViewInterface cannot be null!");
        } else {
            if (this.mCameraHandler != null) {
                this.mCameraHandler.release();
                this.mCameraHandler = null;
            }

            this.mCamView.setAspectRatio((double)((float)this.previewWidth / (float)this.previewHeight));
            this.mCameraHandler = UVCCameraHandler.createHandler(this.mActivity, this.mCamView, 2, this.previewWidth, this.previewHeight, this.mFrameFormat);
        }
    }

    public void updateResolution(int width, int height) {
        if (this.previewWidth != width || this.previewHeight != height) {
            this.previewWidth = width;
            this.previewHeight = height;
            if (this.mCameraHandler != null) {
                this.mCameraHandler.release();
                this.mCameraHandler = null;
            }

            this.mCamView.setAspectRatio((double)((float)this.previewWidth / (float)this.previewHeight));
            this.mCameraHandler = UVCCameraHandler.createHandler(this.mActivity, this.mCamView, 2, this.previewWidth, this.previewHeight, this.mFrameFormat);
            this.openCamera(this.mCtrlBlock);
            (new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException var2) {
                        var2.printStackTrace();
                    }

                    UVCCameraHelper.this.startPreview(UVCCameraHelper.this.mCamView);
                }
            })).start();
        }
    }

    public void registerUSB() {
        if (this.mUSBMonitor != null) {
            this.mUSBMonitor.register();
        }

    }

    public void unregisterUSB() {
        if (this.mUSBMonitor != null) {
            this.mUSBMonitor.unregister();
        }

    }

    public boolean checkSupportFlag(int flag) {
        return this.mCameraHandler != null && this.mCameraHandler.checkSupportFlag((long)flag);
    }

    public int getModelValue(int flag) {
        return this.mCameraHandler != null ? this.mCameraHandler.getValue(flag) : 0;
    }

    public int setModelValue(int flag, int value) {
        return this.mCameraHandler != null ? this.mCameraHandler.setValue(flag, value) : 0;
    }

    public int resetModelValue(int flag) {
        return this.mCameraHandler != null ? this.mCameraHandler.resetValue(flag) : 0;
    }

    public void requestPermission(int index) {
        List<UsbDevice> devList = this.getUsbDeviceList();
        if (devList != null && devList.size() != 0) {
            int count = devList.size();
            if (index >= count) {
                new IllegalArgumentException("index illegal,should be < devList.size()");
            }

            if (this.mUSBMonitor != null) {
                this.mUSBMonitor.requestPermission((UsbDevice)this.getUsbDeviceList().get(index));
            }

        }
    }

    public int getUsbDeviceCount() {
        List<UsbDevice> devList = this.getUsbDeviceList();
        return devList != null && devList.size() != 0 ? devList.size() : 0;
    }

    public List<UsbDevice> getUsbDeviceList() {
        List<DeviceFilter> deviceFilters = DeviceFilter.getDeviceFilters(this.mActivity.getApplicationContext(), R.xml.device_filter);
        return this.mUSBMonitor != null && deviceFilters != null ? this.mUSBMonitor.getDeviceList((DeviceFilter)deviceFilters.get(0)) : null;
    }

//    public void capturePicture(String savePath, OnCaptureListener listener) {
//        if (this.mCameraHandler != null && this.mCameraHandler.isOpened()) {
//            this.mCameraHandler.captureStill(savePath, listener);
//        }
//
//    }

//    public void startPusher(OnEncodeResultListener listener) {
//        if (this.mCameraHandler != null && !this.isPushing()) {
//            this.mCameraHandler.startRecording((RecordParams)null, listener);
//        }
//
//    }

//    public void startPusher(RecordParams params, OnEncodeResultListener listener) {
//        if (this.mCameraHandler != null && !this.isPushing()) {
//            this.mCameraHandler.startRecording(params, listener);
//        }
//
//    }

    public void stopPusher() {
        if (this.mCameraHandler != null && this.isPushing()) {
            this.mCameraHandler.stopRecording();
        }

    }

    public boolean isPushing() {
        return this.mCameraHandler != null ? this.mCameraHandler.isRecording() : false;
    }

    public boolean isCameraOpened() {
        return this.mCameraHandler != null ? this.mCameraHandler.isOpened() : false;
    }

    public void release() {
        if (this.mCameraHandler != null) {
            this.mCameraHandler.release();
            this.mCameraHandler = null;
        }

        if (this.mUSBMonitor != null) {
            this.mUSBMonitor.destroy();
            this.mUSBMonitor = null;
        }

    }

    public USBMonitor getUSBMonitor() {
        return this.mUSBMonitor;
    }

//    public void setOnPreviewFrameListener(OnPreViewResultListener listener) {
//        if (this.mCameraHandler != null) {
//            this.mCameraHandler.setOnPreViewResultListener(listener);
//        }
//
//    }

    private void openCamera(USBMonitor.UsbControlBlock ctrlBlock) {
        if (this.mCameraHandler != null) {
            this.mCameraHandler.open(ctrlBlock);
        }

    }

    public void startPreview(CameraViewInterface cameraView) {
        SurfaceTexture st = cameraView.getSurfaceTexture();
        if (this.mCameraHandler != null) {
            this.mCameraHandler.startPreview(st);
        }

    }

    public void stopPreview() {
        if (this.mCameraHandler != null) {
            this.mCameraHandler.stopPreview();
        }

    }

//    public void startCameraFoucs() {
//        if (this.mCameraHandler != null) {
//            this.mCameraHandler.startCameraFoucs();
//        }
//
//    }

//    public List<Size> getSupportedPreviewSizes() {
//        return this.mCameraHandler == null ? null : this.mCameraHandler.getSupportedPreviewSizes();
//    }

    public void setDefaultPreviewSize(int defaultWidth, int defaultHeight) {
        if (this.mUSBMonitor != null) {
            throw new IllegalStateException("setDefaultPreviewSize should be call before initMonitor");
        } else {
            this.previewWidth = defaultWidth;
            this.previewHeight = defaultHeight;
        }
    }

    public void setDefaultFrameFormat(int format) {
        if (this.mUSBMonitor != null) {
            throw new IllegalStateException("setDefaultFrameFormat should be call before initMonitor");
        } else {
            this.mFrameFormat = format;
        }
    }

    public int getPreviewWidth() {
        return this.previewWidth;
    }

    public int getPreviewHeight() {
        return this.previewHeight;
    }

    static {
        ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    public interface OnMyDevConnectListener {
        void onAttachDev(UsbDevice var1);

        void onDettachDev(UsbDevice var1);

        void onConnectDev(UsbDevice var1, boolean var2);

        void onDisConnectDev(UsbDevice var1);
    }
}
