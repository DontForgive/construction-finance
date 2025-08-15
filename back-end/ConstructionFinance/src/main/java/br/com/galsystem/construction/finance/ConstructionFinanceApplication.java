package br.com.galsystem.construction.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.galsystem.construction.finance")
public class ConstructionFinanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConstructionFinanceApplication.class, args);
	}

}
