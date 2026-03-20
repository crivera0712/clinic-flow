package com.clinicflow.clinic_flow.body_region;

import com.clinicflow.clinic_flow.cases.Case;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.util.HashSet;
import java.util.Set;

@DynamicInsert
@Entity
@Getter
@Setter
@Table(name = "body_regions")
public class BodyRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "br_id")
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "bodyRegion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Case> cases = new HashSet<>();


}
