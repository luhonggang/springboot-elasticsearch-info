//package com.fengdong.retrieval;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//
///**
// * @author LuHongGang
// * @version 1.0
// * @date 2020/4/28 13:49
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//public class HelloControllerTest {
//
//    @Autowired
//    private MockMvc mvc;
//
//    @Test
//    public void getHello() throws Exception {
//        mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string(equalTo("Greetings from Spring Boot!")));
//    }
//}
