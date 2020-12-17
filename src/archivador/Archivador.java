/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package archivador;

/**
 *
 * @author usuario
 */
public class Archivador {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        FileSystem este = new FileSystem(3, 5);
        GUI.Gui gui = new GUI.Gui(este);
        gui.setVisible(true);
    }
    
}
