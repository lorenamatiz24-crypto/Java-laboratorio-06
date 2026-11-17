package com.example.petmatch_lab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 80)
    private String species;

    @Column(nullable = false)
    private int age;

    @Column(length = 1000)
    private String description;

    protected Pet() {
    }

    public Pet(String name, String species, int age, String description) {
        this.name = name;
        this.species = species;
        this.age = age;
        this.description = description;
    }

    public void updateDetails(
        String name,
        String species,
        int age,
        String description
    ) {
        this.name = name;
        this.species = species;
        this.age = age;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecies() {
        return species;
    }

    public int getAge() {
        return age;
    }

    public String getDescription() {
        return description;
    }
}
