package com.gydronium.storedemo.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "user_model")
public class UserModel {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
}
