package au.com.payadvantage.javawebhooksdemo;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@RestController
class WebhooksController {

  private String _webhookSecret = "Enter your secret here";

  private Logger logger = LoggerFactory.getLogger(WebhooksController.class);

  WebhooksController() {
  }

  @GetMapping("/webhooks")
  ResponseEntity<?> healthCheck() {
    return ResponseEntity.ok("Healthy");
  }

  @PostMapping("/webhooks")
  ResponseEntity<?> processWebhooks(
          @RequestHeader("x-payadvantage-signature") String signatureHeader,
          // Don't use Spring to deserialise the request. The raw request is needed to calculate the signature.
          @RequestBody String payload
  ) {
    /*
     * Webhook arming tests
     * 1: HTTP Status 202 is returned on a success.
     * 2: HTTP Status 401 is returned when the x-payadvantage-signature is hashed with the wrong secret.
     * 3: HTTP Status 401 is returned when the x-payadvantage-signature is hashed with the wrong payload.
     * 4: HTTP Status 400 is returned when the request is in the wrong format.
     */
    
    if (payload == null)
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No content.");

    try
    {
      if (signatureHeader.isEmpty())
      {
        logger.error("Invalid signature.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature.");
      }
      
      String postBodySignature = generateSignature(payload);
      if (!postBodySignature.equals(signatureHeader))
      {
        logger.error("Invalid signature.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature.");
      }
    }
    catch (Exception exception)
    {
      logger.error("Error generating signature.", exception);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.toString());
    }

    try
    {
      // WebhookEvent has @JsonProperty(required = true) attributes to ensure that the format is correct.
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
      WebhookEvent[] webhookEvents = mapper.readValue(payload, WebhookEvent[].class);

      for (WebhookEvent webhookEvent: webhookEvents) {
        logger.info("Received webhook {} {}", webhookEvent.event, webhookEvent.resourceCode);
      }
    }
    catch (Exception exception)
    {
      logger.error("Error deserializing payload.", exception);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.toString());
    }
    
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
  }

  /**
   * Generates a signature for a request payload
   * @param payload
   * @return
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   */
  private String generateSignature(String payload) throws NoSuchAlgorithmException, InvalidKeyException {
    Mac sha256_HMAC = Mac.getInstance("HmacSHA256");

    SecretKeySpec secret_key = new SecretKeySpec(_webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    sha256_HMAC.init(secret_key);

    byte[] postBodyBytes = payload.getBytes(StandardCharsets.UTF_8);
    byte[] hmacPostBody = sha256_HMAC.doFinal(postBodyBytes);

    return Base64.getEncoder().encodeToString(hmacPostBody);
  }
}