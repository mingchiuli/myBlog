package com.markerhub.cooperate.mq.handler.impl;

import com.markerhub.cooperate.CooperateEnum;
import com.markerhub.cooperate.dto.Container;
import com.markerhub.cooperate.dto.MessageDto;
import com.markerhub.cooperate.mq.handler.WSHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class TaskOverHandler implements WSHandler {

    SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public void setSimpMessagingTemplate(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }
    @Override
    public CooperateEnum methodName() {
        return CooperateEnum.TASK_OVER;
    }

    @Override
    public void doHand(MessageDto msg) {
        Container<String> containerV2 = msg.getData();
        String from = containerV2.getData();
        simpMessagingTemplate.convertAndSend("/topic/over", from);
    }
}