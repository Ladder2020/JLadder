package com.jladder.utils.rabbit;

import com.jladder.data.Receipt;
import com.jladder.lang.Strings;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

public class RabbitHelper {

    private static Map<String,RabbitConfig> configs = new HashMap<String,RabbitConfig>();

    public static Receipt send(String message){
        return send(message,"_default_");
    }


    public static Receipt send(String message,String configname){
        RabbitConfig config = configs.get(Strings.isBlank(configname)?"_default_":configname);
        return send(message,config.getQueue(),config.getRoute(),config.getExchange(),config.getHost(),config.getPort(),config.getUsername(),config.getPasword());
    }
    public static Receipt send(RabbitConfig config){
        return send(config.getMessage(),config.getQueue(),config.getRoute(),config.getExchange(),config.getHost(),config.getPort(),config.getUsername(),config.getPasword());
    }

    public static Receipt send(String message, String queue, String route, String exchange,String host,int port,String username,String password){
        try{
//            rabbitTemplate.convertAndSend("xiao_exchange","xiao_route",dataMap);
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setPort(port);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            /**
             exchange :交换器的名称
             type : 交换器的类型，常见的有direct,fanout,topic等
             durable :设置是否持久化。durable设置为true时表示持久化，反之非持久化.持久化可以将交换器存入磁盘，在服务器重启的时候不会丢失相关信息。
             autoDelete：设置是否自动删除。autoDelete设置为true时，则表示自动删除。自动删除的前提是至少有一个队列或者交换器与这个交换器绑定，之后，所有与这个交换器绑定的队列或者交换器都与此解绑。不能错误的理解—当与此交换器连接的客户端都断开连接时，RabbitMq会自动删除本交换器
             internal：设置是否内置的。如果设置为true，则表示是内置的交换器，客户端程序无法直接发送消息到这个交换器中，只能通过交换器路由到交换器这种方式。
             arguments:其它一些结构化的参数，比如：alternate-exchange
             */
//            channel.exchangeDeclare(exchange,"fanout",true);
//            // 声明队列
//            channel.queueDeclare(queue, true, false, false, null);
            // 发行消息到队列
            channel.basicPublish(exchange, route, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
//            channel.close();
//            connection.close();
            return new Receipt(false);
        }catch (Exception e){
            return new Receipt(false);
        }
    }


}
