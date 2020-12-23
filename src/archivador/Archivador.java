/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package archivador;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *
 * @author usuario
 */
public class Archivador {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws FileNotFoundException, IOException {
    UIManager.put("OptionPane.background", Color.decode("#121212"));
    UIManager.getLookAndFeelDefaults().put("Panel.background", Color.decode("#121212"));
    UIManager.put("OptionPane.messageForeground", Color.decode("#FFFFFF"));
    UIManager.put("Button.background", Color.decode("#373a40"));
    UIManager.put("Button.foreground", Color.decode("#FFFFFF"));
    UIManager.put("Label.foreground", Color.decode("#FFFFFF"));


/*
    System.out.println("Indique la cantidad de sectores: ");
    Scanner scan = new Scanner(System.in);
    //int cantSectores = scan.nextInt();
    System.out.println("Indique el tamaño de los sectores: ");
    //int tamannoSector = scan.nextInt();
*/

    // TODO code application logic here
    FileSystem este = new FileSystem();
    //este.discoVirtual.close();
    GUI.Gui gui = new GUI.Gui(este);
    gui.setVisible(true);
  }

}
