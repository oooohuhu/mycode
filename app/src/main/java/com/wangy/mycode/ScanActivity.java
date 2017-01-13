package com.wangy.mycode;


import android.content.Intent;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.wangy.mycode.Base.BaseActivity;
import com.wangy.mycode.Zxing.CaptureActivity;

import java.util.Hashtable;


/**
 * Created by xhb on 2016/12/27.
 */
public class ScanActivity extends BaseActivity implements View.OnClickListener{
    private ImageView result_back;
    private ImageView result_back1;
    private Button bt_codephone;
    private Button bt_code;
    private static final int CHOOSE_PIC = 0;
    private static final int SHEXIANGTOU = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanlayout);
        initView();
    }

    private void initView() {
        result_back1 = (ImageView)findViewById(R.id.result_back);
        bt_codephone = (Button) findViewById(R.id.bt_codephone);
        bt_code = (Button)findViewById(R.id.bt_code);
        bt_code.setOnClickListener(this);
        bt_codephone.setOnClickListener(this);
    }

    //解析二维码图片,返回结果封装在Result对象中
    private com.google.zxing.Result  parseQRcodeBitmap(String bitmapPath){
        //解析转换类型UTF-8
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        //获取到待解析的图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        //如果我们把inJustDecodeBounds设为true，那么BitmapFactory.decodeFile(String path, Options opt)
        //并不会真的返回一个Bitmap给你，它仅仅会把它的宽，高取回来给你
        options.inJustDecodeBounds = true;
        //此时的bitmap是null，这段代码之后，options.outWidth 和 options.outHeight就是我们想要的宽和高了
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath,options);
        //我们现在想取出来的图片的边长（二维码图片是正方形的）设置为400像素
        /**
         options.outHeight = 400;
         options.outWidth = 400;
         options.inJustDecodeBounds = false;
         bitmap = BitmapFactory.decodeFile(bitmapPath, options);
         */
        //以上这种做法，虽然把bitmap限定到了我们要的大小，但是并没有节约内存，如果要节约内存，我们还需要使用inSimpleSize这个属性
        options.inSampleSize = options.outHeight / 400;
        if(options.inSampleSize <= 0){
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
        if (keyCode==KeyEvent.KEYCODE_BACK){
            startActivity(new Intent(ScanActivity.this,MainActivity.class));
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            finish();
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imgPath=null;
        if(resultCode == RESULT_OK){
            switch (requestCode) {
                case CHOOSE_PIC:
                    try {
                        String[] proj = new String[]{MediaStore.Images.Media.DATA};
                        Cursor cursor = ScanActivity.this.getContentResolver().query(data.getData(), proj, null, null, null);

                        if(cursor.moveToFirst()){
                            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                            System.out.println(columnIndex);
                            //获取到用户选择的二维码图片的绝对路径
                            imgPath = cursor.getString(columnIndex);
                        }
                        cursor.close();
                        //获取解析结果
                        Result ret = parseQRcodeBitmap(imgPath);
                        Toast.makeText(ScanActivity.this, "解析结果：" + ret.toString(), Toast.LENGTH_LONG).show();
                    }catch (Exception e){
                        Toast.makeText(ScanActivity.this, "解析结果：" + "扫描失败", Toast.LENGTH_LONG).show();
                    }

                    break;
                case SHEXIANGTOU:
                    try {
                        String result = data.getExtras().getString("result");
                        Toast.makeText(ScanActivity.this,"解析结果：" + result, Toast.LENGTH_LONG).show();
                    }catch (Exception e){
                        Toast.makeText(ScanActivity.this, "解析结果：" + "扫描失败", Toast.LENGTH_LONG).show();
                    }

                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //摄像头扫描
            case R.id.bt_code:{
                //打开扫描界面扫描条形码或二维码
                Intent openCameraIntent = new Intent(ScanActivity.this,CaptureActivity.class);
                startActivityForResult(openCameraIntent, SHEXIANGTOU);
                break;
            }
            //上传图片扫描
            case R.id.bt_codephone:{
                //跳转到图片选择界面去选择一张二维码图片
                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_PICK);

                intent1.setType("image/*");

                Intent intent2 =  Intent.createChooser(intent1, "选择二维码图片");
                startActivityForResult(intent2, CHOOSE_PIC);
                break;
            }
        }
    }
}
