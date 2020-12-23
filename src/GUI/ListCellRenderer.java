/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 *
 * @author felip
 */
public class ListCellRenderer extends DefaultListCellRenderer {

  Font font = new Font("dialog", Font.PLAIN, 14);

  public Component getListCellRendererComponent(
    JList list, Object value, int index,
    boolean isSelected, boolean cellHasFocus) {

    char tipo = ((String) value).charAt(0);
    value = " " + ((String) value).substring(1, ((String) value).length());

    JLabel label = (JLabel) super.getListCellRendererComponent(
      list, value, index, isSelected, cellHasFocus);
    String strImg = (tipo == 'D') ? "src/Imagenes/folder.png" : "src/Imagenes/file.png";
    ImageIcon img = new ImageIcon(new ImageIcon(strImg).getImage().getScaledInstance(18, 18, Image.SCALE_DEFAULT));
    label.setIcon(img); //imageMap.get((String) value)
    label.setHorizontalTextPosition(JLabel.RIGHT);
    label.setFont(font);
    return label;
  }
}
