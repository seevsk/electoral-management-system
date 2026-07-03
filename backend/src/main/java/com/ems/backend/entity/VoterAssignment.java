package com.ems.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Evita errores de serialización con Hibernate
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "voter_assignment")
public class VoterAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "voter_id", nullable = false, insertable = false, updatable = false)
    private Integer voterId;

    @Column(name = "voting_table_id", nullable = false, insertable = false, updatable = false)
    private Integer votingTableId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDate assignedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private Voter voter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voting_table_id", nullable = false)
    private VotingTable votingTable;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVoterId() { return voterId; }
    public void setVoterId(Integer voterId) { this.voterId = voterId; }

    public Integer getVotingTableId() { return votingTableId; }
    public void setVotingTableId(Integer votingTableId) { this.votingTableId = votingTableId; }

    public LocalDate getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDate assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Voter getVoter() { return voter; }
    public void setVoter(Voter voter) { this.voter = voter; }

    public VotingTable getVotingTable() { return votingTable; }
    public void setVotingTable(VotingTable votingTable) { this.votingTable = votingTable; }
}
