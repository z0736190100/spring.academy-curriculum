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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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

    private String buildCashCardsUri(String idParam) {
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

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    void shouldReturnCorrectExistingCashCard() {
        String url = buildCashCardsUri(String.valueOf(EXPECTED_ID));

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity(url, String.class);
        DocumentContext realRespJson = JsonPath.parse(response.getBody());
        Number realId = realRespJson.read($_ID);
        Number realAmount = realRespJson.read($_AMOUNT);

        assertThat(realId).isEqualTo(EXPECTED_ID);
        assertThat(realAmount).isEqualTo(EXPECTED_AMOUNT);
    }

    @Test
    void shouldReturnNotNullWhenRequestedDataExist() {
        String url = buildCashCardsUri(String.valueOf(EXPECTED_ID));

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity(url, String.class);
        DocumentContext realRespJson = JsonPath.parse(response.getBody());

        assertThat(realRespJson).isNotNull();
    }

    @Test
    @DirtiesContext
    void shouldReturnCreatedOnCreateANewCashCard() {
        CashCard newCashCard = new CashCard(null, 250.00, "sarah1");

        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .postForEntity(CASHCARDS_URL, newCashCard, Void.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DirtiesContext
    void shouldHaveLocationHeaderInResponseOnNewCashCardCreated() {
        CashCard newCashCard = new CashCard(null, 250.00, "sarah1");

        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .postForEntity(CASHCARDS_URL, newCashCard, Void.class);

        URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
        assertThat(locationOfNewCashCard).isNotNull();

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity(locationOfNewCashCard, String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DirtiesContext
    void shouldReturnOkOnLocationGetForCashCardCreated() {
        CashCard newCashCard = new CashCard(null, 250.00, "sarah1");

        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .postForEntity(CASHCARDS_URL, newCashCard, Void.class);
        URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity(locationOfNewCashCard, String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // todo refactor all below to meet AAA:
    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        String url = buildCashCardsUri(UNKNOWN_ID);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "abc123").getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    @Disabled("FIXME SMTH WITH CONTEXT IS WRONG")
    @Test
    @DirtiesContext
    void shouldReturnAllCashCardsWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "abc123").getForEntity("/cashcards", String.class);
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
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "abc123").getForEntity(pageAndSizeUrl, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnASortedPageOfCashCards() {
        String pageAndSizeAndSortUrl = "/cashcards?page=0&size=1&sort=amount,asc";
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "abc123").getForEntity(pageAndSizeAndSortUrl, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");
        assertThat(read.size()).isEqualTo(1);

        double amount = documentContext.read("$[0].amount");
        assertThat(amount).isEqualTo(1.00);
    }

    @Test
    void shouldNotReturnACashCardWhenUsingBadCredentials() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("BAD-USER", "abc123")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        response = restTemplate
                .withBasicAuth("sarah1", "BAD-PASSWORD")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUsersWhoAreNotCardOwners() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("hank-owns-no-cards", "qrs456")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/102", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldUpdateAnExistingCashCard() {
        CashCard cashCardUpdate = new CashCard(null, 19.99, null);
        HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .exchange("/cashcards/99", HttpMethod.PUT, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/99", String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        Double amount = documentContext.read("$.amount");

        assertThat(id).isEqualTo(99);
        assertThat(amount).isEqualTo(19.99);
    }

    @Test
    void shouldNotUpdateACashCardThatDoesNotExist() {
        CashCard unknownCard = new CashCard(null, 19.99, null);
        HttpEntity<CashCard> request = new HttpEntity<>(unknownCard);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .exchange("/cashcards/99999", HttpMethod.PUT, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotUpdateACashCardThatIsOwnedBySomeoneElse() {
        CashCard kumarsCard = new CashCard(null, 333.33, null);
        HttpEntity<CashCard> request = new HttpEntity<>(kumarsCard);

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .exchange("/cashcards/102", HttpMethod.PUT, request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
