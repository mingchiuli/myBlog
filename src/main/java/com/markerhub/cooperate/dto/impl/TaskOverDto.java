package com.markerhub.cooperate.dto.impl;

import com.markerhub.cooperate.CooperateEnum;
import com.markerhub.cooperate.dto.Container;
import com.markerhub.cooperate.dto.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class TaskOverDto implements MessageDto, Serializable {

    private Container<String> from;

    @Override
    public CooperateEnum getMethodName() {
        return CooperateEnum.TASK_OVER;
    }

    @Override
    public Container<String> getData() {
        return from;
    }
}