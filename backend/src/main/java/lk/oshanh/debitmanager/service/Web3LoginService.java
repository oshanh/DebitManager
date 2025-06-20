package lk.oshanh.debitmanager.service;

import lk.oshanh.debitmanager.dto.LoginResponseDTO;
import lk.oshanh.debitmanager.dto.Web3LoginRequest;
import lk.oshanh.debitmanager.entity.User;
import lk.oshanh.debitmanager.repository.UserRepository;
import lk.oshanh.debitmanager.utils.Web3Verifier;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;



@Service
@RequiredArgsConstructor
public class Web3LoginService {
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(Web3LoginService.class);

    public boolean verifySignature(Web3LoginRequest request) {
        String address = request.getAddress();
        String signature = request.getSignature();
        String message = request.getMessage();
        return Web3Verifier.verifySignature(address, message, signature);
    }

    public boolean verifyWithNode(Web3LoginRequest request){
        String address = request.getAddress();
        String signature = request.getSignature();
        String message = request.getMessage();

        try {
            // Prepare request payload for the verifier Node.js microservice
            Map<String, String> payload = new HashMap<>();
            payload.put("address", address);
            payload.put("message", message);
            payload.put("signature", signature);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);
            RestTemplate restTemplate = new RestTemplate();

            // Assuming your Node.js verifier runs on port 3001
            String verifierUrl = "http://localhost:3001/api/verify";

            ResponseEntity<Map> response = restTemplate.exchange(
                    verifierUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            boolean isValid = responseBody != null && Boolean.TRUE.equals(responseBody.get("valid"));
            logger.info("Verification validity: {}", isValid);

            if (isValid) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public LoginResponseDTO loginOrRegisterWeb3User(Web3LoginRequest request){
        // Create or get user
        User user = userRepository.findByAddress(request.getAddress()).orElseGet(() -> {
            User newUser = new User();
            newUser.setAddress(request.getAddress());
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setNickname("User_" + request.getAddress().substring(2, 6));
            newUser.setEmail(null);
            return newUser;
        });

        User savedUser = userRepository.save(user);

        LoginResponseDTO responseDTO=new LoginResponseDTO();
        responseDTO.setSuccess(true);
        responseDTO.setNickname(savedUser.getNickname());
        responseDTO.setId(savedUser.getUid());

        logger.info("Login successful for user: {}, success: {}", responseDTO.getNickname(), responseDTO.isSuccess());

        return responseDTO;
    }




}
