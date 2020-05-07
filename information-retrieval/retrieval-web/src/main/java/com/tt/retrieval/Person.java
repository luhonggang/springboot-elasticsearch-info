package com.tt.retrieval;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author LuHongGang
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "demo.person")
public class Person {
    private String lastName;
    private Integer age;
    private Boolean boss;
    private Date birth;

    private Map<String,Object> maps;
    private List<Object> lists;

    @Bean Person getPerson(){
        System.out.println(" 初始化 参数 ： " + lastName);
        return new Person();
    }
}
