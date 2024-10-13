package taz.amin.springcloudstreamtutorial.service;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class MessageConsumer {
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
}
