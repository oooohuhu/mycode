package com.wangy.mycode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.wangy.mycode.utils.IntentUtils;

/**
 * Created by xhb on 2016/12/26.
 */
public class CodeResultActivity extends AppCompatActivity {

    private ImageView codemap;
    private ImageView result_back;
    private Bitmap bitmaprelust;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coderesult);
        Intent intent=getIntent();
        if (intent!=null){
            byte[] bitmapbyte= intent.getByteArrayExtra("bitmap");
            bitmaprelust = bytearraytobitmap(bitmapbyte);
        }
        initView();
    }
    //字节数组转成bitmap
    private Bitmap bytearraytobitmap (byte[] bytes){
        Bitmap Bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        return Bitmap;
    }
    private void initView(){
        codemap = (ImageView)findViewById(R.id.codemap);
        result_back = (ImageView)findViewById(R.id.result_back);
        result_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CodeResultActivity.this,MainActivity.class));
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                finish();
            }
        });

        initdata();
    }
    private void initdata(){
        codemap.setImageBitmap(bitmaprelust);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            startActivity(new Intent(CodeResultActivity.this,MainActivity.class));
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            finish();
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
