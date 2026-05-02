package io.github.graceman.clinic.entity;

import io.github.graceman.alicemvc.annotation.SearchField;
import io.github.graceman.alicemvc.dto.Identifiable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "departments")
public class Department implements Identifiable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @SearchField(operator = SearchField.Operator.LIKE)
    private String name;

    @SearchField(operator = SearchField.Operator.LIKE)
    private String description;

    @Override
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
