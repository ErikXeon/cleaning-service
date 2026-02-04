package com.konalyan.cleaning.cleaning_service.mapper;

import com.konalyan.cleaning.cleaning_service.dto.CreateUserRequest;
import com.konalyan.cleaning.cleaning_service.dto.UserResponse;
import com.konalyan.cleaning.cleaning_service.entity.Role;
import com.konalyan.cleaning.cleaning_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface UserMapper {
    default User toEntity(UserResponse userResponse) {
        User user = new User();
        user.setId(userResponse.id());
        user.setEmail(userResponse.email());
        user.setFirstName(userResponse.firstName());
        user.setLastName(userResponse.lastName());
        user.setEnabled(userResponse.enabled());
        // роли нужно будет добавить вручную через сервис
        return user;
    }

    default UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getEnabled(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }

    User toEntity(CreateUserRequest createUserRequest);

    CreateUserRequest toCreateUserRequest(User user);
}