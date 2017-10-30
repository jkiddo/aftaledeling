package dk.sds.appointment;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

import dk.sds.appointment.configuration.DgwsConfiguration;
import dk.sts.appointment.Application;
import dk.sts.appointment.configuration.ApplicationConfiguration;

@Import({ApplicationConfiguration.class, DgwsConfiguration.class})
@EnableAutoConfiguration
public class DgwsApplication extends Application {


	public static void main(String[] args) throws Exception {
		SpringApplicationBuilder sab = new SpringApplicationBuilder(DgwsApplication.class);
		sab.web(false);
		sab.run(args);
	}	
}
