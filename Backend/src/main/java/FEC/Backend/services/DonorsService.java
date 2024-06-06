package FEC.Backend.services;

import FEC.Backend.models.DonorReceipt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DonorsService {

    private final String apiKey = "sT5EcBtx3MYb9PURfSOW0bCiigbTk7ABmza4TiQT";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<DonorReceipt> getDonorReceiptList(String name, String city) {
        try {
            String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString());
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());

            URI uri = UriComponentsBuilder.fromHttpUrl("https://api.open.fec.gov/v1/schedules/schedule_a/")
                    .queryParam("contributor_name", encodedName)
                    .queryParam("contributor_city", encodedCity)
                    .queryParam("per_page", 100)
                    .queryParam("sort", "-contribution_receipt_date")
                    .queryParam("sort_hide_null", false)
                    .queryParam("sort_null_only", false)
                    .queryParam("api_key", apiKey)
                    .build().toUri();

            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to get donor receipts: " + response.getStatusCode());
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode results = root.path("results");

            List<DonorReceipt> donorReceipts = new ArrayList<>();
            if (results.isArray()) {
                for (JsonNode node : results) {
                    DonorReceipt receipt = mapper.treeToValue(node, DonorReceipt.class);
                    donorReceipts.add(receipt);
                }
            }
            System.out.println(donorReceipts);
            return donorReceipts;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get donor receipts", e);
        }
    }
}
