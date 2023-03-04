package consumers;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import dto.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static constants.Constants.*;

public class Consumer {
    private static Logger logger = Logger.getLogger(Consumer.class.getName());
    int queueNum;
    ArrayList<String> queueNames;

    public Consumer(int queueNum) {
        this.queueNum = queueNum;
        this.queueNames = new ArrayList<>();
        for(int i = 0; i < queueNum; i++){
            queueNames.add(QUEUE_NAME + i);
        }
    }

    public static void main(String[] argv) throws Exception {
        // I don't understand where should I put the users' info, so I just make it stay at cache.
        UserDict dict = new UserDict();
        Consumer consumer = new Consumer(QUEUE_NUM);
        logger.log(Level.INFO, "started");
        for(String queueName : consumer.queueNames){
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(HOSTNAME);
            connectToQueue(queueName, factory, dict);
        }
    }
    private static void connectToQueue(String queueName, ConnectionFactory factory, UserDict dict)throws Exception{
        logger.log(Level.INFO, "trying connect");
        final Connection connection = factory.newConnection();
        logger.log(Level.INFO, "connect success");
        System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");
        Runnable runnable = () -> {
            try {
                final Channel channel = connection.createChannel();
                channel.queueDeclare(queueName, true, false, false, null);
                // max one message per receiver
                channel.basicQos(1);
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    //TODO: if we should put the gson outside the thread
                    Gson gson = new Gson();
                    Swipe swipe = gson.fromJson(message, Swipe.class);
                    dict.updateDict(swipe);
                    logger.log(Level.INFO, "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
                };
                // process messages
                channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        };
        // start threads and block to receive messages
        for(int i = 0; i < CONSUMER_THREAD_NUM; i++){
            Thread recv = new Thread(runnable);
            recv.start();
        }
//        Thread recv2 = new Thread(runnable);
//        recv1.start();
//        recv2.start();
    }
}