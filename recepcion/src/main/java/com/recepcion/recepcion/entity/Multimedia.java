package com.recepcion.recepcion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "multimedia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Multimedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidente_id", nullable = false)
    private Incidente incidente;

    // Archivo
    @Column(name = "url_archivo", nullable = false, columnDefinition = "TEXT")
    private String urlArchivo;

    @Column(name = "url_miniatura", columnDefinition = "TEXT")
    private String urlMiniatura;

    @Column(name = "nombre_archivo", length = 255)
    private String nombreArchivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_archivo", length = 20)
    private TipoArchivo tipoArchivo;

    @Column(name = "formato_archivo", length = 10)
    private String formatoArchivo;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    // Metadatos
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "es_principal")
    private Boolean esPrincipal;

    // Estado de análisis
    @Column(name = "requiere_analisis_ml")
    private Boolean requiereAnalisisMl;

    @Column(name = "analisis_completado")
    private Boolean analisisCompletado;

    @Column(name = "fecha_subida")
    private LocalDateTime fechaSubida;

    // Relación con análisis de imagen
    @OneToOne(mappedBy = "multimedia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AnalisisMlImagen analisisImagen;

    @PrePersist
    protected void onCreate() {
        fechaSubida = LocalDateTime.now();
        if (tipoArchivo == null) {
            tipoArchivo = TipoArchivo.IMAGEN;
        }
        if (esPrincipal == null) {
            esPrincipal = false;
        }
        if (requiereAnalisisMl == null) {
            requiereAnalisisMl = true;
        }
        if (analisisCompletado == null) {
            analisisCompletado = false;
        }
    }
}
