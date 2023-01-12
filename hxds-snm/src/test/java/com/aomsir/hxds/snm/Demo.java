package com.aomsir.hxds.snm;

import com.aomsir.hxds.snm.entity.NewOrderMessage;

import com.aomsir.hxds.snm.task.NewOrderMassageTask;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class Demo {
    @Resource
    private NewOrderMassageTask task;

    @Test
    public void send(){
        NewOrderMessage message=new NewOrderMessage();
        message.setUserId("9527");
        message.setFrom("沈阳北站");
        message.setTo("沈阳站");
        message.setDistance("3.2");
        message.setExpectsFee("46.0");
        message.setMileage("18.6");
        message.setMinute("18");
        message.setFavourFee("0.0");

        ArrayList list=new ArrayList<>(){{
            add(message);
        }};
        task.sendNewOrderMessageAsync(list);
    }

    @Test
    public void recieve(){
        List<NewOrderMessage> list = task.receiveNewOrderMessage(9527);
        list.forEach(one->{
            System.out.println(one.getFrom());
            System.out.println(one.getTo());
        });
    }

}
