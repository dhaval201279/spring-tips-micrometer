package com.its.springtipsmicrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class SpringTipsMicrometerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringTipsMicrometerApplication.class, args);


		/**
		 *   Micrometer capabilities / APIs that can be used without Spring Boot

		 class SlowStatefullThing {
		 public int getCustomersLoggedIntoSystem() {
		 return new Random().nextInt(1000);
		 }
		 }

		 SlowStatefullThing customerService = new SlowStatefullThing();
		 CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
		 compositeMeterRegistry.add(new JmxMeterRegistry(null));
		 compositeMeterRegistry.add(new PrometheusMeterRegistry(null));

		 MeterRegistry mr = compositeMeterRegistry;
		 mr.config().commonTags("region", System.getenv("CLOUD_REGION"));

		 mr.counter("order-placed").increment(123);
		 mr.gauge("speed", 55);
		 mr.gauge("customers-logged-in", customerService, new ToDoubleFunction<SlowStatefullThing>() {
		@Override
		public double applyAsDouble(SlowStatefullThing value) {
		return customerService.getCustomersLoggedIntoSystem();
		}
		});
		 mr.timer("transform-photo-job").record(Duration.ofMillis(12));
		 mr.timer("transform-photo-job").record(() -> System.out.println("Hello World"));
		 String greeting = mr.timer("transform-photo-job").record(new Supplier<String>() {

		@Override
		public String get() {
		// doing work
		return "Hello World";
		}
		});*/





	}

	ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

	@Bean
	MeterRegistryCustomizer <MeterRegistry> registryCustomizer(@Value("${REGION:us-west}") String region) {
		return new MeterRegistryCustomizer<MeterRegistry>() {
			@Override
			public void customize(MeterRegistry registry) {
				registry.config().commonTags("region", region);
			}
		};
	}

	@Bean
	MeterFilter meterFilter() {
		/** Filter metrics for anything whose name starts with jvm. Metrics whose name starts with jvm wont be displayed
		 * One can create composite filters
		 */
		return MeterFilter.denyNameStartsWith("jvm");
	}

	@Bean
	MeterBinder meterBinder() {
		/**
		 * Can be used to get all the metric info for database pools
		 * Look at the one of the implementation of MeterBinder which is DataSourcePoolMetrics
		 */
		return new MeterBinder() {
			@Override
			public void bindTo(MeterRegistry registry) {

			}
		};
	}
	@Bean
	ApplicationRunner runner (MeterRegistry mr) {

		return args -> {
			executorService.scheduleWithFixedDelay(new Runnable() {
               /**
               One way to set up timer for custom stuff
               @Override
               public void run() {
                  mr.timer("transformed-photo-task").record(Duration.ofMillis(
                        (long) Math.random() * 1000));
               }*/

			   /**
				* 2nd way of doing it more control in terms of what data to be captured
				* */
			   public void run() {
				   Timer
					   .builder("transformed-photo-task")
					   .sla(Duration.ofMillis(1), Duration.ofSeconds(10))
					   .publishPercentileHistogram()
					   .tag("format", Math.random() > .5 ? "png" : "jpg ")
					   .register(mr);
			   }
		   },500, 500, TimeUnit.MILLISECONDS);
		};
	}
}