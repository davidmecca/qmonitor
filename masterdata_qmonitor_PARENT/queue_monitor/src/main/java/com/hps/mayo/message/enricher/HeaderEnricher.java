package com.hps.mayo.message.enricher;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public final class HeaderEnricher {
    public static Message<?> transform(Message<?> messageIn, String headerKey, String headerValue) 
    {

        Message<?> messageOut = MessageBuilder
                .withPayload(messageIn.getPayload())
                .copyHeadersIfAbsent(messageIn.getHeaders())
                .setHeaderIfAbsent(headerKey, headerValue).build();

        return messageOut;
    }
}