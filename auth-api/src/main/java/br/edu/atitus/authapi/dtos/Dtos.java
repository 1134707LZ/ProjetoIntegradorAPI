package br.edu.atitus.authapi.dtos;

public final class Dtos {
    private Dtos() {}

    public record SignupRequest(String name, String email, String phone, String password) {}
    public record SigninRequest(String email, String password) {}
    public record UserResponse(Long id, String name, String email, String phone, int type) {}
    public record SigninResponse(String token, UserResponse user) {}
}
