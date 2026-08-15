package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.entity.EmployeeFace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeFaceRepository extends JpaRepository<EmployeeFace, Long> {

    Optional<EmployeeFace> findByEmployee(Employee employee);

    Optional<EmployeeFace> findByEmployeeEmployeeId(Long employeeId);

    boolean existsByEmployeeEmployeeId(Long employeeId);

    /**
     * Nạp kèm employee để tránh N+1 query khi so khớp 1:N (duyệt toàn bộ người đã đăng ký).
     * Với quy mô đồ án (vài chục nhân viên) thì duyệt hết là đủ nhanh; nếu lên hàng nghìn
     * người thì phải đổi sang chỉ mục vector (pgvector) thay vì so tuần tự.
     */
    @Query("SELECT f FROM EmployeeFace f JOIN FETCH f.employee")
    List<EmployeeFace> findAllWithEmployee();
}
