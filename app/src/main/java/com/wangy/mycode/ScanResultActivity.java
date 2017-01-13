package com.wangy.mycode;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wangy.mycode.Base.BaseActivity;
import com.wangy.mycode.Zxing.CaptureActivity;

/**
 * Created by xhb on 2016/12/28.
 */
public class ScanResultActivity extends BaseActivity {

    private ImageView result_back;
    private TextView scanresult_word;
    private WebView scanresult_webview;
    private String result;
    private LinearLayout ll_result_word;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanresultlayout);
        Intent intent = getIntent();
        if (intent != null) {
            result = intent.getStringExtra("result");
        }
        initView();
    }

    private void initView() {
        result_back = (ImageView) findViewById(R.id.result_back);
        result_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(ScanResultActivity.this,CaptureActivity.class));
//                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                finish();
            }
        });
        ll_result_word = (LinearLayout)findViewById(R.id.ll_result_word);
        scanresult_word = (TextView) findViewById(R.id.scanresult_word);
        scanresult_webview = (WebView) findViewById(R.id.scanresult_webview);
        if ((result.contains("http://") || result.contains("https://"))) {
            scanresult_webview.setVisibility(View.VISIBLE);
            ll_result_word.setVisibility(View.GONE);
            setScanresult_webview();
        }else {
            scanresult_webview.setVisibility(View.GONE);
            ll_result_word.setVisibility(View.VISIBLE);
            scanresult_word.setText(result);
        }

    }
private void setScanresult_webview(){
    if ((result.contains("http://") || result.contains("https://"))) {
        //加载需要显示的网页
        scanresult_webview.loadUrl(result);
    }

    //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
    scanresult_webview.setWebViewClient(new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            view.loadUrl(url);
            return true;
        }
    });
    WebSettings webSettings = scanresult_webview.getSettings();
    //设置可以访问文件
    webSettings.setAllowFileAccess(true);
    webSettings.setJavaScriptEnabled(true);//支持js
    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    webSettings.setDomStorageEnabled(true);
    //设置支持缩放
    webSettings.setBuiltInZoomControls(true);
   // 优先使用缓存
    scanresult_webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
    scanresult_webview.setWebChromeClient(new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            // TODO Auto-generated method stub
            if (newProgress == 100) {
                // 网页加载完成
                showCustomToast("网页加载完成");
            } else {
                // 加载中
//                showCustomToast("网页加载中...");
            }

        }
    });
//    scanresult_webview.setDownloadListener(new DownloadListener() {
//        @Override
//        public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
//            String html = "<a href='" + s + "'>" + s + "</a>";
//            scanresult_webview.loadData(html, "text/html", "UTF-8");
//        }
//    });


}
    //弹出警告框
    public void showCustomToast(String text) {
        Toast toast = Toast.makeText(ScanResultActivity.this,
                text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 20, 20);
        LinearLayout toastView = (LinearLayout) toast.getView();
        ImageView imageCodeProject = new ImageView(ScanResultActivity.this);
        imageCodeProject.setImageResource(R.drawable.ico_warning);
        toastView.addView(imageCodeProject, 0);
        toastView.setPadding(100, 85, 100, 85);
        toast.show();
    }

//    返回上一次浏览的页面

    //改写物理按键——返回的逻辑
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            if(scanresult_webview.canGoBack())
            {
                scanresult_webview.goBack();//返回上一页面
                return true;
            }
            else
            {
//                startActivity(new Intent(ScanResultActivity.this,CaptureActivity.class));
//                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                finish();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
