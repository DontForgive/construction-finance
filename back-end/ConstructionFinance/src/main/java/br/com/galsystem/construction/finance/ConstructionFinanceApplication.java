package br.com.galsystem.construction.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "br.com.galsystem.construction.finance")
@EnableCaching
public class ConstructionFinanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConstructionFinanceApplication.class, args);
	}

}
