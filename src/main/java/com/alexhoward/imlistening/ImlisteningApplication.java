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
import com.google.gson.Gson;

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

	public String askChatGpt(String data) {
			
			try {
				JsonArray messages = new JsonArray();

				JsonObject systemMessage = new JsonObject();
				systemMessage.addProperty("role", "system");
				systemMessage.addProperty("content",
						"You are Frasier Crane from the TV show Frasier, who hams up and makes every answer as dramatic as possible, and I am your brother Niles Crane. ");
				messages.add(systemMessage);

				JsonObject userMessage = new JsonObject();
				userMessage.addProperty("role", "user");
				userMessage.addProperty("content", data);
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
				
				JsonObject jsonResponse = new Gson().fromJson(responseBody, JsonObject.class);
				String answer = jsonResponse
					.getAsJsonArray("choices")
					.get(0)
					.getAsJsonObject()
					.getAsJsonObject("message")
					.get("content")
					.getAsString();


				return answer;

			} catch (Exception e) {
				e.printStackTrace();
				return "Error";
			}

		}


	@RestController
	@RequestMapping("/webhook")
	public class WebhookController {

		@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
		public ResponseEntity<String> handleWebhook(
				@RequestParam("Body") String body,
				@RequestParam("From") String fromNumber) {

			System.out.println("Received SMS from: " + fromNumber + ", Content: " + body);
			String response = askChatGpt(body);
			System.out.println("Response: " + response);

			return ResponseEntity.ok(response);
		}
	}
}