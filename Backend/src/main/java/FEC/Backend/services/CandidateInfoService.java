package FEC.Backend.services;

import FEC.Backend.models.CommitteeData;
import FEC.Backend.models.DonorReceipt;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CandidateInfoService {
    private final String apiKey = "sT5EcBtx3MYb9PURfSOW0bCiigbTk7ABmza4TiQT";
    private final RestTemplate restTemplate = new RestTemplate();
    public DonorReceipt getInfo(DonorReceipt receipt){
        String committee_id = receipt.getCommittee_id();

        if (receipt.getMemo_text() != null) {
//            System.out.println("Extracting committee ID from memo_text...");
            String extract = extractCommitteeId(receipt.getMemo_text());
            if (extract != null){
                committee_id = extract;
//                System.out.println("Extracted committee_id: " + committee_id);

            }
        }
        if (committee_id != null){
            CommitteeData committeeData = getCommittee(committee_id);
            if (committeeData != null){
                receipt.setCommittee_id(committeeData.getCommittee_id());
                System.out.println(committeeData.getCommittee_id());
                receipt.setCommittee_name(committeeData.getName());
                System.out.println(committeeData.getName());
                receipt.setParty(committeeData.getParty());
                System.out.println("Committee details updated: " + committeeData);
            }
        }
        return receipt;
    }

    /*public CommitteeData getCommittee(String committee_id) {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.open.fec.gov/v1/committee/" + committee_id)
                .queryParam("api_key", apiKey)
                .toUriString();

        // Make the GET request and capture the response
        CommitteeData committeeData = null;
        try {
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("Raw API Response: " + response);  // Log the raw response
            // Assuming the API returns a JSON structure
            committeeData = restTemplate.getForObject(url, CommitteeData.class);
        } catch (Exception e) {
            // Log the error or handle the exception (e.g., API failure)
            System.err.println("Error fetching committee data: " + e.getMessage());
        }
        System.out.println("CommitteeData after API call: " + committeeData);
        return committeeData;
    }*/


    public CommitteeData getCommittee(String committee_id) {
        // Build the URI using the committee_id and the API key
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("https://api.open.fec.gov/v1/committee/" + committee_id)
                .queryParam("api_key", apiKey);
        URI uri = uriBuilder.build().toUri();

        System.out.println("Request URL: " + uri.toString()); // Print the URL to debug

        // Fetch the data from the API
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to get committee data: " + response.getStatusCode());
        }

        // Parse the response body to extract the committee data
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode results = root.path("results");

        // Create a CommitteeData object from the JSON response
        CommitteeData committeeData = null;
        if (results.isArray() && results.size() > 0) {
            JsonNode committeeNode = results.get(0); // We assume the first result is the committee
            try {
                committeeData = mapper.treeToValue(committeeNode, CommitteeData.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Committee Data: " + committeeData); // Print the parsed committee data for debugging

        return committeeData;
    }






    private static String extractCommitteeId(String memoText) {
        // Regular expression to match text inside parentheses
        String regex = "\\(([^)]+)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(memoText);

        // If a match is found, return the text inside the parentheses
        if (matcher.find()) {
            return matcher.group(1); // matcher.group(1) contains the text inside parentheses
        }

        return null; // Return null if no parentheses are found
    }

}
