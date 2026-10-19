package com.meupersonal.model.user;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class User {

    private String nome;
    private String email;
    private boolean ativo = true;
}
