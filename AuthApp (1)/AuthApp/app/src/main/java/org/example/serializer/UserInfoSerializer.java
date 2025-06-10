package org.example.serializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.example.eventProducer.UserInfoEvent;

import java.io.OutputStream;
import java.util.Map;

public class UserInfoSerializer implements Serializer<UserInfoEvent> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String arg0, UserInfoEvent arg1) {
        byte[] res = null;
        ObjectMapper objMapper = new ObjectMapper();
        try{
            res = objMapper.writeValueAsString(arg1).getBytes();
        }catch(Exception e){
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public void close() {
        Serializer.super.close();
    }
}
