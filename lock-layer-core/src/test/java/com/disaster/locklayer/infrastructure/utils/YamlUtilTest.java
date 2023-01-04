package com.disaster.locklayer.infrastructure.utils;

import org.junit.Test;

public class YamlUtilTest {

    @Test
    public void getValueTest(){
        System.out.println(YmlUtil.getValue("spring.main.web-application-type"));
        System.out.println(YmlUtil.getValue("application.yml","spring.main.web-application-type"));
    }

}
