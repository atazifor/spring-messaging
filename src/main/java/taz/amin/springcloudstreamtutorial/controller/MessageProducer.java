package taz.amin.springcloudstreamtutorial.controller;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageProducer {
    private final StreamBridge streamBridge;

    public MessageProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @PostMapping("/sendInvoice")
    public String sendInvoice(@RequestBody String message) {
        streamBridge.send("invoiceOutput", message);  // Sends to invoice-topic
        return "Invoice message sent: " + message;
    }

    @PostMapping("/sendPayment")
    public String sendPayment(@RequestBody String message) {
        streamBridge.send("paymentOutput", message);  // Sends to payment-topic
        return "Payment message sent: " + message;
    }
}
