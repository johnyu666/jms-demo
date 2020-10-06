package cn.johnyu.origin;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class OrigineApp {
    public static void main(String[] args) throws Exception{
//        send();
        receive();
//        receiveAsyn();
    }

    /**
     * 可以修改相应的代码，完成基于Queue和topic的消息发送，以及不同的事务控制机制
     */
    public static void send() throws Exception {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = connectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        /** 两种不同的消息确认模式的session打开方式
         Session session=connection.createSession(false,Session.CLIENT_ACKNOWLEDGE);
         Session session=connection.createSession(true,Session.SESSION_TRANSACTED);
         **/
        Destination destination = session.createQueue("john");
//        Destination destination=session.createQueue("john_topic");
        MessageProducer producer = session.createProducer(destination);
        TextMessage message = session.createTextMessage();
        message.setText("Hello123 ");
        producer.send(message);
/**
 与session的确认模式相对应的确认
 message.acknowledge();
 session.commit();
 **/
        session.close();
        connection.close();
    }

    /**
     * 消息的异步接方式
     */
    public static void receiveAsyn() throws Exception {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = connectionFactory.createConnection();
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        connection.start();
        Destination destination = session.createTopic("john_topic");
        MessageConsumer consumer = session.createConsumer(destination);
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage m = (TextMessage) message;
                try {
                    System.out.println(m.getText());
//                    m.acknowledge();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 消息的同步接收方式
     */
    public static  void receive() throws Exception{
        ConnectionFactory connectionFactory=new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection=connectionFactory.createConnection();
        Session session=connection.createSession(false,Session.CLIENT_ACKNOWLEDGE);
        connection.start();
        Destination destination=session.createQueue("john");
        MessageConsumer consumer=session.createConsumer(destination);
        TextMessage message=(TextMessage) consumer.receive();
        System.out.println(message.getText());
        message.acknowledge();
//        session.commit();
        session.close();
        connection.close();
    }
}

