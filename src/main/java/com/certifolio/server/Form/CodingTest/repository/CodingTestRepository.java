package com.certifolio.server.Form.CodingTest.repository;

import com.certifolio.server.Form.CodingTest.domain.CodingTest;
import com.certifolio.server.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CodingTestRepository extends JpaRepository<CodingTest, Long> {
    Optional<CodingTest> findByUser(User user);
    Optional<CodingTest> findByBojHandle(String bojHandle);
}
