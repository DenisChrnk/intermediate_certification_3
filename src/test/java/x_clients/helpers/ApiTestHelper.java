package x_clients.helpers;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import x_clients.models.*;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static io.restassured.RestAssured.given;

public class ApiTestHelper {
    Faker faker = new Faker();

    public AuthResponse auth() {
        String appConfigPath = "src/test/resources/env.properties";

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(appConfigPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String name = properties.getProperty("api.user");
        String pass = properties.getProperty("api.pass");
        RestAssured.baseURI = properties.getProperty("baseURI");
        AuthRequest authData = new AuthRequest(name, pass);
        return given()
                .basePath("auth/login")
                .body(authData)
                .contentType(ContentType.JSON)
                .when()
                .post()
                .body().as(AuthResponse.class);
    }

    public UpdateEmployeeRequest createBodyForUpdate() {
        String lastName = faker.name().lastName();
        String email = faker.internet().emailAddress();
        String url = faker.internet().url();
        String phone = faker.phoneNumber().cellPhone();

        return new UpdateEmployeeRequest(lastName, email, url, phone, false);
    }

    public CreateEmployeeWithAllDataRequest createBodyForEmployeeWithCompanyIdAllData(int compId) {
        int id = faker.idNumber().hashCode();
        String firstName = "Bob";
        String lastName = "Bob";
        String middleName = "Bob";
        int companyId = compId;
        String email = faker.internet().emailAddress();
        String url = faker.internet().url();
        String phone = faker.phoneNumber().cellPhone();
        String birthdate = "2024-08-31T20:19:56.214Z";
        boolean isActive = faker.bool().bool();

        return new CreateEmployeeWithAllDataRequest(id, firstName, lastName, middleName, companyId, email, url, phone, birthdate, isActive);
    }

    public CreateEmployeeRequest createBodyForEmployeeWithCompanyId(int compId) {
        int id = faker.idNumber().hashCode();
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String phone = faker.phoneNumber().cellPhone();
        int companyId = compId;

        return new CreateEmployeeRequest(id, firstName, lastName, companyId, phone, true);
    }

    public CreateEmployeeResponse addEmployeeWithMyBody(CreateEmployeeRequest body) {
        String authToken = auth().userToken();

        return given()
                .basePath("employee")
                .header("x-client-token", authToken)
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .post().body().as(CreateEmployeeResponse.class);
    }
}