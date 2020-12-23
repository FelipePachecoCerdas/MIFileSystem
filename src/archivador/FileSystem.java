/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package archivador;

import GUI.Gui;
import java.io.File;
import java.io.FileWriter;   // Import the FileWriter class
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author usuario
 */
public class FileSystem {

  public Folder Actual;
  public Folder root;
  public RandomAccessFile discoVirtual;
  public boolean[] sectores;
  public int[] punteroSectores;
  public int tamanoSector;

  //Vendria siendo la función CREATE
  public FileSystem(/*int pCantSectores, int pTamanoSector*/) throws FileNotFoundException {

    int pCantSectores = -1;
    int pTamanoSector = -1;

    while (pCantSectores == -1) {
      String cant = JOptionPane.showInputDialog(null, "Indique la cantidad de sectores del disco virtual", "Parámetros Iniciales", JOptionPane.QUESTION_MESSAGE);

      if (cant == null || cant.equals("")) {
        System.exit(0);
      }

      try {
        pCantSectores = new Integer(cant);
      } catch (Exception e) {
        continue;
      }

    }

    while (pTamanoSector == -1) {
      String cant = JOptionPane.showInputDialog(null, "Indique tamaño (en bytes) de cada sector del disco virtual", "Parámetros Iniciales", JOptionPane.QUESTION_MESSAGE);

      if (cant == null || cant.equals("")) {
        System.exit(0);
      }

      try {
        pTamanoSector = new Integer(cant);
      } catch (Exception e) {
        continue;
      }

    }
    //Hace algo con eso de la memoria
    File file = new File("discoVirtual.txt");
    file.delete();
    this.discoVirtual = new RandomAccessFile("discoVirtual.txt", "rw");

    this.sectores = new boolean[pCantSectores];
    this.punteroSectores = new int[pCantSectores];
    Arrays.fill(this.punteroSectores, -1);
    this.tamanoSector = pTamanoSector;
    Folder c = new Folder("C:", null);//Crea el folder inicial
    this.root = c;
    this.Actual = c;

    try {
      this.discoVirtual.setLength(pCantSectores * pTamanoSector);
    } catch (IOException ex) {
      Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  //Vendria siendo la función FILE
  public boolean createFile(String pNombre, int pTamano, String pContenido, String pExtension) throws IOException {
    Archivo arc = new Archivo(pNombre, Actual, pTamano, pContenido, pExtension);
    String[] partesContenido = pContenido.split(String.format("(?<=\\G.{%1$d})", this.tamanoSector));
    if (partesContenido.length <= this.verificarSectores()) {
      this.escribirDisco(arc, partesContenido);
      System.out.println(arc.sectorInicial);
      System.out.println(Arrays.toString(this.punteroSectores));
      System.out.println(Arrays.toString(this.sectores));
      Actual.listaCosos.add(arc);
      return true;
    } else {
      System.out.println("No hay suficiente espacio");
      return false;
    }
  }

  //Vendria siendo la función MKDIR
  public Folder createDir(String pNombre) {
    Folder fol = new Folder(pNombre, Actual);
    Actual.listaCosos.add(fol);
    return fol;
  }

  public boolean modFILE(Archivo arc, String contenido) throws IOException {
    arc.Contenido = contenido;
    arc.Tamano = contenido.length();
    String[] partesContenido = contenido.split(String.format("(?<=\\G.{%1$d})", this.tamanoSector));
    if (partesContenido.length - this.misSectores(arc.sectorInicial) <= this.verificarSectores()) {
      this.modificarDisco(arc, partesContenido);
      /*      System.out.println(arc.sectorInicial);
      System.out.println(Arrays.toString(this.punteroSectores));
      System.out.println(Arrays.toString(this.sectores));*/
      return true;
    } else {
      System.out.println("No hay suficiente espacio");
      return false;
    }
  }

  public void ReMove(Registro pBorrar) {
    if (pBorrar instanceof Archivo) {
      Archivo a = (Archivo) pBorrar;
      this.liberarSectores(a.sectorInicial);
      a.padre.listaCosos.remove(pBorrar);

    } else {
      Folder f = (Folder) pBorrar;
      for (int i = 0; i < f.listaCosos.size(); i++) {
        ReMove(f.listaCosos.get(i));
      }

      f.padre.listaCosos.remove(pBorrar);
    }
  }

  public void goUp() {
    if (Actual.padre != null) {
      Actual = Actual.padre;
    }
  }

  public void goDown(Folder pNext) {
    Actual = pNext;
  }

  public String verFile(Archivo arc) throws IOException {
    int sector = arc.sectorInicial;
    String contenido = "";
    while (sector != -1) {
      byte[] contenidoSector = new byte[this.tamanoSector];
      //System.out.println(this.tamanoSector*sector);
      this.discoVirtual.seek(sector * this.tamanoSector);
      this.discoVirtual.read(contenidoSector, 0, this.tamanoSector);
      //System.out.println(this.tamanoSector*sector);

      //contenido += new String(contenidoSector);
      for (int i = 0; i < contenidoSector.length; i++) {
        if (contenidoSector[i] != 0) {
          contenido += (char) contenidoSector[i];
        }
      }

      sector = this.punteroSectores[sector];
    }
    return contenido;

  }

  public int misSectores(int sector) {
    int cant = 0;
    int siguienteSector = 0;
    System.out.println(sector);
    while (sector != -1) {
      siguienteSector = this.punteroSectores[sector];
      cant++;
      sector = siguienteSector;
    }
    return cant;

  }

  public int verificarSectores() {
    int disponibles = 0;
    for (boolean sector : this.sectores) {
      if (sector == false) {
        disponibles++;
      }
    }
    return disponibles;
  }

  public int getSectoresImportar(File f) throws FileNotFoundException, IOException {
    int sectores = 0;

    if (f.isDirectory()) {

      for (int i = 0; i < f.list().length; i++) {
        sectores += getSectoresImportar(new File(f.getAbsolutePath() + "/" + f.list()[i]));
      }

    } else {
      String contenido = "";
      Scanner sc = new Scanner(f);

      while (sc.hasNextLine()) {
        contenido += sc.nextLine();
      }

      int Tamano = contenido.length();

      if (Tamano == 0) {
        return 1;
      } else {
        return Tamano / tamanoSector + ((Tamano % tamanoSector != 0) ? 1 : 0);
      }
    }
    return sectores;

  }

  public boolean contiene(Folder a, Registro r) {
    if (a == r) {
      return true;
    }
    for (int i = 0; i < a.listaCosos.size(); i++) {
      System.out.println("MMMM");
      Registro elem = a.listaCosos.get(i);
      if (elem == r) {
        return true;
      } else if (elem instanceof Folder && contiene((Folder) elem, r)) {
        return true;
      }
    }
    return false;
  }

  public void importar(File f, String s) throws FileNotFoundException, IOException {
    if (f.isDirectory()) {
      Folder respaldo = this.Actual;
      String name = (s.equals("") ? f.getName() : s);
      this.Actual = createDir(name);

      for (int i = 0; i < f.list().length; i++) {
        //System.out.println(f.getAbsolutePath() + "/" + f.list()[i]);
        importar(new File(f.getAbsolutePath() + "/" + f.list()[i]), "");
      }

      this.Actual = respaldo;
    } else {
      String contenido = "";
      Scanner sc = new Scanner(f);

      while (sc.hasNextLine()) {
        contenido += sc.nextLine();
      }

      String n = f.getName();
      String nf = n.substring(0, n.lastIndexOf('.'));
      String ef = n.substring(n.lastIndexOf('.') + 1, n.length());

      String name = (s.equals("") ? nf : s);
      createFile(name, contenido.length(), contenido, ef);
    }

  }

  public boolean exportar(File f, Registro r) throws FileNotFoundException, IOException {
    if (r instanceof Folder) {
      Folder d = (Folder) r;
      String nombreXD = (d.nombre.equals("C:")) ? "C" : d.nombre;
      File file = new File(f.getAbsolutePath() + "/" + nombreXD);

      boolean ok = file.mkdir();
      if (!ok) {
        return ok;
      }

      for (int i = 0; i < d.listaCosos.size(); i++) {
        exportar(file, d.listaCosos.get(i));
      }

    } else {
      File file = new File(f.getAbsolutePath() + "/" + r.nombre + "." + ((Archivo) r).extension);
      boolean ok = file.createNewFile();
      if (!ok) {
        return ok;
      }

      FileWriter myWriter = new FileWriter(file);

      String contenido = "";
      contenido = verFile((Archivo) r);

      myWriter.write(contenido);
      myWriter.close();
      return ok;
    }

    return true;

  }

  public void escribirDisco(Archivo arc, String[] partesContenido) throws IOException {
    int sector = 0, sectorPrevio = 0;
    int cantPartes = partesContenido.length;
    int sectoresEscritos = 0;
    while (sectoresEscritos != cantPartes) {
      if (this.sectores[sector] == false) {
        //this.discoVirtual = new RandomAccessFile("discoVirtual.txt", "rw");
        this.discoVirtual.seek(sector * this.tamanoSector);
        int contenidoTamanno = partesContenido[sectoresEscritos].length();
        String contenidoSector = partesContenido[sectoresEscritos] + new String(new char[this.tamanoSector - contenidoTamanno]);
        this.discoVirtual.write(contenidoSector.getBytes());
        System.out.println(contenidoSector);
        //this.discoVirtual.close();
        this.sectores[sector] = true;

        if (sectoresEscritos != 0) {
          this.punteroSectores[sectorPrevio] = sector;
        } else {
          arc.sectorInicial = sector;
          System.out.println(arc.sectorInicial);
        }
        sectorPrevio = sector;
        sectoresEscritos++;
      }
      sector++;
    }

  }

  public void modificarDisco(Archivo arc, String[] partesContenido) throws IOException {
    int sector = 0, sectorPrevio = 0, sectoresEscritos = 0;
    boolean ultimoOriginal = false;
    if (arc.sectorInicial != -1) {
      sector = arc.sectorInicial;
      while (sectoresEscritos != partesContenido.length) {
        if (ultimoOriginal == false || (ultimoOriginal == true && this.sectores[sector] == false)) {
          this.discoVirtual.seek(sector * this.tamanoSector);
          int contenidoTamanno = partesContenido[sectoresEscritos].length();
          String contenidoSector = partesContenido[sectoresEscritos] + new String(new char[this.tamanoSector - contenidoTamanno]);
          this.discoVirtual.write(contenidoSector.getBytes());
          System.out.println(contenidoSector);
          //this.discoVirtual.close();
          this.sectores[sector] = true;

          if (sectoresEscritos != 0) {
            this.punteroSectores[sectorPrevio] = sector;
          } else {
            arc.sectorInicial = sector;
            System.out.println(arc.sectorInicial);
          }
          sectorPrevio = sector;
          sectoresEscritos++;
          if (ultimoOriginal == false) {
            if (this.punteroSectores[sector] != -1) {
              sector = this.punteroSectores[sector];
            } else {
              ultimoOriginal = true;
              sector = -1;
            }
          }
        }
        if (ultimoOriginal == true) {
          sector++;
        }
      }
      if (ultimoOriginal == false) {
        this.punteroSectores[sectorPrevio] = -1;
        this.liberarSectores(sector);
      }
    } else {
      this.escribirDisco(arc, partesContenido);
    }

  }

  public void liberarSectores(int sector) {
    int siguienteSector = 0;
    System.out.println(sector);
    while (sector != -1) {
      siguienteSector = this.punteroSectores[sector];
      this.sectores[sector] = false;
      this.punteroSectores[sector] = -1;
      sector = siguienteSector;
    }
  }

  public ArrayList<Registro> buscar(String buscar) {
    String ext = null, nombre = null;
    if (buscar.length() >= 2 && buscar.substring(0, 2).equals("*.")) {
      ext = buscar.substring(buscar.indexOf(".") + 1, buscar.length());
    } else {
      nombre = buscar;
    }

    ArrayList<Registro> encontrados = new ArrayList<Registro>();

    if (ext != null) {
      buscarPorExtension(encontrados, root, ext);
    } else {
      buscarPorNombre(encontrados, root, nombre);
    }

    //Actual.match(buscar, encontrados);
    return encontrados;
  }

  public void buscarPorExtension(ArrayList<Registro> e, Registro r, String ext) {
    if (r instanceof Folder) {
      Folder f = (Folder) r;
      for (int i = 0; i < f.listaCosos.size(); i++) {
        Registro elem = f.listaCosos.get(i);
        buscarPorExtension(e, elem, ext);
      }

    } else {
      Archivo a = (Archivo) r;
      if (a.extension.equals(ext)) {
        e.add(r);
      }
    }
  }

  public void buscarPorNombre(ArrayList<Registro> e, Registro r, String nombre) {
    String name = (r instanceof Folder) ? r.nombre : (((Archivo) r).nombre + "." + ((Archivo) r).extension);
    if (name.contains(nombre)) {
      e.add(r);
    }

    if (r instanceof Folder) {
      Folder f = (Folder) r;
      for (int i = 0; i < f.listaCosos.size(); i++) {
        Registro elem = f.listaCosos.get(i);
        buscarPorNombre(e, elem, nombre);
      }

    }
  }
}
