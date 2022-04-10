package com.lilterest.viaf;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;


public class SRDialog {
    private Context context;
    private Activity activity;
    private String tag;
    public SRDialog(Context context, Activity activity, String tag) {
        this.context = context;
        this.activity = activity;
        this.tag = tag;
    }

    // 호출할 다이얼로그 함수를 정의한다.
    public void callFunction() {

        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        final Dialog dlg = new Dialog(activity);

        // 액티비티의 타이틀바를 숨긴다.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.sr_dialog);

        // 커스텀 다이얼로그를 노출한다.
        dlg.show();

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        final ImageView yes_btn = (ImageView)dlg.findViewById(R.id.sr_dialog_yes);
        final ImageView mtm_btn = (ImageView)dlg.findViewById(R.id.sr_dialog_mtm);

        yes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tag == "enhancing") {
                    Intent move = new Intent(activity.getApplicationContext(), DataSelectActivity.class);
                    move.putExtra("flag", "enhance");
                    move.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(move);
                }else if(tag == "mosaic"){
                    Intent move = new Intent(activity.getApplicationContext(), DataSelectActivity.class);
                    move.putExtra("flag", "mosaic");
                    move.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(move);
                }
                dlg.dismiss();

            }
        });

        mtm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dlg.dismiss();
            }
        });
    }
}
