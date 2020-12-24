package archivador;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author usuario
 */
public class Archivador {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws FileNotFoundException, IOException {

    FileSystem fs = new FileSystem();
    GUI.Gui gui = new GUI.Gui(fs);
    gui.setVisible(true);
  }

}
