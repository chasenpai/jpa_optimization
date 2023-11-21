package com.shop;

import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopApplication.class, args);
	}

	@Bean
	Hibernate5JakartaModule hibernate5JakartaModule() {
		Hibernate5JakartaModule module = new Hibernate5JakartaModule();
//		module.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING, true); //강제 지연 로딩
		return module;
	}

}
