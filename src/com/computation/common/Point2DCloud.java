package com.computation.common;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

public class Point2DCloud {

    public static final int GRID_SPACING = 40;
    public static int DPI_SCALING = 2;
    private JPanel panel;
    private JFrame frame;
    private JTable props;
    private List<Point2D> point2Ds;
    private Set<Edge> polygon;
    private HashMap<String, Integer> fieldsMap;
    private HashMap<String, JButton> buttonsMap;
    private HashMap<String, JButton> topButtonsMap;
    private DefaultTableModel model;
    private JPanel buttons;
    private boolean drawEnabled;
    private JPanel topButtons;

    public Point2DCloud(final int count, final int width, final int height, boolean drawEnabled) {

        this.drawEnabled = drawEnabled;

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    model = new DefaultTableModel() {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };

                    buttons = new JPanel();
                    topButtons = new JPanel();
                    panel = new PointPanel();
                    frame = new JFrame();
                    fieldsMap = new HashMap<String, Integer>();
                    polygon = new HashSet<Edge>();
                    point2Ds = Utils.generateRandomPoints(count, width, height, 50);
                    props = new JTable(model);

                    buttonsMap = new HashMap<String, JButton>();
                    buttons.setLayout(new FlowLayout(FlowLayout.CENTER));

                    topButtonsMap = new HashMap<String, JButton>();
                    topButtons.setLayout(new FlowLayout(FlowLayout.LEFT));

                    model.addColumn("Property");
                    model.addColumn("Value");

                    panel.setPreferredSize(new Dimension(width, height));
                    panel.setSize(width, height);

                    props.setRowHeight(25 * DPI_SCALING);
                    props.setFocusable(false);
                    props.setIntercellSpacing(new Dimension(8 * DPI_SCALING, 8 * DPI_SCALING));
                    props.setCellSelectionEnabled(false);
                    props.setTableHeader(null);

                    frame.getContentPane().setLayout(new BorderLayout());
                    frame.getContentPane().add(panel, BorderLayout.CENTER);
                    frame.getContentPane().add(new JScrollPane(props), BorderLayout.EAST);
                    frame.getContentPane().add(buttons, BorderLayout.SOUTH);
                    frame.getContentPane().add(topButtons, BorderLayout.NORTH);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setResizable(false);
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void show() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public void addTopButton(final String name, final Runnable runnable) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JButton jButton = new JButton(name);
                jButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new Thread(runnable).start();
                    }
                });

                topButtonsMap.put(name, jButton);
                topButtons.add(jButton);
                frame.setResizable(true);
                frame.pack();
                frame.setResizable(false);
            }
        });
    }

    public void addButton(final String name, final Runnable runnable) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JButton jButton = null;

                if(buttonsMap.containsKey(name)){
                    jButton = buttonsMap.get(name);
                    for(ActionListener ac : jButton.getActionListeners()){
                        jButton.removeActionListener(ac);
                    }
                } else {
                    jButton = new JButton(name);
                    buttons.add(jButton);
                }

                jButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new Thread(runnable).start();
                    }
                });

                buttonsMap.put(name, jButton);

                frame.setResizable(true);
                frame.pack();
                frame.setResizable(false);
            }
        });
    }

    public void toggleButton(final String name, final boolean toggle){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                buttonsMap.get(name).setName(name + (toggle ? " (on)" : " (off)"));
                frame.pack();
            }
        });
    }

    public void enableButton(final String name, final boolean e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (buttonsMap.containsKey(name)) {
                    buttonsMap.get(name).setEnabled(e);
                }
            }
        });
    }

    public <T> void setField(final String key, final T value) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int row;
                if (fieldsMap.containsKey(key)) {
                    row = fieldsMap.get(key);
                    model.removeRow(row);
                } else {
                    row = model.getRowCount();
                    fieldsMap.put(key, row);
                }

                model.insertRow(row, new String[]{key, value + ""});
            }
        });
    }

    public void setName(final String name) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.setTitle(name);
            }
        });
    }

    public void toast(String msg) {
        JOptionPane.showMessageDialog(frame, msg);
    }

    public List<Point2D> getPoints() {
        return new ArrayList<Point2D>(point2Ds);
    }

    public void removeAllEdges(){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                polygon.removeAll(polygon);
            }
        });
    }

    public void draw() {
        if (!drawEnabled) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Point2DCloud.this.panel.repaint();
            }
        });
    }

    public void addEdge(Edge edge) {
        if (!drawEnabled) {
            return;
        }

        // Thread-safe
        final Edge edgeCpy = new Edge(edge);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Point2DCloud.this.polygon.add(edgeCpy);
                Point2DCloud.this.panel.repaint();
            }
        });
    }

    public void removeEdge(Edge edge) {
        if (!drawEnabled) {
            return;
        }

        // Thread-safe
        final Edge edgeCpy = new Edge(edge);
        final Edge edgeFlip = new Edge(edge.p2, edge.p1);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Point2DCloud.this.polygon.remove(edgeCpy);
                Point2DCloud.this.polygon.remove(edgeFlip);
                Point2DCloud.this.panel.repaint();
            }
        });
    }

    private class PointPanel extends JPanel {

        protected void paintGrid(Graphics2D g2d) {

            int height = getHeight();
            int width = getWidth();

            g2d.setStroke(new BasicStroke(DPI_SCALING));
            g2d.setColor(new Color(0x222222));

            for (int i = GRID_SPACING; i < height; i += GRID_SPACING) {
                g2d.drawLine(0, i, width, i);
            }

            for (int i = GRID_SPACING; i < width; i += GRID_SPACING) {
                g2d.drawLine(i, 0, i, height);
            }
        }

        protected void paintClear(Graphics2D g2d) {
            g2d.setColor(Color.BLACK);
            g2d.setBackground(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        protected void paintEdges(Graphics2D g2d) {
            g2d.setStroke(new BasicStroke(DPI_SCALING * 3));

            for (Edge edge : polygon) {
                g2d.setColor(edge.getColor());
                g2d.drawLine(edge.p1.x, edge.p1.y, edge.p2.x, edge.p2.y);
            }
        }

        protected void paintPoints(Graphics2D g2d) {
            g2d.setStroke(new BasicStroke(1));
            g2d.setColor(Color.WHITE);

            for (Point2D p : point2Ds) {
                g2d.setColor(p.getColor());
                int mlp = p.getColor() != Point2D.UNVISITED ? 2 : 1;
                g2d.fillOval(p.x - DPI_SCALING * 3 * mlp, p.y - DPI_SCALING * 3 * mlp, DPI_SCALING * 6 * mlp, DPI_SCALING * 6 * mlp);
                g2d.drawString(p.debugText, p.x + 10, p.y);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!drawEnabled) {
                return;
            }

            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            paintClear(g2d);
            paintGrid(g2d);
            paintEdges(g2d);
            paintPoints(g2d);
        }
    }

}
