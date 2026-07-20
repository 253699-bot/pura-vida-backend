package com.puravida.modules.businessconfiguration.infrastructure.persistence;

import com.puravida.modules.businessconfiguration.domain.model.BusinessConfiguration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "CONFIGURACION_NEGOCIO")
public class BusinessConfigurationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_config")
    private Integer id;

    @Column(name = "Singleton_key", nullable = false, unique = true)
    private Byte singletonKey;

    @Column(name = "Nombre_fonda", nullable = false, length = 150)
    private String nombreFonda;

    @Column(name = "Logo_url", columnDefinition = "TEXT")
    private String logoKey;

    @Column(name = "Direccion", columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "Horarios", columnDefinition = "TEXT")
    private String horarios;

    @Column(name = "Telefono", length = 20)
    private String telefono;

    @Column(name = "Correo", length = 255)
    private String correo;

    @Column(name = "Actualizado_por")
    private Integer actualizadoPor;

    @Column(name = "Actualizado_en")
    private LocalDateTime actualizadoEn;

    protected BusinessConfigurationEntity() {
    }

    private BusinessConfigurationEntity(
            Integer id,
            Byte singletonKey,
            String nombreFonda,
            String logoKey,
            String direccion,
            String horarios,
            String telefono,
            String correo,
            Integer actualizadoPor,
            LocalDateTime actualizadoEn
    ) {
        this.id = id;
        this.singletonKey = singletonKey;
        this.nombreFonda = nombreFonda;
        this.logoKey = logoKey;
        this.direccion = direccion;
        this.horarios = horarios;
        this.telefono = telefono;
        this.correo = correo;
        this.actualizadoPor = actualizadoPor;
        this.actualizadoEn = actualizadoEn;
    }

    public static BusinessConfigurationEntity fromDomain(BusinessConfiguration configuration) {
        return new BusinessConfigurationEntity(
                configuration.id(), (byte) 1, configuration.nombreFonda(), configuration.logoKey(),
                configuration.direccion(), configuration.horarios(), configuration.telefono(),
                configuration.correo(), configuration.actualizadoPor(), configuration.actualizadoEn()
        );
    }

    public BusinessConfiguration toDomain() {
        return new BusinessConfiguration(
                id, nombreFonda, logoKey, direccion, horarios, telefono, correo,
                actualizadoPor, actualizadoEn
        );
    }
}
