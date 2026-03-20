package com.qureka.global.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private final Long   id;
    private final String userid;
    private final String name;
}
