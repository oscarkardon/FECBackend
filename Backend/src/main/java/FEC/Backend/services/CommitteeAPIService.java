package FEC.Backend.services;

import FEC.Backend.models.CommitteeData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommitteeAPIService {
    private final String apiKey = "sT5EcBtx3MYb9PURfSOW0bCiigbTk7ABmza4TiQT";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<CommitteeData> getCommittees(List<String> committeeIds) {
        int batchSize = 50;
        List<CommitteeData> allCommittees = new ArrayList<>();

        for (int i = 0; i < committeeIds.size(); i += committeeIds.size()) {
            List<String> batch = committeeIds.subList(i, Math.min(i + batchSize, committeeIds.size()));
            String url = buildBatchUrl(batch);
            allCommittees.addAll(fetchCommittees(url));
        }

        return allCommittees;
    }

    private String buildBatchUrl(List<String> batch) {
        String baseUrl = "https://api.open.fec.gov/v1/committees/";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("api_key", apiKey);

        for (String id : batch) {
            builder.queryParam("committee_id", id);
        }

        return builder.toUriString();
    }
    private List<CommitteeData> fetchCommittees(String url) {
        List<CommitteeData> committees = new ArrayList<>();
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = new ObjectMapper().readTree(response).path("results");
            for (JsonNode node : root) {
                committees.add(new ObjectMapper().treeToValue(node, CommitteeData.class));
            }
        } catch (Exception e) {
            System.err.println("Error fetching committee batch: " + e.getMessage());
        }
        return committees;
    }


}
