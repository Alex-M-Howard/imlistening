package com.alexhoward.imlistening;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


@SpringBootApplication
public class ImlisteningApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImlisteningApplication.class, args);
	}

	@RestController
	@RequestMapping("/api")
	public class FrasierController {
		private HttpClient httpClient;
		private HttpPost request;

		public FrasierController() {
			httpClient = HttpClients.createDefault();
			request = new HttpPost("https://api.openai.com/v1/engines/text-davinci-003/completions");
			request.setHeader("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"));
			request.setHeader("Content-Type", "application/json");
			System.out.println(System.getenv("Frasier"));
		}

		

		@GetMapping("/send")
        public String send() {
            try {
                StringEntity requestBody = new StringEntity("{\"prompt\": \"Frasier: \",\"max_tokens\":5}", "UTF-8");
                request.setEntity(requestBody);
                HttpResponse response = httpClient.execute(request);

                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity, "UTF-8");
								System.out.println(responseBody);
								System.out.println(System.getenv("OPENAI_API_KEY"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Hello World";
        }

	}

}



