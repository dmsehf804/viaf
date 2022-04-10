package com.lilterest.viaf;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


public class ProcessingDialog {
    private Context context;
    private Dialog dlg;
    ImageView pro;
    public ProcessingDialog(Context context) {
        this.context = context;
    }

    // 호출할 다이얼로그 함수를 정의한다.
    public void callFunction() {

        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        dlg = new Dialog(context);

        // 액티비티의 타이틀바를 숨긴다.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.processing_dialog);
Log.d("aaaaa","aasdasd");
        // 커스텀 다이얼로그를 노출한다.
        dlg.show();

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        ImageView progressbar = (ImageView)dlg.findViewById(R.id.progressbar);
        pro = (ImageView)dlg.findViewById(R.id.pro);
        Animation spin = AnimationUtils.loadAnimation(context, R.anim.progress_spin);
        Animation pro_anim = AnimationUtils.loadAnimation(context, R.anim.pro);
        spin.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                progressbar.startAnimation(spin);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        pro_anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                pro.setVisibility(View.GONE);
                pro.startAnimation(pro_anim);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        progressbar.startAnimation(spin);
        pro.startAnimation(pro_anim);


    }
    public Dialog getDlg(){
        return dlg;
    }
    public void dismiss(){
        dlg.dismiss();

    }
}
