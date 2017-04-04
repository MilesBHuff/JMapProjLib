/*
 * ProjectionSelectionPanel.java
 *
 * Created on September 16, 2006, 2:54 PM
 */
package ch.ethz.karto.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jhlabs.map.Ellipsoid;
import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

/**
 * ProjectionSelectionPanel lets the user select a projection, applies the
 * selected projection to a group of lines, and displays basic information about
 * the projection.
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ProjectionSelectionPanel extends javax.swing.JPanel {

    /**
     * The lines that are displayed. Must be in geographic coordinates
     * (degrees).
     */
    private ArrayList<MapLine> lines = null;

    private JLabel descriptionLabel;
    private JPanel infoPanel;
    private JCheckBox inverseCheckBox;
    private JLabel lon0Label;
    private JSlider lon0Slider;
    private MapComponent map;
    private JButton nextProjectionButton;
    private JButton previousProjectionButton;
    private JComboBox projectionComboBox;
    private JPanel selectionPanel;

    /**
     * Creates new form ProjectionSelectionPanel
     */
    public ProjectionSelectionPanel() {
        initComponents();

        Object[] projNames = ProjectionFactory.getOrderedProjectionNames();
        projectionComboBox.setModel(new DefaultComboBoxModel(projNames));
    }

    private void project() {

        boolean inverse = inverseCheckBox.isSelected();
        try {
            // find the selected name, create the corresponding projection.
            String projName = (String) projectionComboBox.getSelectedItem();
            Projection projection = ProjectionFactory.getNamedProjection(projName);

            // use the selected projection to project the lines,
            // and pass the projected lines to the map to display.
            if (projection != null) {
                projection.setProjectionLongitudeDegrees(lon0Slider.getValue());
                projection.setEllipsoid(Ellipsoid.SPHERE);
                projection.initialize();

                LineProjector projector = new LineProjector();
                ArrayList<MapLine> projectedLines = new ArrayList<>();
                projector.constructGraticule(projectedLines, projection);
                projector.projectLines(lines, projectedLines, projection);
                if (inverse && projection.hasInverse()) {
                    projectedLines = projector.inverse(projectedLines, projection);
                }

                map.setLines(projectedLines);
            } else {
                map.setLines(null);
            }

            // write some descriptive information about the selected projection.
            updateProjectionInfo(projection);

        } catch (Exception exc) {
            String msg = exc.getMessage();
            String title = "Error";
            JOptionPane.showMessageDialog(selectionPanel, msg, title, JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ProjectionSelectionPanel.class.getName()).log(Level.SEVERE, null, exc);
        }
    }

    /**
     * Set the lines that are projected and displayed.
     *
     * @param lines The lines to project. Must be in geographic coordinates
     * (degrees).
     */
    public void setLines(ArrayList<MapLine> lines) {
        // store the passed lines
        this.lines = lines;
        // pass the new lines to the map that displays the lines.
        map.setLines(lines);

        // reset the graphical user interface to the first projection.
        projectionComboBox.setSelectedIndex(0);
        project();
    }

    /**
     * Write basic information about the projection to the graphical user
     * interface.
     *
     * @projection The Projection that provides the information.
     */
    private void updateProjectionInfo(Projection projection) {
        if (projection == null) {
            descriptionLabel.setText("-");
        } else {
            descriptionLabel.setText(projection.getDescription());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        selectionPanel = new JPanel();
        projectionComboBox = new JComboBox();
        previousProjectionButton = new JButton();
        nextProjectionButton = new JButton();
        inverseCheckBox = new JCheckBox();
        map = new MapComponent();
        infoPanel = new JPanel();
        JLabel descriptionLeadLabel = new JLabel();
        descriptionLabel = new JLabel();
        JLabel longitudeLeadLabel = new JLabel();
        lon0Slider = new JSlider();
        lon0Label = new JLabel();

        setLayout(new BorderLayout(10, 10));

        selectionPanel.setPreferredSize(new Dimension(100, 40));
        selectionPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 10));

        projectionComboBox.setMaximumRowCount(40);
        projectionComboBox.setModel(new DefaultComboBoxModel(new String[] { "Plate Carrée (Geographic)", "Cylindrical Equal-Area", "Cylindrical Conformal (Mercator)", "Conical Equidistant", "Conical Equal-Area (Albers)", "Conical Conformal (Lambert)", "Azimuthal Equidistant", "Azimuthal Equal-Area (Lambert)", "Azimuthal Conformal (Stereographic)", "Azimuthal Orthographic", "Sinusoidal", "Pseudoconical Equal-Area (Bonne)" }));
        projectionComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                projectionComboBoxItemStateChanged(evt);
            }
        });
        selectionPanel.add(projectionComboBox);

        previousProjectionButton.setText("<");
        previousProjectionButton.setPreferredSize(new Dimension(50, 29));
        previousProjectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                previousProjectionButtonActionPerformed(evt);
            }
        });
        selectionPanel.add(previousProjectionButton);

        nextProjectionButton.setText(">");
        nextProjectionButton.setPreferredSize(new Dimension(50, 29));
        nextProjectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                nextProjectionButtonActionPerformed(evt);
            }
        });
        selectionPanel.add(nextProjectionButton);

        inverseCheckBox.setText("Test Inverse");
        inverseCheckBox.setToolTipText("Applies forward and inverse projection, which should result in a Plate Carrée projection.");
        inverseCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                inverseCheckBoxActionPerformed(evt);
            }
        });
        selectionPanel.add(inverseCheckBox);

        add(selectionPanel, BorderLayout.NORTH);

        map.setPreferredSize(new Dimension(400, 300));
        add(map, BorderLayout.CENTER);

        infoPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(""), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        infoPanel.setMinimumSize(new Dimension(400, 96));
        infoPanel.setPreferredSize(new Dimension(500, 200));
        infoPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        descriptionLeadLabel.setText("Description");
        constraints.anchor = GridBagConstraints.EAST;
        infoPanel.add(descriptionLeadLabel, constraints);

        descriptionLabel.setText("-");
        descriptionLabel.setMaximumSize(new Dimension(300, 16));
        descriptionLabel.setMinimumSize(new Dimension(300, 16));
        descriptionLabel.setPreferredSize(new Dimension(300, 16));
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 10, 2, 0);
        infoPanel.add(descriptionLabel, constraints);
        constraints.gridwidth = 1;

        longitudeLeadLabel.setText("Longitude of Origin");
        constraints.gridx = 0;
        constraints.gridy = 6;
        infoPanel.add(longitudeLeadLabel, constraints);

        lon0Slider.setMaximum(180);
        lon0Slider.setMinimum(-180);
        lon0Slider.setValue(0);
        lon0Slider.setMinimumSize(new Dimension(200, 29));
        lon0Slider.setPreferredSize(new Dimension(200, 29));
        lon0Slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                lon0SliderStateChanged(evt);
            }
        });
        constraints.gridx = 1;
        constraints.gridy = 6;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 10, 2, 0);
        infoPanel.add(lon0Slider, constraints);

        lon0Label.setText("0");
        lon0Label.setMaximumSize(new Dimension(50, 16));
        lon0Label.setMinimumSize(new Dimension(50, 16));
        lon0Label.setPreferredSize(new Dimension(50, 16));
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 2;
        constraints.gridy = 6;
        infoPanel.add(lon0Label, constraints);

        add(infoPanel, BorderLayout.SOUTH);
    }

    private void lon0SliderStateChanged(ChangeEvent evt) {
        JSlider slider = (JSlider) evt.getSource();
        lon0Label.setText(Integer.toString(slider.getValue()));
        //if (!slider.getValueIsAdjusting()) {
        project();
        //}
    }

    private void projectionComboBoxItemStateChanged(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            project();
        }
    }

    private void inverseCheckBoxActionPerformed(ActionEvent evt) {
        project();
    }

    private void previousProjectionButtonActionPerformed(ActionEvent evt) {
        int id = projectionComboBox.getSelectedIndex() - 1;
        if (id >= 0) {
            projectionComboBox.setSelectedIndex(id);
            project();
        }
    }

    private void nextProjectionButtonActionPerformed(ActionEvent evt) {
        int id = projectionComboBox.getSelectedIndex() + 1;
        if (id < projectionComboBox.getItemCount()) {
            projectionComboBox.setSelectedIndex(id);
            project();
        }
    }

}
