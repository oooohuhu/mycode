package com.wangy.mycode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.wangy.mycode.Base.BaseActivity;
import com.wangy.mycode.Zxing.CaptureActivity;
import com.wangy.mycode.utils.IntentUtils;
import com.wangy.mycode.utils.StrUtils;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ImageView scan;
    private Button codetype_word;
    private Button codetype_http;
    private EditText editcode;
    private Button bt_code;
    private int QR_WIDTH = 400, QR_HEIGHT = 400;
    private String editring;
    private int isword;
    private int ishttp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        scan = (ImageView) findViewById(R.id.scan);
        codetype_word = (Button) findViewById(R.id.codetype_word);
        codetype_word.setBackgroundResource(R.drawable.select_blue_bg);
        codetype_http = (Button) findViewById(R.id.codetype_http);
        codetype_http.setBackgroundResource(R.drawable.edit_shap_grey);
        editcode = (EditText) findViewById(R.id.editcode);
        bt_code = (Button) findViewById(R.id.bt_code);
        scan.setOnClickListener(this);
        codetype_word.setOnClickListener(this);
        codetype_http.setOnClickListener(this);
        bt_code.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan:
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                IntentUtils.enterIntent(MainActivity.this,intent);
                break;
            case R.id.codetype_word:
                editcode.setHint("请输入内容");
                codetype_http.setBackgroundResource(R.drawable.edit_shap_grey);
                codetype_word.setBackgroundResource(R.drawable.select_blue_bg);

                break;
            case R.id.codetype_http:
                editcode.setHint("请输入网址(http://)");
                codetype_word.setBackgroundResource(R.drawable.edit_shap_grey);
                codetype_http.setBackgroundResource(R.drawable.select_blue_bg);
                break;
            case R.id.bt_code:
                editring = editcode.getText().toString().trim();
                if (StrUtils.isEmpty(editring)) {
                    showCustomToast("请输入内容才可生成二维码?");
                    return;
                }
                Bitmap bitmap = createQRImage(editring);
//                将bitmap转成字节数组
                byte[] bitmapByte = bitmaptobytearray(bitmap);
                Intent intent1 = new Intent(MainActivity.this, CodeResultActivity.class);
                intent1.putExtra("bitmap", bitmapByte);
                IntentUtils.enterIntent(MainActivity.this,intent1);
                break;
            default:
                break;
        }
    }

    //要转换的地址或字符串,可以是中文，输入内容生成二维码
    public Bitmap createQRImage(String string) {
        try {
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(string, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    } else {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }


        return null;
    }

    //弹出警告框
    public void showCustomToast(String text) {
        Toast toast = Toast.makeText(MainActivity.this,
                text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 20, 20);
        LinearLayout toastView = (LinearLayout) toast.getView();
        ImageView imageCodeProject = new ImageView(MainActivity.this);
        imageCodeProject.setImageResource(R.drawable.ico_warning);
        toastView.addView(imageCodeProject, 0);
        toastView.setPadding(100, 85, 100, 85);
        toast.show();
    }

    private byte[] bitmaptobytearray(Bitmap bitmap) {
        //将bitmap转成字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bitmapByte = baos.toByteArray();
        return bitmapByte;
    }
}
