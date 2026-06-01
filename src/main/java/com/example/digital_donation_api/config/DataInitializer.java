package com.example.digital_donation_api.config;

import com.example.digital_donation_api.entity.*;
import com.example.digital_donation_api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final CharityRepository charityRepository;
    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;
    private final EventTimelineRepository eventTimelineRepository;
    private final DonationRepository donationRepository;
    private final PostRepository postRepository;
    private final AnnouncementRepository announcementRepository;
    private final NotificationRepository notificationRepository;
    private final BusinessCardRepository businessCardRepository;
    private final SettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            log.info("Database already seeded — skipping DataInitializer.");
            return;
        }

        log.info("Seeding database with sample data...");

        // ── Roles ─────────────────────────────────────────────────────────────
        Role roleUser  = role("USER");
        Role roleAdmin = role("ADMIN");

        // ── Users ─────────────────────────────────────────────────────────────
        String pw = passwordEncoder.encode("Test@1234");

        User admin  = user("Admin SangKum",  "admin@sangkumfund.com",  pw);
        User sopha  = user("Sopha Kim",       "sopha@example.com",      pw);
        User dara   = user("Dara Chann",      "dara@example.com",       pw);
        User pisey  = user("Pisey Sok",       "pisey@example.com",      pw);
        User makara = user("Makara Rath",     "makara@example.com",     pw);

        // ── User roles ────────────────────────────────────────────────────────
        assignRole(admin,  roleAdmin);
        assignRole(admin,  roleUser);
        assignRole(sopha,  roleUser);
        assignRole(dara,   roleUser);
        assignRole(pisey,  roleUser);
        assignRole(makara, roleUser);

        // ── Charities ─────────────────────────────────────────────────────────
        Charity heartFound = charity(
                sopha,
                "Khmer Heart Foundation",
                "Providing essential healthcare services to underserved communities across Cambodia.",
                "health",
                "health@khmerheart.org",
                CharityStatus.VERIFIED,
                350L, 12000L, 45L, 8,
                "Ensuring every Cambodian has access to quality healthcare regardless of income.",
                4.8
        );

        Charity eduAll = charity(
                dara,
                "Education for All Cambodia",
                "Bridging the educational gap by supplying schools in rural areas with materials and resources.",
                "education",
                "info@edcambodia.org",
                CharityStatus.PENDING,
                120L, 4500L, 20L, 3,
                "Every child deserves the right to learn.",
                4.2
        );

        // ── Events ────────────────────────────────────────────────────────────
        Event cleanWater = event(
                sopha, heartFound,
                "Clean Water for Rural Villages",
                "Help us bring clean, safe drinking water to 10 villages in Kampong Speu province. " +
                "Contaminated water is the leading cause of preventable illness in these communities.",
                "https://images.unsplash.com/photo-1504608524841-42584120d693?w=800",
                new BigDecimal("5000"), new BigDecimal("3200"),
                EventStatus.ACTIVE,
                LocalDate.now().minusDays(15), LocalDate.now().plusDays(45),
                "Kampong Speu, Cambodia", 11.4532, 104.3226, "Environment"
        );

        Event schoolSupplies = event(
                dara, eduAll,
                "School Supplies Drive 2025",
                "Collecting backpacks, notebooks, pencils, and uniforms for 500 students in Mondulkiri. " +
                "Many children attend school without the basic tools they need to succeed.",
                "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=800",
                new BigDecimal("2500"), new BigDecimal("0"),
                EventStatus.PENDING,
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(60),
                "Mondulkiri, Cambodia", 12.4567, 107.1890, "Education"
        );

        Event hospitalEquip = event(
                sopha, heartFound,
                "Hospital Equipment Fund",
                "Raising funds to purchase vital medical equipment — ultrasound machines, oxygen concentrators, " +
                "and surgical instruments — for Kampot Provincial Hospital.",
                "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800",
                new BigDecimal("10000"), new BigDecimal("7540"),
                EventStatus.ACTIVE,
                LocalDate.now().minusDays(30), LocalDate.now().plusDays(30),
                "Kampot, Cambodia", 10.6104, 104.1810, "Healthcare"
        );

        Event communityLib = event(
                makara, null,
                "Community Library Project",
                "Building a free community library in Siem Reap stocked with Khmer and English books " +
                "for children aged 5–18. Includes reading programs every weekend.",
                "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=800",
                new BigDecimal("2000"), new BigDecimal("820"),
                EventStatus.APPROVED,
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(90),
                "Siem Reap, Cambodia", 13.3633, 103.8564, "Education"
        );

        Event floodRelief = event(
                admin, null,
                "Emergency Flood Relief 2024",
                "Immediate assistance for families displaced by the 2024 monsoon flooding. " +
                "Funds cover food, temporary shelter, clean water, and medicine.",
                "https://images.unsplash.com/photo-1541123437800-1bb1317badc2?w=800",
                new BigDecimal("8000"), new BigDecimal("8320"),
                EventStatus.COMPLETED,
                LocalDate.now().minusDays(90), LocalDate.now().minusDays(10),
                "Prey Veng, Cambodia", 11.4854, 105.3258, "Disaster Relief"
        );

        Event youthSports = event(
                dara, null,
                "Youth Sports Program",
                "Launching a free weekend sports program in Phnom Penh for at-risk youth, " +
                "combining football, athletics, and life-skills coaching.",
                "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=800",
                new BigDecimal("3000"), new BigDecimal("0"),
                EventStatus.DRAFT,
                LocalDate.now().plusDays(14), LocalDate.now().plusDays(120),
                "Phnom Penh, Cambodia", 11.5564, 104.9282, "Sports"
        );

        // ── Event images ──────────────────────────────────────────────────────
        eventImage(cleanWater,   "https://images.unsplash.com/photo-1504608524841-42584120d693?w=800", true,  0);
        eventImage(cleanWater,   "https://images.unsplash.com/photo-1548407260-da850faa41e3?w=800", false, 1);
        eventImage(hospitalEquip,"https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800", true,  0);
        eventImage(hospitalEquip,"https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=800", false, 1);
        eventImage(floodRelief,  "https://images.unsplash.com/photo-1541123437800-1bb1317badc2?w=800", true,  0);

        // ── Donations ─────────────────────────────────────────────────────────
        donation(pisey,  cleanWater,   "100.00", false, PaymentMethod.VISA_CARD,    DonationStatus.SUCCESS);
        donation(makara, cleanWater,   "50.00",  false, PaymentMethod.KHQR,         DonationStatus.SUCCESS);
        donation(dara,   cleanWater,   "200.00", false, PaymentMethod.VISA_CARD,    DonationStatus.SUCCESS);
        donation(pisey,  cleanWater,   "75.00",  true,  PaymentMethod.OFFLINE_QR,   DonationStatus.PENDING);
        donation(admin,  cleanWater,   "500.00", false, PaymentMethod.VISA_CARD,    DonationStatus.SUCCESS);
        donation(pisey,  hospitalEquip,"150.00", false, PaymentMethod.KHQR,         DonationStatus.SUCCESS);
        donation(makara, hospitalEquip,"300.00", false, PaymentMethod.VISA_CARD,    DonationStatus.SUCCESS);
        donation(sopha,  communityLib, "100.00", false, PaymentMethod.VISA_CARD,    DonationStatus.SUCCESS);
        donation(pisey,  floodRelief,  "250.00", false, PaymentMethod.KHQR,         DonationStatus.SUCCESS);
        donation(admin,  floodRelief,  "1000.00",false, PaymentMethod.VISA_CARD,    DonationStatus.SUCCESS);

        // ── Event timeline entries ─────────────────────────────────────────────
        timeline(cleanWater,   TimelineType.EVENT,       "Campaign launched and accepting donations.");
        timeline(cleanWater,   TimelineType.MILESTONE,   "Reached 50% of our goal! Thank you to all donors.");
        timeline(hospitalEquip,TimelineType.EVENT,       "Campaign started. Equipment list finalised.");
        timeline(hospitalEquip,TimelineType.MILESTONE,   "75% funded — first batch of equipment ordered!");
        timeline(floodRelief,  TimelineType.EVENT,       "Emergency campaign opened in response to flooding.");
        timeline(floodRelief,  TimelineType.MILESTONE,   "Goal exceeded! All 320 families have received aid.");
        timeline(floodRelief,  TimelineType.UPDATE,      "Campaign closed. Final report published.");

        // ── Announcements ─────────────────────────────────────────────────────
        announcement(cleanWater, heartFound, sopha,
                "Water Pump Delivery Update",
                "Great news! The first water pump has been delivered to Tram Kak village. " +
                "Installation begins next Monday — follow our page for live updates.");
        announcement(hospitalEquip, heartFound, sopha,
                "Equipment Order Placed",
                "Thanks to your incredible generosity we have placed the order for two ultrasound machines. " +
                "Expected delivery: 3 weeks. We will post unboxing photos when they arrive!");

        // ── Blog posts ────────────────────────────────────────────────────────
        post(admin,
                "How SangKumFund is Changing Lives Across Cambodia",
                "how-sangkumfund-is-changing-lives",
                "Since launching, SangKumFund has helped over 15,000 Cambodians through 200+ campaigns.",
                "<h2>Real Impact, Real People</h2>" +
                "<p>When Sopha started her clean-water campaign in Kampong Speu, she had one goal: " +
                "give her village access to safe water. Within six weeks, donors from across the country " +
                "rallied behind her, and today the village has a fully operational pump system serving 200 families.</p>" +
                "<p>Stories like Sopha's are why SangKumFund exists. We believe that with the right platform, " +
                "ordinary people can achieve extraordinary things.</p>",
                "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=1200",
                "impact,story,community",
                true,  PostStatus.PUBLISHED
        );

        post(admin,
                "5 Ways to Make Your Donation Count",
                "5-ways-to-make-your-donation-count",
                "Small donations add up. Here is how to maximise the impact of every dollar you give.",
                "<h2>1. Give to Verified Campaigns</h2><p>SangKumFund verifies all charities. " +
                "Look for the verified badge before donating.</p>" +
                "<h2>2. Set Up Recurring Giving</h2><p>Monthly donations let organisers plan long-term.</p>" +
                "<h2>3. Share the Campaign</h2><p>One share can bring in ten more donors.</p>" +
                "<h2>4. Donate Early</h2><p>Early momentum builds credibility for a campaign.</p>" +
                "<h2>5. Leave a Comment</h2><p>Your words of encouragement matter just as much as money.</p>",
                "https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?w=1200",
                "tips,donation,guide",
                false, PostStatus.PUBLISHED
        );

        post(sopha,
                "Getting Started with Your First Fundraiser",
                "getting-started-with-your-first-fundraiser",
                "Everything you need to know before launching your campaign on SangKumFund.",
                "<p>Launching a fundraiser can feel daunting. This guide walks you through every step...</p>",
                "https://images.unsplash.com/photo-1559027615-cd4628902d4a?w=1200",
                "fundraising,guide,tips",
                false, PostStatus.DRAFT
        );

        // ── Notifications ──────────────────────────────────────────────────────
        notification(pisey,
                "Donation Confirmed",
                "Your donation of $100 to \"Clean Water for Rural Villages\" was successful. Thank you!",
                NotificationType.DONATION, "/events/1", "View Campaign");
        notification(sopha,
                "New Donation Received",
                "Dara Chann donated $200 to your campaign \"Clean Water for Rural Villages\".",
                NotificationType.DONATION, "/events/1", "View Campaign");
        notification(dara,
                "Campaign Pending Review",
                "Your campaign \"School Supplies Drive 2025\" has been submitted and is awaiting admin approval.",
                NotificationType.EVENT, "/events/2", "View Campaign");
        notification(makara,
                "Campaign Approved",
                "Great news! Your campaign \"Community Library Project\" has been approved and is now live.",
                NotificationType.EVENT, "/events/4", "View Campaign");

        // ── Settings ─────────────────────────────────────────────────────────
        settings(admin);
        settings(sopha);
        settings(dara);
        settings(pisey);
        settings(makara);

        // ── Business cards ────────────────────────────────────────────────────
        businessCard(admin,  "default", "Platform Administrator",
                "Managing the SangKumFund platform to connect donors with meaningful causes.",
                "admin-sangkum");
        businessCard(sopha,  "modern",  "Charity Organiser & Health Advocate",
                "Founder of Khmer Heart Foundation. Passionate about rural healthcare access in Cambodia.",
                "sopha-kim");

        log.info("Database seeded successfully. Accounts:");
        log.info("  Admin  — admin@sangkumfund.com  / Test@1234");
        log.info("  Sopha  — sopha@example.com       / Test@1234");
        log.info("  Dara   — dara@example.com         / Test@1234");
        log.info("  Pisey  — pisey@example.com        / Test@1234");
        log.info("  Makara — makara@example.com       / Test@1234");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Role role(String name) {
        Role r = new Role();
        r.setName(name);
        return roleRepository.save(r);
    }

    private User user(String name, String email, String encodedPassword) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(encodedPassword);
        u.setIsActive(true);
        u.setIsBlocked(false);
        u.setFailedLoginAttempts(0);
        u.setAccountLocked(false);
        return userRepository.save(u);
    }

    private void assignRole(User user, Role role) {
        UserRole ur = new UserRole();
        ur.setUser(user);
        ur.setRole(role);
        userRoleRepository.save(ur);
    }

    private Charity charity(User owner, String name, String description, String category,
                            String contactEmail, CharityStatus status,
                            Long totalDonations, Long beneficiaries, Long volunteers, int years,
                            String mission, double rating) {
        Charity c = new Charity();
        c.setOwner(owner);
        c.setName(name);
        c.setDescription(description);
        c.setCategory(category);
        c.setContactEmail(contactEmail);
        c.setStatus(status);
        c.setTotalDonations(totalDonations);
        c.setBeneficiariesCount(beneficiaries);
        c.setVolunteersCount(volunteers);
        c.setYearsActive(years);
        c.setMissionStatement(mission);
        c.setRatingScore(rating);
        if (status == CharityStatus.VERIFIED) {
            c.setVerifiedAt(LocalDateTime.now().minusDays(30));
        }
        return charityRepository.save(c);
    }

    private Event event(User owner, Charity charity, String title, String description,
                        String imageUrl, BigDecimal goal, BigDecimal current,
                        EventStatus status, LocalDate start, LocalDate end,
                        String location, double lat, double lng, String category) {
        Event e = new Event();
        e.setOwner(owner);
        e.setCharity(charity);
        e.setTitle(title);
        e.setDescription(description);
        e.setImageUrl(imageUrl);
        e.setGoalAmount(goal);
        e.setCurrentAmount(current);
        e.setStatus(status);
        e.setVisibility(EventVisibility.PUBLIC);
        e.setStartDate(start);
        e.setEndDate(end);
        e.setLocation(location);
        e.setLatitude(lat);
        e.setLongitude(lng);
        e.setCategory(category);
        return eventRepository.save(e);
    }

    private void eventImage(Event event, String url, boolean primary, int order) {
        EventImage img = new EventImage();
        img.setEvent(event);
        img.setImageUrl(url);
        img.setIsPrimary(primary);
        img.setDisplayOrder(order);
        eventImageRepository.save(img);
    }

    private void donation(User user, Event event, String amount, boolean anonymous,
                          PaymentMethod method, DonationStatus status) {
        Donation d = new Donation();
        d.setUser(user);
        d.setEvent(event);
        d.setAmount(new BigDecimal(amount));
        d.setCurrency("USD");
        d.setIsAnonymous(anonymous);
        d.setPaymentMethod(method);
        d.setStatus(status);
        d.setTransactionRef("TXN-" + System.nanoTime());
        donationRepository.save(d);
    }

    private void timeline(Event event, TimelineType type, String description) {
        EventTimeline t = new EventTimeline();
        t.setEvent(event);
        t.setType(type);
        t.setDescription(description);
        eventTimelineRepository.save(t);
    }

    private void announcement(Event event, Charity charity, User author,
                              String title, String content) {
        Announcement a = new Announcement();
        a.setEvent(event);
        a.setCharity(charity);
        a.setAuthor(author);
        a.setTitle(title);
        a.setContent(content);
        announcementRepository.save(a);
    }

    private void post(User author, String title, String slug, String excerpt,
                      String content, String coverImage, String tags,
                      boolean featured, PostStatus status) {
        Post p = new Post();
        p.setAuthor(author);
        p.setTitle(title);
        p.setSlug(slug);
        p.setExcerpt(excerpt);
        p.setContent(content);
        p.setCoverImageUrl(coverImage);
        p.setTags(tags);
        p.setFeatured(featured);
        p.setStatus(status);
        if (status == PostStatus.PUBLISHED) {
            p.setPublishedAt(LocalDateTime.now().minusDays(3));
        }
        postRepository.save(p);
    }

    private void notification(User user, String title, String message,
                              NotificationType type, String actionUrl, String actionLabel) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setIsRead(false);
        n.setIsDismissed(false);
        n.setActionUrl(actionUrl);
        n.setActionLabel(actionLabel);
        notificationRepository.save(n);
    }

    private void settings(User user) {
        Settings s = new Settings();
        s.setUser(user);
        s.setPrivacySettings("{\"profileVisible\":true,\"donationsVisible\":false,\"eventsVisible\":true}");
        s.setNotificationSettings("{\"emailNotifications\":true,\"donationAlerts\":true,\"eventUpdates\":true,\"systemAlerts\":true}");
        settingsRepository.save(s);
    }

    private void businessCard(User user, String template, String title,
                              String bio, String slug) {
        BusinessCard bc = new BusinessCard();
        bc.setUser(user);
        bc.setTemplate(template);
        bc.setTitle(title);
        bc.setBio(bio);
        bc.setShareSlug(slug);
        bc.setContactInfo("{\"email\":\"" + user.getEmail() + "\"}");
        businessCardRepository.save(bc);
    }
}
