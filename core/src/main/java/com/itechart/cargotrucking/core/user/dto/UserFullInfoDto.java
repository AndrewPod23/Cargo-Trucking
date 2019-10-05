package com.itechart.cargotrucking.core.user.dto;

import com.itechart.cargotrucking.core.user.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class UserFullInfoDto {
    private long id;
    private String name;
    private String surname;
    private String patronymic;
    private LocalDate bornDate;
    private String town;
    private String street;
    private String house;
    private String flat;
    private String passportNum;
    private String issuedBy;
    private String login;
    private Set<UserRole> userRoles;
    private String email;
    private String password;
    private String passwordConfirm;
}
