///                                              ISEP
///                         Projet d'Algorithmie et de Programmation
///                Richard Berrebi - Antoine Monteil - Maximilien Absolut de la Gastine

package tksi.example.mqtttest2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.adlnotifier.helpers.MQTTHelper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MainActivity extends AppCompatActivity {

    MQTTHelper mqttHelper;  //Déclaration de la classe dans le main

    TextView dataReceived;  //textview pour afficher le message



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataReceived = (TextView)findViewById(R.id.dataReceived); //Affichage du message reçu dans le textview

        startMqtt();    //Démarrage du MQTT (avec intervention de la classe MQTTHelper et du callback)
    }

    public void startMqtt() {   //Renvoi de la classe MQTTHelper dans le main
        mqttHelper = new MQTTHelper(getApplicationContext());   //Appel de la classe
        mqttHelper.setCallback(new MqttCallbackExtended() {     //Intervention du callback pour acheminer le message reçu (avec ses fonctions sur l'état) pour entre autre l'afficher dans le textview
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Received:", mqttMessage.toString());
                dataReceived.setText(mqttMessage.toString());   //Enregistrement du message reçu dans dataReceived
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });


    }

    public void publish(View view) {
        mqttHelper.publish();
    }   //Acheminement de la fonction publish issu de la classe MQTTHelper pour le relier à un boutton


}