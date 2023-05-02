package br.edu.unifip.ecommerceapi.models;


import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@Table(name = ("TB_USER"))
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(nullable = false, length = 70)
    private String name;
    @Column(nullable = false)
    private int age;
    @Column(nullable = false)
    private boolean active;
}
