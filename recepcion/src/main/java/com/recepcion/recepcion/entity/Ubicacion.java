package com.recepcion.recepcion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ubicacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "descripcion_textual", nullable = false, columnDefinition = "TEXT")
    private String descripcionTextual;

    @Column(name = "referencia", columnDefinition = "TEXT")
    private String referencia;

    @Column(name = "latitud", precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 11, scale = 8)
    private BigDecimal longitud;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "distrito", length = 100)
    private String distrito;

    @Column(name = "zona", length = 100)
    private String zona;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // Relación con Incidente
    @OneToMany(mappedBy = "ubicacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Incidente> incidentes;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (ciudad == null) {
            ciudad = "Santa Cruz de la Sierra";
        }
    }
}
