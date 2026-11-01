package com.metallurg.calculator.model;

/** Единица оборудования витрины. */
public class Equipment {

    private final int id;
    private final String name;
    private final String type;
    private final Double capacityT;
    private final String description;
    private final String manufacturer;

    public Equipment(int id, String name, String type, Double capacityT,
                     String description, String manufacturer) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacityT = capacityT;
        this.description = description;
        this.manufacturer = manufacturer;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Double getCapacityT() {
        return capacityT;
    }

    public String getDescription() {
        return description;
    }

    public String getManufacturer() {
        return manufacturer;
    }
}
