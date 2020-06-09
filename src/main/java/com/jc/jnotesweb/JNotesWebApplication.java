package com.jc.jnotesweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.jc.jnotesweb.service.cassandra.CassandraSessionManager;

@Configuration
@Import({ CassandraSessionManager.class })
@SpringBootApplication
public class JNotesWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(JNotesWebApplication.class, args);
	}
	

}
