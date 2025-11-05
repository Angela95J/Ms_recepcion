package com.recepcion.recepcion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "solicitante")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitante {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "telefono", nullable = false, length = 20, unique = true)
    private String telefono;

    @Convert(converter = CanalOrigenConverter.class)
    @Column(name = "canal_origen", nullable = false, length = 50)
    private CanalOrigen canalOrigen;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    // Relación con Incidente (un solicitante puede tener muchos incidentes)
    @OneToMany(mappedBy = "solicitante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Incidente> incidentes;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}
