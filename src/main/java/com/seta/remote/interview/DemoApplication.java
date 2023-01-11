package com.seta.remote.interview;

import com.seta.remote.interview.service.InMemoryMovieService;
import com.seta.remote.interview.service.InMemoryWorldDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

//	inMemoryWorldDao.showCountry();
	public static void main(String[] args) {

		SpringApplication.run(DemoApplication.class, args);
	}

}
