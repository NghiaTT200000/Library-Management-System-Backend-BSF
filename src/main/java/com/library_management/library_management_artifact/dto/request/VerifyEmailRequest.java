package com.library_management.library_management_artifact.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String code;
}