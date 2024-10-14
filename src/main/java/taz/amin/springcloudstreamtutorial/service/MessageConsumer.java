package taz.amin.springcloudstreamtutorial.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class MessageConsumer {
    private static Logger LOG = LoggerFactory.getLogger(MessageConsumer.class.getName());
    // Consumer function for customer-related messages
    @Bean
    public Consumer<String> invoiceInput() {
        return message -> {
            System.out.println("Received invoice message: " + message);
        };
    }

    // Consumer function for order-related messages
    @Bean
    public Consumer<String> orderInput() {
        return message -> {
            System.out.println("Received order message: " + message);
        };
    }

    @Bean
    public Consumer<ErrorMessage> errorHandler() {
        return errorMessage -> {
            LOG.error("Error Processing Message");
        };
    }
}
