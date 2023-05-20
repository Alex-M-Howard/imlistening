package com.alexhoward.imlistening;

import org.springframework.boot.SpringApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.messaging.*;
import com.twilio.rest.api.v2010.account.Message;


@SpringBootApplication
public class ImlisteningApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImlisteningApplication.class, args);
	}

	public static class InputData {
		private String userInput;

		public String getUserInput() {
			return userInput;
		}

		public void setUserInput(String userInput) {
			this.userInput = userInput;
		}
	}

	@RestController
	@RequestMapping("/api")
	public class FrasierController {

		@PostMapping("/send")
		public String send(@RequestBody InputData data) {
			System.out.println(data.getUserInput());
			try {
				JsonArray messages = new JsonArray();

				JsonObject systemMessage = new JsonObject();
				systemMessage.addProperty("role", "system");
				systemMessage.addProperty("content",
						"You are Frasier Crane from the TV show Frasier, and I am your brother Niles Crane. ");
				messages.add(systemMessage);

				JsonObject userMessage = new JsonObject();
				userMessage.addProperty("role", "user");
				userMessage.addProperty("content", data.getUserInput());
				messages.add(userMessage);

				JsonObject json = new JsonObject();
				json.add("messages", messages);
				json.addProperty("max_tokens", 150);
				json.addProperty("model", "gpt-3.5-turbo");

				HttpClient httpClient = HttpClients.createDefault();
				HttpPost request = new HttpPost("https://api.openai.com/v1/chat/completions");
				request.setHeader("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"));
				request.setHeader("Content-Type", "application/json");

				StringEntity requestBody = new StringEntity(json.toString(), "UTF-8");
				request.setEntity(requestBody);

				HttpResponse response = httpClient.execute(request);
				HttpEntity entity = response.getEntity();

				String responseBody = EntityUtils.toString(entity, "UTF-8");
				return responseBody;

			} catch (Exception e) {
				e.printStackTrace();
				return "Error";
			}

		}

		@PostMapping("/sms")
		public ResponseEntity<String> handleSms(@RequestParam("Body") String body,
				@RequestParam("From") String fromNumber) {
			try {
				System.out.println("Received SMS from: " + fromNumber + ", Content: " + body);

				InputData inputData = new InputData();
				inputData.setUserInput(body);

				String openAiResponse = this.send(inputData);

				SmsSender.sendSms(openAiResponse, fromNumber);

				Body bodyResponse = new Body.Builder("Your message has been processed.").build();
				com.twilio.twiml.messaging.Message sms = new com.twilio.twiml.messaging.Message.Builder()
						.body(bodyResponse)
						.build();
				MessagingResponse twiml = new MessagingResponse.Builder().message(sms).build();

				try {
					return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(twiml.toXml());
				} catch (TwiMLException e) {
					return ResponseEntity.status(500).body("Error generating TwiML.");
				}

			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.status(500).body("Error processing SMS.");
			}
		}
	
		@GetMapping("/test")
    public ResponseEntity<String> testRoute() {
        return ResponseEntity.ok("This is a test route.");
    }
	
	}
	
	public static class SmsSender {
    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    private static final String TWILIO_NUMBER = System.getenv("TWILIO_PHONE_NUMBER");

    public static void sendSms(String body, String toNumber) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message message = Message.creator(
            new PhoneNumber(toNumber),
            new PhoneNumber(TWILIO_NUMBER),
            body)
            .create();

        System.out.println("Message sent. ID: " + message.getSid());
    }
	}
}




