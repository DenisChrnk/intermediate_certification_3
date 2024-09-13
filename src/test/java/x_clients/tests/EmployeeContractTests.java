package x_clients.tests;

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import x_clients.extendClasses.*;
import x_clients.helpers.ApiTestHelper;
import x_clients.helpers.DbHelper;
import x_clients.models.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static x_clients.helpers.DataForTests.INVALID_ID;

@ExtendWith({ApiTestHelperResolver.class, ValidationFilterResolver.class, AuthResolver.class, DbHelperResolver.class, InsertCompanyResolver.class, DivideExceptionHandler.class})
public class EmployeeContractTests {

    @BeforeAll
    public static void setUp() throws IOException {
        String appConfigPath = "src/test/resources/env.properties";

        Properties properties = new Properties();
        properties.load(new FileInputStream(appConfigPath));

        RestAssured.baseURI = properties.getProperty("baseURI");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("Получение списка сотрудников для компании")
    public void getEmployeesOfCompany(int companyId, DbHelper dbHelper, OpenApiValidationFilter openApiValidationFilter) throws SQLException {
        dbHelper.insertEmployeeWithAllData(companyId);

        given()
                .filter(openApiValidationFilter)
                .basePath("employee")
                .when()
                .get("?company=" + companyId);
    }

    @Test
    @DisplayName("Получение списка сотрудников для несуществующей компании")
    public void getEmployeesOfCompanyWithInvalidId(OpenApiValidationFilter openApiValidationFilter) {
        given()
                .filter(openApiValidationFilter)
                .basePath("employee")
                .when()
                .get("?company=" + INVALID_ID);
    }

    @Test
    @DisplayName("Добавление нового сотрудника с минимальным набором полей")
    public void addNewEmployee(int companyID, ApiTestHelper apiHelper, String authToken, OpenApiValidationFilter openApiValidationFilter) {
        CreateEmployeeRequest body = apiHelper.createBodyForEmployeeWithCompanyId(companyID);

        given()
                .filter(openApiValidationFilter)
                .basePath("employee")
                .header("x-client-token", authToken)
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .post();
    }

    @Test
    @DisplayName("Добавление нового сотрудника со всеми полями по схеме")
    public void addNewEmployeeWithAllData(int companyId, ApiTestHelper apiHelper, String authToken, OpenApiValidationFilter openApiValidationFilter) {
        CreateEmployeeWithAllDataRequest body = apiHelper.createBodyForEmployeeWithCompanyIdAllData(companyId);

        given()
                .filter(openApiValidationFilter)
                .basePath("employee")
                .header("x-client-token", authToken)
                .body(body)
                .contentType(ContentType.JSON)
                .when()
                .post();
    }

    @Test
    @DisplayName("Получение сотрудника по Id")
    public void getEmployeeById(int companyId, DbHelper dbHelper, OpenApiValidationFilter openApiValidationFilter) throws SQLException {
        int employeeId = dbHelper.insertEmployeeWithAllData(companyId);

        given()
                .filter(openApiValidationFilter)
                .basePath("employee")
                .when()
                .get("/" + employeeId);
    }

    @Test
    @DisplayName("Получение сотрудника по несуществующему Id")
    public void getEmployeeByInvalidId(OpenApiValidationFilter openApiValidationFilter) {
        given()
                .filter(openApiValidationFilter)
                .basePath("employee")
                .when()
                .get("/" + INVALID_ID);
    }

    @Test
    @DisplayName("Изменить информацию о сотруднике. Тело запроса согласно схеме")
    public void updateEmployeeInfo(int companyId, DbHelper dbHelper, ApiTestHelper apiHelper, String authToken, OpenApiValidationFilter openApiValidationFilter) throws SQLException {
        int employeeId = dbHelper.insertEmployeeToCompany(companyId);
        UpdateEmployeeRequest newInfoForEmployee = apiHelper.createBodyForUpdate();

        given()
                .filter(openApiValidationFilter)
                .basePath("employee")
                .header("x-client-token", authToken)
                .body(newInfoForEmployee)
                .contentType(ContentType.JSON)
                .when()
                .patch("/" + employeeId);
    }

    @Test
    @DisplayName("Изменить информацию о сотруднике. Тело запроса не по схеме")
    public void updateEmployeeInfoWithoutSchema(int companyId, DbHelper dbHelper, ApiTestHelper apiHelper, String authToken, OpenApiValidationFilter openApiValidationFilter) throws SQLException {
        int employeeId = dbHelper.insertEmployeeToCompany(companyId);
        CreateEmployeeRequest newInfoForEmployee = apiHelper.createBodyForEmployeeWithCompanyId(companyId);

        given()
                .filter(openApiValidationFilter)
                .basePath("employee")
                .header("x-client-token", authToken)
                .body(newInfoForEmployee)
                .contentType(ContentType.JSON)
                .when()
                .patch("/" + employeeId);
    }

    @Test
    @DisplayName("Изменить информацию несуществующему сотруднику")
    public void updateInvalidEmployeeInfo(ApiTestHelper apiHelper, String authToken, OpenApiValidationFilter openApiValidationFilter) {
        UpdateEmployeeRequest newInfoForEmployee = apiHelper.createBodyForUpdate();

        given()
                .filter(openApiValidationFilter)
                .basePath("employee")
                .header("x-client-token", authToken)
                .body(newInfoForEmployee)
                .contentType(ContentType.JSON)
                .when()
                .patch("/" + INVALID_ID);
    }
}
