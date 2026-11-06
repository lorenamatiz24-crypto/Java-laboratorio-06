package com.example.petmatch_lab.model;

public class Pet {

    private final long id;
    private final String name;
    private final String species;
    private final int age;
    private final String size;
    private final String description;

    public Pet(long id, String name, String species, int age, String size, String description) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.age = age;
        this.size = size;
        this.description = description;
    }

    public long getId() {
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

    public String getSize() {
        return size;
    }

    public String getDescription() {
        return description;
    }
}
