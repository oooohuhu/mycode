package com.wangy.mycode.Base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by xhb on 2016/12/27.
 */
public abstract class BaseActivity  extends AppCompatActivity{
    public  BaseActivity(){
    }

//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        LogUtil.i("Acivity创建");
////        App.app().getAppManager().addActivity(this);
////        App.view().inject(this);
//        this.init();
//    }

    protected void onDestroy() {
        super.onDestroy();
//        LogUtil.i("Acivity销毁");
//        App.app().getAppManager().finishActivity(this);
    }

    public void showCustomToast(String text) {
//        AppMsg appMsg = AppMsg.makeText(this, text, AppMsg.STYLE_ALERT);
//        appMsg.setAnimation(17432578, 17432579);
//        appMsg.show();
    }

//    protected abstract void init();

    protected void startActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if(bundle != null) {
            intent.putExtras(bundle);
        }
        this.startActivity(intent);
    }

    public void startActivity(Class<?> cls, String title) {
        Bundle b = new Bundle();
        b.putString("activityTitle", title);
        this.startActivity(cls, b);
    }

    public void startActivity(Class<?> cls, String title, Bundle bundle) {
        if(bundle == null) {
            bundle = new Bundle();
        }

        bundle.putString("activityTitle", title);
        this.startActivity(cls, bundle);
    }

    protected void startActivityForResult(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if(bundle != null) {
            intent.putExtras(bundle);
        }

        this.startActivityForResult(intent, 1);
    }

    public void startActivityForResult(Class<?> cls, String title, Bundle bundle) {
        if(bundle == null) {
            bundle = new Bundle();
        }

        bundle.putString("activityTitle", title);
        this.startActivityForResult(cls, bundle);
    }

    protected void startActivity(String action) {
        this.startActivity((String)action, (Bundle)null);
    }

    protected void startActivity(String action, Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(action);
        if(bundle != null) {
            intent.putExtras(bundle);
        }

        this.startActivity(intent);
    }
}
