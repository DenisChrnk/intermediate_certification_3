package x_clients.models;

public record CreateEmployeeRequest(int id, String firstName, String lastName, int companyId, String phone, boolean isActive){
}