package com.ems.backend.restcontroller;

import com.ems.backend.entity.Candidate;
import com.ems.backend.service.CandidateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates")
public class CandidateRestController {

    private final CandidateService candidateService;

    public CandidateRestController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    /*@GetMapping
    public List<Candidate> findAll() {
        return candidateService.findAllActive();
    } */

    @GetMapping("/{id}")
    public Candidate findById(@PathVariable Integer id) {
        return candidateService.findById(id);
    }

    // probando con paginacion
    @GetMapping
    public Page<Candidate> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return candidateService.findAll(PageRequest.of(page, size));
    }
}