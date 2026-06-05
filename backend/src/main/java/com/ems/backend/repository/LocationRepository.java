package com.ems.backend.repository;

import com.ems.backend.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, String>{

    /* Aca ya tengo heredado algunos metodos "CRUD" gracias al JpaRepository
    * por el momento lo dejo asi debido a que solo lo voy a usar como fk de voters
    * */
}
