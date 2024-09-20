package com.z0736190100.cardcashian;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.z0736190100.cardcashian.model.CashCard;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CardcashianApplicationTests {

    // TODO to have naming convention for variables
    // eg. matching expected values from resource files to have EXPECTED_ in name

    // GOOD VALUES
    public static final int EXPECTED_ID = 99;
    public static final double EXPECTED_AMOUNT = 123.45;
    public static final String CASHCARDS_URL = "/cashcards";
    public static final String $_ID = "$.id";
    public static final String $_AMOUNT = "$.amount";
    // BAD VALUES
    public static final String UNKNOWN_ID = "1000";

    @Autowired
    TestRestTemplate restTemplate;

    private String buildCashCardsUri(String idParam){
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString(CASHCARDS_URL + "/" + "{id}")
                .encode()
                .build();

        return uriComponents.expand(idParam).toUri().toString();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldReturnOkOnExistingCashCardRequest() {
        String url = buildCashCardsUri(String.valueOf(EXPECTED_ID));

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    void shouldReturnCorrectExistingCashCard() {
        String url = buildCashCardsUri(String.valueOf(EXPECTED_ID));

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        DocumentContext realRespJson = JsonPath.parse(response.getBody());
        Number realId = realRespJson.read($_ID);
        Number realAmount = realRespJson.read($_AMOUNT);

        assertThat(realId).isEqualTo(EXPECTED_ID);
        assertThat(realAmount).isEqualTo(EXPECTED_AMOUNT);
    }

    @Test
    void shouldReturnNotNullWhenRequestedDataExist() {
        String url = buildCashCardsUri(String.valueOf(EXPECTED_ID));

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        DocumentContext realRespJson = JsonPath.parse(response.getBody());

        assertThat(realRespJson).isNotNull();
    }

    @Test
    @DirtiesContext
    void shouldReturnCreatedOnCreateANewCashCard() {
        CashCard newCashCard = new CashCard(null, 250.00);

        ResponseEntity<Void> createResponse = restTemplate.postForEntity(CASHCARDS_URL, newCashCard, Void.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DirtiesContext
    void shouldHaveLocationHeaderInResponseOnNewCashCardCreated() {
        CashCard newCashCard = new CashCard(null, 250.00);

        ResponseEntity<Void> createResponse = restTemplate.postForEntity(CASHCARDS_URL, newCashCard, Void.class);

        URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
        assertThat(locationOfNewCashCard).isNotNull();

        ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DirtiesContext
    void shouldReturnOkOnLocationGetForCashCardCreated() {
        CashCard newCashCard = new CashCard(null, 250.00);

        ResponseEntity<Void> createResponse = restTemplate.postForEntity(CASHCARDS_URL, newCashCard, Void.class);
        URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // todo refactor all below to meet AAA:
    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        String url = buildCashCardsUri(UNKNOWN_ID);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    @Disabled("FIXME SMTH WITH CONTEXT IS WRONG")
    @Test
    @DirtiesContext
    void shouldReturnAllCashCardsWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int cashCardCount = documentContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.00, 150.00);
    }

    @Test
    void shouldReturnAPageOfCashCards() {
        String pageAndSizeUrl = "/cashcards?page=0&size=1";
        ResponseEntity<String> response = restTemplate.getForEntity(pageAndSizeUrl, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnASortedPageOfCashCards() {
        String pageAndSizeAndSortUrl = "/cashcards?page=0&size=1&sort=amount,asc";
        ResponseEntity<String> response = restTemplate.getForEntity(pageAndSizeAndSortUrl, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");
        assertThat(read.size()).isEqualTo(1);

        double amount = documentContext.read("$[0].amount");
        assertThat(amount).isEqualTo(150.00);
    }

}
