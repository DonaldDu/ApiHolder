package com.dhy.apiholder.demo;

import com.dhy.apiholder.BaseUrl;

import org.junit.Test;

public class BaseUrlTest {
    @Test
    public void testMe() {
        Class cls = SampleApiHolder.class;
        Class[] interfaces = cls.getInterfaces();
        for (Class anInterface : interfaces) {
            BaseUrl baseUrl = (BaseUrl) anInterface.getAnnotation(BaseUrl.class);
            System.out.println(anInterface.getName()+":"+baseUrl.value());
        }
    }
}
