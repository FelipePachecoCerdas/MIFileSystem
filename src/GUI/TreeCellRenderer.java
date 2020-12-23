/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author felip
 */
public class TreeCellRenderer extends DefaultTreeCellRenderer {

  Font font = new Font("dialog", Font.PLAIN, 14);

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
    boolean leaf, int row, boolean hasFocus) {

    DefaultMutableTreeNode valueX = (DefaultMutableTreeNode) value;
    String valueReal = valueX.toString();

    char tipo = ((String) valueReal).charAt(0);
    valueReal = "" + ((String) valueReal).substring(1, ((String) valueReal).length());
    
    boolean soy = false;
    if (valueReal.contains("$")) {
      soy = true;
      valueReal = valueReal.substring(0, valueReal.length()-1);
      
    }

    JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, valueReal, sel, expanded, leaf, row, hasFocus);
    String strImg = (tipo == 'D') ? "src/Imagenes/folder.png" : "src/Imagenes/file.png";
    ImageIcon img = new ImageIcon(new ImageIcon(strImg).getImage().getScaledInstance(15, 15, Image.SCALE_DEFAULT));
    label.setIcon(img); //imageMap.get((String) value)
    label.setHorizontalTextPosition(JLabel.RIGHT);
    label.setFont(font);
    if (soy) {
      label.setFont(new Font("dialog", Font.BOLD, 16));
    }

    label.setBackground(Color.decode("#433d3c"));
    return label;
  }

  @Override
  public Color getBackgroundNonSelectionColor() {
    return Color.decode("#433d3c");
  }

  @Override
  public Color getBackground() {
    return Color.decode("#433d3c");
  }

  @Override
  public Color getForeground() {
    return Color.decode("#FFFFFF");
  }
}
