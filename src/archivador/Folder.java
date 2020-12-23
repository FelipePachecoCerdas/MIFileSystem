/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package archivador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author usuario
 */
public class Folder extends Registro {

  public ArrayList<Registro> listaCosos = new ArrayList<Registro>();

  public Folder(String pNombre, Folder pPadre) {
    if (pPadre != null) {
      this.path = pPadre.path + pNombre + "/";
      this.nombre = pNombre;
      this.FechaCreacion = new Date();
      this.FechaModificacion = new Date();
      this.padre = pPadre;
    } else {
      this.path = pNombre + "/";
      this.nombre = pNombre;
      this.FechaCreacion = new Date();
      this.FechaModificacion = new Date();
    }
  }

  @Override
  public void cambiarPath() {
    String nuevoPath = "";
    Folder p = padre;
    while (p != null) {
      nuevoPath = p.nombre + "/" + nuevoPath;
      p = p.padre;
    }
    nuevoPath = nuevoPath + this.nombre + "/";
    this.path = nuevoPath;
    //Ahora se debe de cambiar el path de los hijos
    for (Registro r : this.listaCosos) {
      r.cambiarPath();
    }
  }

  public int getSize() {
    int size = 0;
    for (int i = 0; i < listaCosos.size(); i++) {
      size += listaCosos.get(i).getSize();
    }
    return size;
  }

  public int getSectores(FileSystem fs) {
    int sectores = 0;
    for (int i = 0; i < listaCosos.size(); i++) {
      sectores += listaCosos.get(i).getSectores(fs);
    }
    return sectores;
  }

  @Override
  public void match(String buscar, ArrayList<Registro> encontrados) {
    if (this.nombre.contains(buscar)) {
      encontrados.add(this);
    }
    for (Registro r : this.listaCosos) {
      r.match(buscar, encontrados);
    }
  }

  @Override
  public String toString() {
    return nombre;
  }

  public void agregar(Registro r, FileSystem fs) {
    if (r instanceof Folder) {
      Folder vieja = (Folder) r;

      Folder nueva = new Folder(vieja.nombre, this);

      for (int i = 0; i < vieja.listaCosos.size(); i++) {
        nueva.agregar(vieja.listaCosos.get(i), fs);
      }

      this.listaCosos.add(nueva);
    } else {
      Archivo viejo = (Archivo) r;

      Archivo nuevo = new Archivo(viejo.nombre, this, viejo.Tamano, viejo.Contenido, viejo.extension);

      String[] partesContenido = viejo.Contenido.split(String.format("(?<=\\G.{%1$d})", fs.tamanoSector));

      try {
        fs.escribirDisco(nuevo, partesContenido);
      } catch (IOException ex) {
        Logger.getLogger(Folder.class.getName()).log(Level.SEVERE, null, ex);
      }
      this.listaCosos.add(nuevo);
    }
  }

  public void agregarConNombre(Registro r, FileSystem fs, String s) {
    String resp = r.nombre;
    r.nombre = s;
    agregar(r, fs);
    r.nombre = resp;
  }
}
