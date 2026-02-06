package com.konalyan.cleaning.cleaning_service.service;

import com.konalyan.cleaning.cleaning_service.entity.CleaningService;
import com.konalyan.cleaning.cleaning_service.entity.Order;
import com.konalyan.cleaning.cleaning_service.entity.User;
import com.konalyan.cleaning.cleaning_service.enums.OrderStatus;
import com.konalyan.cleaning.cleaning_service.exception.BadRequest;
import com.konalyan.cleaning.cleaning_service.exception.NotFoundException;
import com.konalyan.cleaning.cleaning_service.exception.UserNotFoundException;
import com.konalyan.cleaning.cleaning_service.repository.CleaningServiceRepository;
import com.konalyan.cleaning.cleaning_service.repository.OrderRepository;
import com.konalyan.cleaning.cleaning_service.repository.UserRepository;
import com.lowagie.text.pdf.BaseFont;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CleaningServiceRepository cleaningServiceRepository;

    @Transactional
    public Order createOrder(
            String clientEmail,
            LocalDateTime dateTime,
            List<Long> serviceIds,
            String notes,
            String address
    ){
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new UserNotFoundException(clientEmail));

        List<CleaningService> services = cleaningServiceRepository.findAllById(serviceIds);
        if(services.isEmpty()){
            throw new BadRequest("Выберите хотя бы одну услугу");
        }
        if (services.size() != serviceIds.size()) {
            throw new NotFoundException("Some services not found");
        }

        int durationMinutes = calculateTotalDurationMinutes(services);
        LocalDateTime endTime = dateTime.plusMinutes(durationMinutes);

        validateCleanerAvailability(dateTime, endTime);


        BigDecimal totalPrice = calculateTotalPrice(services);

        Order order = Order.builder()
                .client(client)
                .dateTime(dateTime)
                .services(services)
                .notes(notes)
                .address(address)
                .status(OrderStatus.NEW)
                .totalPrice(totalPrice)
                .build();

        return orderRepository.save(order);
    }

    public List<Order> getMyOrders(String clientEmail) {
        return orderRepository.findAllByClientEmail(clientEmail);
    }


    private BigDecimal calculateTotalPrice(List<CleaningService> services) {
        return services.stream()
                .map(CleaningService::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Order assignCleaner(Long orderId, String cleanerEmail, String managerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        User cleaner = userRepository.findByEmail(cleanerEmail)
                .orElseThrow(() -> new NotFoundException("Cleaner not found"));
        if (cleaner.getRoles().stream().noneMatch(role -> "ROLE_CLEANER".equals(role.getName()))) {
            throw new BadRequest("User dont have role ROLE_CLEANER");
        }

        if (order.getStatus() != OrderStatus.NEW) {
            throw new BadRequest("A cleaner can only be assigned to a new order.");
        }

        if (order.getCleaningStaff() != null) {
            throw new BadRequest("A cleaner has already been assigned to the order.");
        }

        if (order.getDateTime() == null) {
            throw new BadRequest("The date and time are not specified for the order.");
        }

        boolean hasConflict = orderRepository.existsByCleaningStaffEmailAndDateTimeAndStatusIn(
                cleanerEmail,
                order.getDateTime(),
                List.of(OrderStatus.NEW, OrderStatus.IN_PROGRESS)
        );
        if (hasConflict) {
            throw new BadRequest("The cleaner is already busy at this time.");
        }

        order.setCleaningStaff(cleaner);
        order.setStatus(OrderStatus.IN_PROGRESS);

        orderRepository.save(order);

        return orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status, String managerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new NotFoundException("Менеджер не найден"));

        order.setStatus(status);
        order.setManager(manager);

        orderRepository.save(order);
        return orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));
    }

    public List<Order> getOrdersForManager() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public byte[] generatePdfForCleaner(String cleanerEmail, LocalDate date) {
        User cleaner = userRepository.findByEmail(cleanerEmail)
                .orElseThrow(() -> new NotFoundException("Уборщик не найден"));

        if (cleaner.getRoles().stream().noneMatch(role -> "ROLE_CLEANER".equals(role.getName()))) {
            throw new BadRequest("Пользователь не имеет роли уборщика");
        }

        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findAllByCleaningStaffEmailAndDateTimeBetween(
                cleanerEmail,
                start,
                end
        );

        if (orders.isEmpty()) {
            throw new NotFoundException("Заказы для уборщика на выбранную дату не найдены");
        }

        return buildCleanerOrdersPdf(cleaner, orders, targetDate);
    }

    private byte[] buildCleanerOrdersPdf(User cleaner, List<Order> orders, LocalDate date) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Документ А4, ландшафт, отступы 20
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Шрифты iText, только английские, без TTF
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            // Заголовок
            Paragraph title = new Paragraph("Cleaner Task Sheet for " + date, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Информация об уборщике
            Paragraph cleanerInfo = new Paragraph(
                    "Cleaner: " + formatUserName(cleaner) + " (" + cleaner.getEmail() + ")",
                    subtitleFont
            );
            cleanerInfo.setAlignment(Element.ALIGN_CENTER);
            cleanerInfo.setSpacingAfter(15f);
            document.add(cleanerInfo);

            // Таблица
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setWidths(new float[]{2.2f, 2.2f, 3.8f, 2.8f, 2.2f, 4.8f, 3.8f, 2.2f});

            addTableHeader(table, List.of(
                    "Cleaner",
                    "Client",
                    "Address",
                    "Date/Time",
                    "Status",
                    "Services (price / duration, min)",
                    "Notes",
                    "Total Price, $"
            ), headerFont);

            for (Order order : orders) {
                table.addCell(createCell(formatUserName(cleaner), cellFont, Element.ALIGN_LEFT));
                table.addCell(createCell(formatUserName(order.getClient()), cellFont, Element.ALIGN_LEFT));
                table.addCell(createCell(valueOrDash(order.getAddress()), cellFont, Element.ALIGN_LEFT));

                table.addCell(createCell(
                        order.getDateTime() != null ? order.getDateTime().toString() : "-",
                        cellFont,
                        Element.ALIGN_CENTER
                ));

                table.addCell(createCell(
                        order.getStatus() != null ? order.getStatus().name() : "-",
                        cellFont,
                        Element.ALIGN_CENTER
                ));

                table.addCell(createCell(formatServices(order.getServices()), cellFont, Element.ALIGN_LEFT));
                table.addCell(createCell(valueOrDash(order.getNotes()), cellFont, Element.ALIGN_LEFT));

                table.addCell(createCell(
                        order.getTotalPrice() != null ? order.getTotalPrice() + " $" : "-",
                        cellFont,
                        Element.ALIGN_RIGHT
                ));
            }

            document.add(table);
            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace(); // чтобы видеть реальную ошибку
            throw new BadRequest("Error generating PDF: " + e.getMessage());
        }
    }


    private void addTableHeader(PdfPTable table, List<String> headers, Font font) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(5f);
            table.addCell(cell);
        }
    }


    private PdfPCell createCell(String value, Font font, int horizontalAlignment) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(horizontalAlignment);
        cell.setPadding(4f);
        return cell;
    }


    private String formatServices(List<CleaningService> services) {
        if (services == null || services.isEmpty()) {
            return "-";
        }

        return services.stream()
                .map(service -> {
                    String price = service.getPrice() != null ? service.getPrice() + "$" : "-$";
                    String duration = service.getDurationMinutes() != null ? service.getDurationMinutes() + " min" : "- min";
                    return service.getName() + " (" + price + ", " + duration + ")";
                })
                .collect(Collectors.joining("\n"));
    }


    private Font loadFont(float size, boolean bold) {
        try {
            String fontPath = "fonts/DejaVuSans.ttf";

            BaseFont baseFont = BaseFont.createFont(
                    fontPath,
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );

            return new Font(baseFont, size, bold ? Font.BOLD : Font.NORMAL);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить шрифт для PDF", e);
        }
    }

    private String formatUserName(User user) {
        if (user == null) return "-";
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? user.getEmail() : fullName;
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }


    private int calculateTotalDurationMinutes(List<CleaningService> services) {
        int totalMinutes = 0;
        for (CleaningService service : services) {
            Integer duration = service.getDurationMinutes();
            if (duration == null || duration <= 0) {
                throw new BadRequest("Для услуги \"" + service.getName() + "\" не задана длительность");
            }
            totalMinutes += duration;
        }
        return totalMinutes;
    }

    private void validateCleanerAvailability(LocalDateTime requestedStart, LocalDateTime requestedEnd) {
        int cleanersCount = userRepository.countByRolesName("ROLE_CLEANER");
        if (cleanersCount == 0) {
            throw new BadRequest("Нет доступных уборщиков для бронирования");
        }

        List<Order> candidateOrders = orderRepository.findAllByDateTimeBetweenAndStatusIn(
                requestedStart.minusDays(1),
                requestedEnd.plusDays(1),
                List.of(OrderStatus.NEW, OrderStatus.IN_PROGRESS)
        );

        List<Order> overlappingOrders = candidateOrders.stream()
                .filter(order -> isOverlapping(order, requestedStart, requestedEnd))
                .toList();

        if (overlappingOrders.size() >= cleanersCount) {
            LocalDateTime nearestAvailable = findNearestAvailableTime(overlappingOrders, requestedStart);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formatted = nearestAvailable != null ? formatter.format(nearestAvailable) : "неизвестно";
            throw new BadRequest("Нет свободных уборщиков на это время. Ближайший освободится к " + formatted);
        }
    }

    private boolean isOverlapping(Order order, LocalDateTime requestedStart, LocalDateTime requestedEnd) {
        if (order.getDateTime() == null) {
            return false;
        }
        int durationMinutes = calculateExistingOrderDurationMinutes(order);
        LocalDateTime orderStart = order.getDateTime();
        LocalDateTime orderEnd = orderStart.plusMinutes(durationMinutes);
        return orderStart.isBefore(requestedEnd) && orderEnd.isAfter(requestedStart);
    }

    private int calculateExistingOrderDurationMinutes(Order order) {
        if (order.getServices() == null || order.getServices().isEmpty()) {
            return 0;
        }
        int totalMinutes = 0;
        for (CleaningService service : order.getServices()) {
            Integer duration = service.getDurationMinutes();
            if (duration != null && duration > 0) {
                totalMinutes += duration;
            }
        }
        return totalMinutes;
    }

    private LocalDateTime findNearestAvailableTime(List<Order> overlappingOrders, LocalDateTime requestedStart) {
        LocalDateTime nearest = null;
        for (Order order : overlappingOrders) {
            if (order.getDateTime() == null) {
                continue;
            }
            int durationMinutes = calculateExistingOrderDurationMinutes(order);
            LocalDateTime orderEnd = order.getDateTime().plusMinutes(durationMinutes);
            if (orderEnd.isAfter(requestedStart) && (nearest == null || orderEnd.isBefore(nearest))) {
                nearest = orderEnd;
            }
        }
        return nearest;
    }


}
