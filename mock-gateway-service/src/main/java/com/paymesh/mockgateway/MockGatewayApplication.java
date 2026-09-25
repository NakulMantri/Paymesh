package com.paymesh.mockgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MockGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(MockGatewayApplication.class, args);
    }
}
