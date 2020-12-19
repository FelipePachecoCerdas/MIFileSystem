/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package archivador;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner; 
/**
 *
 * @author usuario
 */
public class Archivador {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        System.out.println("Indique la cantidad de sectores: ");
        Scanner scan = new Scanner(System.in);
        int cantSectores = scan.nextInt();
        System.out.println("Indique el tamaño de los sectores: ");
        int tamannoSector = scan.nextInt();
        
        // TODO code application logic here
        FileSystem este = new FileSystem(cantSectores, tamannoSector);
        este.discoVirtual.setLength(cantSectores*tamannoSector);
        //este.discoVirtual.close();
        GUI.Gui gui = new GUI.Gui(este);
        gui.setVisible(true);
    }
    
}
