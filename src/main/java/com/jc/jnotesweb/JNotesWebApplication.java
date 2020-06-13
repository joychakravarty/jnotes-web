package com.jc.jnotesweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jc.jnotesweb.service.cassandra.CassandraSessionManager;

@Configuration
@Import({ CassandraSessionManager.class })
@SpringBootApplication
public class JNotesWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(JNotesWebApplication.class, args);
	}
	
	@Bean
    @Primary
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        return builder.modulesToInstall(new JavaTimeModule());
    }
	

}
