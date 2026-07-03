package com.ems.backend.restcontroller;

import com.ems.backend.entity.Party;
import com.ems.backend.service.PartyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parties")
public class PartyRestController {

    private final PartyService partyService;

    public PartyRestController(PartyService partyService) {
        this.partyService = partyService;
    }

    /*@GetMapping
    public List<Candidate> findAll() {
        return candidateService.findAllActive();
    } */

    @GetMapping("/{id}")
    public Party findById(@PathVariable Integer id) {
        return partyService.findById(id);
    }

    // probando con paginacion
    @GetMapping
    public Page<Party> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return partyService.findAll(PageRequest.of(page, size));
    }
}
