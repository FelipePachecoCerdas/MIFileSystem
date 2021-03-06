/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package archivador;

import java.util.Date;

/**
 *
 * @author usuario
 */
public class Archivo extends Registro {

  public int Tamano;
  public String Contenido;
  public String extension;
  public int sectorInicial;

  public Archivo(String pNombre, Folder pPadre, int pTamano, String pContenido, String pExtension) {
    this.path = pPadre.path + pNombre + "." + pExtension; // "/";
    this.nombre = pNombre;
    this.extension = pExtension;
    this.Tamano = pTamano;
    this.FechaCreacion = new Date();
    this.FechaModificacion = new Date();
    this.Contenido = pContenido;
    this.padre = pPadre;
    this.sectorInicial = -1;
  }

  public int getSize() {
    return Tamano;
  }

  public int getSectores(FileSystem fs) {
    if (Tamano == 0) {
      return 1;
    } else {
      return Tamano / fs.tamanoSector + ((Tamano % fs.tamanoSector != 0) ? 1 : 0);
    }
  }

}
