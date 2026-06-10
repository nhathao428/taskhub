package com.example.taskmanagement.config;

import com.example.taskmanagement.entity.Employee;
import com.example.taskmanagement.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seed một pool nhân viên mẫu (vài chục người) với kỹ năng/phòng ban đa dạng, phục vụ
 * THỬ NGHIỆM engine gợi ý phân công AI (so khớp kỹ năng nhân viên với required_skills của task).
 *
 * Chỉ chạy khi {@code app.seed.sample-employees=true} (mặc định TẮT — đúng yêu cầu không bật
 * mặc định). Số lượng cấu hình qua {@code app.seed.sample-employees-count} (mặc định 30).
 *
 * Các nhân viên này là HỒ SƠ thuần (không gắn tài khoản đăng nhập) — đủ để hiển thị trong danh
 * sách và để AI chấm điểm, không tạo ra hàng chục tài khoản login rác.
 *
 * Idempotent kiểu "đảm bảo tối thiểu N": chỉ tạo bù cho đủ {@code count} nhân viên hiện có, nên
 * chạy lại nhiều lần không nhân bản (an toàn cả với Postgres đã có dữ liệu thật).
 *
 * @Order(20): chạy sau các seeder tài khoản (admin/manager/employee) cho gọn log.
 */
@Component
@Order(20)
public class SampleEmployeeSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleEmployeeSeeder.class);

    private static final int MAX_COUNT = 200; // chặn cấu hình bất thường

    private final EmployeeRepository employeeRepository;

    @Value("${app.seed.sample-employees:false}")
    private boolean enabled;

    @Value("${app.seed.sample-employees-count:30}")
    private int count;

    public SampleEmployeeSeeder(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // Họ.
    private static final String[] SURNAMES = {
            "Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng",
            "Bùi", "Đỗ", "Hồ", "Ngô", "Dương", "Lý", "Đinh", "Tô", "Mai", "Trịnh"
    };

    // Đệm + tên.
    private static final String[] MIDDLE_GIVEN = {
            "Văn An", "Thị Bình", "Hoàng Cường", "Minh Đức", "Thị Hoa", "Văn Khoa", "Thị Lan",
            "Quốc Bảo", "Thị Mai", "Văn Nam", "Thị Ngọc", "Hữu Phúc", "Thị Quỳnh", "Văn Sơn",
            "Thị Trang", "Minh Tuấn", "Thị Uyên", "Văn Vinh", "Thị Xuân", "Quang Huy",
            "Thị Yến", "Văn Hùng", "Thị Hương", "Đức Anh", "Thảo Vy"
    };

    /** Một phòng ban: tên, danh sách chức danh, và pool kỹ năng để bốc ra. */
    private record Dept(String name, String group, String[] positions, String[] skills) {}

    private static final Dept[] DEPARTMENTS = {
            new Dept("Kỹ thuật", "Nhóm Kỹ thuật",
                    new String[]{"Lập trình viên Backend", "Lập trình viên Frontend", "Kỹ sư DevOps",
                            "Kiểm thử (QA)", "Kỹ sư dữ liệu", "Tech Lead"},
                    new String[]{"Java", "Spring Boot", "PostgreSQL", "React", "JavaScript", "TypeScript",
                            "Docker", "Kubernetes", "AWS", "Node.js", "Python", "REST API", "Redis",
                            "CI/CD", "Linux", "GraphQL", "MongoDB", "Microservices"}),
            new Dept("Kinh doanh", "Nhóm Sales",
                    new String[]{"Nhân viên Kinh doanh", "Chuyên viên Sales", "Account Manager", "Telesales"},
                    new String[]{"Sales", "Đàm phán", "CRM", "Chăm sóc khách hàng", "B2B",
                            "Account Management", "Telesales", "Phân tích thị trường"}),
            new Dept("Marketing", "Nhóm Marketing",
                    new String[]{"Chuyên viên Marketing", "Content Creator", "Digital Marketing", "SEO Specialist"},
                    new String[]{"SEO", "Content", "Google Ads", "Facebook Ads", "Email Marketing",
                            "Branding", "Copywriting", "Analytics", "Social Media"}),
            new Dept("Nhân sự", "Nhóm HR",
                    new String[]{"Chuyên viên Tuyển dụng", "Chuyên viên C&B", "HR Generalist"},
                    new String[]{"Tuyển dụng", "C&B", "Đào tạo", "Onboarding", "Quan hệ lao động", "Hành chính"}),
            new Dept("Thiết kế", "Nhóm Design",
                    new String[]{"UI/UX Designer", "Graphic Designer", "Motion Designer"},
                    new String[]{"Figma", "UI Design", "UX Research", "Prototyping", "Illustrator",
                            "Photoshop", "After Effects", "Wireframe"}),
            new Dept("Kế toán", "Nhóm Tài chính",
                    new String[]{"Kế toán viên", "Kế toán tổng hợp", "Chuyên viên Tài chính"},
                    new String[]{"Kế toán", "Excel", "Báo cáo tài chính", "Thuế", "Kiểm toán", "MISA"})
    };

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        int target = Math.min(Math.max(count, 0), MAX_COUNT);
        long existing = employeeRepository.count();
        if (existing >= target) {
            log.info("Đã có {} nhân viên (>= {}) — bỏ qua seed pool test AI.", existing, target);
            return;
        }

        int created = 0;
        for (int i = (int) existing; i < target; i++) {
            Dept dept = DEPARTMENTS[i % DEPARTMENTS.length];
            Employee emp = new Employee();
            emp.setFirstName(SURNAMES[i % SURNAMES.length]);
            // Lệch chu kỳ để tên đa dạng, ít trùng họ+tên.
            emp.setLastName(MIDDLE_GIVEN[(i * 7 + 3) % MIDDLE_GIVEN.length]);
            emp.setPosition(dept.positions()[i % dept.positions().length]);
            emp.setDepartment(dept.name());
            emp.setGroup(dept.group());
            emp.setSkills(pickSkills(dept.skills(), i));
            employeeRepository.save(emp);
            created++;
        }
        log.info("Đã seed {} nhân viên mẫu cho test AI (tổng {} nhân viên).", created, target);
    }

    /** Bốc 3 kỹ năng liền kề trong pool (lệch theo index) → mỗi người một tổ hợp khác nhau. */
    private static String pickSkills(String[] pool, int i) {
        int n = pool.length;
        return String.join(", ",
                List.of(pool[i % n], pool[(i + 1) % n], pool[(i + 2) % n]));
    }
}
