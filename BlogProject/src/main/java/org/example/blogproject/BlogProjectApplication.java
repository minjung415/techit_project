package org.example.blogproject;

import lombok.extern.slf4j.Slf4j;
import org.example.blogproject.domain.User;
import org.example.blogproject.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class BlogProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogProjectApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(UserRepository userRepository){
		return args -> {
//			User user = new User("a","a","a","a");
//			userRepository.save(user);
//			log.info("ok ::: " + user.getId());
		};
	}
}
