package com.tt.retrieval;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * 交给spring容器进行管理
 * 达到当前程序结束的时候自动调用destroy()方法 测试
 * @author LuHongGang
 * @version 1.0
 */
@Component
public class CloseMethodSelfExecute implements DisposableBean{

    @Override
    public void destroy() throws Exception {
        System.out.println(" ####################### 当前项目被关闭的时候 自动释放内存资源调用测试 #######################");
    }
}
