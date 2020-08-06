package com.poc.stagers;

import java.util.Locale;

import com.poc.stagers.controller.StagersController;
import com.poc.stagers.models.EventStatus;
import com.poc.stagers.models.Role;
import com.poc.stagers.models.EventStatus.EventStatusEnum;
import com.poc.stagers.repositories.EventStatusRepository;
import com.poc.stagers.repositories.RoleRepository;
import com.poc.stagers.service.SequenceGeneratorService;
import com.poc.test.TestLinkManager;
import com.poc.util.ClassUtil;
import com.poc.util.JarScanner;
import com.poc.util.StringsUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StagersApplication
{
   @Autowired
   private SequenceGeneratorService sequenceGeneratorService;
   
   public static void main(final String[] args) {
	   final Locale en_US = new Locale("en", "US");
	   ResourceManager.initialize();
	   ResourceManager.load("en_US", "messages", en_US);
	   
	   SpringApplication.run(StagersApplication.class, args);
	   
	   if (StringsUtil.isTestAutomationMode()) {
		   TestLinkManager.getInstance();
		   registerAnnotationListener();
		   ClassUtil.scanClasses();
	   }
   }
   
   @Bean
   CommandLineRunner init(RoleRepository roleRepository, EventStatusRepository eventStatusRepository) {
      return (args) -> {
         Role adminRole = roleRepository.findByRole("ADMIN");
         Role userRole;
         if (adminRole == null) {
            userRole = new Role();
            userRole.setId(this.sequenceGeneratorService.generateSequence("roles_sequence"));
            userRole.setRole("ADMIN");
            roleRepository.save(userRole);
         }

         userRole = roleRepository.findByRole("USER");
         Role eventStatusx;
         if (userRole == null) {
            eventStatusx = new Role();
            eventStatusx.setId(this.sequenceGeneratorService.generateSequence("roles_sequence"));
            eventStatusx.setRole("USER");
            roleRepository.save(eventStatusx);
         }

         for (EventStatusEnum status : EventStatusEnum.values()) {
            EventStatus eventStatus = eventStatusRepository.findByEventStatus(status.toString());
            if (eventStatus == null) {
               EventStatus newEventStatus = new EventStatus();
               newEventStatus.setId(this.sequenceGeneratorService.generateSequence("event_status_sequence"));
               newEventStatus.setStatus(status.toString());
               eventStatusRepository.save(newEventStatus);
            }
         }

      };
   }
   
   protected static void registerAnnotationListener() {
		JarScanner.registerAnnotationListener(StagersController.class, 
			new JarScanner.AnnotationListener() {
				public void annotationDiscovered(String className, String annotationType) {
					TestLinkManager.getInstance().registerAnnotationClass(className);
				}
			});
   }
}

