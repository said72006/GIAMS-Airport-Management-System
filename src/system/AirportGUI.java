package system;

import exceptions.DuplicatePassengerException;
import exceptions.FlightFullException;
import exceptions.InvalidFlightStatusException;
import file.FileManager;
import model.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Modern Swing GUI for GIAMS.
 * No external libraries are required.
 */
public class AirportGUI extends JFrame {

    // ---------- Theme ----------
    private static final Color NAVY = new Color(14, 24, 43);
    private static final Color NAVY_2 = new Color(21, 34, 58);
    private static final Color BG = new Color(244, 247, 252);
    private static final Color CARD = Color.WHITE;
    private static final Color TEXT = new Color(28, 38, 56);
    private static final Color MUTED = new Color(111, 124, 145);
    private static final Color BORDER = new Color(224, 230, 239);
    private static final Color CYAN = new Color(24, 196, 214);
    private static final Color BLUE = new Color(60, 112, 245);
    private static final Color PURPLE = new Color(125, 88, 255);
    private static final Color GREEN = new Color(35, 181, 126);
    private static final Color ORANGE = new Color(247, 153, 67);
    private static final Color RED = new Color(231, 76, 94);

    private final Airport airport;
    private final String passengersFilePath;
    private final String pilotsFilePath;
    private final String attendantsFilePath;
    private final String flightsLogFilePath;

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);
    private final Map<String, NavButton> navButtons = new HashMap<>();

    private final JLabel pageTitle = new JLabel("Dashboard");
    private final JLabel pageSubtitle = new JLabel("Overview of airport operations");
    private final JLabel clockLabel = new JLabel();
    private final JLabel saveStateLabel = new JLabel("All changes are in memory");

    private final JLabel peopleCountLabel = statValueLabel();
    private final JLabel passengerCountLabel = statValueLabel();
    private final JLabel staffCountLabel = statValueLabel();
    private final JLabel flightCountLabel = statValueLabel();
    private final JLabel departedCountLabel = statValueLabel();

    private final DefaultTableModel passengerModel = nonEditableModel(
            new String[]{"System ID", "National ID", "Name", "Nationality", "Passport", "Destination", "Expiry"}
    );
    private final DefaultTableModel staffModel = nonEditableModel(
            new String[]{"Type", "National ID", "Name", "Nationality", "Salary", "Shift", "Details"}
    );
    private final DefaultTableModel flightModel = nonEditableModel(
            new String[]{"Flight", "Destination", "Pilot", "Capacity", "Booked", "Status"}
    );
    private final DefaultTableModel recentFlightModel = nonEditableModel(
            new String[]{"Flight", "Destination", "Pilot", "Seats", "Status"}
    );

    private final JTable passengerTable = modernTable(passengerModel);
    private final JTable staffTable = modernTable(staffModel);
    private final JTable flightTable = modernTable(flightModel);
    private final JTable recentFlightTable = modernTable(recentFlightModel);

    private final JTextArea reportArea = new JTextArea();
    private final JLabel flightSummaryLabel = new JLabel();

    private Timer clockTimer;

    public AirportGUI(Airport airport,
                      String passengersFilePath,
                      String pilotsFilePath,
                      String attendantsFilePath,
                      String flightsLogFilePath) {

        this.airport = airport;
        this.passengersFilePath = passengersFilePath;
        this.pilotsFilePath = pilotsFilePath;
        this.attendantsFilePath = attendantsFilePath;
        this.flightsLogFilePath = flightsLogFilePath;

        configureGlobalUI();

        setTitle("GIAMS | Gaza International Airport Management System");
        setSize(1320, 820);
        setMinimumSize(new Dimension(1120, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(BG);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        AirportGUI.this,
                        "Save all data before closing GIAMS?",
                        "Exit GIAMS",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) return;
                if (choice == JOptionPane.YES_OPTION) saveData(true);
                dispose();
            }
        });

        setLayout(new BorderLayout());
        add(createSidebar(), BorderLayout.WEST);
        add(createMainArea(), BorderLayout.CENTER);

        refreshAll();
        showPage("dashboard", "Dashboard", "Overview of airport operations");
        startClock();
    }

    // =========================================================
    // LAYOUT
    // =========================================================

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBackground(NAVY);
        sidebar.setBorder(new EmptyBorder(22, 16, 18, 16));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(0, 4, 26, 4));

        JLabel logo = new JLabel("✈", SwingConstants.CENTER);
        logo.setOpaque(true);
        logo.setBackground(CYAN);
        logo.setForeground(NAVY);
        logo.setFont(new Font("SansSerif", Font.BOLD, 23));
        logo.setPreferredSize(new Dimension(48, 48));
        logo.setBorder(new RoundedBorder(CYAN, 16, 0));

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        JLabel name = new JLabel("GIAMS");
        name.setForeground(Color.WHITE);
        name.setFont(new Font("SansSerif", Font.BOLD, 23));
        JLabel sub = new JLabel("Airport Control");
        sub.setForeground(new Color(154, 169, 193));
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        brandText.add(name);
        brandText.add(Box.createVerticalStrut(3));
        brandText.add(sub);

        brand.add(logo, BorderLayout.WEST);
        brand.add(brandText, BorderLayout.CENTER);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

        JLabel menu = new JLabel("  MANAGEMENT");
        menu.setForeground(new Color(104, 123, 153));
        menu.setFont(new Font("SansSerif", Font.BOLD, 10));
        menu.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(menu);
        nav.add(Box.createVerticalStrut(10));

        addNav(nav, "dashboard", "⌂", "Dashboard", "Overview of airport operations");
        addNav(nav, "passengers", "◉", "Passengers", "Passenger records and bookings");
        addNav(nav, "staff", "◆", "Staff", "Pilots and flight attendants");
        addNav(nav, "flights", "✈", "Flights", "Create, book and update flights");
        addNav(nav, "report", "▤", "Daily Report", "Operational summary and records");

        sidebar.add(nav, BorderLayout.CENTER);

        RoundedPanel bottom = new RoundedPanel(18, NAVY_2);
        bottom.setLayout(new BorderLayout(10, 0));
        bottom.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel dot = new JLabel("●");
        dot.setForeground(GREEN);
        dot.setFont(new Font("SansSerif", Font.BOLD, 16));
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        JLabel online = new JLabel("System Online");
        online.setForeground(Color.WHITE);
        online.setFont(new Font("SansSerif", Font.BOLD, 12));
        JLabel storage = new JLabel("Local file storage active");
        storage.setForeground(new Color(147, 164, 190));
        storage.setFont(new Font("SansSerif", Font.PLAIN, 10));
        texts.add(online);
        texts.add(storage);
        bottom.add(dot, BorderLayout.WEST);
        bottom.add(texts, BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNav(JPanel nav, String key, String icon, String title, String subtitle) {
        NavButton button = new NavButton(icon, title);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        button.addActionListener(e -> showPage(key, title, subtitle));
        navButtons.put(key, button);
        nav.add(button);
        nav.add(Box.createVerticalStrut(7));
    }

    private JPanel createMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.add(createTopbar(), BorderLayout.NORTH);

        contentPanel.setBackground(BG);
        contentPanel.setBorder(new EmptyBorder(0, 24, 24, 24));
        contentPanel.add(createDashboardPanel(), "dashboard");
        contentPanel.add(createPassengersPanel(), "passengers");
        contentPanel.add(createStaffPanel(), "staff");
        contentPanel.add(createFlightsPanel(), "flights");
        contentPanel.add(createReportPanel(), "report");
        main.add(contentPanel, BorderLayout.CENTER);
        return main;
    }

    private JPanel createTopbar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG);
        top.setBorder(new EmptyBorder(22, 26, 16, 24));

        JPanel headings = new JPanel();
        headings.setOpaque(false);
        headings.setLayout(new BoxLayout(headings, BoxLayout.Y_AXIS));
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        pageTitle.setForeground(TEXT);
        pageSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pageSubtitle.setForeground(MUTED);
        headings.add(pageTitle);
        headings.add(Box.createVerticalStrut(4));
        headings.add(pageSubtitle);
        top.add(headings, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        RoundedPanel timeCard = new RoundedPanel(15, CARD);
        timeCard.setLayout(new BorderLayout());
        timeCard.setBorder(new EmptyBorder(8, 13, 8, 13));
        clockLabel.setForeground(MUTED);
        clockLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        timeCard.add(clockLabel);

        ModernButton save = new ModernButton("Save Data", BLUE, Color.WHITE);
        save.setPreferredSize(new Dimension(122, 38));
        save.addActionListener(e -> saveData(true));

        right.add(timeCard);
        right.add(save);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private void showPage(String key, String title, String subtitle) {
        contentLayout.show(contentPanel, key);
        pageTitle.setText(title);
        pageSubtitle.setText(subtitle);
        for (Map.Entry<String, NavButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(key));
        }
        if ("report".equals(key)) refreshReport();
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);

        GradientPanel hero = new GradientPanel(26, BLUE, PURPLE);
        hero.setPreferredSize(new Dimension(0, 168));
        hero.setLayout(new BorderLayout());
        hero.setBorder(new EmptyBorder(25, 28, 24, 28));

        JPanel heroText = new JPanel();
        heroText.setOpaque(false);
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));
        JLabel welcome = new JLabel("Welcome back to GIAMS");
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 27));
        JLabel desc = new JLabel("Manage passengers, staff and flights from one live operations dashboard.");
        desc.setForeground(new Color(226, 235, 255));
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JLabel live = new JLabel("  ●  LIVE OPERATIONS  ");
        live.setOpaque(true);
        live.setBackground(new Color(255, 255, 255, 45));
        live.setForeground(Color.WHITE);
        live.setFont(new Font("SansSerif", Font.BOLD, 10));
        live.setBorder(new EmptyBorder(5, 8, 5, 8));
        live.setMaximumSize(new Dimension(130, 28));
        heroText.add(welcome);
        heroText.add(Box.createVerticalStrut(8));
        heroText.add(desc);
        heroText.add(Box.createVerticalStrut(16));
        heroText.add(live);
        hero.add(heroText, BorderLayout.CENTER);

        JLabel plane = new JLabel("✈");
        plane.setForeground(new Color(255, 255, 255, 125));
        plane.setFont(new Font("SansSerif", Font.PLAIN, 94));
        plane.setBorder(new EmptyBorder(0, 0, 0, 24));
        hero.add(plane, BorderLayout.EAST);

        JPanel north = new JPanel(new BorderLayout(0, 15));
        north.setOpaque(false);
        north.add(hero, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 5, 13, 0));
        stats.setOpaque(false);
        stats.add(createStatCard("People", peopleCountLabel, "registered", BLUE, "◎"));
        stats.add(createStatCard("Passengers", passengerCountLabel, "travelers", CYAN, "◉"));
        stats.add(createStatCard("Staff", staffCountLabel, "crew members", PURPLE, "◆"));
        stats.add(createStatCard("Flights", flightCountLabel, "total records", ORANGE, "✈"));
        stats.add(createStatCard("Departed", departedCountLabel, "completed", GREEN, "✓"));
        north.add(stats, BorderLayout.CENTER);
        panel.add(north, BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridBagLayout());
        lower.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        g.insets = new Insets(0, 0, 0, 14);

        g.gridx = 0;
        g.weightx = 0.67;
        lower.add(createRecentFlightsCard(), g);

        g.gridx = 1;
        g.weightx = 0.33;
        g.insets = new Insets(0, 0, 0, 0);
        lower.add(createQuickActionsCard(), g);
        panel.add(lower, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String title, JLabel value, String caption, Color accent, String icon) {
        RoundedPanel card = new RoundedPanel(20, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 16, 14, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setForeground(MUTED);
        t.setFont(new Font("SansSerif", Font.BOLD, 11));
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setOpaque(true);
        iconLabel.setForeground(accent);
        iconLabel.setBackground(lighten(accent, 0.87f));
        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        iconLabel.setPreferredSize(new Dimension(32, 32));
        iconLabel.setBorder(new RoundedBorder(lighten(accent, 0.87f), 11, 0));
        top.add(t, BorderLayout.WEST);
        top.add(iconLabel, BorderLayout.EAST);

        value.setForeground(TEXT);
        value.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel c = new JLabel(caption);
        c.setForeground(MUTED);
        c.setFont(new Font("SansSerif", Font.PLAIN, 10));

        card.add(top, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(c, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createRecentFlightsCard() {
        RoundedPanel card = new RoundedPanel(20, CARD);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(17, 18, 16, 18));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        JLabel title = sectionTitle("Recent Flights");
        JLabel sub = sectionSubtitle("Latest flight records and their current status");
        labels.add(title);
        labels.add(Box.createVerticalStrut(3));
        labels.add(sub);

        JButton viewAll = linkButton("View all →");
        viewAll.addActionListener(e -> showPage("flights", "Flights", "Create, book and update flights"));
        head.add(labels, BorderLayout.WEST);
        head.add(viewAll, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        styleStatusColumn(recentFlightTable, 4);
        JScrollPane scroll = cleanScroll(recentFlightTable);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createQuickActionsCard() {
        RoundedPanel card = new RoundedPanel(20, CARD);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(17, 18, 18, 18));

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        labels.add(sectionTitle("Quick Actions"));
        labels.add(Box.createVerticalStrut(3));
        labels.add(sectionSubtitle("Common operations in one click"));
        card.add(labels, BorderLayout.NORTH);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.add(quickAction("＋", "Add Passenger", "Register a new traveler", BLUE, this::showAddPassengerDialog));
        actions.add(Box.createVerticalStrut(9));
        actions.add(quickAction("✈", "Create Flight", "Assign pilot and capacity", PURPLE, this::showCreateFlightDialog));
        actions.add(Box.createVerticalStrut(9));
        actions.add(quickAction("✓", "Book Passenger", "Add traveler to a flight", CYAN, this::showBookingDialog));
        actions.add(Box.createVerticalStrut(9));
        actions.add(quickAction("↻", "Update Status", "Move flight to next state", ORANGE, this::showUpdateStatusDialog));
        card.add(actions, BorderLayout.CENTER);

        saveStateLabel.setForeground(MUTED);
        saveStateLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        saveStateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(saveStateLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel quickAction(String icon, String title, String subtitle, Color accent, Runnable action) {
        RoundedPanel p = new RoundedPanel(14, new Color(248, 250, 253));
        p.setLayout(new BorderLayout(11, 0));
        p.setBorder(new EmptyBorder(10, 11, 10, 11));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel i = new JLabel(icon, SwingConstants.CENTER);
        i.setPreferredSize(new Dimension(38, 38));
        i.setOpaque(true);
        i.setBackground(lighten(accent, 0.88f));
        i.setForeground(accent);
        i.setFont(new Font("SansSerif", Font.BOLD, 17));
        i.setBorder(new RoundedBorder(lighten(accent, 0.88f), 12, 0));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setForeground(TEXT);
        t.setFont(new Font("SansSerif", Font.BOLD, 12));
        JLabel s = new JLabel(subtitle);
        s.setForeground(MUTED);
        s.setFont(new Font("SansSerif", Font.PLAIN, 10));
        texts.add(t);
        texts.add(Box.createVerticalStrut(2));
        texts.add(s);

        JLabel arrow = new JLabel("›");
        arrow.setForeground(MUTED);
        arrow.setFont(new Font("SansSerif", Font.BOLD, 20));
        p.add(i, BorderLayout.WEST);
        p.add(texts, BorderLayout.CENTER);
        p.add(arrow, BorderLayout.EAST);

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { p.setFill(new Color(240, 245, 252)); }
            @Override public void mouseExited(MouseEvent e) { p.setFill(new Color(248, 250, 253)); }
            @Override public void mouseClicked(MouseEvent e) { action.run(); }
        };
        p.addMouseListener(mouse);
        for (Component c : p.getComponents()) c.addMouseListener(mouse);
        return p;
    }

    // =========================================================
    // DATA PAGES
    // =========================================================

    private JPanel createPassengersPanel() {
        JPanel panel = pagePanel();
        RoundedPanel card = dataCard();
        card.setLayout(new BorderLayout(0, 14));

        JPanel toolbar = createToolbar(
                "Passenger Directory",
                "Search, add and book registered passengers",
                passengerTable,
                new ModernButton("+ Add Passenger", BLUE, Color.WHITE),
                new ModernButton("Book on Flight", CYAN, NAVY)
        );
        Component[] toolbarButtons = ((JPanel) toolbar.getComponent(1)).getComponents();
        ((JButton) toolbarButtons[1]).addActionListener(e -> showAddPassengerDialog());
        ((JButton) toolbarButtons[2]).addActionListener(e -> showBookingDialog());
        card.add(toolbar, BorderLayout.NORTH);

        card.add(cleanScroll(passengerTable), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStaffPanel() {
        JPanel panel = pagePanel();
        RoundedPanel card = dataCard();
        card.setLayout(new BorderLayout(0, 14));

        JPanel toolbar = createToolbar(
                "Airport Staff",
                "Pilots and flight attendants currently registered",
                staffTable,
                new ModernButton("+ Add Pilot", PURPLE, Color.WHITE),
                new ModernButton("+ Add Attendant", BLUE, Color.WHITE)
        );
        Component[] toolbarButtons = ((JPanel) toolbar.getComponent(1)).getComponents();
        ((JButton) toolbarButtons[1]).addActionListener(e -> showAddPilotDialog());
        ((JButton) toolbarButtons[2]).addActionListener(e -> showAddAttendantDialog());
        card.add(toolbar, BorderLayout.NORTH);
        card.add(cleanScroll(staffTable), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFlightsPanel() {
        JPanel panel = pagePanel();

        RoundedPanel summary = new RoundedPanel(18, new Color(234, 241, 255));
        summary.setLayout(new BorderLayout());
        summary.setBorder(new EmptyBorder(13, 16, 13, 16));
        JLabel icon = new JLabel("✈");
        icon.setForeground(BLUE);
        icon.setFont(new Font("SansSerif", Font.BOLD, 24));
        flightSummaryLabel.setForeground(TEXT);
        flightSummaryLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        summary.add(icon, BorderLayout.WEST);
        summary.add(flightSummaryLabel, BorderLayout.CENTER);
        panel.add(summary, BorderLayout.NORTH);

        RoundedPanel card = dataCard();
        card.setLayout(new BorderLayout(0, 14));
        JPanel toolbar = createToolbar(
                "Flight Operations",
                "Create flights, manage bookings and control flight status",
                flightTable,
                new ModernButton("+ Create Flight", BLUE, Color.WHITE),
                new ModernButton("Book Passenger", CYAN, NAVY),
                new ModernButton("Update Status", ORANGE, Color.WHITE)
        );
        Component[] toolbarButtons = ((JPanel) toolbar.getComponent(1)).getComponents();
        ((JButton) toolbarButtons[1]).addActionListener(e -> showCreateFlightDialog());
        ((JButton) toolbarButtons[2]).addActionListener(e -> showBookingDialog());
        ((JButton) toolbarButtons[3]).addActionListener(e -> showUpdateStatusDialog());
        card.add(toolbar, BorderLayout.NORTH);
        styleStatusColumn(flightTable, 5);
        card.add(cleanScroll(flightTable), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = pagePanel();
        RoundedPanel card = dataCard();
        card.setLayout(new BorderLayout(0, 13));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        labels.add(sectionTitle("Daily Operations Report"));
        labels.add(Box.createVerticalStrut(3));
        labels.add(sectionSubtitle("Generated from the current in-memory airport data"));
        head.add(labels, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        ModernButton refresh = new ModernButton("Refresh Report", BLUE, Color.WHITE);
        ModernButton save = new ModernButton("Save Data", GREEN, Color.WHITE);
        refresh.addActionListener(e -> refreshReport());
        save.addActionListener(e -> saveData(true));
        actions.add(refresh);
        actions.add(save);
        head.add(actions, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        reportArea.setForeground(TEXT);
        reportArea.setBackground(new Color(249, 251, 254));
        reportArea.setBorder(new EmptyBorder(15, 15, 15, 15));
        reportArea.setLineWrap(false);
        JScrollPane reportScroll = cleanScroll(reportArea);
        reportScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        card.add(reportScroll, BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel pagePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        return p;
    }

    private RoundedPanel dataCard() {
        RoundedPanel card = new RoundedPanel(20, CARD);
        card.setBorder(new EmptyBorder(18, 18, 16, 18));
        return card;
    }

    private JPanel createToolbar(String title, String subtitle, JTable table, ModernButton... buttons) {
        JPanel root = new JPanel(new BorderLayout(12, 0));
        root.setOpaque(false);

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        labels.add(sectionTitle(title));
        labels.add(Box.createVerticalStrut(3));
        labels.add(sectionSubtitle(subtitle));
        root.add(labels, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JTextField search = new JTextField();
        search.setPreferredSize(new Dimension(185, 36));
        search.putClientProperty("JTextField.placeholderText", "Search...");
        search.setToolTipText("Type to filter this table");
        search.setBorder(new CompoundRoundedBorder(BORDER, 12, new EmptyBorder(7, 11, 7, 11)));
        attachSearch(search, table);
        right.add(search);
        for (ModernButton button : buttons) {
            button.setPreferredSize(new Dimension(Math.max(118, button.getPreferredSize().width + 16), 36));
            right.add(button);
        }
        root.add(right, BorderLayout.EAST);
        return root;
    }

    // =========================================================
    // DIALOGS / ACTIONS
    // =========================================================

    private void showAddPassengerDialog() {
        JTextField name = field();
        JTextField nationalId = field();
        JTextField nationality = field("Palestinian");
        JTextField passport = field();
        JTextField destination = field();
        JTextField expiry = field("2030-01-01");

        JPanel form = createFormPanel();
        addField(form, "Full Name", name);
        addField(form, "National ID", nationalId);
        addField(form, "Nationality", nationality);
        addField(form, "Passport Number", passport);
        addField(form, "Destination", destination);
        addField(form, "Passport Expiry (YYYY-MM-DD)", expiry);

        if (!showFormDialog("Add Passenger", "Register a new traveler in GIAMS", form)) return;

        try {
            requireText(name, "Name");
            requireText(nationalId, "National ID");
            requireText(passport, "Passport Number");
            requireText(destination, "Destination");
            LocalDate date = LocalDate.parse(expiry.getText().trim());

            Passenger passenger = new Passenger(
                    name.getText().trim(), nationalId.getText().trim(), nationality.getText().trim(),
                    passport.getText().trim(), destination.getText().trim(), date
            );
            airport.addPerson(passenger);
            markUnsaved();
            refreshAll();
            showToast("Passenger added successfully", GREEN);
        } catch (DateTimeParseException ex) {
            showError("Invalid date. Use YYYY-MM-DD.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showAddPilotDialog() {
        JTextField name = field();
        JTextField nationalId = field();
        JTextField nationality = field("Palestinian");
        JTextField salary = field();
        JComboBox<Shift> shift = combo(Shift.values());
        JTextField license = field();
        JTextField certificate = field();
        JTextField hours = field();

        JPanel form = createFormPanel();
        addField(form, "Full Name", name);
        addField(form, "National ID", nationalId);
        addField(form, "Nationality", nationality);
        addField(form, "Salary", salary);
        addField(form, "Shift", shift);
        addField(form, "License", license);
        addField(form, "Certificate", certificate);
        addField(form, "Flight Hours", hours);

        if (!showFormDialog("Add Pilot", "Add a pilot to airport staff", form)) return;

        try {
            requireText(name, "Name");
            requireText(nationalId, "National ID");
            requireText(license, "License");
            requireText(certificate, "Certificate");
            double salaryValue = parsePositiveDouble(salary.getText(), "Salary");
            int hoursValue = parseNonNegativeInt(hours.getText(), "Flight Hours");

            Pilot pilot = new Pilot(
                    name.getText().trim(), nationalId.getText().trim(), nationality.getText().trim(),
                    salaryValue, (Shift) shift.getSelectedItem(), license.getText().trim(),
                    certificate.getText().trim(), hoursValue
            );
            airport.addPerson(pilot);
            markUnsaved();
            refreshAll();
            showToast("Pilot added successfully", PURPLE);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showAddAttendantDialog() {
        JTextField name = field();
        JTextField nationalId = field();
        JTextField nationality = field("Palestinian");
        JTextField salary = field();
        JComboBox<Shift> shift = combo(Shift.values());
        JTextField languages = field("Arabic,English");
        JTextField numberOfFlights = field("0");

        JPanel form = createFormPanel();
        addField(form, "Full Name", name);
        addField(form, "National ID", nationalId);
        addField(form, "Nationality", nationality);
        addField(form, "Salary", salary);
        addField(form, "Shift", shift);
        addField(form, "Languages (comma-separated)", languages);
        addField(form, "Number of Flights", numberOfFlights);

        if (!showFormDialog("Add Flight Attendant", "Register a cabin crew member", form)) return;

        try {
            requireText(name, "Name");
            requireText(nationalId, "National ID");
            double salaryValue = parsePositiveDouble(salary.getText(), "Salary");
            int flightsValue = parseNonNegativeInt(numberOfFlights.getText(), "Number of Flights");

            ArrayList<String> languageList = new ArrayList<>();
            Arrays.stream(languages.getText().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).forEach(languageList::add);
            if (languageList.isEmpty()) throw new IllegalArgumentException("Enter at least one language.");

            FlightAttendant attendant = new FlightAttendant(
                    name.getText().trim(), nationalId.getText().trim(), nationality.getText().trim(),
                    salaryValue, (Shift) shift.getSelectedItem(), languageList, flightsValue
            );
            airport.addPerson(attendant);
            markUnsaved();
            refreshAll();
            showToast("Flight attendant added successfully", BLUE);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showCreateFlightDialog() {
        ArrayList<Pilot> pilots = getPilots();
        if (pilots.isEmpty()) {
            showError("No pilots found. Add a pilot first.");
            return;
        }

        JTextField destination = field();
        JSpinner capacity = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 1));
        capacity.setPreferredSize(new Dimension(290, 36));
        JComboBox<Pilot> pilotBox = new JComboBox<>(pilots.toArray(new Pilot[0]));
        pilotBox.setRenderer(personRenderer("License", p -> ((Pilot) p).getLicense()));
        pilotBox.setPreferredSize(new Dimension(290, 36));

        JPanel form = createFormPanel();
        addField(form, "Destination", destination);
        addField(form, "Capacity", capacity);
        addField(form, "Pilot", pilotBox);

        if (!showFormDialog("Create Flight", "Create a new scheduled flight", form)) return;

        try {
            requireText(destination, "Destination");
            Pilot selectedPilot = (Pilot) pilotBox.getSelectedItem();
            Flight flight = new Flight(destination.getText().trim(), selectedPilot,
                    (Integer) capacity.getValue(), new ArrayList<>());
            airport.addFlight(flight);
            markUnsaved();
            refreshAll();
            showToast("Flight " + flight.getId() + " created successfully", BLUE);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showBookingDialog() {
        ArrayList<Flight> flights = airport.getFlights();
        ArrayList<Passenger> passengers = getPassengers();
        if (flights.isEmpty()) { showError("No flights available."); return; }
        if (passengers.isEmpty()) { showError("No passengers available."); return; }

        JComboBox<Flight> flightBox = new JComboBox<>(flights.toArray(new Flight[0]));
        flightBox.setRenderer(flightRenderer());
        flightBox.setPreferredSize(new Dimension(330, 36));
        JComboBox<Passenger> passengerBox = new JComboBox<>(passengers.toArray(new Passenger[0]));
        passengerBox.setRenderer(personRenderer("ID", Person::getNationalId));
        passengerBox.setPreferredSize(new Dimension(330, 36));

        JPanel form = createFormPanel();
        addField(form, "Flight", flightBox);
        addField(form, "Passenger", passengerBox);

        if (!showFormDialog("Book Passenger", "Assign a passenger to an existing flight", form)) return;

        Flight flight = (Flight) flightBox.getSelectedItem();
        Passenger passenger = (Passenger) passengerBox.getSelectedItem();
        try {
            flight.bookPassenger(passenger);
            markUnsaved();
            refreshAll();
            showToast(passenger.getName() + " booked on Flight " + flight.getId(), CYAN);
        } catch (DuplicatePassengerException | FlightFullException ex) {
            showError("Booking failed: " + ex.getMessage());
        }
    }

    private void showUpdateStatusDialog() {
        if (airport.getFlights().isEmpty()) { showError("No flights available."); return; }

        JComboBox<Flight> flightBox = new JComboBox<>(airport.getFlights().toArray(new Flight[0]));
        flightBox.setRenderer(flightRenderer());
        flightBox.setPreferredSize(new Dimension(340, 36));
        JComboBox<FlightStatus> statusBox = combo(FlightStatus.values());

        JPanel form = createFormPanel();
        addField(form, "Flight", flightBox);
        addField(form, "New Status", statusBox);

        if (!showFormDialog("Update Flight Status", "Only valid status transitions will be accepted", form)) return;

        Flight flight = (Flight) flightBox.getSelectedItem();
        FlightStatus newStatus = (FlightStatus) statusBox.getSelectedItem();
        try {
            flight.changeStatus(newStatus);
            markUnsaved();
            refreshAll();
            showToast("Flight " + flight.getId() + " is now " + newStatus, statusColor(newStatus));
        } catch (InvalidFlightStatusException ex) {
            showError("Invalid status change: " + ex.getMessage());
        }
    }

    private boolean showFormDialog(String title, String subtitle, JPanel form) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 14));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(12, 12, 8, 12));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setForeground(TEXT);
        t.setFont(new Font("SansSerif", Font.BOLD, 20));
        JLabel s = new JLabel(subtitle);
        s.setForeground(MUTED);
        s.setFont(new Font("SansSerif", Font.PLAIN, 12));
        head.add(t);
        head.add(Box.createVerticalStrut(4));
        head.add(s);
        wrapper.add(head, BorderLayout.NORTH);
        wrapper.add(form, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this, wrapper, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        return result == JOptionPane.OK_OPTION;
    }

    // =========================================================
    // REFRESH / SAVE
    // =========================================================

    private void refreshAll() {
        refreshSummary();
        refreshPassengers();
        refreshStaff();
        refreshFlights();
        refreshRecentFlights();
        refreshReport();
    }

    private void refreshSummary() {
        int passengers = 0;
        int staff = 0;
        for (Person person : airport.getPeople()) {
            if (person instanceof Passenger) passengers++;
            if (person instanceof Employee) staff++;
        }
        peopleCountLabel.setText(String.valueOf(airport.getPeople().size()));
        passengerCountLabel.setText(String.valueOf(passengers));
        staffCountLabel.setText(String.valueOf(staff));
        flightCountLabel.setText(String.valueOf(airport.getFlights().size()));
        departedCountLabel.setText(String.valueOf(Flight.getTotalFlightsToday()));
    }

    private void refreshPassengers() {
        passengerModel.setRowCount(0);
        for (Person person : airport.getPeople()) {
            if (person instanceof Passenger) {
                Passenger p = (Passenger) person;
                passengerModel.addRow(new Object[]{
                        p.getId(), p.getNationalId(), p.getName(), p.getNationality(),
                        p.getPassportNumber(), p.getDestination(), p.getPassportExpiry()
                });
            }
        }
    }

    private void refreshStaff() {
        staffModel.setRowCount(0);
        for (Person person : airport.getPeople()) {
            if (person instanceof Pilot) {
                Pilot p = (Pilot) person;
                staffModel.addRow(new Object[]{
                        "Pilot", p.getNationalId(), p.getName(), p.getNationality(),
                        String.format("$%.0f", p.getSalary()), p.getShift(),
                        p.getLicense() + " • " + p.getFlightHours() + " hrs"
                });
            } else if (person instanceof FlightAttendant) {
                FlightAttendant a = (FlightAttendant) person;
                staffModel.addRow(new Object[]{
                        "Flight Attendant", a.getNationalId(), a.getName(), a.getNationality(),
                        String.format("$%.0f", a.getSalary()), a.getShift(),
                        String.join(", ", a.getLanguages()) + " • " + a.getNumberOfFlights() + " flights"
                });
            }
        }
    }

    private void refreshFlights() {
        flightModel.setRowCount(0);
        int scheduled = 0, boarding = 0, departed = 0, cancelled = 0;
        for (Flight f : airport.getFlights()) {
            flightModel.addRow(new Object[]{
                    "#" + f.getId(), f.getDestination(),
                    f.getPilot() != null ? f.getPilot().getName() : "None",
                    f.getCapacity(), f.getPassengers().size() + " / " + f.getCapacity(), f.getFlightStatus()
            });
            switch (f.getFlightStatus()) {
                case SCHEDULED: scheduled++; break;
                case BOARDING: boarding++; break;
                case DEPARTED: departed++; break;
                case CANCELLED: cancelled++; break;
            }
        }
        flightSummaryLabel.setText("   " + scheduled + " scheduled   •   " + boarding + " boarding   •   " +
                departed + " departed   •   " + cancelled + " cancelled");
    }

    private void refreshRecentFlights() {
        recentFlightModel.setRowCount(0);
        ArrayList<Flight> flights = airport.getFlights();
        int start = Math.max(0, flights.size() - 5);
        for (int i = flights.size() - 1; i >= start; i--) {
            Flight f = flights.get(i);
            recentFlightModel.addRow(new Object[]{
                    "#" + f.getId(), f.getDestination(),
                    f.getPilot() != null ? f.getPilot().getName() : "None",
                    f.getPassengers().size() + "/" + f.getCapacity(), f.getFlightStatus()
            });
        }
    }

    private void refreshReport() {
        reportArea.setText(airport.buildDailyReport());
        reportArea.setCaretPosition(0);
    }

    private void saveData(boolean notify) {
        FileManager.savePeople(passengersFilePath, airport, Passenger.class);
        FileManager.savePeople(pilotsFilePath, airport, Pilot.class);
        FileManager.savePeople(attendantsFilePath, airport, FlightAttendant.class);
        FileManager.saveFlights(flightsLogFilePath, airport);
        saveStateLabel.setText("Saved just now");
        saveStateLabel.setForeground(GREEN);
        if (notify) showToast("All data saved successfully", GREEN);
    }

    private void markUnsaved() {
        saveStateLabel.setText("Unsaved changes");
        saveStateLabel.setForeground(ORANGE);
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private void configureGlobalUI() {
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TextField.font", new Font("SansSerif", Font.PLAIN, 13));
        UIManager.put("ComboBox.font", new Font("SansSerif", Font.PLAIN, 13));
        UIManager.put("Spinner.font", new Font("SansSerif", Font.PLAIN, 13));
        UIManager.put("Button.font", new Font("SansSerif", Font.BOLD, 12));
        UIManager.put("Table.font", new Font("SansSerif", Font.PLAIN, 12));
        UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 11));
        UIManager.put("ToolTip.background", NAVY_2);
        UIManager.put("ToolTip.foreground", Color.WHITE);
    }

    private void startClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM  •  HH:mm:ss");
        Runnable update = () -> clockLabel.setText(LocalDateTime.now().format(formatter));
        update.run();
        clockTimer = new Timer(1000, e -> update.run());
        clockTimer.start();
    }

    private static JLabel statValueLabel() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("SansSerif", Font.BOLD, 27));
        return label;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(new Font("SansSerif", Font.BOLD, 17));
        return label;
    }

    private JLabel sectionSubtitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return label;
    }

    private JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setForeground(BLUE);
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JTextField field() { return field(""); }

    private JTextField field(String text) {
        JTextField f = new JTextField(text);
        f.setPreferredSize(new Dimension(290, 36));
        f.setForeground(TEXT);
        f.setBackground(Color.WHITE);
        f.setCaretColor(BLUE);
        f.setBorder(new CompoundRoundedBorder(BORDER, 10, new EmptyBorder(7, 10, 7, 10)));
        return f;
    }

    private <T> JComboBox<T> combo(T[] values) {
        JComboBox<T> box = new JComboBox<>(values);
        box.setPreferredSize(new Dimension(290, 36));
        box.setBackground(Color.WHITE);
        box.setForeground(TEXT);
        return box;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(2, 0, 2, 0));
        return panel;
    }

    private void addField(JPanel panel, String label, Component component) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.gridy = panel.getComponentCount() / 2;

        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel l = new JLabel(label);
        l.setForeground(TEXT);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setPreferredSize(new Dimension(180, 28));
        panel.add(l, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }

    private static JTable modernTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(37);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(236, 240, 246));
        table.setSelectionBackground(new Color(230, 239, 255));
        table.setSelectionForeground(TEXT);
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setBackground(new Color(248, 250, 253));
        table.getTableHeader().setForeground(new Color(84, 98, 120));
        table.getTableHeader().setReorderingAllowed(false);
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        table.setFillsViewportHeight(true);
        return table;
    }

    private JScrollPane cleanScroll(Component component) {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    private void styleStatusColumn(JTable table, int columnIndex) {
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("SansSerif", Font.BOLD, 10));
                if (!isSelected && value instanceof FlightStatus) {
                    Color c = statusColor((FlightStatus) value);
                    label.setForeground(c);
                    label.setBackground(lighten(c, 0.90f));
                    label.setOpaque(true);
                }
                return label;
            }
        });
    }

    private void attachSearch(JTextField search, JTable table) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) table.getModel());
        table.setRowSorter(sorter);
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String text = search.getText().trim();
                sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });
    }

    private void requireText(JTextField field, String fieldName) {
        if (field.getText().trim().isEmpty()) throw new IllegalArgumentException(fieldName + " is required.");
    }

    private double parsePositiveDouble(String text, String fieldName) {
        try {
            double value = Double.parseDouble(text.trim());
            if (value < 0) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid non-negative number.");
        }
    }

    private int parseNonNegativeInt(String text, String fieldName) {
        try {
            int value = Integer.parseInt(text.trim());
            if (value < 0) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid non-negative integer.");
        }
    }

    private ArrayList<Passenger> getPassengers() {
        ArrayList<Passenger> list = new ArrayList<>();
        for (Person p : airport.getPeople()) if (p instanceof Passenger) list.add((Passenger) p);
        return list;
    }

    private ArrayList<Pilot> getPilots() {
        ArrayList<Pilot> list = new ArrayList<>();
        for (Person p : airport.getPeople()) if (p instanceof Pilot) list.add((Pilot) p);
        return list;
    }

    private static DefaultTableModel nonEditableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private ListCellRenderer<Object> personRenderer(String detailName, DetailProvider detailProvider) {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Person) {
                    Person p = (Person) value;
                    setText(p.getName() + "   •   " + detailName + ": " + detailProvider.get(p));
                }
                setBorder(new EmptyBorder(6, 8, 6, 8));
                return this;
            }
        };
    }

    private ListCellRenderer<Object> flightRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Flight) {
                    Flight f = (Flight) value;
                    setText("Flight #" + f.getId() + "  →  " + f.getDestination() + "   •   " + f.getFlightStatus());
                }
                setBorder(new EmptyBorder(6, 8, 6, 8));
                return this;
            }
        };
    }

    private void showToast(String message, Color accent) {
        final JDialog toast = new JDialog(this, false);
        toast.setUndecorated(true);
        RoundedPanel body = new RoundedPanel(16, NAVY_2);
        body.setLayout(new BorderLayout(10, 0));
        body.setBorder(new EmptyBorder(11, 14, 11, 16));
        JLabel dot = new JLabel("●");
        dot.setForeground(accent);
        JLabel text = new JLabel(message);
        text.setForeground(Color.WHITE);
        text.setFont(new Font("SansSerif", Font.BOLD, 12));
        body.add(dot, BorderLayout.WEST);
        body.add(text, BorderLayout.CENTER);
        toast.setContentPane(body);
        toast.pack();
        Point frame = getLocationOnScreen();
        toast.setLocation(frame.x + getWidth() - toast.getWidth() - 28,
                frame.y + getHeight() - toast.getHeight() - 45);
        toast.setVisible(true);
        Timer timer = new Timer(2400, e -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "GIAMS", JOptionPane.ERROR_MESSAGE);
    }

    private static Color statusColor(FlightStatus status) {
        if (status == null) return MUTED;
        switch (status) {
            case SCHEDULED: return BLUE;
            case BOARDING: return ORANGE;
            case DEPARTED: return GREEN;
            case CANCELLED: return RED;
            default: return MUTED;
        }
    }

    private static Color lighten(Color color, float amount) {
        int r = (int) (color.getRed() + (255 - color.getRed()) * amount);
        int g = (int) (color.getGreen() + (255 - color.getGreen()) * amount);
        int b = (int) (color.getBlue() + (255 - color.getBlue()) * amount);
        return new Color(Math.min(255, r), Math.min(255, g), Math.min(255, b));
    }

    private interface DetailProvider { String get(Person person); }

    // =========================================================
    // CUSTOM COMPONENTS
    // =========================================================

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private Color fill;

        RoundedPanel(int radius, Color fill) {
            this.radius = radius;
            this.fill = fill;
            setOpaque(false);
        }

        void setFill(Color fill) {
            this.fill = fill;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class GradientPanel extends JPanel {
        private final int radius;
        private final Color start;
        private final Color end;

        GradientPanel(int radius, Color start, Color end) {
            this.radius = radius;
            this.start = start;
            this.end = end;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setClip(shape);
            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 20));
            g2.fillOval(getWidth() - 190, -70, 250, 250);
            g2.setColor(new Color(255, 255, 255, 13));
            g2.fillOval(getWidth() - 330, 55, 190, 190);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ModernButton extends JButton {
        private final Color base;
        private final Color hover;
        private boolean hovered;

        ModernButton(String text, Color base, Color foreground) {
            super(text);
            this.base = base;
            this.hover = base.brighter();
            setForeground(foreground);
            setFont(new Font("SansSerif", Font.BOLD, 11));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 13, 8, 13));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovered ? hover : base);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class NavButton extends JButton {
        private boolean active;
        private boolean hovered;

        NavButton(String icon, String title) {
            super(icon + "    " + title);
            setHorizontalAlignment(SwingConstants.LEFT);
            setForeground(new Color(180, 193, 214));
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, 14, 0, 14));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            setForeground(active ? Color.WHITE : new Color(180, 193, 214));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active || hovered) {
                g2.setColor(active ? new Color(53, 79, 122) : new Color(31, 47, 74));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                if (active) {
                    g2.setColor(CYAN);
                    g2.fillRoundRect(0, 9, 4, getHeight() - 18, 4, 4);
                }
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedBorder implements Border {
        private final Color color;
        private final int radius;
        private final int thickness;

        RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override public Insets getBorderInsets(Component c) { return new Insets(thickness, thickness, thickness, thickness); }
        @Override public boolean isBorderOpaque() { return false; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            if (thickness <= 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + thickness / 2, y + thickness / 2,
                    width - thickness, height - thickness, radius, radius);
            g2.dispose();
        }
    }

    private static class CompoundRoundedBorder implements Border {
        private final Border outer;
        private final Border inner;

        CompoundRoundedBorder(Color color, int radius, Border inner) {
            this.outer = new RoundedBorder(color, radius, 1);
            this.inner = inner;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            Insets o = outer.getBorderInsets(c);
            Insets i = inner.getBorderInsets(c);
            return new Insets(o.top + i.top, o.left + i.left, o.bottom + i.bottom, o.right + i.right);
        }

        @Override public boolean isBorderOpaque() { return false; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            outer.paintBorder(c, g, x, y, width, height);
        }
    }
}
