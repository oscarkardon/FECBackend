package FEC.Backend.services;

import FEC.Backend.models.CandidateData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class CandidateAPIService {
    private final String apiKey = "sT5EcBtx3MYb9PURfSOW0bCiigbTk7ABmza4TiQT";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<CandidateData> getCandidates(List<String> candidateIds) {
        int batchSize = 50;
        List<CandidateData> allCandidates = new ArrayList<>();

        for (int i = 0; i < candidateIds.size(); i += candidateIds.size()) {
            List<String> batch = candidateIds.subList(i, Math.min(i + batchSize, candidateIds.size()));
            String url = buildBatchUrl(batch);
            allCandidates.addAll(fetchCandidates(url));
        }

        return allCandidates;
    }

    private String buildBatchUrl(List<String> batch) {
        String baseUrl = "https://api.open.fec.gov/v1/candidates/search";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("api_key", apiKey);

        for (String id : batch) {
            builder.queryParam("candidate_id", id);
        }

        return builder.toUriString();
    }
    private List<CandidateData> fetchCandidates(String url) {
        List<CandidateData> candidates = new ArrayList<>();
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = new ObjectMapper().readTree(response).path("results");
            for (JsonNode node : root) {
                candidates.add(new ObjectMapper().treeToValue(node, CandidateData.class));
            }
        } catch (Exception e) {
            System.err.println("Error fetching committee batch: " + e.getMessage());
        }
        return candidates;
    }
}
