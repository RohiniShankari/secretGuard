//package com.example.secretGuard;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class SecretGuardApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(SecretGuardApplication.class, args);
//	}
//
//}

package com.example.secretGuard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SecretGuardApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecretGuardApplication.class, args);
    }
}
