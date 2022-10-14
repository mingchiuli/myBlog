package com.markerhub.ws.dto.impl;

import com.markerhub.common.vo.UserEntityVo;
import com.markerhub.ws.dto.Container;
import com.markerhub.ws.dto.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@AllArgsConstructor
public class InitDto implements Serializable, MessageDto {
    Container<Bind> data;

    public static String mark = "init";

    @Override
    public String getMethodName() {
        return mark;
    }

    @Override
    public Container<Bind> getData() {
        return data;
    }


    @Data
    @AllArgsConstructor
    public static class Bind implements Serializable {
        String blogId;
        ArrayList<UserEntityVo> users;
    }
}