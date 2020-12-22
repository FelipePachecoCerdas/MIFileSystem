/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package archivador;

import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author usuario
 */
public class Registro implements Cloneable {

  public String nombre;
  public String path;
  public Folder padre;
  public Date FechaCreacion;

  public void cambiarPath() {
    String nuevoPath = "";
    Folder p = padre;
    System.out.println(p);
    while (p != null) {
      nuevoPath = p.nombre + "/" + nuevoPath;
      System.out.println(p);
      p = p.padre;
    }
    nuevoPath = nuevoPath + this.nombre + "/";
    this.path = nuevoPath;
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public void match(String buscar, ArrayList<Registro> encontrados) {
    if (this.nombre.contains(buscar)) {
      encontrados.add(this);
    }
  }

  public int getSize() {
    return 0;
  }
  
  public int getSectores(FileSystem fs) {
    return 0;
  }

}
