package com.albany.restapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CustomerProfiles")
public class CustomerProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerId;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String street;
    
    private String city;
    
    private String state;
    
    private String postalCode;
    
    @Column(name = "total_services")
    private Integer totalServices = 0;
    
    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;
    
    @Column(name = "membership_status")
    @Enumerated(EnumType.STRING)
    private MembershipStatus membershipStatus = MembershipStatus.Standard;
    
    // Getter for formatted membership status string
    public String getMembershipStatusString() {
        return membershipStatus != null ? membershipStatus.name() : "Standard";
    }
    
    // Setter that handles string values for membership status
    public void setMembershipStatus(String status) {
        if (status == null || status.isEmpty()) {
            this.membershipStatus = MembershipStatus.Standard;
        } else {
            try {
                this.membershipStatus = MembershipStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                // Default to Standard if invalid value
                this.membershipStatus = MembershipStatus.Standard;
            }
        }
    }
    
    // Enum for membership status
    public enum MembershipStatus {
        Standard,
        Premium
    }
}