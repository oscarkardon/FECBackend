package FEC.Backend.services;

import FEC.Backend.models.CommitteeData;
import FEC.Backend.models.DonorReceipt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;


import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DonorsService {

    private final String apiKey = "sT5EcBtx3MYb9PURfSOW0bCiigbTk7ABmza4TiQT";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private CommitteeIdService candidateInfoService;
    @Autowired CommitteeAPIService committeeAPIService;

    public List<DonorReceipt> getDonorReceiptList(String name, String city, String state, String zipcode) {
        try {
            String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString());
            String encodedCity = city != null ? URLEncoder.encode(city, StandardCharsets.UTF_8.toString()) : "";
            String encodedState = state != null ? URLEncoder.encode(state, StandardCharsets.UTF_8.toString()) : "";
            String encodedZipcode = zipcode != null ? URLEncoder.encode(zipcode, StandardCharsets.UTF_8.toString()) : "";

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("https://api.open.fec.gov/v1/schedules/schedule_a/")
                    .queryParam("contributor_name", encodedName)
                    .queryParam("per_page", 100)
                    .queryParam("sort", "-contribution_receipt_date")
                    .queryParam("sort_hide_null", false)
                    .queryParam("sort_null_only", false)
                    .queryParam("api_key", apiKey);


            if (!encodedCity.isEmpty()) {
                uriBuilder.queryParam("contributor_city", encodedCity);
            }
            if (!encodedState.isEmpty()) {
                uriBuilder.queryParam("contributor_state", encodedState);
            }
            if (!encodedZipcode.isEmpty()) {
                uriBuilder.queryParam("contributor_zip", encodedZipcode);
            }

            URI uri = uriBuilder.build().toUri();
            System.out.println("Request URL: " + uri.toString());

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
                    receipt.setCommittee_id(candidateInfoService.getCommitteeId(receipt));
                    donorReceipts.add(receipt);
                }
            }


            //Adds the Committee info below
            //collects the unique committeeIds
            Set<String> uniqueCommitteeIds = donorReceipts.stream()
                    .map(DonorReceipt::getCommittee_id)
                    .filter(id -> id != null && !id.isEmpty())
                    .collect(Collectors.toSet());

            // Fetch committee data for unique IDs
            List<CommitteeData> committees = committeeAPIService.getCommittees(new ArrayList<>(uniqueCommitteeIds));
            // Map committee data back to donor receipts
            Map<String, CommitteeData> committeeMap = committees.stream()
                    .collect(Collectors.toMap(CommitteeData::getCommittee_id, committee -> committee));
            //sets new data
            for (DonorReceipt receipt : donorReceipts) {
                String committeeId = receipt.getCommittee_id();
                if (committeeId != null && committeeMap.containsKey(committeeId)) {
                    CommitteeData committee = committeeMap.get(committeeId);
                    receipt.setCommittee_name(committee.getName());
                    receipt.setParty(committee.getParty());
                    if (receipt.getParty() == null){
                        receipt.setParty("None");
                    }
                }
            }

            return donorReceipts;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get donor receipts", e);
        }
    }
}