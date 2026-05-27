package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test mẫu chạy thật trên PostgreSQL 16 qua Testcontainers.
 * - Constitution Principle II (Testing Standards): integration test JPA MUST chạy
 *   trên PostgreSQL thật, không dùng H2 in-memory.
 * - Khi máy không có Docker chạy: test sẽ bị SKIP bởi disabledWithoutDocker = true.
 *   Trong CI có Docker, test sẽ chạy đầy đủ.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class EmployeeRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void saveAndFindByDepartment_roundTripsThroughRealPostgres() {
        Employee a = new Employee();
        a.setFirstName("Hảo");
        a.setLastName("Nguyễn");
        a.setDepartment("Engineering");
        a.setPosition("Backend Dev");
        employeeRepository.save(a);

        Employee b = new Employee();
        b.setFirstName("Linh");
        b.setLastName("Trần");
        b.setDepartment("Engineering");
        b.setPosition("Mobile Dev");
        employeeRepository.save(b);

        Employee c = new Employee();
        c.setFirstName("Khoa");
        c.setLastName("Phạm");
        c.setDepartment("Marketing");
        employeeRepository.save(c);

        List<Employee> engineering = employeeRepository.findByDepartment("Engineering");
        assertThat(engineering)
                .extracting(Employee::getLastName)
                .containsExactlyInAnyOrder("Nguyễn", "Trần");

        List<Employee> marketing = employeeRepository.findByDepartment("Marketing");
        assertThat(marketing).hasSize(1);
        assertThat(marketing.get(0).getFirstName()).isEqualTo("Khoa");
    }

    @Test
    void findByUser_returnsEmptyWhenNullPassed() {
        Optional<Employee> found = employeeRepository.findByUser(null);
        assertThat(found).isEmpty();
    }
}
