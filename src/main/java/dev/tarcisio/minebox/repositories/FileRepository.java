package dev.tarcisio.minebox.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.tarcisio.minebox.entities.File;

@Repository
public interface FileRepository extends JpaRepository<File, String> {
  List<File> findAllByUserId(String userId);
}
