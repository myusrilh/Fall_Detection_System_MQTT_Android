package helper;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttHelperAsync {
    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;
    private static Context context;
    private static View view;


    // structure -> tcp://host:port
    final String serverUri = "tcp://broker.hivemq.com:1883";

    final String clientId = "FallDetectionSystemAndroid";
    final String subscriptionTopic = "label/machine_learning";

    String username = "fallDetectionAndroid";
    String password = "fall123";

    public static void getMqttMessage(View v, Context c){
        view = v;
        context = c;
    }

    public void initMqtt() {
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                try{
                    Log.w("Mqtt", message.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setKeepAliveInterval(MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT);
        mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
    }

    public void setCallback(MqttCallbackExtended callback){
        mqttAndroidClient.setCallback(callback);
    }

    public void connect(){
        try {
            mqttAndroidClient.connect(mqttConnectOptions);
        } catch (MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }

    public boolean isConnected(){
        return mqttAndroidClient.isConnected();
    }

    public void disconnect(){
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }

    private void subscribeToTopic() {
        try{
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt","Subscribe failed!");
                }
            });
        } catch (MqttException e) {
            System.err.println("Exception whilst subscribing");
            e.printStackTrace();
        }
    }
}
