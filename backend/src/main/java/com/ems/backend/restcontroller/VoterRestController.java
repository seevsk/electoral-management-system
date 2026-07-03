package com.ems.backend.restcontroller;

import com.ems.backend.entity.Voter;
import com.ems.backend.service.VoterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/voters")
public class VoterRestController {

    private final VoterService voterService;

    public VoterRestController(VoterService voterService) {
        this.voterService = voterService;
    }

    /*@GetMapping
    public List<Candidate> findAll() {
        return candidateService.findAllActive();
    } */

    @GetMapping("/{id}")
    public Voter findById(@PathVariable Integer id) {
        return voterService.findById(id);
    }

    // probando con paginacion
    @GetMapping
    public Page<Voter> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return voterService.findAll(PageRequest.of(page, size));
    }

}
