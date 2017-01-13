package com.wangy.mycode.Zxing;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.wangy.mycode.Base.BaseActivity;
import com.wangy.mycode.R;
import com.wangy.mycode.RGBLuminanceSource;
import com.wangy.mycode.ScanResultActivity;
import com.wangy.mycode.Zxing.camera.CameraManager;
import com.wangy.mycode.Zxing.decoding.CaptureActivityHandler;
import com.wangy.mycode.Zxing.decoding.InactivityTimer;
import com.wangy.mycode.Zxing.view.ViewfinderView;
import com.wangy.mycode.utils.IntentUtils;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by xhb on 2016/12/28.
 */
public class CaptureActivity extends BaseActivity implements SurfaceHolder.Callback, View.OnClickListener {
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private ImageView result_back;
    private Button btn_scan;
    private Button btn_phone;
    private static final int CHOOSE_PIC = 0;
    private TextView btn_shine;
    private boolean isopen;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
        //ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
        CameraManager.init(getApplication());
        initview();
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    private void initview() {
        result_back = (ImageView) findViewById(R.id.result_back);
        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_phone = (Button) findViewById(R.id.btn_phone);
        btn_shine = (TextView) findViewById(R.id.btn_shine);
        result_back.setOnClickListener(this);
        btn_scan.setOnClickListener(this);
        btn_phone.setOnClickListener(this);
        btn_shine.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * Handler scan result
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        //FIXME
        if (resultString.equals("")) {
            Toast.makeText(CaptureActivity.this, "扫描失败！", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(CaptureActivity.this, ScanResultActivity.class);
            intent.putExtra("result", resultString);
            IntentUtils.enterIntent(CaptureActivity.this, intent);
        }
//        CaptureActivity.this.finish();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan: {
                btn_scan.setBackgroundResource(R.drawable.blueroll);
                btn_phone.setBackgroundResource(R.drawable.tranroll);
                break;
            }
            case R.id.btn_phone: {
                btn_phone.setBackgroundResource(R.drawable.blueroll);
                btn_scan.setBackgroundResource(R.drawable.tranroll);
                //跳转到图片选择界面去选择一张二维码图片
                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_PICK);

                intent1.setType("image/*");

                Intent intent2 = Intent.createChooser(intent1, "选择二维码图片");
                startActivityForResult(intent2, CHOOSE_PIC);
                break;
            }
            case R.id.result_back: {
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                finish();
                break;
            }
            case R.id.btn_shine: {
                if (isopen) {
                    btn_shine.setTextColor(Color.WHITE);
                    btn_shine.setBackgroundResource(R.drawable.white);
                } else {
                    btn_shine.setTextColor(Color.parseColor("#038cf7"));
                    btn_shine.setBackgroundResource(R.drawable.blue);
                }
                isopen = !isopen;
                //控制开启和关闭闪光灯
                CameraManager.get().flashHandler();
            }
            default: {
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imgPath = null;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHOOSE_PIC:
                    try {
                        String[] proj = new String[]{MediaStore.Images.Media.DATA};
                        Cursor cursor = CaptureActivity.this.getContentResolver().query(data.getData(), proj, null, null, null);

                        if (cursor.moveToFirst()) {
                            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                            System.out.println(columnIndex);
                            //获取到用户选择的二维码图片的绝对路径
                            imgPath = cursor.getString(columnIndex);
                        }
                        cursor.close();
                        //获取解析结果
                        final String finalImgPath = imgPath;

                        Result ret = parseQRcodeBitmap(finalImgPath);
                        if (ret != null) {
                            Intent intent = new Intent(CaptureActivity.this, ScanResultActivity.class);
                            intent.putExtra("result", ret.toString());
                            IntentUtils.enterIntent(CaptureActivity.this, intent);
                        } else {
                            Toast.makeText(CaptureActivity.this, "扫描失败", Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(CaptureActivity.this, "扫描失败", Toast.LENGTH_LONG).show();
                    }

                    break;
            }
        }
    }

    //解析二维码图片,返回结果封装在Result对象中
    private com.google.zxing.Result parseQRcodeBitmap(String bitmapPath) {
        //解析转换类型UTF-8
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        //获取到待解析的图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        //如果我们把inJustDecodeBounds设为true，那么BitmapFactory.decodeFile(String path, Options opt)
        //并不会真的返回一个Bitmap给你，它仅仅会把它的宽，高取回来给你
        options.inJustDecodeBounds = true;
        //此时的bitmap是null，这段代码之后，options.outWidth 和 options.outHeight就是我们想要的宽和高了
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        //我们现在想取出来的图片的边长（二维码图片是正方形的）设置为400像素
        /**
         options.outHeight = 400;
         options.outWidth = 400;
         options.inJustDecodeBounds = false;
         bitmap = BitmapFactory.decodeFile(bitmapPath, options);
         */
        //以上这种做法，虽然把bitmap限定到了我们要的大小，但是并没有节约内存，如果要节约内存，我们还需要使用inSimpleSize这个属性
        options.inSampleSize = options.outHeight / 400;
        if (options.inSampleSize <= 0) {
            options.inSampleSize = 1; //防止其值小于或等于0
        }
        /**
         * 辅助节约内存设置
         *
         * options.inPreferredConfig = Bitmap.Config.ARGB_4444;    // 默认是Bitmap.Config.ARGB_8888
         * options.inPurgeable = true;
         * options.inInputShareable = true;
         */
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        //新建一个RGBLuminanceSource对象，将bitmap图片传给此对象
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(bitmap);
        //将图片转换成二进制图片
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(rgbLuminanceSource));
        //初始化解析对象
        QRCodeReader reader = new QRCodeReader();
        //开始解析
        Result result = null;
        try {
            result = reader.decode(binaryBitmap, hints);
        } catch (Exception e) {
            // TODO: handle exception
        }

        return result;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
