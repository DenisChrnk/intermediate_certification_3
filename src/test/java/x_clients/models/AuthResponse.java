package x_clients.models;

public record AuthResponse(String userToken, String role, String displayName, String login) {
}