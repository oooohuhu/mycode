package com.wangy.mycode.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.wangy.mycode.R;

/**
 * Created by xhb on 2016/12/21.
 */
public class IntentUtils {
    public  static  void enterIntent(Context context,Intent intent){
//        Intent intent = new Intent(context,class1);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(R.anim.in_from_right,
                R.anim.out_to_left);
    }
    public  static  void backIntent(Context context){
        ((Activity) context).finish();
        ((Activity) context).overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }
}
