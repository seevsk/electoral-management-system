package com.ems.backend.restcontroller;

import com.ems.backend.entity.Election;
import com.ems.backend.entity.Party;
import com.ems.backend.service.ElectionService;
import com.ems.backend.service.PartyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/election")
public class ElectionRestController {

    private final ElectionService electionService;

    public ElectionRestController(ElectionService electionService) {
        this.electionService = electionService;
    }

    /*@GetMapping
    public List<Candidate> findAll() {
        return candidateService.findAllActive();
    } */

    @GetMapping("/{id}")
    public Election findById(@PathVariable Integer id) {
        return electionService.findById(id);
    }

    // probando con paginacion
    @GetMapping
    public Page<Election> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return electionService.findAll(PageRequest.of(page, size));
    }
}
