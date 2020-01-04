package com.ayuan.iot;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private EditText et_url;
    private EditText et_client_id;
    private EditText et_username;
    private EditText et_password;
    private Button btn_open_link;
    private Button btn_close_link;
    /*private EditText et_topic;*/
    private Button btn_subscrib;
    private ListView lv_topic_result_message;
    private TextView tv_topic_result_message;
    private EditText et_message;
    private Button btn_send_message;
    private String url;
    private String id;
    private String username;
    private String password;
    private MqttClient mqttClient;
    private MqttConnectOptions mqttConnectOptions;
    private ArrayList<MessageBean> messageBeans;
    private CustomerAdapter customerAdapter;
    private String topic;
    private ArrayList<String> topicList = new ArrayList<>();
    private AutoCompleteTextView autoCompleteTextView;
    /*private MqttConnectThread mqttConnectThread = new MqttConnectThread();*/
    /**
     * 自动填充内容
     */
    private static final String[] TOPICSELECTS = new String[]{
            // 设备向该主题发布消息，可更新物影子。
            "$baidu/iot/shadow/lightShadow/update/accepted",
            "$baidu/iot/shadow/lightShadow/update/rejected",
            //向该主题发布消息，可获取该设备的物影子。
            "$baidu/iot/shadow/lightShadow/get/accepted",
            "$baidu/iot/shadow/lightShadow/get/rejected",
            //当物影子中的期望值字段有更新时，该主题获得消息
            "$baidu/iot/shadow/lightShadow/delta",
            //向该主题发布任意JSON格式消息，可清空该设备的物影子。
            "$baidu/iot/shadow/lightShadow/delete/accepted",
            "$baidu/iot/shadow/lightShadow/delete/rejected",
            //订阅该主题，会收到物影子reported的变化，变化条件包括增加属性、减少属性、属性值变化。物接入会将reported字段中发生变化的当前值和更新值发送到该主题。
            "$baidu/iot/shadow/lightShadow/update/documents",
            //订阅该主题，会收到当物影子的reported字段发生变化时的物影子全部信息。snapshot主题和documents主题都是在物影子的reported字段发生变化时触发，snapshot主题会收到物影子的全部信息，documents主题只会收到reported中发生变化的值。
            "$baidu/iot/shadow/lightShadow/update/snapshot",
            //物接入会为该主题绑定订阅和发布权限，设备可以订阅或发布符合 $baidu/iot/general/# 的主题。例如，设备A发布消息到主题$baidu/iot/general/a，设备B订阅主题$baidu/iot/general/a，则设备A就能与设备B进行通信。
            "$baidu/iot/general/#"
    };
    private ArrayAdapter<String> autoTextAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        listener();
    }

    private void initData() {
        //初始化列表
        messageBeans = new ArrayList<>();
        customerAdapter = new CustomerAdapter();
        lv_topic_result_message.setAdapter(customerAdapter);
        lv_topic_result_message.setSelection(messageBeans.size() - 1);

        autoTextAdapter = new ArrayAdapter<String>(this, R.layout.item_autotext, TOPICSELECTS);
        autoCompleteTextView.setAdapter(autoTextAdapter);
    }

    private void initView() {
        et_url = (EditText) findViewById(R.id.et_url);
        et_client_id = (EditText) findViewById(R.id.et_client_id);
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        btn_open_link = (Button) findViewById(R.id.btn_open_link);
        btn_close_link = (Button) findViewById(R.id.btn_close_link);
        /*et_topic = (EditText) findViewById(R.id.et_topic);*/
        btn_subscrib = (Button) findViewById(R.id.btn_subscrib);
        lv_topic_result_message = (ListView) findViewById(R.id.lv_topic_result_message);
        tv_topic_result_message = (TextView) findViewById(R.id.tv_topic_result_message);
        et_message = (EditText) findViewById(R.id.et_message);
        btn_send_message = (Button) findViewById(R.id.btn_send_message);

        btn_open_link.setOnClickListener(this);
        btn_close_link.setOnClickListener(this);
        btn_subscrib.setOnClickListener(this);
        btn_send_message.setOnClickListener(this);
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setOnClickListener(this);
    }

    private void listener() {
        lv_topic_result_message.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tv_topic_result_message.setText(customerAdapter.getItem(position).getMessage());
            }
        });

        /*et_topic.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                topic = s.toString();
                topicIsSubscribe();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });*/

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                topic = s.toString();
                topicIsSubscribe();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                topic = TOPICSELECTS[position];
                topicIsSubscribe();
            }
        });
    }

    /**
     * 验证主题是否已经订阅
     * 如果已经订阅，则禁用按钮，如果没有，按钮可用
     */
    private void topicIsSubscribe() {
        if (topicList.contains(topic)) {
            btn_subscrib.setEnabled(false);
            btn_subscrib.setBackground(getDrawable(R.drawable.btn_line_onpress));
        } else {
            btn_subscrib.setEnabled(true);
            btn_subscrib.setBackground(getDrawable(R.drawable.btn_selector));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_link:
                // 开启连接
                connect();
                break;
            case R.id.btn_close_link:
                // 关闭连接
                disConnect();
                break;
            case R.id.btn_subscrib:
                // 订阅主题
                subscribe();
                break;
            case R.id.btn_send_message:
                // 向指定主题发送消息
                sendMessageToTopic();
                break;
        }
    }

    private void connect() {
        url = et_url.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        id = et_client_id.getText().toString().trim() + UUID.randomUUID().toString();
        if (TextUtils.isEmpty(id)) {
            Toast.makeText(this, "clilentID不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        username = et_username.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        password = et_password.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        initMQTT();
    }

    private void disConnect() {
        if (mqttClient == null || !mqttClient.isConnected()) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    mqttClient.disconnect();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            App.showToast("连接已关闭");
                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 订阅主题
     */
    private void subscribe() {
        try {
            if (mqttClient == null || !mqttClient.isConnected()) {
                App.showToast("请先建立连接");
            } else {
                if (!TextUtils.isEmpty(topic)) {
                    mqttClient.subscribe(topic, 0);
                    topicList.add(topic);
                    for (String s : topicList) {
                        Log.i(TAG, "哈哈哈：" + s);
                    }
                    topicIsSubscribe();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            App.showToast("主题订阅成功！");
                        }
                    });
                }
            }
        } catch (MqttException e) {
            e.printStackTrace();
            App.showToast("不存在该主题！！！");
        }
    }

    /**
     * 向主题发送消息
     */
    private void sendMessageToTopic() {
        final String message = et_message.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            App.showToast("请输入需要发送的消息！");
        } else if (mqttClient == null || !mqttClient.isConnected()) {
            App.showToast("请先建立连接！");
        } else {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        /**
                         * 只能发送消息的主题
                         * 1、$baidu/iot/shadow/lightShadow/update
                         * 2、$baidu/iot/shadow/lightShadow/get
                         * 3、$baidu/iot/shadow/lightShadow/delta
                         */
                        if (topicList.size() != 0/* && topicList.contains(topic)*/) {
                            mqttClient.publish(topic, new MqttMessage(message.getBytes()));
                        } /*else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    App.showToast("该主题尚未订阅，请订阅主题！");
                                }
                            });
                        }*/
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    /**
     * 初始化配置Mqtt
     */
    private void initMQTT() {
        if (mqttClient != null && mqttClient.isConnected()) {
            App.showToast("已连接");
            return;
        }
        try {
            //参数一：主机地址；参数二：客户端ID，一般以客户端唯一标识符，不能够和其他客户端重名；参数三：数据保存在内存
            mqttClient = new MqttClient(url, id, new MemoryPersistence());

            // MQTT的连接设置
            mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(false);// 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            mqttConnectOptions.setUserName(username);// 设置连接的用户名(自己的服务器没有设置用户名)
            mqttConnectOptions.setPassword(password.toCharArray());// 设置连接的密码(自己的服务器没有设置密码)
            mqttConnectOptions.setConnectionTimeout(10);// 设置连接超时时间 单位为秒
            mqttConnectOptions.setKeepAliveInterval(20);// 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制

            mqttClient.setCallback(new MqttCallback() {
                /**
                 * 连接丢失后执行
                 * @param throwable
                 */
                @Override
                public void connectionLost(Throwable throwable) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            topicList.clear();
                            App.showToast("连接丢失");
                        }
                    });
                }

                /**
                 * 获取的消息会执行这里
                 * @param topic 主题
                 * @param mqttMessage 消息
                 * @throws Exception 抛出所有异常
                 */
                @Override
                public void messageArrived(final String topic, final MqttMessage mqttMessage) throws Exception {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messageBeans.add(0, new MessageBean(topic, mqttMessage.toString()));
                            customerAdapter.notifyDataSetChanged();
                            if (messageBeans.size() > 0) {
                                tv_topic_result_message.setText(messageBeans.get(0).getMessage());
                            }
                        }
                    });
                }

                /**
                 * 发送消息成功后执行
                 *
                 * @param iMqttDeliveryToken
                 */
                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            App.showToast("消息发送成功！！！");
                            for (String s : topicList) {
                                Log.i(TAG, "topicList：" + s);
                            }
                        }
                    });
                }
            });

            //连接
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        mqttClient.connect(mqttConnectOptions);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                App.showToast("连接建立成功");
                            }
                        });
                    } catch (MqttSecurityException e) {
                        e.printStackTrace();
                        //安全问题连接失败
                        Log.e("安全问题连接失败", e.getMessage() + "");
                    } catch (MqttException e) {
                        e.printStackTrace();
                        //连接失败原因
                        Log.e("连接失败原因", "" + e.getMessage());
                    }
                }
            }.start();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    class CustomerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return messageBeans.size();
        }

        @Override
        public MessageBean getItem(int position) {
            return messageBeans.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.item_message, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_topic.setText(getItem(position).getTopic());
            holder.tv_message.setText(getItem(position).getMessage());
            return convertView;
        }

        class ViewHolder {
            public View rootView;
            public TextView tv_topic;
            public TextView tv_message;

            public ViewHolder(View rootView) {
                this.rootView = rootView;
                this.tv_topic = (TextView) rootView.findViewById(R.id.tv_topic);
                this.tv_message = (TextView) rootView.findViewById(R.id.tv_message);
            }
        }
    }

    private class MqttConnectThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                // 连接服务器，连接上不会阻塞在这里
                mqttClient.connect(mqttConnectOptions);
                Log.i(TAG, "是否在循环：----------11111111111 ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        App.showToast("连接成功");
                        Log.i(TAG, "只走一边：----------222222222222");
                    }
                });
            } catch (MqttSecurityException e) {
                //安全问题连接失败
                Log.e("安全问题连接失败", "e");
            } catch (MqttException e) {
                //连接失败原因
                Log.e("连接失败原因", "" + e);
            }
        }
    }
}
