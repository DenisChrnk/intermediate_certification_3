package x_clients.models;

public record UpdateEmployeeRequest(String lastName, String email, String url, String phone, boolean isActive) {
}