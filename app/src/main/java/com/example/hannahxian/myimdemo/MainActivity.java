package com.example.hannahxian.myimdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hannahxian.myimdemo.UI.Chart;
import com.example.hannahxian.myimdemo.UI.Login;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

public class MainActivity extends AppCompatActivity {

    EditText mUsername;

    Button mChart,mLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 判断sdk是否登录成功过，并没有退出和被踢，否则跳转到登陆界面
        if(!EMClient.getInstance().isLoggedInBefore()){
            Intent intent = new Intent(MainActivity.this,Login.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        initView();
        initListner();
    }

    private void initView(){
        mUsername = findViewById(R.id.ec_edit_chat_id);
        mChart = findViewById(R.id.ec_btn_start_chat);
        mLogout = findViewById(R.id.ec_btn_sign_out);
    }

    private void initListner(){
        mChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chartId = mUsername.getText().toString().trim();
                if (!TextUtils.isEmpty(chartId)){
                    // 获取当前登录用户的 username
                    String currentUserName = EMClient.getInstance().getCurrentUser();
                    if (currentUserName.equalsIgnoreCase(chartId)){
                        Toast.makeText(MainActivity.this, "不能和自己聊天", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(MainActivity.this,Chart.class);
                    intent.putExtra("chartId",chartId);
                    startActivity(intent);
                }else{
                    Toast.makeText(MainActivity.this, "请输入正确的用户名", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout(){
        // 调用sdk的退出登录方法，第一个参数表示是否解绑推送的token，没有使用推送或者被踢都要传false
        EMClient.getInstance().logout(false, new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.i("Demo test success", "logout success");
                // 调用退出成功，结束app
                finish();
            }

            @Override
            public void onError(int i, String s) {
                Log.i("Demo test error", "logout error " + i + " - " + s);
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }
}
