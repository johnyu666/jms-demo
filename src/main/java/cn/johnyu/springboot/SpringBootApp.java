package cn.johnyu.springboot;

import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.*;

@SpringBootApplication
@EnableJms
//@EnableTransactionManagement
public class SpringBootApp {
    public static void main(String[] args) {
      ApplicationContext context= SpringApplication.run(SpringBootApp.class,args);
//        SendService sendService=context.getBean(SendService.class);
//        sendService.send3("您有一条新的订单:");
//        ReceiveService receiveService=context.getBean(ReceiveService.class);
//        receiveService.receive();

    }

    /**
     * 以下信息为定制时使用,一般情况可以省略
     */

    //由容器生成Destination,并在发送和接收中使用（参看send3），可以覆盖配置中的"pub-sub"设置
    @Bean
    public Destination createDestination(){
        return new ActiveMQTopic("john_topic");
    }

    /**
     * 定制MessaageConverter bean,并传入到定制的JmsTemplate
     * 提供了传送对象的序列化和反序列化机制：本处采用了Jackson2MessageConverter
     */
    @Bean // Serialize message content to json using TextMessage
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        //传递自定义对象，可以使用"TEXT"或"BYTES",传递TextMessages and BytesMessages可以使用OBJECT
        //此处将使用json的数据格式 {"to":"tom","body":"你好，我是John"}
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("className");//"className"可以为任意字符，用于标记传递数据的类型
        return converter;
    }



    /**
     * 定制JmsTemplate,同时利用参数传入容器中的bean,如：ConnectionFactory
     */
    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestinationName("john");
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setMessageConverter(messageConverter);
        return jmsTemplate;
    }

    /**
     * 定制了TransactionManager，为@Transactional或定制JmsListenerContainerFactory时使用
     */
    @Bean
    public PlatformTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new JmsTransactionManager(connectionFactory);
    }

    /**
     * 定制JmsListenerContainerFactory，并可以利用它产生JMSListener
     */
    @Bean
    public JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory,
                                                    DefaultJmsListenerContainerFactoryConfigurer configurer
            , PlatformTransactionManager transactionManager) {
        DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
//        factory.setConcurrency("3");//同时有3-10个监听
//        factory.setTransactionManager(transactionManager); //指定TranscationManager后，即可以使用@Transcactional来进行事务控制
        configurer.configure(jmsListenerContainerFactory, connectionFactory);
        return jmsListenerContainerFactory;
    }

}
@Service
class SendService{
    @Resource
    JmsTemplate jmsTemplate;
    @Resource
    Destination destination;

//    @Transactional 事务控制
    public void send(String msg){
        jmsTemplate.send("john", new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage textMessage=session.createTextMessage(msg);
                return  textMessage;
            }
        });
    }
    //直接指定destinationName
    public void send2(String msg){
        jmsTemplate.convertAndSend("john",msg);
    }

    public void send3(String msg){
        jmsTemplate.convertAndSend(destination,msg);
    }
}

@Service
class ReceiveService{
    @Resource
    JmsTemplate jmsTemplate;
    @Resource
    Destination destination;
    public void receive(){
        TextMessage textMessag=(TextMessage) jmsTemplate.receive("john");
        try {
            System.out.println(textMessag.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @JmsListener(destination = "john_topic")
    public void receiveAysn(String msg){
        System.out.println(msg);
    }

}
