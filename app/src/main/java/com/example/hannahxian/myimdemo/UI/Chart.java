package com.example.hannahxian.myimdemo.UI;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.hannahxian.myimdemo.R;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.util.List;

public class Chart extends AppCompatActivity implements EMMessageListener {

    final static String TAG = "Chart";

    // 聊天信息输入框
    private EditText mInputEdit;
    // 发送按钮
    private Button mSendBtn;

    // 显示内容的 TextView
    private TextView mContentText;

    // 消息监听器
    private EMMessageListener mMessageListener;
    // 聊天的 ID
    private String mChatId;
    // 当前会话对象
    private EMConversation mConversation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        // 获取当前会话的username(如果是群聊就是群id)
        mChatId = getIntent().getStringExtra("chartId");
        mMessageListener = this;

        initView();
        initConversation();
    }

    private void initView() {
        mInputEdit = (EditText) findViewById(R.id.ec_edit_message_input);
        mSendBtn = (Button) findViewById(R.id.ec_btn_send);
        mContentText = (TextView) findViewById(R.id.ec_text_content);

        mContentText.setMovementMethod(new ScrollingMovementMethod());

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mInputEdit.getText().toString();
                if (!TextUtils.isEmpty(content)){
                    mInputEdit.setText("");

                    EMMessage message = EMMessage.createTxtSendMessage(content,mChatId);
                    mContentText.setText(mContentText.getText()
                                    + "\n发送："
                                    + content
                                    + " - time: "
                                    + message.getMsgTime());
                    EMClient.getInstance().chatManager().sendMessage(message);
                    message.setMessageStatusCallback(new EMCallBack() {
                        @Override
                        public void onSuccess() {
                            Log.i(TAG, "onSuccess: send message");
                        }

                        @Override
                        public void onError(int i, String s) {
                            Log.d(TAG, "onError: "+s);
                        }

                        @Override
                        public void onProgress(int i, String s) {
                            // 消息发送进度，一般只有在发送图片和文件等消息才会有回调，txt不回调
                        }
                    });
                }
            }
        });
    }

    private void initConversation() {
        mConversation = EMClient.getInstance().chatManager().getConversation(mChatId,null,true);

        mConversation.markAllMessagesAsRead();
        int count = mConversation.getAllMsgCount();
        int tCount = mConversation.getAllMessages().size();
        Log.i(TAG, "initConversation: count is {}, tCount is {}"+count+"-"+tCount);
        if (tCount < count && tCount<20){
            String msgID = mConversation.getAllMessages().get(0).getMsgId();
            mConversation.loadMoreMsgFromDB(msgID,20-tCount);
        }
        if (mConversation.getAllMessages().size() > 0){
            EMMessage message = mConversation.getLastMessage();
            EMTextMessageBody body = (EMTextMessageBody) message.getBody();
            mContentText.setText("聊天记录：" + body.getMessage() + " - time: " + mConversation.getLastMessage()
                    .getMsgTime());
        }
    }



    /**
     * 自定义实现Handler，主要用于刷新UI操作
     */
    Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    EMMessage message = (EMMessage) msg.obj;
                    // 这里只是简单的demo，也只是测试文字消息的收发，所以直接将body转为EMTextMessageBody去获取内容
                    EMTextMessageBody body = (EMTextMessageBody) message.getBody();
                    // 将新的消息内容和时间加入到下边
                    mContentText.setText(mContentText.getText()
                            + "\n接收："
                            + body.getMessage()
                            + " - time: "
                            + message.getMsgTime());
                    break;
            }
        }
    };

    @Override protected void onResume() {
        super.onResume();
        // 添加消息监听
        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
    }

    @Override protected void onStop() {
        super.onStop();
        // 移除消息监听
        EMClient.getInstance().chatManager().removeMessageListener(mMessageListener);
    }


    /**
     * --------------------------------- Message Listener -------------------------------------
     * 环信消息监听主要方法
     */


    /**
     * 收到新消息
     *
     * @param list 收到的新消息集合
     */
    @Override
    public void onMessageReceived(List<EMMessage> list) {
// 循环遍历当前收到的消息
        for (EMMessage message : list) {
            Log.i(TAG, "onMessageReceived: "+message);
            if (message.getFrom().equals(mChatId)) {
                // 设置消息为已读
                mConversation.markMessageAsRead(message.getMsgId());

                // 因为消息监听回调这里是非ui线程，所以要用handler去更新ui
                Message msg = mHandler.obtainMessage();
                msg.what = 0;
                msg.obj = message;
                mHandler.sendMessage(msg);
            } else {
                // TODO 如果消息不是当前会话的消息发送通知栏通知
            }
        }

    }

    /**
     * 收到新的 CMD 消息
     */
    @Override
    public void onCmdMessageReceived(List<EMMessage> list) {
        for (int i = 0; i < list.size(); i++) {
            // 透传消息
            EMMessage cmdMessage = list.get(i);
            EMCmdMessageBody body = (EMCmdMessageBody) cmdMessage.getBody();
            Log.i(TAG, "onCmdMessageReceived: "+body);
        }

    }

    /**
     * 收到新的已读回执
     *
     * @param list 收到消息已读回执
     */
    @Override
    public void onMessageRead(List<EMMessage> list) {

    }

    /**
     * 收到新的发送回执
     * TODO 无效 暂时有bug
     *
     * @param list 收到发送回执的消息集合
     */
    @Override
    public void onMessageDelivered(List<EMMessage> list) {

    }

    /**
     * 消息撤回回调
     *
     * @param list 撤回的消息列表
     */
    @Override
    public void onMessageRecalled(List<EMMessage> list) {

    }

    /**
     * 消息的状态改变
     *
     * @param emMessage 发生改变的消息
     * @param o 包含改变的消息
     */
    @Override
    public void onMessageChanged(EMMessage emMessage, Object o) {

    }


}
