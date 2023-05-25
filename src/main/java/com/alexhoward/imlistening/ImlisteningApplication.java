package com.alexhoward.imlistening;

import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpHeaders;
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
import java.util.Random;

@SpringBootApplication
public class ImlisteningApplication {

		private static final String[] FRASIER_QUOTES = {
				"Frasier has left the building, but fear not, he's merely contemplating a cure for this conundrum. Please try again.",
				"My apologies, but it seems your inquiry has slipped into radio silence. Much like my dear brother Niles around a particularly pungent camembert. Do give it another whirl, won't you?",
				"Apologies, my dear user. This is more confounding than the third act of a Tchaikovsky opera. I'll be right back after a brief intermission. Kindly rephrase or try again.",
				"Ah, the plot thickens, much like my famous béchamel sauce. I'm afraid your request is currently in a state of existential crisis. Please try again."
		};

		private static final Random RANDOM = new Random();

		public static String getQuote() {
				int index = RANDOM.nextInt(FRASIER_QUOTES.length);
				return FRASIER_QUOTES[index];
		}


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
				json.addProperty("max_tokens", 250);
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

				JsonArray choicesArray = jsonResponse.getAsJsonArray("choices");

				if (choicesArray != null && choicesArray.size() > 0) {
					JsonObject firstChoice = choicesArray.get(0).getAsJsonObject();
					JsonObject message = firstChoice.getAsJsonObject("message");

					if (message != null && message.has("content")) {
						String answer = message.get("content").getAsString();
						return answer;
					}
				}
						

            return getQuote();

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
	
	@RestController
	@RequestMapping("/webhookError")
	public class WebhookErrorController {

	    private static final String[] FRASIER_QUOTES = {
		"As we speak, hordes of viral Visigoths are hurling themselves over the battlements of my immune system, laying waste to my... Oh, dear God, you see how weak I am? I can't even finish a simple Visigoth metaphor. Please try again later.",
		"My apologies, but it seems your inquiry has slipped into radio silence. Much like my dear brother Niles around a particularly pungent camembert. Do give it another whirl, won't you?",
		"Apologies, my dear user. This is more confounding than the third act of a Tchaikovsky opera. I'll be right back after a brief intermission. Kindly rephrase or try again.",
		"Ah, the plot thickens, much like my famous béchamel sauce. I'm afraid your request is currently in a state of existential crisis. Please try again."
	    };

	    private static final Random RANDOM = new Random();

	    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	    public ResponseEntity<String> handleWebhookError() {
	        int index = RANDOM.nextInt(FRASIER_QUOTES.length);
		String quote = FRASIER_QUOTES[index];

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);

		return ResponseEntity.ok().headers(headers).body(quote);
            }
    }
}
