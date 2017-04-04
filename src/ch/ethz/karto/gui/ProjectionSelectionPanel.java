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
import com.jhlabs.map.proj.AzimuthalProjection;
import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

/**
 * ProjectionSelectionPanel lets the user select a projection, applies the
 * selected projection to a group of lines, and displays basic information about
 * the projection.
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ProjectionSelectionPanel extends JPanel {

    private Projection projection = null;

    /**
     * The lines that are displayed. Must be in geographic coordinates
     * (degrees).
     */
    private ArrayList<MapLine> lines = null;

    private JLabel descriptionLabel = new JLabel();
    private JPanel infoPanel = new JPanel();
    private JCheckBox inverseCheckBox = new JCheckBox();
    private JLabel longitudeLeadLabel = new JLabel("Longitude of Origin");
    private JLabel lon0Label = new JLabel("0");
    private JSlider lon0Slider = new JSlider();
    private JLabel latitudeLeadLabel = new JLabel("Latitude of Origin");
    private JLabel lat0Label = new JLabel("0");
    private JSlider lat0Slider = new JSlider();
    private MapComponent map = new MapComponent();
    private JButton nextProjectionButton = new JButton(">");
    private JButton previousProjectionButton = new JButton("<");
    private JComboBox projectionComboBox = new JComboBox();
    private JPanel selectionPanel = new JPanel();

    /**
     * Creates new form ProjectionSelectionPanel
     */
    public ProjectionSelectionPanel() {
        initComponents();

        Object[] projNames = ProjectionFactory.getOrderedProjectionNames();
        projectionComboBox.setModel(new DefaultComboBoxModel(projNames));

        updateControls();
    }

    private void project() {

        boolean inverse = inverseCheckBox.isSelected();
        try {
            // find the selected name, create the corresponding projection.
            String projName = (String) projectionComboBox.getSelectedItem();
            projection = ProjectionFactory.getNamedProjection(projName);

            // use the selected projection to project the lines,
            // and pass the projected lines to the map to display.
            if (projection != null) {
                projection.setProjectionLongitudeDegrees(lon0Slider.getValue());
                projection.setProjectionLatitudeDegrees(lat0Slider.getValue());
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
            updateProjectionInfo();

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
        updateControls();
    }

    /**
     * Write basic information about the projection to the graphical user
     * interface.
     */
    private void updateProjectionInfo() {
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

        setLayout(new BorderLayout(10, 10));

        /*
         * selection panel
         */

        selectionPanel.setPreferredSize(new Dimension(100, 40));
        selectionPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 10));

        projectionComboBox.setMaximumRowCount(40);
        projectionComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                projectionComboBoxItemStateChanged(evt);
            }
        });
        selectionPanel.add(projectionComboBox);

        previousProjectionButton.setPreferredSize(new Dimension(50, 29));
        previousProjectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                previousProjectionButtonActionPerformed(evt);
            }
        });
        selectionPanel.add(previousProjectionButton);

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

        /*
         * map
         */

        map.setPreferredSize(new Dimension(400, 300));
        add(map, BorderLayout.CENTER);

        /*
         * info panel
         */

        infoPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(""), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        infoPanel.setMinimumSize(new Dimension(400, 96));
        infoPanel.setPreferredSize(new Dimension(500, 200));
        infoPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        // description of the selected projection

        constraints.gridy = 0;

        JLabel descriptionLeadLabel = new JLabel("Description");
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

        // Longitude slider

        constraints.gridy = 1;

        constraints.gridx = 0;
        constraints.weightx = 0;
        infoPanel.add(longitudeLeadLabel, constraints);

        setValues(lon0Slider, 180, -180, 0);
        setSliderSizes(lon0Slider);
        lon0Slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                lon0SliderStateChanged(evt);
            }
        });
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 10, 2, 0);
        infoPanel.add(lon0Slider, constraints);

        setLabelSizes(lon0Label);
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 2;
        infoPanel.add(lon0Label, constraints);

        // Latitude slider

        constraints.gridy = 2;

        constraints.gridx = 0;
        constraints.weightx = 0;
        infoPanel.add(latitudeLeadLabel, constraints);

        setValues(lat0Slider, 180, -180, 0);
        setSliderSizes(lat0Slider);
        lat0Slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                lat0SliderStateChanged(evt);
            }
        });
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 10, 2, 0);
        infoPanel.add(lat0Slider, constraints);

        setLabelSizes(lat0Label);
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 2;
        infoPanel.add(lat0Label, constraints);

        add(infoPanel, BorderLayout.SOUTH);
    }

    private void setValues(JSlider slider, int max, int min, int value) {
        slider.setMaximum(max);
        slider.setMinimum(min);
        slider.setValue(value);
    }

    private void setSliderSizes(JSlider slider) {
        slider.setMinimumSize(new Dimension(200, 29));
        slider.setPreferredSize(new Dimension(200, 29));
    }

    private void setLabelSizes(JLabel label) {
        label.setMaximumSize(new Dimension(50, 16));
        label.setMinimumSize(new Dimension(50, 16));
        label.setPreferredSize(new Dimension(50, 16));
    }

    private void lon0SliderStateChanged(ChangeEvent evt) {
        JSlider slider = (JSlider) evt.getSource();
        lon0Label.setText(Integer.toString(slider.getValue()));
        //if (!slider.getValueIsAdjusting()) {
        project();
        //}
    }

    private void lat0SliderStateChanged(ChangeEvent evt) {
        JSlider slider = (JSlider) evt.getSource();
        lat0Label.setText(Integer.toString(slider.getValue()));
        //if (!slider.getValueIsAdjusting()) {
        project();
        //}
    }

    private void projectionComboBoxItemStateChanged(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            project();
            updateControls();
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

    private void updateControls() {
        if (projection == null) {
            return;
        }
        boolean showLatControls = false;
        if (projection instanceof AzimuthalProjection) {
            showLatControls = true;
        }
        latitudeLeadLabel.setVisible(showLatControls);
        lat0Slider.setVisible(showLatControls);
        lat0Label.setVisible(showLatControls);
    }

}
