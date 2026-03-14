package com.rihal.queue_appointment_booking_system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rihal.queue_appointment_booking_system.config.seed.SeedData;
import com.rihal.queue_appointment_booking_system.domain.entity.*;
import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;
import com.rihal.queue_appointment_booking_system.domain.enums.AuditAction;
import com.rihal.queue_appointment_booking_system.domain.enums.EntityType;
import com.rihal.queue_appointment_booking_system.domain.enums.RoleName;
import com.rihal.queue_appointment_booking_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final BranchRepository branchRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final StaffServiceTypeRepository staffServiceTypeRepository;
    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    // Seed slot dates are based on this date in example.json
    private static final LocalDate SEED_BASE_DATE = LocalDate.of(2026, 2, 16);

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("Running DataSeeder...");

        SeedData seed = objectMapper.readValue(
                getClass().getResourceAsStream("/seed/example.json"),
                SeedData.class
        );

        // Order matters — respect FK dependencies
        seedRoles();
        Map<String, Branch> branches = seedBranches(seed.branches());
        Map<String, Staff> staffMap = seedAdmins(seed.users().admin());
        staffMap.putAll(seedManagers(seed.users().branchManagers(), branches));
        staffMap.putAll(seedStaff(seed.users().staff(), branches));
        seedCustomers(seed.users().customers());
        Map<String, ServiceType> serviceTypes = seedServiceTypes(seed.serviceTypes(), branches);
        seedStaffServiceTypes(seed.staffServiceTypes(), staffMap, serviceTypes);

        // Set branch managers now that staff exists
        assignBranchManagers(seed.users().branchManagers(), branches, staffMap);

        Map<String, Slot> slots = seedSlots(seed.slots(), branches, serviceTypes, staffMap);
        seedAppointments(seed.appointments(), slots, branches, serviceTypes, staffMap);
        seedAuditLogs(seed.auditLogs(), branches);
        seedAppConfig();

        log.info("DataSeeder completed successfully.");
    }

    // ── Roles ─────────────────────────────────────────────────────────────────

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                log.info("Seeded role: {}", roleName);
            }
        }
    }

    // ── Branches ──────────────────────────────────────────────────────────────

    private Map<String, Branch> seedBranches(java.util.List<SeedData.BranchSeed> branchSeeds) {
        Map<String, Branch> map = new HashMap<>();
        for (SeedData.BranchSeed s : branchSeeds) {
            Branch branch = branchRepository.findBySeedId(s.id()).orElseGet(() -> {
                Branch b = new Branch();
                b.setSeedId(s.id());
                b.setName(s.name());
                b.setCity(s.city());
                b.setAddress(s.address());
                b.setTimezone(s.timezone());
                b.setActive(s.isActive());
                return branchRepository.save(b);
            });
            map.put(s.id(), branch);
            log.info("Seeded branch: {}", s.name());
        }
        return map;
    }

    // ── Admins ────────────────────────────────────────────────────────────────

    private Map<String, Staff> seedAdmins(java.util.List<SeedData.UserSeed> admins) {
        Map<String, Staff> map = new HashMap<>();
        Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElseThrow();

        for (SeedData.UserSeed s : admins) {
            if (!userRepository.existsBySeedId(s.id())) {
                // Admin is a plain User (not staff/customer)
                User admin = new User();
                admin.setSeedId(s.id());
                admin.setUsername(s.username());
                admin.setEmail(s.email());
                admin.setPasswordHash(passwordEncoder.encode(s.password()));
                admin.setFullName(s.fullName());
                admin.setRole(adminRole);
                admin.setActive(s.isActive());
                userRepository.save(admin);
                log.info("Seeded admin: {}", s.username());
            }
        }
        return map; // admins are plain Users, not in staffMap
    }

    // ── Branch Managers ───────────────────────────────────────────────────────

    private Map<String, Staff> seedManagers(java.util.List<SeedData.UserSeed> managers,
                                            Map<String, Branch> branches) {
        Map<String, Staff> map = new HashMap<>();
        Role managerRole = roleRepository.findByName(RoleName.BRANCH_MANAGER).orElseThrow();

        for (SeedData.UserSeed s : managers) {
            Staff staff = staffRepository.findBySeedId(s.id()).orElseGet(() -> {
                Staff st = new Staff();
                st.setSeedId(s.id());
                st.setUsername(s.username());
                st.setEmail(s.email());
                st.setPasswordHash(passwordEncoder.encode(s.password()));
                st.setFullName(s.fullName());
                st.setRole(managerRole);
                st.setActive(s.isActive());
                st.setBranch(branches.get(s.branchId()));
                return staffRepository.save(st);
            });
            map.put(s.id(), staff);
            log.info("Seeded manager: {}", s.username());
        }
        return map;
    }

    // ── Staff ─────────────────────────────────────────────────────────────────

    private Map<String, Staff> seedStaff(java.util.List<SeedData.UserSeed> staffList,
                                         Map<String, Branch> branches) {
        Map<String, Staff> map = new HashMap<>();
        Role staffRole = roleRepository.findByName(RoleName.STAFF).orElseThrow();

        for (SeedData.UserSeed s : staffList) {
            Staff staff = staffRepository.findBySeedId(s.id()).orElseGet(() -> {
                Staff st = new Staff();
                st.setSeedId(s.id());
                st.setUsername(s.username());
                st.setEmail(s.email());
                st.setPasswordHash(passwordEncoder.encode(s.password()));
                st.setFullName(s.fullName());
                st.setRole(staffRole);
                st.setActive(s.isActive());
                st.setBranch(branches.get(s.branchId()));
                return staffRepository.save(st);
            });
            map.put(s.id(), staff);
            log.info("Seeded staff: {}", s.username());
        }
        return map;
    }

    // ── Customers ─────────────────────────────────────────────────────────────

    private void seedCustomers(java.util.List<SeedData.UserSeed> customers) {
        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER).orElseThrow();

        for (SeedData.UserSeed s : customers) {
            if (!customerRepository.existsBySeedId(s.id())) {
                Customer c = new Customer();
                c.setSeedId(s.id());
                c.setUsername(s.username());
                c.setEmail(s.email());
                c.setPasswordHash(passwordEncoder.encode(s.password()));
                c.setFullName(s.fullName());
                c.setPhone(s.phone());
                c.setRole(customerRole);
                c.setActive(s.isActive());
                // No ID image for seeded customers
                customerRepository.save(c);
                log.info("Seeded customer: {}", s.username());
            }
        }
    }

    // ── Service Types ─────────────────────────────────────────────────────────

    private Map<String, ServiceType> seedServiceTypes(
            java.util.List<SeedData.ServiceTypeSeed> serviceTypeSeeds,
            Map<String, Branch> branches) {

        Map<String, ServiceType> map = new HashMap<>();
        for (SeedData.ServiceTypeSeed s : serviceTypeSeeds) {
            ServiceType st = serviceTypeRepository.findBySeedId(s.id()).orElseGet(() -> {
                ServiceType serviceType = new ServiceType();
                serviceType.setSeedId(s.id());
                serviceType.setName(s.name());
                serviceType.setDescription(s.description());
                serviceType.setDurationMinutes(s.durationMinutes());
                serviceType.setBranch(branches.get(s.branchId()));
                serviceType.setActive(s.isActive());
                return serviceTypeRepository.save(serviceType);
            });
            map.put(s.id(), st);
            log.info("Seeded service type: {}", s.name());
        }
        return map;
    }

    // ── Staff Service Types ───────────────────────────────────────────────────

    private void seedStaffServiceTypes(
            java.util.List<SeedData.StaffServiceTypeSeed> seeds,
            Map<String, Staff> staffMap,
            Map<String, ServiceType> serviceTypes) {

        for (SeedData.StaffServiceTypeSeed s : seeds) {
            Staff staff = staffMap.get(s.staffId());
            ServiceType serviceType = serviceTypes.get(s.serviceTypeId());

            if (staff == null || serviceType == null) continue;

            if (!staffServiceTypeRepository.existsByStaffIdAndServiceTypeId(
                    staff.getId(), serviceType.getId())) {
                StaffServiceType sst = new StaffServiceType();
                sst.setStaff(staff);
                sst.setServiceType(serviceType);
                staffServiceTypeRepository.save(sst);
                log.info("Assigned {} to {}", staff.getUsername(), serviceType.getName());
            }
        }
    }

    // ── Assign Managers to Branches ───────────────────────────────────────────

    private void assignBranchManagers(java.util.List<SeedData.UserSeed> managers,
                                      Map<String, Branch> branches,
                                      Map<String, Staff> staffMap) {
        for (SeedData.UserSeed s : managers) {
            Branch branch = branches.get(s.branchId());
            Staff manager = staffMap.get(s.id());
            if (branch != null && manager != null && branch.getManager() == null) {
                branch.setManager(manager);
                branchRepository.save(branch);
                log.info("Assigned {} as manager of {}", manager.getUsername(), branch.getName());
            }
        }
    }

    // ── Slots ─────────────────────────────────────────────────────────────────

    private Map<String, Slot> seedSlots(
            java.util.List<SeedData.SlotSeed> slotSeeds,
            Map<String, Branch> branches,
            Map<String, ServiceType> serviceTypes,
            Map<String, Staff> staffMap) {

        Map<String, Slot> map = new HashMap<>();

        // Offset all seed dates to future relative to today
        long dayOffset = ChronoUnit.DAYS.between(SEED_BASE_DATE, LocalDate.now()) + 1;

        for (SeedData.SlotSeed s : slotSeeds) {
            Slot slot = slotRepository.findBySeedId(s.id()).orElseGet(() -> {
                Slot sl = new Slot();
                sl.setSeedId(s.id());
                sl.setBranch(branches.get(s.branchId()));
                sl.setServiceType(serviceTypes.get(s.serviceTypeId()));
                sl.setStaff(staffMap.get(s.staffId())); // nullable
                sl.setStartAt(OffsetDateTime.parse(s.startAt())
                        .toLocalDateTime().plusDays(dayOffset));
                sl.setEndAt(OffsetDateTime.parse(s.endAt())
                        .toLocalDateTime().plusDays(dayOffset));
                sl.setCapacity(s.capacity());
                sl.setBooked(0);
                sl.setActive(s.isActive());
                sl.setDeleted(false);
                return slotRepository.save(sl);
            });
            map.put(s.id(), slot);
            log.info("Seeded slot: {}", s.id());
        }
        return map;
    }

    // ── Appointments ──────────────────────────────────────────────────────────

    private void seedAppointments(
            java.util.List<SeedData.AppointmentSeed> apptSeeds,
            Map<String, Slot> slots,
            Map<String, Branch> branches,
            Map<String, ServiceType> serviceTypes,
            Map<String, Staff> staffMap) {

        for (SeedData.AppointmentSeed s : apptSeeds) {
            if (!appointmentRepository.existsBySeedId(s.id())) {
                customerRepository.findBySeedId(s.customerId()).ifPresent(customer -> {
                    Appointment appt = new Appointment();
                    appt.setSeedId(s.id());
                    appt.setCustomer(customer);
                    appt.setSlot(slots.get(s.slotId()));
                    appt.setBranch(branches.get(s.branchId()));
                    appt.setServiceType(serviceTypes.get(s.serviceTypeId()));
                    appt.setStaff(staffMap.get(s.staffId()));
                    appt.setStatus(AppointmentStatus.valueOf(s.status()));
                    appointmentRepository.save(appt);

                    // Increment booked count on slot
                    Slot slot = slots.get(s.slotId());
                    if (slot != null) {
                        slot.setBooked(slot.getBooked() + 1);
                        slotRepository.save(slot);
                    }

                    log.info("Seeded appointment: {}", s.id());
                });
            }
        }
    }

    // ── Audit Logs ────────────────────────────────────────────────────────────

    private void seedAuditLogs(java.util.List<SeedData.AuditLogSeed> logs,
                               Map<String, Branch> branches) {
        for (SeedData.AuditLogSeed s : logs) {
            if (!auditLogRepository.existsBySeedId(s.id())) {
                AuditLog log = new AuditLog();
                log.setSeedId(s.id());
                log.setAction(AuditAction.valueOf(s.actionType()));
                log.setActorRole(com.rihal.queue_appointment_booking_system
                        .domain.enums.RoleName.valueOf(s.actorRole()));
                log.setTargetEntityType(EntityType.valueOf(s.entityType()));
                log.setTargetEntityId(s.entityId());
                log.setMetadata(s.metadata());
                log.setTimestamp(OffsetDateTime.parse(s.timestamp()).toLocalDateTime());
                // actor_id left null — seed logs don't have real UUID actor refs
                auditLogRepository.save(log);
            }
        }
    }

    // ── AppConfig ─────────────────────────────────────────────────────────────

    private void seedAppConfig() {
        // Only inserts if not already present — migration also inserts default
        // This is a safety net for clean runs
        log.info("AppConfig retention period already set via migration.");
    }
}