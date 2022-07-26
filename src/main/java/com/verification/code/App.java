package com.verification.code;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * 
 *  说明：加入swagger api文档接口
 *  @author: dev
 *  @date 2022-07-01
 */
@SpringBootApplication
@EnableSwagger2
public class App {
    public static void main( String[] args ){
    	SpringApplication.run(App.class, args);
    }
}
