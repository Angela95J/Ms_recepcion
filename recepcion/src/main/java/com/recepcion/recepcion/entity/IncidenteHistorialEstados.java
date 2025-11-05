package com.recepcion.recepcion.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "incidente_historial_estados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenteHistorialEstados {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidente_id", nullable = false)
    private Incidente incidente;

    @Column(name = "estado_anterior", length = 30)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 30)
    private String estadoNuevo;

    @Column(name = "usuario_cambio", length = 100)
    private String usuarioCambio;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "fecha_cambio")
    private LocalDateTime fechaCambio;

    @PrePersist
    protected void onCreate() {
        fechaCambio = LocalDateTime.now();
        if (usuarioCambio == null) {
            usuarioCambio = "SISTEMA";
        }
    }
}
