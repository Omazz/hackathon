package com.example.AlgosWeb.Controller.REST;

import com.example.AlgosWeb.Entity.Box;
import com.example.AlgosWeb.Entity.MultipartInputStreamFileResource;
import com.example.AlgosWeb.Service.BoxService;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/box/api")
public class BoxRESTController {

    @Autowired
    private BoxService operations;

    @GetMapping("/all")
    public List<Box> allBoxes(){
        return operations.allBoxes();
    }

    @GetMapping("/concrete/{id}")
    public Box concreteBox(@PathVariable("id")int id){
        return operations.concreteBox(id);
    }

    @PostMapping(value = "/save", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> saveBox(@RequestPart("box") Box box,
                       @RequestPart("image") MultipartFile image) throws IOException {
        // URL Flask‑сервиса (при условии, что он запущен на localhost:5000)
        String flaskUrl = "http://localhost:5050/predict";
        // Формируем тело запроса как multipart/form-data
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new MultipartInputStreamFileResource(image.getInputStream(), image.getOriginalFilename()));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(flaskUrl, requestEntity, Map.class);
        if(response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Boolean fragile = (Boolean) response.getBody().get("fragile");
            box.setFragile(fragile);
        } else {
            box.setFragile(false);
        }
        operations.saveBox(box);

        return ResponseEntity.ok("Данные успешно сохранены");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HttpStatus> deleteBox(@PathVariable("id")int id){
        operations.deleteBox(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
