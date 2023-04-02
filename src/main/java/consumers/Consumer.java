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
import conn.RMQChannelFactory;
import conn.RMQChannelPool;
import dto.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import controller.*;


import static constants.Constants.*;
import static constants.Constants.CHANNEL_POOL_CAPACITY;

public class Consumer {
    private static Logger logger = Logger.getLogger(Consumer.class.getName());
    String queueName;
    DynamoDbClient dynamoDbClient;
    UserDict dict;
    RMQChannelFactory rmqChannelFactory;
    RMQChannelPool rmqChannelPool;

    public Consumer(int queueNum) {
       queueName = QUEUE_NAME + queueNum;
    }

    public void init() throws IOException, TimeoutException {
        dict = new UserDict();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOSTNAME);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        try{
            logger.log(Level.INFO, "trying connect");
            Connection connection = factory.newConnection();
            logger.log(Level.INFO, "connect success");

            rmqChannelFactory = new RMQChannelFactory(connection);
            rmqChannelPool = new RMQChannelPool(CHANNEL_POOL_CAPACITY, rmqChannelFactory);

            Channel channel = rmqChannelPool.borrowObject();
            channel.basicQos(1);
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("x-queue-type", "quorum");
            channel.queueDeclare(queueName, true, false, false, arguments);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] argv) throws Exception {
        Consumer consumer = new Consumer(QUEUE_NUM);
        consumer.init();
        consumer.connectToDB();

        logger.log(Level.INFO, "started");
        consumer.ProcessMsg();
    }
    private void connectToDB(){
        System.setProperty("aws.accessKeyId", accessKeyId);
        System.setProperty("aws.secretAccessKey", secretAccessKey);
        System.setProperty("aws.sessionToken", sessionToken);
        Region region = Region.US_EAST_1;
        this.dynamoDbClient = DynamoDbClient.builder()
                .credentialsProvider(SystemPropertyCredentialsProvider.create())
                .region(region)
                .build();
    }

    private void ProcessMsg()throws Exception{
        DBController dbController = new DBController();
        System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");
        Runnable runnable = () -> {
            try {
                Channel channel = rmqChannelPool.borrowObject();
//                channel.queueDeclare(queueName, true, false, false, null);
                // max one message per receiver
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    //TODO: if we should put the gson outside the thread
                    Gson gson = new Gson();
                    Swipe swipe = gson.fromJson(message, Swipe.class);

                    //AS3: update the SwipeData database
                    if(swipe.getRightOrNot()){
                        dbController.putItemInTable(this.dynamoDbClient, DB_ER2EE, "Swiper", swipe.getSwiper().toString(), "Swipee", swipe.getSwipee().toString());
                        dbController.putItemInTable(this.dynamoDbClient, DB_EE2ER, "Swipee", swipe.getSwipee().toString(), "Swiper", swipe.getSwiper().toString());
                    } else {
                        dbController.putItemInTable(this.dynamoDbClient, DB_DIS, "Swiper", swipe.getSwiper().toString(), "Swipee", swipe.getSwipee().toString());
                    }
//                    //AS2: store the swipe data into a data structure.
//                    this.dict.updateDict(swipe);
                    logger.log(Level.INFO, "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
                };
                // process messages
                channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
                rmqChannelPool.returnObject(channel);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        };
        // start threads and block to receive messages
        for(int i = 0; i < CONSUMER_THREAD_NUM; i++){
            Thread recv = new Thread(runnable);
            recv.start();
        }
    }
}