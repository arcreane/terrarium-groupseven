///                                              ISEP
///                         Projet d'Algorithmie et de Programmation
///                Richard Berrebi - Antoine Monteil - Maximilien Absolut de la Gastine

package com.example.adlnotifier.helpers;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;


public class MQTTHelper {
    public MqttAndroidClient mqttAndroidClient;

    // -----------------DÉCOMMENTER LES LIGNES POUR TESTER NOTRE CLIENT SUR LE BROKER MQTTX. LE TEST SUR LE BROKER DE L'ISEP EST INCERTAIN --------------------------------------

    //final String serverUri = "tcp://broker.emqx.io:1883"; // URI serveur utilisée pour nos tests, fonctionnel pour notre broker de test
    final String serverUri = "tcp://srv-lora.isep.fr:1883"; //URI de l'ISEP (nous ne savons pas si un message a été bien reçue par ce broker)

    //final String clientId = "mqttx_005bac11"; //ClientID de notre client MQTT utilisé pour nos tests, fonctionnel
    final String clientId = "0004a30b0025b1aa"; //ClientID de l'appareil relié au broker MQTT de l'ISEP, nous ne savons pas si le message envoyé a été reçu
    //final String subscriptionTopic = "detector/humiditysubscriber"; //topic créée pour nos tests sur le broker MQTTX, fonctionnel, l'application reçoit les messages
    //final String publicationTopic = "detector/humiditypublisher"; //topic créée pour nos tests sur le broker MQTTX, fonctionnel, l'application peut envoyer les messages sur ce topic, le broker de test les reçoit
    final String subscriptionTopic = "sensor/humiditysubscriber"; //supposé topic d'écoute pour recevoir les valeurs d'humidité venant de l'appareil connecté à LoRa
    final String publicationTopic = "sensor/humidityreceiver"; //supposé topic de publication pour envoyant les valeurs d'humidité du client à l'appareil connecté à LoRa


    //final String username = "nom_test"; //nom d'identifiant de test pour la connexion au broker de test
    //final String password = "mdp_test"; //mot de pas d'identifiant de test pour la connexion au broker de test
    final String username = "eleve"; //nom d'identifiant du broker LoRa l'ISEP (pour les élèves)
    final String password = "isep2018"; //mot de passe d'identifiant du broker de LoRa de l'ISEP (pour les élèves)



    public MQTTHelper(Context context){
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);    //Lancement du client Android avec enregistrement des infos de connexion : l'URI serveur et l'ID client
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {  //Utilisation du callbak après avoir "entré" les infos du broker MQTT (URI et ID client)
            @Override
            public void connectComplete(boolean b, String s) {      //Ces fonctions (connectComplete, connectionLost, messageArrived, deliveryComplete), donnent l'état du callback
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        connect(); //La connexion au broker s'effectue qu'après le lancement du client

    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);    //Callback utilisé pour transiter le message dans le Main afin de l'acheminer dans un textview
    }

    private void connect(){  //Fonction de connexion au broker MQTT, contient des options pour la reconnexion, l'identifiant et le mot de passe
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(false);
            mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() { //Utilisation d'un try...catch pour détecter les erreur pour le test de la fonction de connexion
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt", "Launched");
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();  //Déconnexion de buffer pour avoir une meilleur réactivité
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribe(); //La fonction subscribe se lance si et seulement si la connexion est établie correctement
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + serverUri + ", " + exception.toString()); //Message d'erreur si erreur
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace(); //Rapport d'erreur en cas d'erreur de connexion
        }
    }



    private void subscribe() {  //Fonction d'écoute de topic (recevoir des messages relayés par le broker). Utilisation d'un try...catch pour être averti de l'état de la fonction de souscription de la librairie
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {     //Utilisation de la fonction incluse dans les librairies de Gradle, écoute du topic qui relaye les messages (subscriptionTopic).
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {    //Renvoie les infos sur les états de la fonction
                    Log.w("Mqtt","Subscribed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscription failed");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception subscription"); //Rapport d'erreur en cas d'erreur de réception
            ex.printStackTrace();
        }
    }



    public void publish(){
        int numberMessage = 89; //Donnée numérique (entier) à envoyer au terrarium
        byte[] encodedPayload;
        String message = String.valueOf(numberMessage); //Conversion de l'entier en String
        try {
            encodedPayload = message.getBytes("UTF-8"); //Encodage du message
            MqttMessage messageSent = new MqttMessage(encodedPayload);
            mqttAndroidClient.publish(publicationTopic, messageSent); //Try...catch de la fonction publish avec le message préalablement encodé, sur le topic de publication (publicationTopic)
        } catch (UnsupportedEncodingException | MqttException ex) {
            System.err.println("Exception publication"); // Rapport d'erreur en cas d'erreur de réception
            ex.printStackTrace();
        }

    }

}