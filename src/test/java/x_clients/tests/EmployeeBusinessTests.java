package x_clients.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import x_clients.extendClasses.*;
import x_clients.helpers.ApiTestHelper;
import x_clients.helpers.DbHelper;
import x_clients.models.CreateEmployeeRequest;
import x_clients.models.EmployeeDb;
import x_clients.models.UpdateEmployeeRequest;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static x_clients.helpers.DataForTests.INVALID_ID;

@ExtendWith({ApiTestHelperResolver.class, AuthResolver.class, DbHelperResolver.class, InsertCompanyResolver.class})
public class EmployeeBusinessTests {

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
    public void getEmployeesOfCompany(int companyId, DbHelper dbHelper) throws SQLException {
        String employeeIdOne = String.valueOf(dbHelper.insertEmployeeToCompany(companyId));
        String employeeIdTwo = String.valueOf(dbHelper.insertEmployeeToCompany(companyId));

        given()
                .basePath("employee")
                .when()
                .get("?company=" + companyId)
                .then()
                .body(containsString(employeeIdOne))
                .and()
                .body(containsString(employeeIdTwo));
    }

    @Test
    @DisplayName("Добавление нового сотрудника в компанию")
    public void addNewEmployee(int companyId, ApiTestHelper apiHelper, DbHelper dbHelper) throws SQLException {
        CreateEmployeeRequest employeeInfo = apiHelper.createBodyForEmployeeWithCompanyId(companyId);
        String employeeName = employeeInfo.firstName();

        int employeeId = apiHelper.addEmployeeWithMyBody(employeeInfo).id();

        EmployeeDb dbEmployeeInfo = dbHelper.selectEmployeeById(employeeId);
        int compIdForAssert = dbEmployeeInfo.companyId();
        String employeeNameForAssert = dbEmployeeInfo.firstName();
        assertEquals(employeeName, employeeNameForAssert);
        assertEquals(companyId, compIdForAssert);
    }

    @Test
    @DisplayName("Добавление нового сотрудника в несуществующую компанию")
    public void addNewEmployeeToInvalidCompany(ApiTestHelper apiHelper, DbHelper dbHelper) throws SQLException {
        CreateEmployeeRequest employeeInfo = apiHelper.createBodyForEmployeeWithCompanyId(INVALID_ID);
        apiHelper.addEmployeeWithMyBody(employeeInfo);

        int quantityEmployees = dbHelper.selectQuantityEmployeesOfComp(INVALID_ID);
        assertEquals(quantityEmployees, 0);
    }

    @Test
    @DisplayName("Добавление одного и того же сотрудника два раза в одну компанию")
    public void addEmployeeTwice(int companyId, ApiTestHelper apiHelper, DbHelper dbHelper) throws SQLException {
        CreateEmployeeRequest employeeBody = apiHelper.createBodyForEmployeeWithCompanyId(companyId);
        String firstName = employeeBody.firstName();
        apiHelper.addEmployeeWithMyBody(employeeBody);
        apiHelper.addEmployeeWithMyBody(employeeBody);
        int quantityEmployees = dbHelper.selectQuantityEmployeesOfComp(companyId);

        assertEquals(quantityEmployees, 2);

        ResultSet result = dbHelper.selectSetEmployeesByCompanyId(companyId);
        while (result.next()) {
            assertEquals(firstName, result.getString("first_name"));
        }
    }

    @Test
    @DisplayName("Получить сотрудника по id")
    public void getEmployeeById(int companyId, DbHelper dbHelper) throws SQLException {
        String employeeId = String.valueOf(dbHelper.insertEmployeeToCompany(companyId));

        given()
                .basePath("employee")
                .when()
                .get("/" + employeeId)
                .then()
                .body(containsString(employeeId));
    }

    @Test
    @DisplayName("Изменение данных сотрудника. Тело запроса по схеме")
    public void updateEmployeeInfo(int companyId, ApiTestHelper apiHelper, String authToken, DbHelper dbHelper) throws SQLException {
        int employeeId = dbHelper.insertEmployeeToCompany(companyId);
        EmployeeDb employeeInfo = dbHelper.selectEmployeeById(employeeId);
        UpdateEmployeeRequest newInfoForEmployee = apiHelper.createBodyForUpdate();

        given()
                .basePath("employee")
                .header("x-client-token", authToken)
                .body(newInfoForEmployee)
                .contentType(ContentType.JSON)
                .when()
                .patch("/" + employeeId);

        EmployeeDb updatedEmployeeInfo = dbHelper.selectEmployeeById(employeeId);

        assertNotEquals(employeeInfo.lastName(), updatedEmployeeInfo.lastName());
        assertEquals(newInfoForEmployee.lastName(), updatedEmployeeInfo.lastName());
        assertEquals(employeeInfo.firstName(), updatedEmployeeInfo.firstName());
    }

    @Test
    @DisplayName("Изменение данных сотрудника. Тело запроса не по схеме")
    public void updateEmployeeInfoWithoutSchema(int companyId, ApiTestHelper apiHelper, String authToken, DbHelper dbHelper) throws SQLException {
        int employeeId = dbHelper.insertEmployeeToCompany(companyId);
        EmployeeDb employeeInfo = dbHelper.selectEmployeeById(employeeId);
        CreateEmployeeRequest newInfoForEmployee = apiHelper.createBodyForEmployeeWithCompanyId(companyId);

        given()
                .basePath("employee")
                .header("x-client-token", authToken)
                .body(newInfoForEmployee)
                .contentType(ContentType.JSON)
                .when()
                .patch("/" + employeeId);

        EmployeeDb updatedEmployeeInfo = dbHelper.selectEmployeeById(employeeId);

        assertNotEquals(employeeInfo.lastName(), updatedEmployeeInfo.lastName());
        assertEquals(newInfoForEmployee.lastName(), updatedEmployeeInfo.lastName());
        assertEquals(employeeInfo.firstName(), updatedEmployeeInfo.firstName());
    }
}