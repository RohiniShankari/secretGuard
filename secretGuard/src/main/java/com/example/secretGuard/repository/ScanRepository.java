package com.example.secretGuard.repository;



import com.example.secretGuard.entity.Scan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanRepository extends JpaRepository<Scan, Long> {

}